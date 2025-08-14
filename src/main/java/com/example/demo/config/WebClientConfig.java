package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${webclient.base-url:}")
    private String baseUrl;

    @Bean
    public WebClient webClient() {
        WebClient.Builder builder = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024));
        
        // 如果配置了基础 URL，则使用它
        if (baseUrl != null && !baseUrl.isEmpty()) {
            builder = builder.baseUrl(baseUrl);
        }
        
        return builder.build();
    }
}