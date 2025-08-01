package com.service.ai_agent.agent.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AgentResponse {
    private final String output;
    private final ResponseStatus status;

    public enum ResponseStatus {
        SUCCESS,
        FAILED,
        REQUIRES_HUMAN_INTERVENTION
    }
}