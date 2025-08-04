package com.service.ai_agent.agent.memory;

import com.service.ai_agent.agent.memory.repository.ConversationHistoryRepository;
import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.domain.ConversationHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentMemoryService {

    // Make VectorStore optional to work without database
    private final Optional<VectorStore> vectorStore;
    private final Optional<ConversationHistoryRepository> historyRepository;

    @Transactional
    public void saveInteraction(AgentRequest request, String aiResponse) {
        try {
            // Only save to database if repository is available
            if (historyRepository.isPresent()) {
                saveToDatabase(request, aiResponse);
            } else {
                log.warn("Database repository not available, skipping conversation save for session: {}",
                        request.getSessionId());
            }

            // Only save to vector store if available
            if (vectorStore.isPresent()) {
                saveToVectorStore(request, aiResponse);
            } else {
                log.warn("VectorStore not available, skipping vector save for session: {}",
                        request.getSessionId());
            }

            log.info("Saved interaction for user {} in session {} (tenant: {})",
                    request.getUserId(), request.getSessionId(), request.getTenantId());

        } catch (Exception e) {
            log.error("Failed to save interaction for user {} in session {}: {}",
                    request.getUserId(), request.getSessionId(), e.getMessage(), e);
            // Don't throw exception to prevent app from failing
        }
    }

    @Transactional(readOnly = true)
    public String retrieveContext(String sessionId, String tenantId) {
        try {
            // Try vector store first if available
            if (vectorStore.isPresent()) {
                SearchRequest searchRequest = SearchRequest.builder()
                        .query("")
                        .filterExpression(String.format("sessionId == '%s' && tenantId == '%s'", sessionId, tenantId))
                        .topK(10)
                        .build();

                List<Document> results = vectorStore.get().similaritySearch(searchRequest);
                return results.stream()
                        .map(doc -> String.format("[%s]: %s",
                                doc.getMetadata().get("role"),
                                doc.getText()))
                        .collect(Collectors.joining("\n"));
            }

            // Fallback to database if available
            if (historyRepository.isPresent()) {
                return retrieveContextFromDatabase(sessionId, tenantId);
            }

            // If neither available, return empty context
            log.warn("No storage available for context retrieval, returning empty context for session: {}", sessionId);
            return "";

        } catch (Exception e) {
            log.warn("Failed to retrieve context for session {}: {}", sessionId, e.getMessage());
            return "";
        }
    }

    @Transactional(readOnly = true)
    public List<ConversationHistory> getSessionHistory(String sessionId, String tenantId) {
        if (historyRepository.isPresent()) {
            return historyRepository.get().findBySessionIdAndTenantIdOrderByCreatedAtAsc(sessionId, tenantId);
        }
        log.warn("Database repository not available, returning empty history for session: {}", sessionId);
        return List.of();
    }

    @Transactional(readOnly = true)
    public long getSessionMessageCount(String sessionId, String tenantId) {
        if (historyRepository.isPresent()) {
            return historyRepository.get().countBySessionIdAndTenantId(sessionId, tenantId);
        }
        return 0;
    }

    @Transactional
    public void cleanupOldConversations(String tenantId, Instant cutoffTime) {
        if (historyRepository.isPresent()) {
            try {
                historyRepository.get().deleteOldConversations(tenantId, cutoffTime);
                log.info("Cleaned up old conversations for tenant {} before {}", tenantId, cutoffTime);
            } catch (Exception e) {
                log.error("Failed to cleanup old conversations for tenant {}: {}", tenantId, e.getMessage(), e);
            }
        }
    }

    // Private helper methods
    private void saveToDatabase(AgentRequest request, String aiResponse) {
        // Tạo metadata cho user message
        Map<String, String> userMetadata = createMetadata(request, "user_input");
        userMetadata.put("input_length", String.valueOf(request.getInput().length()));
        userMetadata.put("request_type", determineRequestType(request.getInput()));

        // Lưu user message vào DB quan hệ
        ConversationHistory userEntry = ConversationHistory.builder()
                .tenantId(request.getTenantId())
                .userId(request.getUserId())
                .sessionId(request.getSessionId())
                .role(ConversationHistory.ConversationRole.USER)
                .content(request.getInput())
                .metadata(userMetadata)
                .createdAt(Instant.now())
                .build();
        historyRepository.get().save(userEntry);

        // Tạo metadata cho AI response
        Map<String, String> aiMetadata = createMetadata(request, "ai_response");
        aiMetadata.put("response_length", String.valueOf(aiResponse.length()));
        aiMetadata.put("processing_timestamp", Instant.now().toString());

        // Lưu AI response vào DB quan hệ
        ConversationHistory aiEntry = ConversationHistory.builder()
                .tenantId(request.getTenantId())
                .userId(request.getUserId())
                .sessionId(request.getSessionId())
                .role(ConversationHistory.ConversationRole.ASSISTANT)
                .content(aiResponse)
                .metadata(aiMetadata)
                .createdAt(Instant.now())
                .build();
        historyRepository.get().save(aiEntry);
    }

    private Map<String, String> createMetadata(AgentRequest request, String messageType) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("message_type", messageType);
        metadata.put("tenant_id", request.getTenantId());
        metadata.put("user_id", request.getUserId());
        metadata.put("session_id", request.getSessionId());
        metadata.put("timestamp", Instant.now().toString());

        // Thêm context nếu có
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            metadata.put("has_context", "true");
            metadata.put("context_keys", String.join(",", request.getContext().keySet()));
        }

        return metadata;
    }

    private String determineRequestType(String input) {
        String lowerInput = input.toLowerCase();
        if (lowerInput.contains("tạo") || lowerInput.contains("create")) {
            return "creation_request";
        } else if (lowerInput.contains("giá") || lowerInput.contains("price")) {
            return "pricing_inquiry";
        } else if (lowerInput.contains("template") || lowerInput.contains("mẫu")) {
            return "template_inquiry";
        }
        return "general_inquiry";
    }

    private void saveToVectorStore(AgentRequest request, String aiResponse) {
        try {
            Map<String, Object> userDocMetadata = Map.of(
                    "role", "USER",
                    "sessionId", request.getSessionId(),
                    "tenantId", request.getTenantId(),
                    "userId", request.getUserId(),
                    "timestamp", Instant.now().toString()
            );

            Map<String, Object> aiDocMetadata = Map.of(
                    "role", "ASSISTANT",
                    "sessionId", request.getSessionId(),
                    "tenantId", request.getTenantId(),
                    "userId", request.getUserId(),
                    "timestamp", Instant.now().toString()
            );

            List<Document> documents = List.of(
                    new Document(request.getInput(), userDocMetadata),
                    new Document(aiResponse, aiDocMetadata)
            );

            vectorStore.get().add(documents);
        } catch (Exception e) {
            log.warn("Failed to save to vector store: {}", e.getMessage());
        }
    }

    private String retrieveContextFromDatabase(String sessionId, String tenantId) {
        List<ConversationHistory> history = historyRepository.get()
                .findBySessionIdAndTenantIdOrderByCreatedAtAsc(sessionId, tenantId);

        return history.stream()
                .map(conv -> String.format("[%s]: %s", conv.getRole().getValue(), conv.getContent()))
                .collect(Collectors.joining("\n"));
    }
}

