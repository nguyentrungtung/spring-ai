package com.service.ai_agent.agent.tools;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ToolRegistry {

    private final Map<String, Function> tools;

    public ToolRegistry(ApplicationContext context) {
        this.tools = context.getBeansOfType(Tool.class).values().stream()
                .collect(Collectors.toMap(Tool::getName, tool -> tool));
    }

    public Function getTool(String name) {
        return tools.get(name);
    }

    public Map<String, Function> getTools() {
        return tools;
    }
}