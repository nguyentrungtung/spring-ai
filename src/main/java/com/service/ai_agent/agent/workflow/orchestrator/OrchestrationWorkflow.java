package com.service.ai_agent.agent.workflow.orchestrator;

import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.agent.response.AgentResponse;

public interface OrchestrationWorkflow {
    AgentResponse process(AgentRequest request);
}