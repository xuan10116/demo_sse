package com.example.demo.controller;

import org.springframework.http.MediaType;
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
        // 创建一个简单的Flux，每隔1秒发送一个数字
        return Flux.interval(Duration.ofSeconds(1))
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
     * 示例3: 错误处理
     * 展示Reactor中的错误处理机制
     */
    @GetMapping(value = "/error-handling", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
     * 展示zip、merge等合并操作符
     */
    @GetMapping(value = "/combining", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> combiningOperators() {
        // 创建两个不同的Flux
        Flux<String> flux1 = Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Flux1: " + i)
                .take(5);
                
        Flux<String> flux2 = Flux.interval(Duration.ofMillis(500))
                .map(i -> "Flux2: " + i)
                .take(10);
                
        // 合并两个Flux
        return Flux.merge(flux1, flux2)
                .map(data -> data + " at " + LocalDateTime.now());
    }

    /**
     * 示例6: 缓存和批处理
     * 展示buffer和window操作符
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
     * 示例7: 条件操作符
     * 展示takeUntil、skipWhile等条件操作符
     */
    @GetMapping(value = "/conditional", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> conditional() {
        Random random = new Random();
        
        return Flux.interval(Duration.ofMillis(500))
                .map(i -> random.nextInt(100)) // 生成0-100的随机数
                .takeUntil(i -> i > 90) // 当遇到大于90的数时停止
                .map(i -> "随机数: " + i);
    }

    /**
     * 示例8: 转换操作符
     * 展示flatMap、concatMap等转换操作符
     */
    @GetMapping(value = "/transforming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> transforming() {
        return Flux.just("a", "b", "c")
                .concatMap(s -> {
                    // concatMap保证顺序
                    return Flux.interval(Duration.ofMillis(300))
                            .take(3)
                            .map(i -> s + "-" + i);
                });
    }

    /**
     * 示例9: 实际应用场景 - 模拟聊天消息流
     * 结合现有SSE功能，展示实际应用
     */
    @GetMapping(value = "/chat-example", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatExample() {
        List<String> users = Arrays.asList("Alice", "Bob", "Charlie");
        List<String> messages = Arrays.asList("Hello!", "How are you?", "Fine, thanks!", 
                                            "What's up?", "Not much", "Bye!");
        
        return Flux.interval(Duration.ofSeconds(1))
                .zipWith(Flux.fromIterable(users).repeat())
                .zipWith(Flux.fromIterable(messages).repeat())
                .map(tuple -> {
                    Tuple2<Tuple2<Long, String>, String> tuple2 = (Tuple2<Tuple2<Long, String>, String>) tuple;
                    return String.format("[%s] %s: %s", 
                                       LocalDateTime.now().toString(), 
                                       tuple2.getT1().getT2(), 
                                       tuple2.getT2());
                })
                .take(10);
    }
}