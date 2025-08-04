package com.service.ai_agent.agent.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Input content is required")
    private String input;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Builder.Default
    private Map<String, Object> context = new HashMap<>();

    // Helper methods
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    public void addContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public Object getContext(String key) {
        return context != null ? context.get(key) : null;
    }
}