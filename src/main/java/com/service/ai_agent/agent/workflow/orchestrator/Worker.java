package com.service.ai_agent.agent.workflow.orchestrator;

import java.util.function.Function;

public interface Worker<T, R> extends Function<T, R> {
    String getName();
    String getDescription();
}