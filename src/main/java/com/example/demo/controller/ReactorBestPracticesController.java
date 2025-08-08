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
@RequestMapping("/best-practices")
public class ReactorBestPracticesController {

    /**
     * 最佳实践1: 避免在操作符中使用阻塞操作
     * 如果必须使用阻塞操作，应该使用合适的调度器
     */
    @GetMapping(value = "/avoid-blocking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> avoidBlockingOperations() {
        return Flux.range(1, 10)
                .publishOn(Schedulers.boundedElastic()) // 使用弹性调度器处理阻塞操作
                .map(this::blockingOperation)
                .map(result -> "处理结果: " + result);
    }

    /**
     * 模拟阻塞操作
     */
    private String blockingOperation(int input) {
        try {
            // 模拟阻塞操作，如数据库查询或外部API调用
            Thread.sleep(100);
            return "处理完成: " + input;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * 最佳实践2: 正确处理资源共享
     * 避免在流中使用共享可变状态
     */
    @GetMapping(value = "/shared-state", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sharedStateHandling() {
        // 错误示例：使用共享可变状态
        StringBuilder badExample = new StringBuilder();
        
        return Flux.range(1, 5)
                .map(i -> {
                    // 错误：修改共享状态
                    badExample.append(i).append(",");
                    return "共享状态: " + badExample.toString();
                })
                .delayElements(Duration.ofMillis(500));
    }

    /**
     * 最佳实践3: 合理使用缓存
     * 使用cache()操作符缓存流的结果
     */
    @GetMapping("/caching")
    public Mono<String> cachingExample() {
        Mono<Long> cachedMono = Mono.fromCallable(System::currentTimeMillis)
                .cache(Duration.ofSeconds(10)); // 缓存10秒
        
        return cachedMono
                .map(time -> "当前时间戳: " + time);
    }

    /**
     * 最佳实践4: 正确处理错误
     * 使用不同的错误处理操作符
     */
    @GetMapping(value = "/error-handling", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> properErrorHandling() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> {
                    if (i == 3) {
                        throw new RuntimeException("模拟业务异常");
                    }
                    return "正常数据: " + i;
                })
                .onErrorContinue((error, obj) -> {
                    // 记录错误但继续处理
                    System.err.println("捕获到错误: " + error.getMessage() + " 对象: " + obj);
                })
                .map(data -> data + " 已处理");
    }

    /**
     * 最佳实践5: 使用合适的操作符
     * 根据需要选择flatMap、concatMap或switchMap
     */
    @GetMapping(value = "/operator-selection", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> operatorSelection() {
        // 使用concatMap保证顺序
        Flux<String> concatExample = Flux.just("A", "B", "C")
                .concatMap(this::asyncOperation);
        
        // 使用flatMap并行处理
        Flux<String> flatExample = Flux.just("1", "2", "3")
                .flatMap(this::asyncOperation);
        
        return Flux.merge(concatExample, flatExample)
                .map(result -> "处理结果: " + result);
    }

    /**
     * 模拟异步操作
     */
    private Mono<String> asyncOperation(String input) {
        // 模拟异步操作，随机延迟
        int delay = ThreadLocalRandom.current().nextInt(100, 500);
        return Mono.just(input + "-processed")
                .delayElement(Duration.ofMillis(delay));
    }

    /**
     * 最佳实践6: 资源管理
     * 正确管理资源的获取和释放
     */
    @GetMapping("/resource-management")
    public Mono<String> resourceManagement() {
        // 使用usingWhen正确管理资源
        return Mono.usingWhen(
                Mono.fromCallable(() -> "获取资源"), // 获取资源
                resource -> Mono.fromCallable(() -> "使用资源: " + resource), // 使用资源
                resource -> Mono.fromRunnable(() -> System.out.println("清理资源: " + resource)), // 清理资源
                (resource, error) -> Mono.fromRunnable(() -> System.out.println("异常清理资源: " + resource)), // 异常清理
                resource -> Mono.fromRunnable(() -> System.out.println("最终清理资源: " + resource)) // 最终清理
        );
    }
}