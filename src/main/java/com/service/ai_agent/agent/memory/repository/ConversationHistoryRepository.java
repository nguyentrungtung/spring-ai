package com.service.ai_agent.agent.memory.repository;

import com.service.ai_agent.domain.ConversationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ConversationHistory following DDD patterns
 * Updated to support JSONB metadata queries
 */
@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, UUID> {

    /**
     * Tìm tất cả conversation theo sessionId và tenantId
     */
    List<ConversationHistory> findBySessionIdAndTenantIdOrderByCreatedAtAsc(
            String sessionId, String tenantId);

    /**
     * Tìm conversation theo userId, tenantId với phân trang
     */
    Page<ConversationHistory> findByUserIdAndTenantIdOrderByCreatedAtDesc(
            String userId, String tenantId, Pageable pageable);

    /**
     * Tìm conversation trong một khoảng thời gian
     */
    @Query("SELECT ch FROM ConversationHistory ch WHERE ch.tenantId = :tenantId " +
           "AND ch.userId = :userId AND ch.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY ch.createdAt ASC")
    List<ConversationHistory> findByUserAndTimeRange(
            @Param("tenantId") String tenantId,
            @Param("userId") String userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Tìm conversation cuối cùng của user trong session
     */
    Optional<ConversationHistory> findTopBySessionIdAndTenantIdOrderByCreatedAtDesc(
            String sessionId, String tenantId);

    /**
     * Đếm số conversation trong session
     */
    long countBySessionIdAndTenantId(String sessionId, String tenantId);

    /**
     * Tìm conversation theo role trong session
     */
    List<ConversationHistory> findBySessionIdAndTenantIdAndRoleOrderByCreatedAtAsc(
            String sessionId, String tenantId, ConversationHistory.ConversationRole role);

    /**
     * Xóa conversation cũ hơn thời gian chỉ định
     */
    @Query("DELETE FROM ConversationHistory ch WHERE ch.tenantId = :tenantId " +
           "AND ch.createdAt < :cutoffTime")
    void deleteOldConversations(@Param("tenantId") String tenantId,
                               @Param("cutoffTime") Instant cutoffTime);

    /**
     * Tìm conversation có metadata key cụ thể (JSONB query)
     */
    @Query(value = "SELECT * FROM conversation_history " +
                   "WHERE tenant_id = :tenantId AND metadata ? :metadataKey",
           nativeQuery = true)
    List<ConversationHistory> findByTenantIdAndMetadataKey(
            @Param("tenantId") String tenantId,
            @Param("metadataKey") String metadataKey);

    /**
     * Tìm conversation có metadata key-value cụ thể (JSONB query)
     */
    @Query(value = "SELECT * FROM conversation_history " +
                   "WHERE tenant_id = :tenantId AND metadata ->> :metadataKey = :metadataValue",
           nativeQuery = true)
    List<ConversationHistory> findByTenantIdAndMetadata(
            @Param("tenantId") String tenantId,
            @Param("metadataKey") String metadataKey,
            @Param("metadataValue") String metadataValue);

    /**
     * Tìm conversation theo request type từ metadata
     */
    @Query(value = "SELECT * FROM conversation_history " +
                   "WHERE tenant_id = :tenantId AND metadata ->> 'request_type' = :requestType " +
                   "ORDER BY created_at DESC",
           nativeQuery = true)
    List<ConversationHistory> findByTenantIdAndRequestType(
            @Param("tenantId") String tenantId,
            @Param("requestType") String requestType);

    /**
     * Search trong metadata với JSONB contains operator
     */
    @Query(value = "SELECT * FROM conversation_history " +
                   "WHERE tenant_id = :tenantId AND metadata @> CAST(:metadataJson AS jsonb)",
           nativeQuery = true)
    List<ConversationHistory> findByTenantIdAndMetadataContains(
            @Param("tenantId") String tenantId,
            @Param("metadataJson") String metadataJson);
}
