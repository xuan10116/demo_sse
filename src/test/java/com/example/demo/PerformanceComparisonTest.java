package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class PerformanceComparisonTest {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080")
            .build();

    /**
     * 测试阻塞式实现的性能
     */
    @Test
    public void testBlockingPerformance() throws InterruptedException {
        System.out.println("开始测试阻塞式实现性能...");
        
        // 模拟20个并发用户
        int concurrentUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        Instant start = Instant.now();
        
        // 每个用户发送5个请求
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 5; j++) {
                    try {
                        List<String> result = webClient.get()
                                .uri("/performance/blocking")
                                .retrieve()
                                .bodyToFlux(String.class)
                                .collectList()
                                .block(Duration.ofSeconds(30));
                        
                        System.out.println("用户 " + userId + " 第 " + j + " 次请求完成，数据量: " + result.size());
                    } catch (Exception e) {
                        System.err.println("用户 " + userId + " 请求失败: " + e.getMessage());
                    }
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        
        System.out.println("阻塞式实现测试完成，总耗时: " + duration + " 毫秒");
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 测试响应式实现的性能
     */
    @Test
    public void testReactivePerformance() throws InterruptedException {
        System.out.println("开始测试响应式实现性能...");
        
        // 模拟20个并发用户
        int concurrentUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        Instant start = Instant.now();
        
        // 每个用户发送5个请求
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 5; j++) {
                    try {
                        List<String> result = webClient.get()
                                .uri("/performance/reactive")
                                .retrieve()
                                .bodyToMono(List.class)
                                .block(Duration.ofSeconds(30));
                        
                        System.out.println("用户 " + userId + " 第 " + j + " 次请求完成，数据量: " + result.size());
                    } catch (Exception e) {
                        System.err.println("用户 " + userId + " 请求失败: " + e.getMessage());
                    }
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        
        System.out.println("响应式实现测试完成，总耗时: " + duration + " 毫秒");
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 测试阻塞式流式实现的性能
     */
    @Test
    public void testBlockingStreamPerformance() throws InterruptedException {
        System.out.println("开始测试阻塞式流式实现性能...");
        
        // 模拟10个并发用户
        int concurrentUsers = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        Instant start = Instant.now();
        
        // 每个用户发送3个请求
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 3; j++) {
                    try {
                        List<String> result = webClient.get()
                                .uri("/performance/blocking-stream")
                                .retrieve()
                                .bodyToFlux(String.class)
                                .collectList()
                                .block(Duration.ofSeconds(30));
                        
                        System.out.println("用户 " + userId + " 第 " + j + " 次请求完成，数据量: " + result.size());
                    } catch (Exception e) {
                        System.err.println("用户 " + userId + " 请求失败: " + e.getMessage());
                    }
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        
        System.out.println("阻塞式流式实现测试完成，总耗时: " + duration + " 毫秒");
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * 测试响应式流式实现的性能
     */
    @Test
    public void testReactiveStreamPerformance() throws InterruptedException {
        System.out.println("开始测试响应式流式实现性能...");
        
        // 模拟10个并发用户
        int concurrentUsers = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        Instant start = Instant.now();
        
        // 每个用户发送3个请求
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 3; j++) {
                    try {
                        List<String> result = webClient.get()
                                .uri("/performance/reactive-stream")
                                .accept(MediaType.TEXT_EVENT_STREAM)
                                .retrieve()
                                .bodyToFlux(String.class)
                                .collectList()
                                .block(Duration.ofSeconds(30));
                        
                        System.out.println("用户 " + userId + " 第 " + j + " 次请求完成，数据量: " + result.size());
                    } catch (Exception e) {
                        System.err.println("用户 " + userId + " 请求失败: " + e.getMessage());
                    }
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        
        System.out.println("响应式流式实现测试完成，总耗时: " + duration + " 毫秒");
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}