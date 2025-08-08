package com.example.demo;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

public class ReactorTestingExamples {

    /**
     * 示例1: 测试Mono
     */
    @Test
    public void testMono() {
        Mono<String> mono = Mono.just("Hello World");
        
        StepVerifier.create(mono)
                .expectNext("Hello World")
                .verifyComplete();
    }

    /**
     * 示例2: 测试Flux
     */
    @Test
    public void testFlux() {
        Flux<String> flux = Flux.just("A", "B", "C");
        
        StepVerifier.create(flux)
                .expectNext("A")
                .expectNext("B")
                .expectNext("C")
                .verifyComplete();
    }

    /**
     * 示例3: 测试带有错误的流
     */
    @Test
    public void testError() {
        Flux<String> flux = Flux.just("A", "B")
                .concatWith(Mono.error(new RuntimeException("模拟错误")));
        
        StepVerifier.create(flux)
                .expectNext("A")
                .expectNext("B")
                .expectError(RuntimeException.class)
                .verify();
    }

    /**
     * 示例4: 测试时间相关操作
     */
    @Test
    public void testTimeBasedSequence() {
        Flux<String> flux = Flux.interval(Duration.ofMillis(100))
                .map(i -> "数据 " + i)
                .take(3);
        
        StepVerifier.create(flux)
                .expectNext("数据 0")
                .expectNext("数据 1")
                .expectNext("数据 2")
                .expectComplete()
                .verify(Duration.ofSeconds(1));
    }

    /**
     * 示例5: 测试背压行为
     */
    @Test
    public void testBackpressure() {
        Flux<Integer> flux = Flux.range(1, 1000);
        
        StepVerifier.create(flux, 10) // 请求10个元素
                .expectNextCount(10)
                .expectComplete()
                .verify();
    }

    /**
     * 示例6: 测试转换操作符
     */
    @Test
    public void testTransformation() {
        Flux<String> flux = Flux.just("a", "b", "c")
                .map(String::toUpperCase);
        
        StepVerifier.create(flux)
                .expectNext("A", "B", "C")
                .verifyComplete();
    }
}