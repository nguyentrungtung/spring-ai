package com.service.ai_agent.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "conversation_history")
public class ConversationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private String userId;

    @Column(nullable = false, updatable = false)
    private String sessionId;

    @Column(nullable = false, updatable = false)
    private String tenantId;

    @Column(nullable = false, updatable = false)
    private String role;

    @Column(columnDefinition = "TEXT", nullable = false, updatable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}