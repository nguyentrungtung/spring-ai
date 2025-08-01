package com.service.ai_agent.infrastructure.laravel_api;

import com.service.ai_agent.infrastructure.laravel_api.dto.PricingPlanDTO;
import com.service.ai_agent.infrastructure.laravel_api.dto.TemplateDTO;
import com.service.ai_agent.infrastructure.laravel_api.dto.WebsiteCreationRequestDTO;
import com.service.ai_agent.infrastructure.laravel_api.dto.WebsiteCreationResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class LaravelApiClient {

    private final RestTemplate restTemplate;
    private final String laravelApiBaseUrl;

    public LaravelApiClient(RestTemplate restTemplate, @Value("${laravel.api.base-url}") String laravelApiBaseUrl) {
        this.restTemplate = restTemplate;
        this.laravelApiBaseUrl = laravelApiBaseUrl;
    }

    /**
     * Lấy danh sách các giao diện website từ Laravel Service.
     * @return Danh sách các Template, hoặc danh sách rỗng nếu có lỗi.
     */
    public List<TemplateDTO> getAvailableTemplates() {
        String url = laravelApiBaseUrl + "/templates";
        log.info("Calling Laravel API to get templates: {}", url);
        try {
            TemplateDTO[] templates = restTemplate.getForObject(url, TemplateDTO[].class);
            return templates != null ? Arrays.asList(templates) : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Error calling Laravel API for templates at url: {}", url, e);
            // Trả về danh sách rỗng để không làm sập ứng dụng
            return Collections.emptyList();
        }
    }

    /**
     * Lấy danh sách các gói giá từ Laravel Service.
     * @return Danh sách các gói giá, hoặc danh sách rỗng nếu có lỗi.
     */
    public List<PricingPlanDTO> getPricingPlans() {
        String url = laravelApiBaseUrl + "/pricing-plans";
        log.info("Calling Laravel API to get pricing plans: {}", url);
        try {
            PricingPlanDTO[] plans = restTemplate.getForObject(url, PricingPlanDTO[].class);
            return plans != null ? Arrays.asList(plans) : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Error calling Laravel API for pricing plans at url: {}", url, e);
            return Collections.emptyList();
        }
    }

    // ... (restTemplate, baseUrl, constructor, và các phương thức cũ)

    /**
     * Gửi yêu cầu tạo một website mới đến Laravel Service.
     * @param request DTO chứa thông tin để tạo website.
     * @return DTO chứa thông tin của website vừa được tạo, hoặc null nếu có lỗi.
     */
    public WebsiteCreationResponseDTO createWebsite(WebsiteCreationRequestDTO request) {
        String url = laravelApiBaseUrl + "/websites";
        log.info("Calling Laravel API to create a website: {} with payload: {}", url, request);
        try {
            // Sử dụng postForObject để gửi request POST và nhận về đối tượng response
            return restTemplate.postForObject(url, request, WebsiteCreationResponseDTO.class);
        } catch (RestClientException e) {
            log.error("Error calling Laravel API for website creation at url: {}", url, e);
            // Trả về null để Tool có thể xử lý lỗi
            return null;
        }
    }

}