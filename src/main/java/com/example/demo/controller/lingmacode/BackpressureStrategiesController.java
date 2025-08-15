package com.example.demo.controller.lingmacode;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@RestController
@RequestMapping("/backpressure")
public class BackpressureStrategiesController {

    private static final Logger logger = Logger.getLogger(BackpressureStrategiesController.class.getName());

    /**
     * 示例1: 使用onBackpressureBuffer策略
     * 缓冲所有多余的元素直到达到指定的限制
     */
    @GetMapping(value = "/buffer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureBuffer() {
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureBuffer(100, 
                    item -> logger.info("丢弃的项目: " + item))
                .take(300)
                .map(i -> "Buffer策略处理数据: " + i);
    }

    /**
     * 示例2: 使用onBackpressureDrop策略
     * 当下游无法跟上时，丢弃多余的元素
     */
    @GetMapping(value = "/drop", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureDrop() {
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureDrop(item -> logger.info("丢弃的项目: " + item))
                .take(300)
                .map(i -> "Drop策略处理数据: " + i);
    }

    /**
     * 示例3: 使用onBackpressureLatest策略
     * 当下游无法跟上时，只保留最新的元素
     */
    @GetMapping(value = "/latest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureLatest() {
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureLatest()
                .take(300)
                .map(i -> "Latest策略处理数据: " + i);
    }

    /**
     * 示例4: 使用onBackpressureError策略
     * 当下游无法跟上时，发出一个错误信号
     */
    @GetMapping(value = "/error", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureError() {
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureError()
                .map(i -> "Error策略处理数据: " + i)
                .onErrorResume(throwable -> {
                    logger.severe("背压错误: " + throwable.getMessage());
                    return Flux.just("背压错误发生，流已终止");
                });
    }

    /**
     * 示例5: 使用limitRate限制请求速率
     * 控制上游发布者的请求速率
     */
    @GetMapping(value = "/limit-rate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> limitRate() {
        return Flux.interval(Duration.ofMillis(1))
                .limitRate(10) // 限制请求速率
                .take(100)
                .map(i -> "限速策略处理数据: " + i);
    }

    /**
     * 示例6: 自定义背压处理 - 结合多种策略
     * 演示如何组合使用不同的背压策略
     */
    @GetMapping(value = "/custom", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> customBackpressureHandling() {
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger dropped = new AtomicInteger(0);

        return Flux.interval(Duration.ofMillis(5))
                // 使用缓冲策略，最多缓冲50个元素
                .onBackpressureBuffer(50, 
                    item -> {
                        dropped.incrementAndGet();
                        logger.info("自定义处理 - 丢弃的项目: " + item);
                    })
                .take(200)
                .map(i -> {
                    int p = processed.incrementAndGet();
                    int d = dropped.get();
                    return String.format("自定义策略 - 处理: %d, 丢弃: %d, 当前数据: %d", p, d, i);
                });
    }

    /**
     * 示例7: 使用sample操作符处理背压
     * 以指定的时间间隔采样数据流
     */
    @GetMapping(value = "/sample", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sampleBackpressure() {
        return Flux.interval(Duration.ofMillis(1))
                .sample(Duration.ofMillis(100)) // 每100ms采样一次
                .map(i -> "采样策略处理数据: " + i);
    }

    /**
     * 示例8: 使用window和buffer操作符处理背压
     * 将数据流分组处理
     */
    @GetMapping(value = "/window-buffer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> windowBufferBackpressure() {
        return Flux.interval(Duration.ofMillis(10))
                .window(10) // 将数据流分为每10个元素一组
                .flatMap(window -> 
                    window.buffer(10) // 缓冲每组的元素
                         .map(buffer -> "窗口缓冲策略处理数据: " + buffer)
                )
                .take(20);
    }
}