-- AI Agent Database Migration - Complete Schema for Development
-- Version: V1.0.0
-- Description: Complete database setup with schema, indexes, sample data for localhost development
-- Author: AI Agent Development Team
-- Date: 2025-08-04
-- Environment: Development (localhost)

-- =============================================================================
-- 1. CREATE EXTENSIONS
-- =============================================================================

-- Create UUID extension for primary keys
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create vector extension for Spring AI PgVector support
CREATE EXTENSION IF NOT EXISTS vector;

-- =============================================================================
-- 2. CREATE MAIN TABLES WITH OPTIMIZED SCHEMA
-- =============================================================================

-- Create conversation_history table with JSONB metadata (optimized)
CREATE TABLE IF NOT EXISTS conversation_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    session_id VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM')),
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Add constraints for data integrity
    CONSTRAINT chk_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
    CONSTRAINT chk_tenant_id_not_empty CHECK (LENGTH(TRIM(tenant_id)) > 0),
    CONSTRAINT chk_user_id_not_empty CHECK (LENGTH(TRIM(user_id)) > 0),
    CONSTRAINT chk_session_id_not_empty CHECK (LENGTH(TRIM(session_id)) > 0)
);

-- Create vector_store table for Spring AI PgVector
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}',
    embedding vector(1536),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Add constraints
    CONSTRAINT chk_vector_content_not_empty CHECK (LENGTH(TRIM(content)) > 0)
);

-- =============================================================================
-- 3. CREATE OPTIMIZED INDEXES FOR PERFORMANCE
-- =============================================================================

-- Primary indexes for conversation_history
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_tenant_user
    ON conversation_history(tenant_id, user_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_session
    ON conversation_history(session_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_created
    ON conversation_history(created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_role
    ON conversation_history(role);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_tenant_session
    ON conversation_history(tenant_id, session_id, created_at);

-- JSONB indexes for metadata queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_metadata_gin
    ON conversation_history USING gin(metadata);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_metadata_request_type
    ON conversation_history USING btree((metadata ->> 'request_type'));

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_metadata_message_type
    ON conversation_history USING btree((metadata ->> 'message_type'));

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_conversation_metadata_source
    ON conversation_history USING btree((metadata ->> 'source'));

-- Vector store indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS vector_store_embedding_idx
    ON vector_store USING hnsw (embedding vector_cosine_ops);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_vector_store_metadata_gin
    ON vector_store USING gin(metadata);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_vector_store_created
    ON vector_store(created_at DESC);

-- =============================================================================
-- 4. CREATE FUNCTIONS AND TRIGGERS
-- =============================================================================

-- Function to automatically update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

-- Trigger for conversation_history table
DROP TRIGGER IF EXISTS update_conversation_history_updated_at ON conversation_history;
CREATE TRIGGER update_conversation_history_updated_at
    BEFORE UPDATE ON conversation_history
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- 5. CREATE UTILITY FUNCTIONS FOR DEVELOPMENT
-- =============================================================================

-- Function to search conversations by metadata filters
CREATE OR REPLACE FUNCTION search_conversations_by_metadata(
    p_tenant_id VARCHAR(100),
    p_metadata_filters JSONB DEFAULT '{}'::jsonb
)
RETURNS TABLE (
    id UUID,
    tenant_id VARCHAR(100),
    user_id VARCHAR(100),
    session_id VARCHAR(100),
    role VARCHAR(50),
    content TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    SELECT ch.id, ch.tenant_id, ch.user_id, ch.session_id,
           ch.role::VARCHAR(50), ch.content, ch.metadata, ch.created_at
    FROM conversation_history ch
    WHERE ch.tenant_id = p_tenant_id
    AND (p_metadata_filters = '{}'::jsonb OR ch.metadata @> p_metadata_filters)
    ORDER BY ch.created_at DESC;
END;
$$ LANGUAGE plpgsql;

-- Function to get metadata statistics by tenant
CREATE OR REPLACE FUNCTION get_metadata_stats(p_tenant_id VARCHAR(100))
RETURNS TABLE (
    metadata_key TEXT,
    metadata_value TEXT,
    count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        keys.key::TEXT as metadata_key,
        values.value::TEXT as metadata_value,
        COUNT(*)::BIGINT as count
    FROM conversation_history ch,
         jsonb_each_text(ch.metadata) as keys,
         jsonb_each_text(ch.metadata) as values
    WHERE ch.tenant_id = p_tenant_id
    AND keys.key = values.key
    GROUP BY keys.key, values.value
    ORDER BY count DESC;
END;
$$ LANGUAGE plpgsql;

-- Function to cleanup old conversations
CREATE OR REPLACE FUNCTION cleanup_old_conversations(
    p_tenant_id VARCHAR(100),
    p_days_to_keep INTEGER DEFAULT 30
)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM conversation_history
    WHERE tenant_id = p_tenant_id
    AND created_at < NOW() - INTERVAL '1 day' * p_days_to_keep;

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- 6. CREATE VIEWS FOR DEVELOPMENT & REPORTING
-- =============================================================================

-- View for conversation statistics by tenant
CREATE OR REPLACE VIEW conversation_stats_by_tenant AS
SELECT
    tenant_id,
    COUNT(*) as total_messages,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(DISTINCT session_id) as unique_sessions,
    COUNT(CASE WHEN role = 'USER' THEN 1 END) as user_messages,
    COUNT(CASE WHEN role = 'ASSISTANT' THEN 1 END) as assistant_messages,
    COUNT(CASE WHEN metadata ->> 'request_type' = 'creation_request' THEN 1 END) as creation_requests,
    COUNT(CASE WHEN metadata ->> 'request_type' = 'pricing_inquiry' THEN 1 END) as pricing_inquiries,
    COUNT(CASE WHEN metadata ->> 'request_type' = 'template_inquiry' THEN 1 END) as template_inquiries,
    MIN(created_at) as first_message_date,
    MAX(created_at) as last_message_date,
    AVG(LENGTH(content)) as avg_content_length
FROM conversation_history
GROUP BY tenant_id;

-- View for recent conversations with metadata (development helper)
CREATE OR REPLACE VIEW recent_conversations AS
SELECT
    id,
    tenant_id,
    user_id,
    session_id,
    role,
    LEFT(content, 100) || CASE WHEN LENGTH(content) > 100 THEN '...' ELSE '' END as content_preview,
    metadata,
    created_at
FROM conversation_history
WHERE created_at >= NOW() - INTERVAL '7 days'
ORDER BY created_at DESC;

-- View for session analytics
CREATE OR REPLACE VIEW session_analytics AS
SELECT
    tenant_id,
    session_id,
    user_id,
    COUNT(*) as message_count,
    COUNT(CASE WHEN role = 'USER' THEN 1 END) as user_message_count,
    COUNT(CASE WHEN role = 'ASSISTANT' THEN 1 END) as assistant_message_count,
    MIN(created_at) as session_start,
    MAX(created_at) as session_end,
    MAX(created_at) - MIN(created_at) as session_duration,
    array_agg(DISTINCT metadata ->> 'request_type') FILTER (WHERE metadata ->> 'request_type' IS NOT NULL) as request_types
FROM conversation_history
GROUP BY tenant_id, session_id, user_id
ORDER BY session_start DESC;

-- =============================================================================
-- 7. INSERT SAMPLE DATA FOR DEVELOPMENT
-- =============================================================================

-- Insert comprehensive sample conversations for development and testing
INSERT INTO conversation_history (tenant_id, user_id, session_id, role, content, metadata) VALUES

-- === TENANT 1: Development Company ===
-- Session 1: Website Creation Flow
('dev-tenant-001', 'user-dev-001', 'session-website-001', 'USER',
 'Tôi muốn tạo một website bán hàng online cho shop quần áo của tôi',
 '{"request_type": "creation_request", "message_type": "user_input", "source": "web_app", "business_type": "fashion", "complexity": "medium"}'),

('dev-tenant-001', 'user-dev-001', 'session-website-001', 'ASSISTANT',
 'Tôi sẽ giúp bạn tạo website bán quần áo online. Trước tiên, cho tôi biết thêm về sản phẩm và khách hàng mục tiêu của bạn.',
 '{"message_type": "ai_response", "response_type": "consultation", "next_steps": "gather_requirements", "tools_used": "none"}'),

('dev-tenant-001', 'user-dev-001', 'session-website-001', 'USER',
 'Tôi bán quần áo thời trang cho giới trẻ, chủ yếu là áo thun, quần jeans và phụ kiện',
 '{"request_type": "creation_request", "message_type": "user_input", "source": "web_app", "product_category": "fashion", "target_audience": "young_adults"}'),

('dev-tenant-001', 'user-dev-001', 'session-website-001', 'ASSISTANT',
 'Tuyệt vời! Tôi sẽ tạo cho bạn website với template fashion hiện đại, tích hợp giỏ hàng và thanh toán online.',
 '{"message_type": "ai_response", "response_type": "solution", "template_suggested": "fashion-modern", "features": "cart,payment,responsive", "tools_used": "website_creation"}'),

-- Session 2: Pricing Inquiry
('dev-tenant-001', 'user-dev-002', 'session-pricing-001', 'USER',
 'Cho tôi xem bảng giá các gói dịch vụ của bạn',
 '{"request_type": "pricing_inquiry", "message_type": "user_input", "source": "mobile_app", "urgency": "normal"}'),

('dev-tenant-001', 'user-dev-002', 'session-pricing-001', 'ASSISTANT',
 'Chúng tôi có 3 gói dịch vụ: Basic (500k), Premium (1M), Enterprise (2M). Mỗi gói có tính năng và hỗ trợ khác nhau.',
 '{"message_type": "ai_response", "response_type": "information", "plans_shown": 3, "tools_used": "pricing_info"}'),

-- Session 3: Template Browsing
('dev-tenant-001', 'user-dev-003', 'session-template-001', 'USER',
 'Tôi muốn xem các mẫu giao diện website có sẵn',
 '{"request_type": "template_inquiry", "message_type": "user_input", "source": "web_app", "category": "all"}'),

('dev-tenant-001', 'user-dev-003', 'session-template-001', 'ASSISTANT',
 'Chúng tôi có nhiều template đẹp: Fashion Store, Business Corporate, Creative Portfolio, và E-commerce Modern.',
 '{"message_type": "ai_response", "response_type": "showcase", "templates_shown": 4, "tools_used": "template_info"}'),

-- === TENANT 2: Startup Company ===
-- Session 1: Complex Business Requirements
('startup-tenant-002', 'user-startup-001', 'session-complex-001', 'SYSTEM',
 'System initialized for new tenant startup-tenant-002',
 '{"message_type": "system_message", "event_type": "tenant_initialization", "source": "system"}'),

('startup-tenant-002', 'user-startup-001', 'session-complex-001', 'USER',
 'Chúng tôi cần một platform e-learning với tích hợp video, quiz và certificate',
 '{"request_type": "creation_request", "message_type": "user_input", "source": "web_app", "business_type": "education", "complexity": "high", "features": "video,quiz,certificate"}'),

('startup-tenant-002', 'user-startup-001', 'session-complex-001', 'ASSISTANT',
 'Đây là dự án phức tạp! Tôi sẽ thiết kế platform e-learning với LMS đầy đủ, hỗ trợ video streaming và quản lý khóa học.',
 '{"message_type": "ai_response", "response_type": "consultation", "complexity": "high", "estimated_timeline": "4-6_weeks", "tools_used": "website_creation"}'),

-- Session 2: Multi-language Support
('startup-tenant-002', 'user-startup-002', 'session-multi-lang-001', 'USER',
 'We need a website with English and Vietnamese support',
 '{"request_type": "creation_request", "message_type": "user_input", "source": "web_app", "language": "en", "requirements": "multilingual", "languages": "en,vi"}'),

('startup-tenant-002', 'user-startup-002', 'session-multi-lang-001', 'ASSISTANT',
 'I can create a multilingual website with English and Vietnamese support, including automatic language detection.',
 '{"message_type": "ai_response", "response_type": "solution", "language": "en", "features": "i18n,auto_detect", "tools_used": "website_creation"}'),

-- === TENANT 3: Enterprise Client ===
-- Session 1: Enterprise Requirements
('enterprise-tenant-003', 'user-enterprise-001', 'session-enterprise-001', 'USER',
 'Chúng tôi cần tích hợp với hệ thống ERP hiện tại và cần bảo mật cao',
 '{"request_type": "creation_request", "message_type": "user_input", "source": "enterprise_portal", "client_type": "enterprise", "requirements": "erp_integration,high_security", "compliance": "required"}'),

('enterprise-tenant-003', 'user-enterprise-001', 'session-enterprise-001', 'ASSISTANT',
 'Với yêu cầu enterprise, tôi sẽ thiết kế kiến trúc microservices với API gateway, authentication mạnh và monitoring đầy đủ.',
 '{"message_type": "ai_response", "response_type": "consultation", "architecture": "microservices", "security_level": "enterprise", "tools_used": "website_creation"}')

ON CONFLICT DO NOTHING;

-- =============================================================================
-- 8. CREATE DEVELOPMENT HELPER PROCEDURES
-- =============================================================================

-- Procedure to reset development data
CREATE OR REPLACE FUNCTION reset_dev_data()
RETURNS VOID AS $$
BEGIN
    DELETE FROM conversation_history WHERE metadata ->> 'source' LIKE '%dev%' OR tenant_id LIKE '%dev%';
    DELETE FROM vector_store WHERE metadata ->> 'environment' = 'development';

    RAISE NOTICE 'Development data has been reset';
END;
$$ LANGUAGE plpgsql;

-- Procedure to generate test conversations
CREATE OR REPLACE FUNCTION generate_test_conversations(
    p_tenant_id VARCHAR(100),
    p_user_count INTEGER DEFAULT 5,
    p_session_count INTEGER DEFAULT 10
)
RETURNS VOID AS $$
DECLARE
    i INTEGER;
    j INTEGER;
    user_id_val VARCHAR(100);
    session_id_val VARCHAR(100);
BEGIN
    FOR i IN 1..p_user_count LOOP
        user_id_val := p_tenant_id || '-user-' || LPAD(i::text, 3, '0');

        FOR j IN 1..p_session_count LOOP
            session_id_val := p_tenant_id || '-session-' || LPAD((i * p_session_count + j)::text, 4, '0');

            -- Insert user message
            INSERT INTO conversation_history (tenant_id, user_id, session_id, role, content, metadata)
            VALUES (
                p_tenant_id,
                user_id_val,
                session_id_val,
                'USER',
                'Test message ' || j || ' from user ' || i,
                jsonb_build_object(
                    'message_type', 'user_input',
                    'source', 'test_generation',
                    'test_user', i,
                    'test_session', j
                )
            );

            -- Insert assistant response
            INSERT INTO conversation_history (tenant_id, user_id, session_id, role, content, metadata)
            VALUES (
                p_tenant_id,
                user_id_val,
                session_id_val,
                'ASSISTANT',
                'Test response ' || j || ' for user ' || i,
                jsonb_build_object(
                    'message_type', 'ai_response',
                    'source', 'test_generation',
                    'test_user', i,
                    'test_session', j
                )
            );
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Generated % test conversations for tenant %', (p_user_count * p_session_count * 2), p_tenant_id;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- 9. ADD COMPREHENSIVE COMMENTS
-- =============================================================================

-- Table comments
COMMENT ON TABLE conversation_history IS 'Main table storing AI agent conversation history with multi-tenant support and JSONB metadata';
COMMENT ON TABLE vector_store IS 'Vector embeddings storage for Spring AI PgVector integration with semantic search capabilities';

-- Column comments for conversation_history
COMMENT ON COLUMN conversation_history.tenant_id IS 'Tenant identifier for multi-tenant architecture (required for all queries)';
COMMENT ON COLUMN conversation_history.user_id IS 'User identifier within tenant scope';
COMMENT ON COLUMN conversation_history.session_id IS 'Session identifier for conversation continuity and context';
COMMENT ON COLUMN conversation_history.role IS 'Role of message sender: USER (human input), ASSISTANT (AI response), or SYSTEM (system messages)';
COMMENT ON COLUMN conversation_history.content IS 'Message content - user input or AI response text';
COMMENT ON COLUMN conversation_history.metadata IS 'JSONB metadata for flexible key-value storage with efficient querying support';

-- Index comments
COMMENT ON INDEX idx_conversation_metadata_gin IS 'GIN index for efficient JSONB metadata queries using PostgreSQL operators';
COMMENT ON INDEX idx_conversation_tenant_session IS 'Composite index for fast tenant+session queries with chronological ordering';
COMMENT ON INDEX vector_store_embedding_idx IS 'HNSW index for fast vector similarity search using cosine distance';

-- Function comments
COMMENT ON FUNCTION search_conversations_by_metadata IS 'Search conversations by tenant with optional JSONB metadata filters';
COMMENT ON FUNCTION get_metadata_stats IS 'Get statistical analysis of metadata keys and values by tenant';
COMMENT ON FUNCTION cleanup_old_conversations IS 'Cleanup old conversations for data retention compliance';
COMMENT ON FUNCTION generate_test_conversations IS 'Generate test conversation data for development and testing';

-- View comments
COMMENT ON VIEW conversation_stats_by_tenant IS 'Comprehensive statistics view for conversation analytics by tenant';
COMMENT ON VIEW recent_conversations IS 'Development helper view showing recent conversations with content preview';
COMMENT ON VIEW session_analytics IS 'Session-level analytics with duration, message counts, and request type analysis';

-- =============================================================================
-- DEVELOPMENT DATABASE SETUP COMPLETED
-- =============================================================================

-- Display setup summary
DO $$
BEGIN
    RAISE NOTICE '=============================================================================';
    RAISE NOTICE 'AI AGENT DATABASE SETUP COMPLETED FOR DEVELOPMENT';
    RAISE NOTICE '=============================================================================';
    RAISE NOTICE 'Created Tables: conversation_history, vector_store';
    RAISE NOTICE 'Created Indexes: % indexes for performance optimization', 12;
    RAISE NOTICE 'Created Functions: % utility functions', 5;
    RAISE NOTICE 'Created Views: % reporting views', 3;
    RAISE NOTICE 'Inserted Sample Data: % conversation records', (SELECT COUNT(*) FROM conversation_history);
    RAISE NOTICE 'Environment: Development (localhost)';
    RAISE NOTICE '=============================================================================';
END $$;
