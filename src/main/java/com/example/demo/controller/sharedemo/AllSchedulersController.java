package com.example.demo.controller.sharedemo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
@RequestMapping("/all-schedulers")
public class AllSchedulersController {

    /**
     * 示例1: Schedulers.immediate() - 在当前线程执行
     * 适用于轻量级、快速完成的操作
     */
    @GetMapping(value = "/immediate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> immediateScheduler() {
        return Flux.range(1, 5)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "数据 " + i + " 在线程 " + threadName;
                })
                .subscribeOn(Schedulers.immediate()) // 在当前线程执行
                .map(data -> data + " -> 使用 immediate 调度器");
    }

    /**
     * 示例2: Schedulers.single() - 全局单线程
     * 适用于轻量级、非并行的任务
     */
    @GetMapping(value = "/single", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> singleScheduler() {
        return Flux.range(1, 5)
                .publishOn(Schedulers.single()) // 使用单线程调度器
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "数据 " + i + " 在线程 " + threadName;
                })
                .delayElements(Duration.ofSeconds(1)); // 添加延迟以便观察
    }
    /**
     * 示例2.2: Schedulers.newSingle() - 全局单线程
     * 适用于轻量级、快速完成的操作
     */
    @GetMapping(value = "/newSingle", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> newSingleScheduler() {
        return Flux.range(1, 5)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "数据 " + i + " 在线程 " + threadName;
                })
                .subscribeOn(Schedulers.newSingle("new的single调度器")) // 在当前线程执行
                .map(data -> data + " -> 使用 自定义new的 single调度器");
    }


    /**
     * 示例3: Schedulers.boundedElastic() - 有界弹性线程池
     * 适用于I/O密集型、阻塞操作
     */
    @GetMapping(value = "/bounded-elastic", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> boundedElasticScheduler() {
        return Flux.range(1, 5)
                .publishOn(Schedulers.boundedElastic()) // 使用有界弹性调度器
                .map(i -> {
                    // 模拟阻塞操作
                    simulateBlockingOperation();
                    String threadName = Thread.currentThread().getName();
                    return "阻塞操作 " + i + " 在线程 " + threadName + " 完成";
                })
                .delayElements(Duration.ofSeconds(1));
    }

    /**
     * 示例4: Schedulers.parallel() - 固定大小线程池（CPU核数）
     * 适用于CPU密集型计算
     */
    @GetMapping(value = "/parallel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> parallelScheduler() {
        return Flux.range(1, 10)
                .publishOn(Schedulers.parallel()) // 使用并行调度器
                .map(i -> {
                    // 模拟CPU密集型操作
                    simulateCpuIntensiveOperation();
                    String threadName = Thread.currentThread().getName();
                    return "CPU计算 " + i + " 在线程 " + threadName + " 完成";
                })
                .delayElements(Duration.ofMillis(500));
    }

    /**
     * 模拟阻塞操作
     */
    private void simulateBlockingOperation() {
        try {
            Thread.sleep(300); // 模拟阻塞操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 模拟CPU密集型操作
     */
    private void simulateCpuIntensiveOperation() {
        // 模拟CPU计算
        double result = 0;
        for (int i = 0; i < 100000; i++) {
            result += Math.sqrt(i);
        }
    }

    /**
     * 获取当前线程信息
     */
    private String getCurrentThreadInfo(String schedulerType) {
        String threadName = Thread.currentThread().getName();
        return "调度器类型: " + schedulerType + " -> 线程: " + threadName;
    }
}