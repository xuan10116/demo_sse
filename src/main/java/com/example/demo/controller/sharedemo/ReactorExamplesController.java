package com.example.demo.controller.sharedemo;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/reactor")
public class ReactorExamplesController {

    /**
     * 示例1: 基础Flux创建和使用
     * 展示如何创建Flux以及基本操作符
     */
    @GetMapping(value = "/flux-basic", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> basicFlux() {
//        final Flux<String> just = Flux.just("Hello", "World");
//        final Flux<String> range = Flux.range(1, 5).map(i -> "数据 " + i + " at " + LocalDateTime.now());
        // 创建一个简单的Flux，每隔1秒发送一个数字
        return Flux.interval(Duration.ofSeconds(1))
                .log()
                .take(10) // 限制为10个元素
                .map(i -> "Flux item: " + i + " at " + LocalDateTime.now());
    }

    /**
     * 示例2: 基础Mono创建和使用
     * 展示Mono的使用方式
     */
    @GetMapping("/mono-basic")
    public Mono<String> basicMono() {
        // 创建一个Mono，包含单个值
        return Mono.just("Hello from Mono at " + LocalDateTime.now());
    }

    /**
     * 示例2: Mono的empty和never
     * 展示Mono的特殊状态
     */
    @GetMapping("/mono-special")
    public Mono<String> monoSpecial() {
        // 创建一个空的Mono，立即完成
        Mono<String> emptyMono = Mono.empty();
        
        // 创建一个永远不会发送数据也不会完成的Mono
        Mono<String> neverMono = Mono.never();
        
        // 创建一个立即完成的Mono
        Mono<String> justMono = Mono.just("立即完成的Mono at " + LocalDateTime.now());
        
        // 这里我们返回一个组合的结果
        return Mono.when(emptyMono, justMono)
                .then(Mono.just("多个Mono处理完成 at " + LocalDateTime.now()));
    }

    /**
     * 示例3: 错误处理
     * 展示Reactor中的错误处理机制
     */
    @GetMapping(value = "/errorResume-handling", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> errorHandling() {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(i -> {
                    if (i == 3) {
                        // 模拟错误情况
                        return Mono.error(new RuntimeException("模拟错误发生在: " + i));
                    }
                    return Mono.just("正常数据: " + i);
                })
                .onErrorResume(e -> {
                    // 错误恢复，返回替代值
                    return Mono.just("错误已处理: " + e.getMessage());
                })
                .log()
                .map(data -> data + " processed at " + LocalDateTime.now());
    }
    @GetMapping(value = "/errorContinue-handling", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> errorContinueHandling() {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(i -> {
                    if (i == 3) {
                        // 模拟错误情况
                        return Mono.error(new RuntimeException("模拟错误发生在: " + i));
                    }
                    return Mono.just("正常数据: " + i);
                })
                .log()
                .takeUntil(i -> i.length() > 7)
                .onErrorContinue(RuntimeException.class, (e, v) -> System.out.println("错误已处理: " + e.getMessage()))
                .map(data -> data + " processed at " + LocalDateTime.now());
    }

    /**
     * 示例4: 背压处理
     * 展示如何处理背压
     */
    @GetMapping(value = "/backpressure", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressure() {
        // 创建一个快速发射数据的Flux
        Flux<Long> fastEmitter = Flux.interval(Duration.ofMillis(100));
        
        // 使用onBackpressureBuffer处理背压
        return fastEmitter
                .onBackpressureBuffer(10, 
                    dropped -> System.out.println("丢弃的数据: " + dropped))
                .take(30)
                .map(i -> "处理后的数据: " + i);
    }

    /**
     * 示例5: 合并操作符
     * merge
     */
    @GetMapping(value = "/merge", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> mergeOperators() {
        Flux<String> flux1 = Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Flux1: " + i)
                .take(5);
//        Flux<String> flux2 = Flux.interval(Duration.ofSeconds(1))
//                .map(i -> "Flux2: " + i)
//                .take(5);
        Mono<String> mono1 = Mono.delay(Duration.ofMillis(500))
                .map(i -> "Mono1: " + i);
                
        // 合并两个Flux
        return Flux.merge(flux1, mono1)
                .map(data -> data + " at " + LocalDateTime.now());
    }

    /**
     * 示例5: 合并操作符
     * zip
     */
    @GetMapping(value = "/zip", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> zipOperators() {
        // 创建两个不同的Flux
        Flux<String> flux1 = Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Flux1: " + i)
                .take(5);

        Flux<String> flux2 = Flux.interval(Duration.ofMillis(500))
                .map(i -> "Flux2: " + i)
                .take(10);

        // 合并两个Flux
        return Flux.zip(flux1, flux2)
                .map(data -> data + " at " + LocalDateTime.now());
    }

    /**
     * 示例6: 缓存和批处理
     * Buffer示例 - 直接将元素分组为集合
     */
    @GetMapping(value = "/buffering", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> buffering() {
        List<String> data = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        
        return Flux.fromIterable(data)
                .delayElements(Duration.ofMillis(200)) // 每200ms发送一个元素
                .buffer(3) // 每3个元素组成一个List
                .map(list -> "批处理数据: " + list);
    }

    /**
     * 示例6: 缓存和批处理
     * window操作符示例 - 将元素分组为内部Flux流
     */
    @GetMapping(value = "/window-example", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> windowExample() {
        return Flux.interval(Duration.ofMillis(100))
                .take(20)
                .window(5) // 每5个元素分为一组窗口
                .flatMap(window -> 
                    window.reduce("", (acc, value) -> acc + " " + value)
                         .map(reduced -> "窗口数据: [" + reduced.trim() + "]")
                );
    }

    /**
     * 基于时间的window示例
     * 展示如何基于时间窗口处理数据
     */
    @GetMapping(value = "/window-time-based", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> timeBasedWindow() {
        return Flux.interval(Duration.ofMillis(100))
                .take(30)
                .window(Duration.ofSeconds(1)) // 每1秒创建一个新窗口
                .flatMap(window -> 
                    window.count()
                         .map(count -> "在1秒时间窗口内收到 " + count + " 个元素")
                );
    }

    /**
     * 示例7: 条件操作符
     * takeUntil
     */
    @GetMapping(value = "/takeUntil-conditional", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> takeConditional() {
        Random random = new Random();
        
        return Flux.interval(Duration.ofMillis(500))
                .map(i -> random.nextInt(100)) // 生成0-100的随机数
                .takeUntil(i -> i > 90) // 当遇到大于90的数时停止
                .map(i -> "随机数: " + i);
    }


    /**
     * 示例7: 条件操作符
     * skipWhile
     */
    @GetMapping(value = "/skip-conditional", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> skipConditional() {
        Random random = new Random();

        return Flux.interval(Duration.ofMillis(500))
                .log()
                .map(i -> random.nextInt(100)) // 生成0-100的随机数
                .skipWhile(i -> i < 50) // 当遇到大于50的数开始
                .takeUntil(i -> i > 90) // 当遇到大于90的数时停止
                .map(i -> "随机数: " + i);
    }
}