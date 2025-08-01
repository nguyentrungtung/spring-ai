package com.service.ai_agent.api;

import com.service.ai_agent.agent.orchestration.AgentOrchestrator;
import com.service.ai_agent.api.dto.ChatRequest;
import com.service.ai_agent.api.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final AgentOrchestrator agentOrchestrator;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest) {
        String response = agentOrchestrator.process(chatRequest);
        return new ChatResponse(response);
    }
}