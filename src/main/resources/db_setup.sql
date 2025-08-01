-- Cần quyền superuser để tạo extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Bảng lưu vector để tìm kiếm ngữ cảnh
CREATE TABLE vector_store (
    id UUID PRIMARY KEY,
    content TEXT,
    metadata JSON,
    embedding vector(1536) -- 1536 là số chiều của OpenAI
);

-- Bảng lưu lịch sử hội thoại để truy vết
CREATE TABLE conversation_history (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);