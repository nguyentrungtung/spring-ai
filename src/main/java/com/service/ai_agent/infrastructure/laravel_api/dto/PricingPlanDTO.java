package com.service.ai_agent.infrastructure.laravel_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PricingPlanDTO {
    @JsonProperty("plan_name")
    private String planName;
    private double price;
    private String currency;
    private List<String> features;
}