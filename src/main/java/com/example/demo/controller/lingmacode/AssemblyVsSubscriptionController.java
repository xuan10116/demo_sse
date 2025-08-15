package com.example.demo.controller.lingmacode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Duration;

@RestController
public class AssemblyVsSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(AssemblyVsSubscriptionController.class);

    /**
     * 演示装配时和订阅时的区别
     * 装配时：创建操作符链的时候
     * 订阅时：调用subscribe()方法的时候
     */
    @GetMapping("/assembly-vs-subscription")
    public Mono<String> demonstrateAssemblyVsSubscription() {
        StringBuilder result = new StringBuilder();

        result.append("=== 装配时 vs 订阅时演示 ===\n\n");

        // 装配时 - 这段代码在装配时执行
        result.append("1. 装配时 (Assembly time):\n");
        
        Flux<String> flux = Flux.just("A", "B", "C")                                    // 装配时
                .map(s -> {
                    logger.info("装配时 map 操作: {}", s);                              // 订阅时才执行
                    return s.toLowerCase();
                })
                .filter(s -> {
                    logger.info("装配时 filter 操作: {}", s);                           // 订阅时才执行
                    return s.equals("a") || s.equals("b");
                });

        result.append("   - Flux操作符链已定义，但尚未执行\n");
        result.append("   - 此时只是创建了操作链，没有实际数据流动\n\n");

        // 订阅时 - subscribe() 调用后开始执行
        result.append("2. 订阅时 (Subscription time):\n");
        
        StringBuilder subscriptionResult = new StringBuilder();
        flux.doOnNext(s -> subscriptionResult.append("处理元素: ").append(s).append("\n"))
            .subscribe(s -> {
                logger.info("订阅时接收元素: {}", s);
            });
            
        result.append("   - 调用subscribe()后，操作链开始执行\n");
        result.append(subscriptionResult.toString());

        return Mono.just(result.toString());
    }

    /**
     * 演示在装配时进行优化
     * 通过检查流的类型来一个接一个的替换操作符，对流的链路进行优化
     */
    @GetMapping("/assembly-optimization")
    public Mono<String> demonstrateAssemblyOptimization() {
        StringBuilder result = new StringBuilder();

        result.append("=== 装配时优化演示 ===\n\n");

        // 不优化的流
        result.append("1. 未优化的流:\n");
        Flux<Integer> unoptimizedFlux = Flux.range(1, 10)
                .filter(i -> i > 5)
                .map(i -> i * 2)
                .filter(i -> i < 15);
        
        result.append("   - 包含两个filter和一个map操作\n");
        result.append("   - 每个元素都会经过所有操作符\n\n");

        // 手动优化 - 在装配时合并操作
        result.append("2. 装配时优化后的流:\n");
        Flux<Integer> optimizedFlux = Flux.range(1, 10)
                .filter(i -> i > 5 && i * 2 < 15)  // 合并条件
                .map(i -> i * 2);
        
        result.append("   - 在装配时优化：合并filter条件\n");
        result.append("   - 减少了操作符的数量，提高执行效率\n\n");
        
        result.append("3. 优化后执行结果:\n");
        StringBuilder executionResult = new StringBuilder();
        optimizedFlux.doOnNext(i -> executionResult.append("输出: ").append(i).append("\n"))
                    .subscribe();
        
        result.append(executionResult.toString());
        
        return Mono.just(result.toString());
    }

    /**
     * 演示在装配时添加监控和Hooks
     * 在组装过程中为流提供一些Hooks，并启用一些额外的日志记录、跟踪、度量收集
     */
    @GetMapping("/assembly-monitoring")
    public Mono<String> demonstrateAssemblyMonitoring() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== 装配时监控演示 ===\n\n");
        
        result.append("1. 添加Hooks和日志记录:\n");
        
        // 在装配时添加监控
        Flux<String> monitoredFlux = Flux.just("task1", "task2", "task3")
                .doOnSubscribe(subscription -> 
                    logger.info("监控: 流开始订阅"))
                .doOnNext(item -> 
                    logger.info("监控: 处理元素 {}", item))
                .doOnComplete(() -> 
                    logger.info("监控: 流处理完成"))
                .doOnError(error -> 
                    logger.error("监控: 流处理出错", error));
        
        result.append("   - 在装配时添加了各种监控钩子\n");
        result.append("   - doOnSubscribe: 订阅时触发\n");
        result.append("   - doOnNext: 每个元素处理时触发\n");
        result.append("   - doOnComplete: 完成时触发\n");
        result.append("   - doOnError: 出错时触发\n\n");
        
        result.append("2. 执行结果:\n");
        monitoredFlux.subscribe();
        result.append("   - 查看日志输出以观察监控效果\n\n");
        
        // 使用Context进行跟踪
        result.append("3. 使用Context进行跟踪:\n");
        Flux<String> contextFlux = Flux.just("Hello", "World")
                .flatMap(s -> Mono.deferContextual(ctx -> {
                    String userId = ctx.get("userId");
                    logger.info("跟踪: 用户 {} 处理消息 {}", userId, s);
                    return Mono.just(s + "[" + userId + "]");
                }));
                
        result.append("   - 使用Context传递跟踪信息\n");
        result.append("   - 在操作符中访问上下文信息\n\n");
        
        result.append("4. 执行带Context的流:\n");
        StringBuilder contextResult = new StringBuilder();
        contextFlux
            .doOnNext(s -> contextResult.append("处理结果: ").append(s).append("\n"))
            .contextWrite(Context.of("userId", "user123"))
            .subscribe();
            
        result.append(contextResult.toString());
        
        return Mono.just(result.toString());
    }

    /**
     * 综合演示
     */
    @GetMapping("/assembly-comprehensive")
    public Mono<String> comprehensiveDemo() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== 综合演示：装配时优化和监控 ===\n\n");
        
        // 创建一个模拟的业务流，在装配时进行优化和监控
        Flux<String> businessFlow = Flux.interval(Duration.ofMillis(100))
                .take(5)
                .map(String::valueOf)
                .doOnSubscribe(sub -> logger.info("业务流开始"))
                .doOnNext(item -> logger.info("处理业务数据: {}", item))
                .doOnComplete(() -> logger.info("业务流完成"));
        
        result.append("1. 创建了一个模拟的业务数据流\n");
        result.append("2. 在装配时添加了监控钩子\n");
        result.append("3. 流将自动执行...\n\n");
        
        // 收集执行结果
        StringBuilder executionResult = new StringBuilder();
        businessFlow
            .doOnNext(s -> executionResult.append("业务数据: ").append(s).append("\n"))
            .subscribe();
        
        try {
            // 等待流执行完成
            Thread.sleep(600);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        result.append("4. 执行结果:\n");
        result.append(executionResult.toString());
        result.append("\n5. 检查日志以查看监控输出\n");
        
        return Mono.just(result.toString());
    }
}