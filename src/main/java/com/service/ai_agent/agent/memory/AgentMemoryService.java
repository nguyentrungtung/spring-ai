package com.service.ai_agent.agent.memory;

import com.service.ai_agent.agent.memory.repository.ConversationHistoryRepository;
import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.domain.ConversationHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentMemoryService {

    private final VectorStore vectorStore;
    private final ConversationHistoryRepository historyRepository;

    public void saveInteraction(AgentRequest request, String aiResponse) {
        // Lưu vào DB quan hệ để truy vết
        ConversationHistory userEntry = new ConversationHistory();
        userEntry.setUserId(request.getUserId());
        userEntry.setSessionId(request.getSessionId());
        userEntry.setTenantId(request.getTenantId());
        userEntry.setRole("USER");
        userEntry.setContent(request.getInput());
        historyRepository.save(userEntry);

        ConversationHistory aiEntry = new ConversationHistory();
        aiEntry.setUserId(request.getUserId());
        aiEntry.setSessionId(request.getSessionId());
        aiEntry.setTenantId(request.getTenantId());
        aiEntry.setRole("ASSISTANT");
        aiEntry.setContent(aiResponse);
        historyRepository.save(aiEntry);

        // Lưu vào Vector Store để tìm kiếm ngữ cảnh
        List<Document> documents = List.of(
                new Document(request.getInput(), Map.of("role", "USER", "sessionId", request.getSessionId())),
                new Document(aiResponse, Map.of("role", "ASSISTANT", "sessionId", request.getSessionId()))
        );
        vectorStore.add(documents);
    }

    public String retrieveContext(String sessionId) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query("")
                .filterExpression("sessionId == '" + sessionId + "'")
                .topK(10)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        return results.stream()
                .map(doc -> String.format("[%s]: %s", doc.getMetadata().get("role"), doc.getText()))
                .collect(Collectors.joining("\n"));
    }
}