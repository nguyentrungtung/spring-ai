package com.service.ai_agent.agent.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class AgentRequest {
    private final String userId;
    private final String sessionId;
    private final String tenantId;
    private final String input;
    private final Map<String, Object> metadata;
}