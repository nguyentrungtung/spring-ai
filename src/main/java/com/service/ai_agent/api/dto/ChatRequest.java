package com.service.ai_agent.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.HashMap;

/**
 * DTO cho chat request từ client
 * Chứa các field bắt buộc theo DDD requirements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    @NotBlank(message = "Input content cannot be empty")
    @Size(max = 10000, message = "Input content too long")
    private String input;

    @NotBlank(message = "User ID is required")
    @Size(max = 100, message = "User ID too long")
    private String userId;

    @NotBlank(message = "Session ID is required")
    @Size(max = 100, message = "Session ID too long")
    private String sessionId;

    @NotBlank(message = "Tenant ID is required")
    @Size(max = 100, message = "Tenant ID too long")
    private String tenantId;

    @Builder.Default
    private Map<String, Object> context = new HashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
