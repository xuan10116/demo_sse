package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/webclient")
public class WebClientController {

    private final WebClient webClient;

    public WebClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/httpbin")
    public Mono<String> callHttpBin() {
        return webClient.get()
                .uri("https://httpbin.org/get")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Error calling httpbin.org");
    }

    @GetMapping("/jsonplaceholder")
    public Mono<String> callJsonPlaceholder() {
        return webClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts/1")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Error calling jsonplaceholder.typicode.com");
    }

    /**
     * 用于测试的端点，可以配置 baseUrl 来指向 MockWebServer
     */
    @GetMapping("/test")
    public Mono<String> callTestEndpoint() {
        return webClient.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Error calling test endpoint");
    }

    /**
     * 模拟获取用户信息的端点
     */
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getUserInfo() {
        return webClient.get()
                .uri("/user/123")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorMap(WebClientResponseException.class, 
                           ex -> new RuntimeException("Failed to fetch user info: " + ex.getMessage()));
    }

    /**
     * 模拟 POST 请求的端点
     */
    @GetMapping("/post-test")
    public Mono<String> callPostTestEndpoint() {
        return webClient.post()
                .uri("/post")
                .bodyValue("{\"name\": \"test\", \"value\": \"data\"}")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorReturn("Error calling post test endpoint");
    }
}