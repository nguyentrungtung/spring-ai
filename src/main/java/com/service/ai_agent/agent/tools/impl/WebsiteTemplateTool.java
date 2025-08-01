package com.service.ai_agent.agent.tools.impl;

import com.service.ai_agent.agent.tools.Tool;
import com.service.ai_agent.infrastructure.laravel_api.LaravelApiClient;
import com.service.ai_agent.infrastructure.laravel_api.dto.TemplateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebsiteTemplateTool implements Tool<WebsiteTemplateTool.Request, WebsiteTemplateTool.Response> {

    private final LaravelApiClient laravelApiClient;

    @Override
    public String getName() {
        return "getAvailableTemplatesTool";
    }

    @Override
    public String getDescription() {
        return "Lấy danh sách tất cả các giao diện website có sẵn của hệ thống. Dùng khi người dùng muốn xem các mẫu giao diện.";
    }

    public record Request() {}
    public record TemplateInfo(String id, String name, String description, String previewImageUrl) {}
    public record Response(List<TemplateInfo> templates) {}

    @Override
    public Response apply(Request request) {
        System.out.println("Executing tool: " + getName() + " by calling external API.");

        List<TemplateDTO> dtoList = laravelApiClient.getAvailableTemplates();

        List<TemplateInfo> templateInfos = dtoList.stream()
                .map(dto -> new TemplateInfo(dto.getId(), dto.getName(), dto.getDescription(), dto.getPreviewImageUrl()))
                .collect(Collectors.toList());

        return new Response(templateInfos);
    }
}