package com.service.ai_agent.agent.workflow.chain;

import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.agent.response.AgentResponse;

public interface ChainWorkflow {
    AgentResponse execute(AgentRequest request);
}