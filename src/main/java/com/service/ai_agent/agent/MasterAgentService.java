package com.service.ai_agent.agent;


import com.service.ai_agent.agent.request.AgentRequest;
import com.service.ai_agent.agent.response.AgentResponse;
import com.service.ai_agent.agent.workflow.chain.ChainWorkflow;
import com.service.ai_agent.agent.workflow.orchestrator.OrchestrationWorkflow;
import com.service.ai_agent.agent.workflow.route.IntentBasedRoutingWorkflow;
import com.service.ai_agent.agent.workflow.route.RoutingWorkflow;
import com.service.ai_agent.api.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MasterAgentService {

    private final RoutingWorkflow router;
    private final @Qualifier("consultingChainWorkflow") ChainWorkflow consultingWorkflow;
    private final @Qualifier("defaultOrchestrationWorkflow") OrchestrationWorkflow orchestrationWorkflow;

    public String processRequest(ChatRequest chatRequest) {
        AgentRequest agentRequest = new AgentRequest(
                chatRequest.userId(),
                chatRequest.sessionId(),
                chatRequest.tenantId(),
                chatRequest.message(),
                Map.of()
        );

        String route = router.determineRoute(agentRequest);
        System.out.println("Determined Route: " + route);

        AgentResponse response;

        switch (route) {
            case IntentBasedRoutingWorkflow.ROUTE_CONSULTING_WORKFLOW:
                response = consultingWorkflow.execute(agentRequest);
                break;

            case IntentBasedRoutingWorkflow.ROUTE_ORCHESTRATION_WORKFLOW:
            default:
                response = orchestrationWorkflow.process(agentRequest);
                break;
        }

        return response.getOutput();
    }
}