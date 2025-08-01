package com.service.ai_agent.agent.workflow.route;

import com.service.ai_agent.agent.request.AgentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IntentBasedRoutingWorkflow implements RoutingWorkflow {

    private final ChatClient chatClient;

    public static final String ROUTE_CONSULTING_WORKFLOW = "workflow:chain:consulting";
    public static final String ROUTE_ORCHESTRATION_WORKFLOW = "workflow:orchestration:default";

    private static final String ROUTING_PROMPT_TEMPLATE = """
            You are an expert intent classifier. Your task is to classify the user's request into one of the following categories:
            - CONSULTING: The user is asking for advice, recommendations, or doesn't know what to choose (e.g., 'tư vấn cho tôi', 'nên chọn mẫu nào', 'tôi muốn làm web bán hàng thì sao').
            - ORCHESTRATION: The user has a direct command or question that can be answered by using tools (e.g., 'tạo cho tôi web', 'giá gói nâng cao là bao nhiêu', 'liệt kê các giao diện').

            Based on the user's request below, what is the correct category?
            Request: "{input}"
            
            Respond with only one word: CONSULTING or ORCHESTRATION.
            """;

    @Override
    public String determineRoute(AgentRequest request) {
        PromptTemplate promptTemplate = new PromptTemplate(ROUTING_PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(Map.of("input", request.getInput()));

        String intent = chatClient.prompt(prompt).call().content().trim().toUpperCase();

        if ("CONSULTING".equals(intent)) {
            return ROUTE_CONSULTING_WORKFLOW;
        }
        
        return ROUTE_ORCHESTRATION_WORKFLOW;
    }
}