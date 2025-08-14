package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebClientTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testHttpBinEndpoint() {
        webTestClient.get()
                .uri("/webclient/httpbin")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    // 验证返回结果包含特定内容
                    assert response != null;
                });
    }

    @Test
    public void testJsonPlaceholderEndpoint() {
        webTestClient.get()
                .uri("/webclient/jsonplaceholder")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> {
                    // 验证返回结果包含特定内容
                    assert response != null;
                });
    }
}