package com.service.ai_agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenAiConfig {

    /**
     * Tạo một Bean ChatClient duy nhất cho toàn bộ ứng dụng, được cấu hình sẵn
     * để sử dụng tất cả các tool có trong hệ thống.
     *
     * @param chatModel    Bean ChatModel được Spring AI tự động cấu hình (ví dụ: OpenAiChatModel).
     * @return Một instance của ChatClient đã được "trang bị" đầy đủ các tool.
     */
    @Bean
    @Description("ChatClient được cấu hình để có khả năng gọi hàm (Function Calling) với tất cả các tool đã đăng ký.")
    public ChatClient functionCallingChatClient(ChatModel chatModel) {

        System.out.println("==================================================");
        System.out.println("Đang khởi tạo ChatClient với Spring AI 1.0.0");
        System.out.println("==================================================");

        // Với Spring AI 1.0.0, sử dụng cách đơn giản hơn
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Chỉ định OpenAI embedding model là primary để tránh conflict
     */
    @Bean
    @Primary
    @Description("Primary embedding model sử dụng OpenAI")
    public EmbeddingModel primaryEmbeddingModel(@Qualifier("openAiEmbeddingModel") EmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }
}