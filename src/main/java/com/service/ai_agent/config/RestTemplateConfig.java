package com.service.ai_agent.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            // Thiết lập timeout cho việc kết nối đến server
            .connectTimeout(Duration.ofMillis(3000))
            .readTimeout(Duration.ofMillis(3000))
            .build();
    }
}