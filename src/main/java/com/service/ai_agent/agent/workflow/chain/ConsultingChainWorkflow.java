package com.service.ai_agent.agent.workflow.chain;

import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.agent.response.AgentResponse;
import com.service.ai_agent.agent.tools.impl.WebsiteTemplateTool;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("consultingChainWorkflow")
@RequiredArgsConstructor
public class ConsultingChainWorkflow implements ChainWorkflow {

    private final WebsiteTemplateTool getTemplatesTool;
    // Việc inject này bây giờ đã hoạt động nhờ có AiClientConfig
    private final ChatClient chatClient;

    @Data
    private static class ConsultingContext {
        private AgentRequest originalRequest;
        private List<WebsiteTemplateTool.TemplateInfo> availableTemplates;
        private String finalRecommendation;
    }

    @Override
    public AgentResponse execute(AgentRequest request) {
        ConsultingContext context = new ConsultingContext();
        context.setOriginalRequest(request);

        WorkflowStep<ConsultingContext> fetchTemplatesStep = this::fetchAvailableTemplates;
        WorkflowStep<ConsultingContext> analyzeAndRecommendStep = this::analyzeAndRecommend;

        ConsultingContext finalContext = fetchTemplatesStep.andThen(analyzeAndRecommendStep).execute(context);(analyzeAndRecommendStep).execute(context);

        return new AgentResponse(finalContext.getFinalRecommendation(), AgentResponse.ResponseStatus.SUCCESS);
    }

    private ConsultingContext fetchAvailableTemplates(ConsultingContext context) {
        System.out.println("[Chain] Step 1: Fetching available templates...");
        WebsiteTemplateTool.Response response = getTemplatesTool.apply(new WebsiteTemplateTool.Request());
        context.setAvailableTemplates(response.templates());
        return context;
    }

    private ConsultingContext analyzeAndRecommend(ConsultingContext context) {
        System.out.println("[Chain] Step 2: Analyzing and recommending...");
        String templatesAsText = context.getAvailableTemplates().stream()
                .map(t -> String.format("- ID: %s, Name: %s, Description: %s", t.id(), t.name(), t.description()))
                .collect(Collectors.joining("\n"));

        String promptText = """
                You are a helpful consultant. Based on the user's request and the list of available website templates, provide a helpful recommendation.
                Your answer must be friendly and address the user directly.

                User's Request: "{request}"

                Available Templates:
                {templates}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
                "request", context.getOriginalRequest().getInput(),
                "templates", templatesAsText
        ));

        // --- ĐÂY LÀ THAY ĐỔI QUAN TRỌNG NHẤT ---
        // Xóa dòng code cũ:
        // String recommendation = chatClient.call(prompt).getResult().getOutput().getContent();

        // Thay bằng cách gọi theo chuỗi (fluent API) của phiên bản cũ:
        String recommendation = chatClient.prompt(prompt) // Bắt đầu xây dựng request với Prompt
                .call()                                   // Thực hiện lời gọi API
                .content();                               // Lấy nội dung text từ response

        context.setFinalRecommendation(recommendation);
        return context;
    }
}