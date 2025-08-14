package com.example.demo;

import com.example.demo.config.WebClientConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

public class WebClientFluxTest {

    private static MockWebServer mockWebServer;
    private static WebClient webClient;

    @BeforeAll
    static void setUp() throws IOException {
        // 创建 MockWebServer 实例
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // 配置 WebClient 使用 MockWebServer 的基地址
        WebClientConfig config = new WebClientConfig();
        webClient = config.webClient();
        webClient = webClient.mutate()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
    }

    @AfterAll
    static void tearDown() throws IOException {
        // 关闭 MockWebServer
        mockWebServer.shutdown();
    }

    @Test
    public void testWebClientWithFluxResponse() {
        // 准备模拟响应 - JSON数组
        String jsonResponse = "[{\"id\": 1, \"name\": \"Item 1\"}, {\"id\": 2, \"name\": \"Item 2\"}, {\"id\": 3, \"name\": \"Item 3\"}]";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        // 使用 bodyToFlux 处理数组响应
        var response = webClient.get()
                .uri("/items")
                .retrieve()
                .bodyToFlux(String.class); // 这里为了简化直接使用String.class

        // 验证Flux流
        StepVerifier.create(response)
                .expectNextCount(1) // 整个JSON数组作为单个元素
                .expectComplete()
                .verify();
    }

    @Test
    public void testWebClientWithFluxStreamResponse() {
        // 准备模拟流式响应
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/stream+json")
                .setBody("{\"id\": 1, \"name\": \"Item 1\"}\n")
                .setBodyDelay(1, java.util.concurrent.TimeUnit.SECONDS)); // 延迟1秒

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/stream+json")
                .setBody("{\"id\": 2, \"name\": \"Item 2\"}\n")
                .setBodyDelay(1, java.util.concurrent.TimeUnit.SECONDS)); // 延迟1秒

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/stream+json")
                .setBody("{\"id\": 3, \"name\": \"Item 3\"}\n"));

        // 使用 bodyToFlux 处理流式响应
        var response = webClient.get()
                .uri("/stream")
                .retrieve()
                .bodyToFlux(String.class);

        // 验证Flux流
        StepVerifier.create(response)
                .expectNext("{\"id\": 1, \"name\": \"Item 1\"}")
                .expectNext("{\"id\": 2, \"name\": \"Item 2\"}")
                .expectNext("{\"id\": 3, \"name\": \"Item 3\"}")
                .expectComplete()
                .verify();
    }
}