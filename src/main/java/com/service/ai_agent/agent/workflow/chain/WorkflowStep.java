package com.service.ai_agent.agent.workflow.chain;

@FunctionalInterface
public interface WorkflowStep<T> {
    T execute(T context);

    default WorkflowStep<T> andThen(WorkflowStep<T> next) {
        return context -> next.execute(this.execute(context));
    }
}