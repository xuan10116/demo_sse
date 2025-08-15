package com.example.demo.controller.lingmacode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
public class AssemblyChainController {

    private static final Logger logger = LoggerFactory.getLogger(AssemblyChainController.class);

    /**
     * 演示组装好的调用链及订阅后订阅链的传播
     */
    @GetMapping("/chain-propagation")
    public Mono<String> demonstrateChainPropagation() {
        StringBuilder result = new StringBuilder();

        result.append("=== 组装好的调用链及订阅链传播演示 ===\n\n");

        // 1. 组装阶段 - 创建操作符链
        result.append("1. 组装阶段 (Assembly Time):\n");
        result.append("   创建Flux操作符链...\n");

        Flux<String> fluxChain = Flux.just("A", "B", "C", "D", "E")
                .doOnNext(item -> logger.info("1. 数据源: {}", item))
                .publishOn(Schedulers.boundedElastic())
                .map(String::toUpperCase)
                .doOnNext(item -> logger.info("2. map操作后: {}", item))
                .filter(s -> !s.equals("C"))
                .doOnNext(item -> logger.info("3. filter操作后: {}", item))
                .map(s -> "Processed: " + s)
                .doOnNext(item -> logger.info("4. 第二个map操作后: {}", item));

        result.append("   操作符链已组装完成:\n");
        result.append("   Flux.just() -> doOnNext -> publishOn -> map -> doOnNext -> filter -> doOnNext -> map -> doOnNext\n\n");

        // 2. 订阅阶段 - 订阅链的传播
        result.append("2. 订阅阶段 (Subscription Time):\n");
        result.append("   调用subscribe()方法，订阅链开始传播...\n\n");

        StringBuilder subscriptionResult = new StringBuilder();
        fluxChain
            .doOnSubscribe(sub -> {
                logger.info("5. 订阅开始");
                subscriptionResult.append("   - 订阅事件向上传播至源头\n");
            })
            .doOnComplete(() -> {
                logger.info("6. 流处理完成");
                subscriptionResult.append("   - 完成事件向下传播至订阅者\n");
            })
            .subscribe(
                data -> {
                    logger.info("7. 订阅者接收到数据: {}", data);
                    subscriptionResult.append("   - 接收到数据: ").append(data).append("\n");
                },
                error -> {
                    logger.error("8. 订阅者接收到错误", error);
                    subscriptionResult.append("   - 接收到错误: ").append(error.getMessage()).append("\n");
                },
                () -> {
                    logger.info("9. 订阅者接收到完成信号");
                    subscriptionResult.append("   - 接收到完成信号\n");
                }
            );

        result.append(subscriptionResult.toString());
        result.append("\n3. 查看日志以观察订阅链传播过程中的线程信息\n");

        return Mono.just(result.toString());
    }

    /**
     * 演示订阅链传播过程中的信号流动
     */
    @GetMapping("/signal-propagation")
    public Mono<String> demonstrateSignalPropagation() {
        StringBuilder result = new StringBuilder();

        result.append("=== 订阅链中信号传播演示 ===\n\n");

        result.append("1. 信号类型:\n");
        result.append("   - onSubscribe: 订阅信号，从下游向上游传播\n");
        result.append("   - onNext: 数据信号，从上游向下游传播\n");
        result.append("   - onComplete: 完成信号，从上游向下游传播\n");
        result.append("   - onError: 错误信号，从上游向下游传播\n\n");

        // 组装阶段
        result.append("2. 组装操作符链:\n");
        Flux<Integer> flux = Flux.range(1, 3)
                .doOnSubscribe(s -> logger.info("A. [{}] onSubscribe信号", Thread.currentThread().getName()))
                .doOnNext(i -> logger.info("B. [{}] onNext信号: {}", Thread.currentThread().getName(), i))
                .map(i -> i * 2)
                .doOnNext(i -> logger.info("C. [{}] onNext信号(经过map): {}", Thread.currentThread().getName(), i))
                .filter(i -> i > 2)
                .doOnNext(i -> logger.info("D. [{}] onNext信号(经过filter): {}", Thread.currentThread().getName(), i))
                .doOnComplete(() -> logger.info("E. [{}] onComplete信号", Thread.currentThread().getName()))
                .doOnError(e -> logger.error("F. [{}] onError信号", Thread.currentThread().getName(), e));

        result.append("   Flux.range(1, 3) -> doOnSubscribe -> doOnNext -> map -> doOnNext -> filter -> doOnNext -> doOnComplete -> doOnError\n\n");

        // 订阅阶段
        result.append("3. 订阅并观察信号传播:\n");
        flux.subscribe(
            data -> logger.info("G. [{}] 订阅者接收数据: {}", Thread.currentThread().getName(), data),
            error -> logger.error("H. [{}] 订阅者接收错误", Thread.currentThread().getName(), error),
            () -> logger.info("I. [{}] 订阅者接收完成信号", Thread.currentThread().getName())
        );

        result.append("   - 查看日志观察信号传播过程和线程切换\n\n");

        result.append("4. 信号传播方向:\n");
        result.append("   onSubscribe: 订阅者 -> doOnError -> doOnComplete -> doOnNext(filter) -> doOnNext(map) -> doOnNext -> doOnSubscribe -> 数据源\n");
        result.append("   onNext/onComplete/onError: 数据源 -> doOnSubscribe -> doOnNext -> map -> doOnNext(map) -> filter -> doOnNext(filter) -> 订阅者\n");

        return Mono.just(result.toString());
    }

    /**
     * 演示复杂的操作符链和订阅传播
     */
    @GetMapping("/complex-chain")
    public Mono<String> demonstrateComplexChain() {
        StringBuilder result = new StringBuilder();

        result.append("=== 复杂操作符链和订阅传播演示 ===\n\n");

        result.append("1. 构建复杂操作符链:\n");

        Flux<String> complexChain = Flux.interval(Duration.ofMillis(200))
                .take(3)
                .doOnSubscribe(s -> logger.info("1. [{}] onSubscribe - interval", Thread.currentThread().getName()))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(i -> logger.info("2. [{}] onNext - publishOn之后: {}", Thread.currentThread().getName(), i))
                .map(Object::toString)
                .subscribeOn(Schedulers.parallel())
                .doOnNext(s -> logger.info("3. [{}] onNext - subscribeOn之后: {}", Thread.currentThread().getName(), s))
                .map(s -> "Item: " + s)
                .doOnNext(s -> logger.info("4. [{}] onNext - map之后: {}", Thread.currentThread().getName(), s));

        result.append("   Flux.interval -> take -> doOnSubscribe -> publishOn -> doOnNext -> map -> subscribeOn -> doOnNext -> map -> doOnNext\n\n");

        result.append("2. 订阅并观察传播:\n");
        StringBuilder output = new StringBuilder();
        
        complexChain.subscribe(
            data -> {
                logger.info("5. [{}] 订阅者接收数据: {}", Thread.currentThread().getName(), data);
                output.append("接收到: ").append(data).append("\n");
            },
            error -> {
                logger.error("6. [{}] 订阅者接收错误", Thread.currentThread().getName(), error);
                output.append("错误: ").append(error.getMessage()).append("\n");
            },
            () -> {
                logger.info("7. [{}] 订阅者接收完成信号", Thread.currentThread().getName());
                output.append("完成\n");
            }
        );

        try {
            // 等待流执行完成
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        result.append(output.toString());
        result.append("\n3. 查看日志观察复杂的订阅传播和线程切换\n");

        return Mono.just(result.toString());
    }
}