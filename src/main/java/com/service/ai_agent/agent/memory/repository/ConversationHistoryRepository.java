package com.service.ai_agent.agent.memory.repository;

import com.service.ai_agent.domain.ConversationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, UUID> {
}