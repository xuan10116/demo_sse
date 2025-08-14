package com.example.demo;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class WebClientControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUpMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownMockServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setupTest() {
        // 设置 WebClient 的 baseUrl 指向 MockWebServer
        System.setProperty("webclient.base-url", mockWebServer.url("/").toString());
    }

    @Test
    public void testCallTestEndpoint() {
        // 准备模拟响应
        String expectedResponse = "{\"message\": \"Hello from mock server\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(expectedResponse));

        // 测试 WebClientController 的 /webclient/test 端点
        webTestClient.get()
                .uri("/webclient/test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(expectedResponse);
    }

    @Test
    public void testGetUserInfo() {
        // 准备模拟用户信息响应
        String userResponse = "{\"id\": 123, \"name\": \"John Doe\", \"email\": \"john.doe@example.com\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(userResponse));

        // 测试 WebClientController 的 /webclient/user 端点
        webTestClient.get()
                .uri("/webclient/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(userResponse);
    }

    @Test
    public void testGetUserInfoWithError() {
        // 模拟服务器错误
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"error\": \"Internal Server Error\"}"));

        // 测试 WebClientController 的 /webclient/user 端点在错误情况下的处理
        webTestClient.get()
                .uri("/webclient/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk() // 我们的控制器会捕获错误并返回字符串
                .expectBody(String.class)
                .value(response -> {
                    assert response.contains("Failed to fetch user info");
                });
    }

    @Test
    public void testCallPostTestEndpoint() {
        // 准备模拟 POST 响应
        String postResponse = "{\"result\": \"Data posted successfully\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(postResponse));

        // 测试 WebClientController 的 /webclient/post-test 端点
        webTestClient.get()
                .uri("/webclient/post-test")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(postResponse);
    }
}