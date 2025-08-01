package com.service.ai_agent.agent.tools.impl;

import com.service.ai_agent.agent.tools.Tool;
import com.service.ai_agent.infrastructure.laravel_api.LaravelApiClient;
import com.service.ai_agent.infrastructure.laravel_api.dto.PricingPlanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PricingInfoTool implements Tool<PricingInfoTool.Request, PricingInfoTool.Response> {

    // Inject client API thật sự
    private final LaravelApiClient laravelApiClient;

    @Override
    public String getName() {
        return "getPricingPlansTool";
    }

    @Override
    public String getDescription() {
        return "Lấy thông tin chi tiết về các gói dịch vụ và giá hiện tại của hệ thống. Dùng khi người dùng hỏi về giá, chi phí, hoặc các gói dịch vụ.";
    }

    // Các record định nghĩa input/output cho Tool
    public record Request() {}
    public record PlanInfo(String planName, double price, String currency, List<String> features) {}
    public record Response(List<PlanInfo> plans) {}

    @Override
    public Response apply(Request request) {
        System.out.println("Executing tool: " + getName() + " by calling external API.");

        // Gọi API thật sự
        List<PricingPlanDTO> dtoList = laravelApiClient.getPricingPlans();

        // Chuyển đổi từ DTO của lớp infrastructure sang record Response của Tool
        List<PlanInfo> planInfos = dtoList.stream()
                .map(dto -> new PlanInfo(dto.getPlanName(), dto.getPrice(), dto.getCurrency(), dto.getFeatures()))
                .collect(Collectors.toList());

        return new Response(planInfos);
    }
}