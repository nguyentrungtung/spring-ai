package com.service.ai_agent.agent.tools.impl;

import com.service.ai_agent.agent.tools.Tool;
import com.service.ai_agent.infrastructure.laravel_api.LaravelApiClient;
import com.service.ai_agent.infrastructure.laravel_api.dto.WebsiteCreationRequestDTO;
import com.service.ai_agent.infrastructure.laravel_api.dto.WebsiteCreationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebsiteCreationTool implements Tool<WebsiteCreationTool.Request, WebsiteCreationTool.Response> {

    private final LaravelApiClient laravelApiClient;

    @Override
    public String getName() {
        return "createWebsiteTool";
    }

    @Override
    public String getDescription() {
        return "Tạo một website mới cho người dùng. Sử dụng tool này khi người dùng yêu cầu 'tạo web', 'xây dựng trang web', 'làm cho tôi một web'. Tool này cần một mô tả về website và một mã giao diện (templateId) nếu người dùng cung cấp.";
    }

    /**
     * Dữ liệu đầu vào cho tool, được LLM điền vào.
     * @param description Mô tả về website người dùng muốn (ví dụ: 'cửa hàng bán hoa', 'blog du lịch').
     * @param templateId Mã của giao diện người dùng đã chọn (ví dụ: 'tpl-fashion-01'). Có thể là null.
     */
    public record Request(String description, String templateId) {}

    /**
     * Kết quả trả về của tool.
     * @param status Trạng thái của tác vụ tạo web (ví dụ: 'success', 'failed').
     * @param websiteId ID của website vừa được tạo.
     * @param websiteUrl URL để truy cập website mới.
     */
    public record Response(String status, String websiteId, String websiteUrl) {}

    @Override
    public Response apply(Request request) {
        System.out.println("Executing tool: " + getName() + " with description: '" + request.description() + "' and templateId: '" + request.templateId() + "'");

        // 1. Tạo request DTO để gửi đi
        WebsiteCreationRequestDTO apiRequest = new WebsiteCreationRequestDTO(request.description(), request.templateId());

        // 2. Gọi API thật sự
        WebsiteCreationResponseDTO apiResponse = laravelApiClient.createWebsite(apiRequest);

        // 3. Xử lý kết quả trả về từ API
        if (apiResponse == null) {
            // Trường hợp gọi API thất bại
            return new Response("failed", null, "Không thể tạo website do lỗi hệ thống. Vui lòng thử lại sau.");
        }

        // 4. Trả về kết quả thành công
        return new Response(apiResponse.getStatus(), apiResponse.getWebsiteId(), apiResponse.getWebsiteUrl());
    }
}