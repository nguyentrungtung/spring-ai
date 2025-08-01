package com.service.ai_agent.infrastructure.laravel_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebsiteCreationResponseDTO {
    private String status;
    @JsonProperty("website_id")
    private String websiteId;
    @JsonProperty("website_url")
    private String websiteUrl;
}