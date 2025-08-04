package com.service.ai_agent.api;

import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.agent.response.AgentResponse;
import com.service.ai_agent.agent.workflow.orchestrator.OrchestrationWorkflow;
import com.service.ai_agent.api.dto.ChatRequest;
import com.service.ai_agent.api.mapper.ChatRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Controller chính để xử lý các tương tác chat với AI Agent.
 * Đóng vai trò là điểm cuối (endpoint) của API cho các ứng dụng client.
 * Cập nhật để hỗ trợ multi-tenant và metadata theo DDD patterns.
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ChatController {

    // Inject trực tiếp OrchestrationWorkflow, là điểm bắt đầu của mọi logic AI.
    private final OrchestrationWorkflow orchestrationWorkflow;
    
    // Inject mapper để chuyển đổi DTO → Domain Object
    private final ChatRequestMapper chatRequestMapper;

    /**
     * Tiếp nhận một yêu cầu chat, xử lý nó thông qua AI Agent và trả về phản hồi.
     *
     * @param request Đối tượng yêu cầu từ client, chứa tenant_id, user_id, session_id và metadata
     * @return Một đối tượng AgentResponse chứa câu trả lời từ AI.
     */
    @PostMapping
    public ResponseEntity<AgentResponse> handleChat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request from user {} in tenant {} (session: {}): {}", 
                request.getUserId(), request.getTenantId(), request.getSessionId(), request.getInput());

        // --- Validation cơ bản ---
        if (request.getInput() == null || request.getInput().isBlank()) {
            log.warn("Received an empty or null chat request from user {} in tenant {}", 
                    request.getUserId(), request.getTenantId());
            AgentResponse errorResponse = new AgentResponse(
                    "Your request could not be processed because the input was empty.",
                    AgentResponse.ResponseStatus.FAILED
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // --- Chuyển đổi từ DTO sang domain object sử dụng Mapper ---
            AgentRequest agentRequest = chatRequestMapper.toAgentRequestWithDefaults(request);

            // --- Chuyển giao yêu cầu cho AI Agent xử lý ---
            // Đây là nơi toàn bộ "phép thuật" của Agentic Workflow bắt đầu.
            AgentResponse response = orchestrationWorkflow.process(agentRequest);

            log.info("Sending agent response to user {} in tenant {} (session: {}): {}", 
                    request.getUserId(), request.getTenantId(), request.getSessionId(), response.getOutput());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // --- Xử lý lỗi toàn cục ---
            log.error("An unexpected error occurred while processing chat request from user {} in tenant {}: {}", 
                    request.getUserId(), request.getTenantId(), e.getMessage(), e);
            AgentResponse errorResponse = new AgentResponse(
                    "An internal server error occurred. Please try again later.",
                    AgentResponse.ResponseStatus.FAILED
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint để kiểm tra service có hoạt động không
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Agent Service is running");
    }
}