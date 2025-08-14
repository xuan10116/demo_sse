package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/default-scheduler")
public class DefaultSchedulerController {

    /**
     * 示例1: 默认调度器行为
     * 展示不指定调度器时的执行线程
     */
    @GetMapping(value = "/default-behavior", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> defaultBehavior() {
        return Flux.range(1, 5)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "数据 " + i + " 在线程 " + threadName + " 上执行";
                })
                .delayElements(Duration.ofSeconds(1));
    }

    /**
     * 示例2: 对比指定调度器和默认调度器
     * 展示调度器对执行线程的影响
     */
    @GetMapping(value = "/comparison", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> schedulerComparison() {
        // 默认调度器部分
        Flux<String> defaultFlux = Flux.range(1, 3)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "默认调度器 - 数据 " + i + " 在线程 " + threadName;
                });

        // 添加延迟以便观察
        Flux<String> delayedFlux = Flux.interval(Duration.ofSeconds(1))
                .take(3)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "定时任务 - 数据 " + i + " 在线程 " + threadName;
                });

        return Flux.concat(defaultFlux, delayedFlux);
    }

    /**
     * 示例3: 阻塞操作在默认调度器上的影响
     * 展示在默认线程上执行阻塞操作的问题
     */
    @GetMapping(value = "/blocking-default", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> blockingOnDefault() {
        return Flux.range(1, 10)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    // 模拟阻塞操作
                    simulateBlockingOperation();
                    return "阻塞操作 " + i + " 在线程 " + threadName + " 上完成";
                });
    }

    /**
     * 示例4: 非阻塞操作在默认调度器上的表现
     * 展示正常的非阻塞操作
     */
    @GetMapping(value = "/non-blocking-default", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> nonBlockingOnDefault() {
        return Flux.range(1, 10)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    // 非阻塞计算操作
                    long result = performNonBlockingCalculation(i);
                    return "计算结果 " + result + " 在线程 " + threadName + " 上完成";
                })
                .delayElements(Duration.ofMillis(500));
    }

    /**
     * 模拟阻塞操作
     */
    private void simulateBlockingOperation() {
        try {
            // 模拟阻塞500毫秒
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 模拟非阻塞计算操作
     */
    private long performNonBlockingCalculation(int input) {
        // 快速计算操作
        return input * input * input;
    }
}