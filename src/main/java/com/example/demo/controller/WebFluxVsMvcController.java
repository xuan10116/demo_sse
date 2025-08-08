package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/comparison")
public class WebFluxVsMvcController {

    /**
     * 传统Spring MVC方式 - 阻塞式
     * 这种方式会阻塞线程直到所有数据准备完成
     */
    @GetMapping("/mvc-blocking")
    public String mvcBlocking() throws InterruptedException {
        // 模拟耗时操作
        Thread.sleep(2000);
        return "传统MVC响应 - " + LocalDateTime.now();
    }

    /**
     * WebFlux方式 - 非阻塞式
     * 这种方式不会阻塞线程，可以处理更多并发请求
     */
    @GetMapping("/webflux-nonblocking")
    public Mono<String> webFluxNonBlocking() {
        // 使用delayElement模拟耗时操作，但不阻塞线程
        return Mono.just("WebFlux响应 - " + LocalDateTime.now())
                .delayElement(Duration.ofSeconds(2));
    }

    /**
     * 传统Spring MVC方式 - 流式数据
     * 这里展示传统方式实现类似SSE的效果
     */
    // 注意：这个方法实际上不能很好地工作，因为MVC是阻塞式的
    // 这里仅为了对比展示
    @GetMapping("/mvc-stream")
    public String mvcStream() throws InterruptedException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000); // 模拟耗时操作
            result.append("MVC流数据 ").append(i).append(" - ").append(LocalDateTime.now()).append("\n");
        }
        return result.toString();
    }

    /**
     * WebFlux方式 - 真正的流式数据
     * 使用Flux实现真正的流式响应
     */
    @GetMapping(value = "/webflux-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> webFluxStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .map(i -> "WebFlux流数据 " + i + " - " + LocalDateTime.now());
    }

    /**
     * 展示背压处理的差异
     * WebFlux可以自然处理背压，而传统MVC需要额外处理
     */
    @GetMapping(value = "/backpressure-demo", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureDemo() {
        // 创建一个快速发射数据的流
        return Flux.interval(Duration.ofMillis(100))
                .onBackpressureBuffer(50) // 处理背压
                .map(i -> "数据项: " + i + " 时间: " + LocalDateTime.now())
                .take(100);
    }
}