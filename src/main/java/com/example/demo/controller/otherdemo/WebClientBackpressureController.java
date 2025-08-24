package com.example.demo.controller.otherdemo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.logging.Logger;

@RestController
@RequestMapping("/webclient-backpressure")
public class WebClientBackpressureController {

    private final WebClient webClient;
    private static final Logger logger = Logger.getLogger(WebClientBackpressureController.class.getName());

    public WebClientBackpressureController(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 使用WebClient演示跨服务背压控制
     * 通过limitRate控制从远程服务获取数据的速率
     * 通过onBackpressureBuffer处理客户端来不及处理的数据
     */
    @GetMapping(value = "/subscriber", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> webClientBackpressureDemo() {
        // 调用我们自己的模拟远程服务
        return webClient.get()
                .uri("http://localhost:8080/webclient-backpressure/publisher")
                .retrieve()
                .bodyToFlux(String.class)
                .limitRate(5) // 限制从远程服务接收数据的速率，实现背压控制
                .log()
                .onBackpressureBuffer(300, BufferOverflowStrategy.ERROR) // 添加背压缓冲策略
                .map(data -> {
                    // 模拟处理数据需要时间
                    try {
                        Thread.sleep(200); // 增加处理时间以更好地演示背压效果
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "处理远程数据: " + data;
                })
                .onErrorResume(throwable -> {
                    logger.severe("WebClient背压错误: " + throwable.getMessage());
                    return Flux.just("发生错误，流已终止");
                });
    }


    /**
     * 模拟一个快速产生数据的远程服务端点
     * 用于演示跨服务背压控制
     */
    @GetMapping(value = "/publisher", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> fastDataStream() {
        return Flux.interval(Duration.ofMillis(5))
                .take(200)
                .map(i -> "来活了 #" + i)
                .log();
    }
}