package com.service.ai_agent.agent.workflow.route;

import com.service.ai_agent.agent.request.AgentRequest;

public interface RoutingWorkflow {
    String determineRoute(AgentRequest request);
}