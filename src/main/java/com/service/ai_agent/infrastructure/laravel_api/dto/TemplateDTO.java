package com.service.ai_agent.infrastructure.laravel_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TemplateDTO {
    private String id;
    private String name;
    private String description;

    // Sử dụng khi tên thuộc tính trong JSON là snake_case
    @JsonProperty("preview_image_url")
    private String previewImageUrl;
}
