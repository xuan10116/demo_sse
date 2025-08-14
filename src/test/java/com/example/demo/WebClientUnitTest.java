package com.example.demo;

import com.example.demo.config.WebClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.test.StepVerifier;

import java.time.Duration;

public class WebClientUnitTest {

    private WebClient webClient;

    @BeforeEach
    public void setUp() {
        WebClientConfig config = new WebClientConfig();
        webClient = config.webClient();
    }

    @Test
    public void testWebClientGetRequest() {
        // 测试对真实API的调用
        var response = webClient.get()
                .uri("https://httpbin.org/get")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(response)
                .expectNextMatches(result -> result.contains("httpbin.org"))
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    public void testWebClientErrorHandling() {
        // 测试错误处理
        var response = webClient.get()
                .uri("https://invalid-nonexistent-domain-12345.com")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Error occurred");

        StepVerifier.create(response)
                .expectNext("Error occurred")
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    public void testWebClientWithTimeout() {
        var response = webClient.get()
                .uri("https://httpbin.org/delay/1") // 这个接口会延迟1秒返回
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(response.timeout(Duration.ofSeconds(5)))
                .expectNextMatches(result -> result.contains("httpbin.org"))
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }
}