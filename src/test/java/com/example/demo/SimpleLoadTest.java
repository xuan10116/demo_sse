package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleLoadTest {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8080")
            .build();

    public static void main(String[] args) throws InterruptedException {
        SimpleLoadTest test = new SimpleLoadTest();
        
        System.out.println("确保应用程序正在运行在端口8080上...");
        Thread.sleep(5000); // 等待用户启动应用程序
        
        // 执行各种性能测试
        test.runBlockingTest();
        test.runReactiveTest();
        test.runBlockingStreamTest();
        test.runReactiveStreamTest();
    }

    public void runBlockingTest() throws InterruptedException {
        System.out.println("\n=== 阻塞式实现性能测试 ===");
        performLoadTest("/performance/blocking", false, 20, 5);
    }

    public void runReactiveTest() throws InterruptedException {
        System.out.println("\n=== 响应式实现性能测试 ===");
        performLoadTest("/performance/reactive", false, 20, 5);
    }

    public void runBlockingStreamTest() throws InterruptedException {
        System.out.println("\n=== 阻塞式流式实现性能测试 ===");
        performLoadTest("/performance/blocking-stream", false, 10, 3);
    }

    public void runReactiveStreamTest() throws InterruptedException {
        System.out.println("\n=== 响应式流式实现性能测试 ===");
        performLoadTest("/performance/reactive-stream", true, 10, 3);
    }

    private void performLoadTest(String uri, boolean isStream, int concurrentUsers, int requestsPerUser) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        Instant start = Instant.now();
        
        // 每个用户发送指定数量的请求
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerUser; j++) {
                    try {
                        if (isStream) {
                            // 流式响应处理
                            List<String> result = webClient.get()
                                    .uri(uri)
                                    .accept(MediaType.TEXT_EVENT_STREAM)
                                    .retrieve()
                                    .bodyToFlux(String.class)
                                    .collectList()
                                    .block(Duration.ofSeconds(30));
                            
                            System.out.println("用户 " + userId + " 第 " + (j+1) + " 次请求完成，接收数据项数量: " + result.size());
                        } else {
                            // 普通响应处理
                            Object result = webClient.get()
                                    .uri(uri)
                                    .retrieve()
                                    .bodyToMono(Object.class)
                                    .block(Duration.ofSeconds(30));
                            
                            System.out.println("用户 " + userId + " 第 " + (j+1) + " 次请求完成");
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        System.err.println("用户 " + userId + " 第 " + (j+1) + " 次请求失败: " + e.getMessage());
                        errorCount.incrementAndGet();
                    }
                }
            }, executor);
            
            futures.add(future);
        }
        
        // 等待所有请求完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        
        System.out.println("测试结果:");
        System.out.println("  总请求数: " + (concurrentUsers * requestsPerUser));
        System.out.println("  成功请求数: " + successCount.get());
        System.out.println("  失败请求数: " + errorCount.get());
        System.out.println("  总耗时: " + duration + " 毫秒");
        System.out.println("  平均响应时间: " + (duration / (double) successCount.get()) + " 毫秒/请求");
        System.out.println("  吞吐量: " + (successCount.get() / (duration / 1000.0)) + " 请求/秒");
        
        executor.shutdown();
    }
}