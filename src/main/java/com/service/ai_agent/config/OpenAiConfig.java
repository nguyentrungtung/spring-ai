package com.service.ai_agent.config;

import com.service.ai_agent.agent.tools.ToolRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class OpenAiConfig {

    /**
     * Tạo một Bean ChatClient duy nhất cho toàn bộ ứng dụng, được cấu hình sẵn
     * để sử dụng tất cả các tool có trong ToolRegistry.
     *
     * @param chatModel    Bean ChatModel được Spring AI tự động cấu hình (ví dụ: OpenAiChatModel).
     * @param toolRegistry Nơi đăng ký tất cả các tool của Agent.
     * @return Một instance của ChatClient đã được "trang bị" đầy đủ các tool.
     */
    @Bean
    @Description("ChatClient được cấu hình để có khả năng gọi hàm (Function Calling) với tất cả các tool đã đăng ký.")
    public ChatClient functionCallingChatClient(ChatModel chatModel, ToolRegistry toolRegistry) {

        // Lấy ra danh sách tên của tất cả các tool
        // Đây là các "function name" mà LLM sẽ biết để gọi
        List<String> toolNames = toolRegistry.getTools().keySet().stream().toList();

        System.out.println("==================================================");
        System.out.println("Đang khởi tạo ChatClient với các tool sau:");
        toolNames.forEach(toolName -> System.out.println("- " + toolName));
        System.out.println("==================================================");


        // Sử dụng ChatClient.Builder để tạo client
        // Đây là cách làm chuẩn và linh hoạt nhất trong Spring AI 0.8.x
        return ChatClient.builder(chatModel)
                .defaultTools(toolRegistry)
                .build();
    }
}