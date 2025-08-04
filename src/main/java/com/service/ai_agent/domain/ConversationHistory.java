package com.service.ai_agent.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversation_history", indexes = {
    @Index(name = "idx_conversation_tenant_user", columnList = "tenant_id, user_id"),
    @Index(name = "idx_conversation_session", columnList = "session_id"),
    @Index(name = "idx_conversation_created", columnList = "created_at"),
    @Index(name = "idx_conversation_metadata", columnList = "metadata")
})
public class ConversationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
    private String tenantId;

    @Column(name = "user_id", nullable = false, updatable = false, length = 100)
    private String userId;

    @Column(name = "session_id", nullable = false, updatable = false, length = 100)
    private String sessionId;

    @Column(nullable = false, updatable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConversationRole role;

    @Column(columnDefinition = "TEXT", nullable = false, updatable = false)
    private String content;

    // Sử dụng JSONB thay vì ElementCollection để đơn giản hóa
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Domain methods
    public boolean isUserMessage() {
        return ConversationRole.USER.equals(this.role);
    }

    public boolean isAssistantMessage() {
        return ConversationRole.ASSISTANT.equals(this.role);
    }

    public boolean belongsToUser(String userId, String tenantId) {
        return this.userId.equals(userId) && this.tenantId.equals(tenantId);
    }

    public boolean belongsToSession(String sessionId) {
        return this.sessionId.equals(sessionId);
    }

    // Metadata helper methods
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public boolean hasMetadata(String key) {
        return metadata != null && metadata.containsKey(key);
    }

    // Enum for conversation roles
    public enum ConversationRole {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system");

        private final String value;

        ConversationRole(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}