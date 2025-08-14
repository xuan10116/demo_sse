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

public class WebClientMockTest {

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
    public void testWebClientWithMockResponse() {
        // 准备模拟响应
        String expectedResponse = "{\"message\": \"Hello, Mock!\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(expectedResponse));

        // 发送请求并验证响应
        var response = webClient.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(response)
                .expectNext(expectedResponse)
                .expectComplete()
                .verify();
    }

    @Test
    public void testWebClientWithMultipleResponses() {
        // 准备多个模拟响应
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\": 1, \"name\": \"First\"}"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\": 2, \"name\": \"Second\"}"));

        // 发送第一个请求
        var firstResponse = webClient.get()
                .uri("/item/1")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(firstResponse)
                .expectNext("{\"id\": 1, \"name\": \"First\"}")
                .expectComplete()
                .verify();

        // 发送第二个请求
        var secondResponse = webClient.get()
                .uri("/item/2")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(secondResponse)
                .expectNext("{\"id\": 2, \"name\": \"Second\"}")
                .expectComplete()
                .verify();
    }

    @Test
    public void testWebClientWithError() {
        // 模拟服务器错误响应
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Internal Server Error\"}"));

        var response = webClient.get()
                .uri("/error")
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new RuntimeException("Server error: " + errorBody);
                                }))
                .bodyToMono(String.class);

        StepVerifier.create(response)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void testWebClientWithDelay() {
        // 模拟延迟响应
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\": \"delayed response\"}")
                .setHeadersDelay(2, java.util.concurrent.TimeUnit.SECONDS));

        var response = webClient.get()
                .uri("/delayed")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(response)
                .expectNext("{\"status\": \"delayed response\"}")
                .expectComplete()
                .verify();
    }
}