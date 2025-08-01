package com.service.ai_agent.infrastructure.laravel_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebsiteCreationRequestDTO {
    private String description;
    @JsonProperty("template_id")
    private String templateId;
}