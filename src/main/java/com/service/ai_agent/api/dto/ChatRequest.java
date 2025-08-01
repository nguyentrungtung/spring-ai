package com.service.ai_agent.api.dto;

import lombok.NonNull;

public record ChatRequest(
    @NonNull String userId,
    @NonNull String sessionId,
    @NonNull String tenantId,
    @NonNull String message
) {}