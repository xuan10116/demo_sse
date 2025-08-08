package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/common-issues")
public class CommonIssuesController {

    /**
     * 问题1: 错误地在响应式流中使用阻塞操作
     * 问题代码示例
     */
    @GetMapping(value = "/blocking-mistake", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> blockingMistake() {
        // 错误示例：在主线程中执行阻塞操作
        return Flux.range(1, 5)
                .map(i -> {
                    // 错误：直接在响应式流中使用阻塞操作
                    try {
                        Thread.sleep(1000); // 这会阻塞Netty线程
                        return "数据 " + i;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * 问题1解决方案: 使用合适的调度器
     */
    @GetMapping(value = "/blocking-solution", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> blockingSolution() {
        // 正确示例：使用调度器处理阻塞操作
        return Flux.range(1, 5)
                .publishOn(Schedulers.boundedElastic()) // 使用弹性调度器
                .map(i -> {
                    try {
                        Thread.sleep(1000); // 在弹性线程池中执行
                        return "数据 " + i;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * 问题2: 共享可变状态导致的问题
     * 问题代码示例
     */
    @GetMapping(value = "/mutable-state-issue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> mutableStateIssue() {
        List<String> sharedList = new ArrayList<>();
        
        return Flux.range(1, 5)
                .map(i -> {
                    // 错误：多个流操作共享可变状态
                    sharedList.add("item-" + i);
                    return "共享列表大小: " + sharedList.size();
                })
                .delayElements(Duration.ofMillis(500));
    }

    /**
     * 问题2解决方案: 避免共享可变状态
     */
    @GetMapping(value = "/mutable-state-solution", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> mutableStateSolution() {
        AtomicInteger counter = new AtomicInteger(0);
        
        return Flux.range(1, 5)
                .map(i -> {
                    // 正确：使用线程安全的原子操作
                    int count = counter.incrementAndGet();
                    return "计数器值: " + count;
                })
                .delayElements(Duration.ofMillis(500));
    }

    /**
     * 问题3: 错误处理不当
     * 问题代码示例
     */
    @GetMapping(value = "/error-handling-issue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> errorHandlingIssue() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> {
                    if (i == 3) {
                        throw new RuntimeException("发生异常");
                    }
                    return "数据: " + i;
                })
                // 错误：没有适当的错误处理
                .map(data -> data + " 已处理");
    }

    /**
     * 问题3解决方案: 正确的错误处理
     */
    @GetMapping(value = "/error-handling-solution", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> errorHandlingSolution() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> {
                    if (i == 3) {
                        throw new RuntimeException("发生异常");
                    }
                    return "数据: " + i;
                })
                .onErrorContinue((error, obj) -> {
                    // 正确：记录错误并继续处理
                    System.err.println("处理数据时出错: " + obj + ", 错误: " + error.getMessage());
                })
                .map(data -> data + " 已处理");
    }

    /**
     * 问题4: 背压处理不当
     * 问题代码示例
     */
    @GetMapping(value = "/backpressure-issue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureIssue() {
        // 创建一个快速发射数据的流
        return Flux.interval(Duration.ofMillis(1))
                // 错误：没有处理背压
                .map(i -> "快速数据: " + i);
    }

    /**
     * 问题4解决方案: 正确处理背压
     */
    @GetMapping(value = "/backpressure-solution", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureSolution() {
        // 创建一个快速发射数据的流
        return Flux.interval(Duration.ofMillis(1))
                .onBackpressureBuffer(1000) // 正确：处理背压
                .map(i -> "受控数据: " + i)
                .limitRate(100); // 限制请求速率
    }

    /**
     * 问题5: 内存泄漏 - 没有正确取消订阅
     * 问题代码示例
     */
    @GetMapping("/subscription-leak")
    public Mono<String> subscriptionLeak() {
        // 错误：长时间运行的流没有正确管理
        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(i -> System.out.println("后台任务: " + i))
                .subscribe(); // 错误：没有保存订阅引用，无法取消
        
        return Mono.just("启动了后台任务（但无法控制）");
    }

    /**
     * 问题5解决方案: 正确管理订阅
     */
    @GetMapping("/subscription-management")
    public Mono<String> subscriptionManagement() {
        // 正确：保存订阅引用以便后续管理
        var subscription = Flux.interval(Duration.ofSeconds(1))
                .doOnNext(i -> System.out.println("受控后台任务: " + i))
                .subscribe();
        
        // 可以在适当时机取消订阅
        // subscription.dispose();
        
        return Mono.just("启动了受控后台任务");
    }
}