package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/parallel-scheduler")
public class ParallelSchedulerController {

    /**
     * 示例1: CPU密集型任务
     * 展示如何使用并行调度器处理计算密集型任务
     */
    @GetMapping(value = "/cpu-intensive", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> cpuIntensiveTask() {
        return Flux.range(1, 10)
                .publishOn(Schedulers.parallel()) // 使用并行调度器
                .map(this::performCpuIntensiveTask)
                .map(result -> "计算结果: " + result);
    }

    /**
     * 模拟CPU密集型任务
     */
    private long performCpuIntensiveTask(int input) {
        // 模拟计算密集型操作
        long result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += Math.pow(ThreadLocalRandom.current().nextDouble(), 2);
        }
        return result + input;
    }

    /**
     * 示例2: 并行处理多个任务
     * 展示如何利用并行调度器并行处理多个任务
     */
    @GetMapping(value = "/parallel-processing", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> parallelProcessing() {
        return Flux.range(1, 20)
                .groupBy(i -> i % 4) // 将任务分为4组
                .flatMap(groupedFlux -> groupedFlux
                        .publishOn(Schedulers.parallel()) // 每组使用并行调度器
                        .map(this::performCpuIntensiveTask)
                )
                .map(result -> "并行处理结果: " + result);
    }

    /**
     * 示例3: 并行调度器与默认调度器对比
     * 展示使用并行调度器和默认调度器的线程差异
     */
    @GetMapping(value = "/thread-comparison", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> threadComparison() {
        return Flux.range(1, 5)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "默认调度器 - 数据 " + i + " 在线程 " + threadName;
                })
                .publishOn(Schedulers.parallel()) // 切换到并行调度器
                .map(data -> {
                    String threadName = Thread.currentThread().getName();
                    return data + " -> 并行调度器 - 在线程 " + threadName;
                })
                .delayElements(Duration.ofSeconds(1));
    }
}