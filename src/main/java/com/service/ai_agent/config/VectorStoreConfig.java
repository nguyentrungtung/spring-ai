package com.service.ai_agent.config;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingClient embeddingClient) {
        return new PgVectorStore(
            jdbcTemplate,
            embeddingClient,
            PgVectorStore.PgVectorStoreSettings.builder()
                .withVectorDimensions(1536) // OpenAI 'text-embedding-ada-002'
                .withTableName("vector_store") // Tên bảng lưu vector
                .build()
        );
    }
}