package com.example.demo.controller.lingmacode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/completable-future")
public class CompletableFutureController {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 传统 Future 的局限性示例
     * 1. 阻塞式获取结果
     * 2. 不支持函数式编程和链式操作
     * 3. 不支持组合多个 Future
     * 4. 不支持异常处理
     */
    @GetMapping("/traditional-future")
    public String traditionalFuture() throws ExecutionException, InterruptedException {
        // 传统 Future 只能阻塞等待结果
        java.util.concurrent.Future<String> future = executor.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Result from traditional Future";
        });

        // 必须阻塞等待结果
        return future.get(); // 这里会阻塞直到结果返回
    }

    /**
     * CompletableFuture 的改进 - 非阻塞回调
     * 1. 支持异步回调，无需阻塞等待
     * 2. 支持链式操作
     */
    @GetMapping("/non-blocking")
    public CompletableFuture<String> nonBlocking() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Result from CompletableFuture";
        }, executor).thenApply(result -> result + " - processed at " + System.currentTimeMillis());
        // 注意：这里没有阻塞，直接返回 CompletableFuture
    }

    /**
     * CompletableFuture 的改进 - 链式操作
     * 可以方便地对结果进行一系列转换
     */
    @GetMapping("/chaining")
    public CompletableFuture<String> chaining() {
        return CompletableFuture.supplyAsync(() -> {
            // 第一步：获取用户ID
            return "user123";
        }, executor)
        .thenApply(userId -> {
            // 第二步：根据用户ID获取订单信息
            return "Orders for " + userId + ": [Order1, Order2]";
        })
        .thenApply(orders -> {
            // 第三步：处理订单信息
            return orders + " - processed at " + System.currentTimeMillis();
        });
    }

    /**
     * CompletableFuture 的改进 - 组合多个异步操作
     * 可以方便地组合多个独立的异步操作
     */
    @GetMapping("/composition")
    public CompletableFuture<String> composition() {
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "User: John";
        }, executor);

        CompletableFuture<String> ordersFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Orders: [Order1, Order2, Order3]";
        }, executor);

        // 组合两个异步操作的结果
        return userFuture.thenCombine(ordersFuture, (user, orders) -> 
            user + ", " + orders + " at " + System.currentTimeMillis());
    }

    /**
     * CompletableFuture 的改进 - 异常处理
     * 提供了更好的异常处理机制
     */
    @GetMapping("/exception-handling")
    public CompletableFuture<String> exceptionHandling() {
        return CompletableFuture.supplyAsync(() -> {
            // 模拟异常情况
            if (Math.random() > 0.5) {
                throw new RuntimeException("Random error occurred");
            }
            return "Success result";
        }, executor)
        .handle((result, throwable) -> {
            if (throwable != null) {
                return "Handled exception: " + throwable.getMessage();
            }
            return result + " - processed successfully";
        });
    }

    /**
     * CompletableFuture 的改进 - 多个任务的并行处理
     * 可以等待多个任务完成
     */
    @GetMapping("/multiple-tasks")
    public CompletableFuture<String> multipleTasks() {
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Task 1 completed";
        }, executor);

        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Task 2 completed";
        }, executor);

        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Task 3 completed";
        }, executor);

        // 等待所有任务完成
        return CompletableFuture.allOf(task1, task2, task3)
                .thenApply(v -> {
                    try {
                        return task1.get() + ", " + task2.get() + ", " + task3.get();
                    } catch (Exception e) {
                        return "Error occurred: " + e.getMessage();
                    }
                });
    }

    /**
     * 与 Reactor 的对比示例
     * 展示 CompletableFuture 与 Reactor Mono 的区别
     */
    @GetMapping("/vs-reactor")
    public CompletableFuture<String> vsReactor() {
        // CompletableFuture 方式
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "CompletableFuture result";
        }, executor)
        .thenApply(result -> result + " processed at " + System.currentTimeMillis());
    }
}