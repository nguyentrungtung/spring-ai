package com.service.ai_agent.agent.workflow.orchestrator;

import com.service.ai_agent.agent.memory.AgentMemoryService;
import com.service.ai_agent.agent.prompt.SystemPromptFactory;
import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.agent.response.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("defaultOrchestrationWorkflow")
@RequiredArgsConstructor
public class DefaultOrchestrationWorkflow implements OrchestrationWorkflow {

    private final ChatClient chatClient;
    private final AgentMemoryService memoryService;
    private final SystemPromptFactory promptFactory;

    @Override
    public AgentResponse process(AgentRequest request) {
        String context = memoryService.retrieveContext(request.getSessionId());
        String systemPromptText = promptFactory.createSystemPrompt(context);
        SystemMessage systemMessage = new SystemMessage(systemPromptText);
        UserMessage userMessage = new UserMessage(request.getInput());

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        String aiResponseContent = chatClient.prompt(prompt).call().content().trim().toUpperCase();


        memoryService.saveInteraction(request, aiResponseContent);

        return new AgentResponse(aiResponseContent, AgentResponse.ResponseStatus.SUCCESS);
    }
}