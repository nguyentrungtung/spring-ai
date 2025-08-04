package com.service.ai_agent.api.mapper;

import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.api.dto.ChatRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper class để chuyển đổi giữa API DTOs và Domain Objects
 * Tuân thủ DDD pattern và Single Responsibility Principle
 */
@Component
public class ChatRequestMapper {

    /**
     * Chuyển đổi từ ChatRequest DTO sang AgentRequest Domain Object
     * 
     * @param chatRequest DTO từ API layer
     * @return AgentRequest domain object cho business layer
     */
    public AgentRequest toAgentRequest(ChatRequest chatRequest) {
        return AgentRequest.builder()
                .userId(chatRequest.getUserId())
                .sessionId(chatRequest.getSessionId())
                .tenantId(chatRequest.getTenantId())
                .input(chatRequest.getInput())
                .context(chatRequest.getContext())
                .metadata(chatRequest.getMetadata())
                .build();
    }

    /**
     * Thêm enrichment metadata cho request
     * 
     * @param chatRequest original request
     * @param enrichmentData additional metadata
     * @return enriched AgentRequest
     */
    public AgentRequest toAgentRequestWithEnrichment(ChatRequest chatRequest, 
                                                   String enrichmentData) {
        AgentRequest agentRequest = toAgentRequest(chatRequest);
        
        // Thêm enrichment metadata
        agentRequest.addMetadata("request_timestamp", System.currentTimeMillis());
        agentRequest.addMetadata("api_version", "v1");
        agentRequest.addMetadata("enrichment", enrichmentData);
        
        return agentRequest;
    }

    /**
     * Validate và tạo AgentRequest với default values
     */
    public AgentRequest toAgentRequestWithDefaults(ChatRequest chatRequest) {
        return AgentRequest.builder()
                .userId(chatRequest.getUserId())
                .sessionId(chatRequest.getSessionId())
                .tenantId(chatRequest.getTenantId())
                .input(chatRequest.getInput())
                .context(chatRequest.getContext() != null ? chatRequest.getContext() : new java.util.HashMap<>())
                .metadata(chatRequest.getMetadata() != null ? chatRequest.getMetadata() : new java.util.HashMap<>())
                .build();
    }
}