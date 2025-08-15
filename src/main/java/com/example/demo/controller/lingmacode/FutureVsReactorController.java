package com.example.demo.controller.lingmacode;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/comparison")
public class FutureVsReactorController {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 传统 CompletableFuture 方式实现异步处理 - 阻塞版本
     * 模拟获取用户订单信息的场景
     */
    @GetMapping("/future/orders")
    public String getOrdersWithFuture() throws Exception {
        long startTime = System.currentTimeMillis();
        
        // 模拟获取用户信息
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // 模拟数据库查询耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "User: John";
        }, executor);
        
        // 模拟获取订单列表
        CompletableFuture<String> ordersFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500); // 模拟数据库查询耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Orders: [Order1, Order2, Order3]";
        }, executor);
        
        // 组合结果
        CompletableFuture<String> resultFuture = userFuture
                .thenCombine(ordersFuture, (user, orders) -> {
                    long endTime = System.currentTimeMillis();
                    return user + ", " + orders + " (Time taken: " + (endTime - startTime) + "ms)";
                });
        
        return resultFuture.get(); // 阻塞等待结果
    }

    /**
     * 传统 CompletableFuture 方式实现异步处理 - 非阻塞版本
     * 直接返回 CompletableFuture，由 Spring 自动处理
     */
    @GetMapping("/future/orders-async")
    public CompletableFuture<String> getOrdersWithFutureAsync() {
        long startTime = System.currentTimeMillis();
        
        // 模拟获取用户信息
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // 模拟数据库查询耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "User: John";
        }, executor);
        
        // 模拟获取订单列表
        CompletableFuture<String> ordersFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500); // 模拟数据库查询耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Orders: [Order1, Order2, Order3]";
        }, executor);
        
        // 组合结果并直接返回，无需阻塞
        return userFuture.thenCombine(ordersFuture, (user, orders) -> {
            long endTime = System.currentTimeMillis();
            return user + ", " + orders + " (Time taken: " + (endTime - startTime) + "ms)";
        });
    }

    /**
     * Reactor 方式实现异步处理
     * 同样的获取用户订单信息场景
     */
    @GetMapping("/reactor/orders")
    public Mono<String> getOrdersWithReactor() {
        long startTime = System.currentTimeMillis();
        
        // 模拟获取用户信息
        Mono<String> userMono = Mono.fromCallable(() -> {
            try {
                Thread.sleep(1000); // 模拟数据库查询耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "User: John";
        }).subscribeOn(Schedulers.boundedElastic());
        
        // 模拟获取订单列表
        Mono<String> ordersMono = Mono.fromCallable(() -> {
            try {
                Thread.sleep(1500); // 模拟数据库查询耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Orders: [Order1, Order2, Order3]";
        }).subscribeOn(Schedulers.boundedElastic());
        
        // 组合结果
        return Mono.zip(userMono, ordersMono)
                .map(tuple -> {
                    long endTime = System.currentTimeMillis();
                    return tuple.getT1() + ", " + tuple.getT2() + " (Time taken: " + (endTime - startTime) + "ms)";
                });
    }

    /**
     * 传统 Future 方式处理数据流 - 阻塞版本
     * 模拟实时数据处理场景
     */
    @GetMapping("/future/stream")
    public String getStreamWithFuture() throws Exception {
        StringBuilder result = new StringBuilder();
        
        // 模拟处理5个数据项
        for (int i = 0; i < 5; i++) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000); // 模拟处理耗时
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Data item: " + LocalDateTime.now();
            }, executor);
            
            // 阻塞等待每个结果
            result.append(future.get()).append("\n");
        }
        
        return result.toString();
    }

    /**
     * 传统 Future 方式处理数据流 - 非阻塞版本
     * 使用 CompletableFuture.allOf 等待所有任务完成
     */
    @GetMapping("/future/stream-async")
    public CompletableFuture<String> getStreamWithFutureAsync() {
        // 创建5个异步任务
        CompletableFuture<String>[] futures = new CompletableFuture[5];
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(1000); // 模拟处理耗时
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Data item " + index + ": " + LocalDateTime.now();
            }, executor);
        }
        
        // 等待所有任务完成并组合结果
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    StringBuilder result = new StringBuilder();
                    for (CompletableFuture<String> future : futures) {
                        try {
                            result.append(future.get()).append("\n");
                        } catch (Exception e) {
                            // 处理异常
                            result.append("Error: ").append(e.getMessage()).append("\n");
                        }
                    }
                    return result.toString();
                });
    }

    /**
     * Reactor 方式处理数据流
     * 同样的实时数据处理场景，但以非阻塞流的方式
     */
    @GetMapping(value = "/reactor/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getStreamWithReactor() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .map(i -> "Data item: " + LocalDateTime.now())
                .subscribeOn(Schedulers.parallel());
    }

    /**
     * 传统 Callback 方式处理异步操作 - 阻塞版本
     * 模拟异步操作回调处理
     */
    @GetMapping("/callback/example")
    public String getCallbackExample() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // 模拟异步操作
        executor.submit(() -> {
            try {
                Thread.sleep(2000); // 模拟耗时操作
                future.complete("Callback result: Operation completed at " + LocalDateTime.now());
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        });
        
        return future.get(); // 阻塞等待结果
    }

    /**
     * 传统 Callback 方式处理异步操作 - 非阻塞版本
     * 直接返回 CompletableFuture
     */
    @GetMapping("/callback/example-async")
    public CompletableFuture<String> getCallbackExampleAsync() {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // 模拟异步操作
        executor.submit(() -> {
            try {
                Thread.sleep(2000); // 模拟耗时操作
                future.complete("Callback result: Operation completed at " + LocalDateTime.now());
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        });
        
        // 直接返回 CompletableFuture，无需阻塞
        return future;
    }

    /**
     * Reactor 方式处理异步操作
     * 同样的异步操作处理
     */
    @GetMapping("/reactor/callback")
    public Mono<String> getReactorCallback() {
        return Mono.fromCallable(() -> {
            try {
                Thread.sleep(2000); // 模拟耗时操作
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Reactor result: Operation completed at " + LocalDateTime.now();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}