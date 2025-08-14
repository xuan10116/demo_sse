package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/debug")
public class DebugCompletableFutureController {

    private static final Logger logger = LoggerFactory.getLogger(DebugCompletableFutureController.class);
    
    private final ExecutorService executor = Executors.newFixedThreadPool(5, r -> {
        Thread t = new Thread(r);
        t.setName("debug-completable-future-thread-" + t.getId());
        return t;
    });

    /**
     * 带有详细日志记录的CompletableFuture示例
     */
    @GetMapping("/completable-future")
    public CompletableFuture<String> debugCompletableFuture() {
        logger.info("开始处理请求，当前线程: {}", Thread.currentThread().getName());
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            logger.info("执行异步任务，当前线程: {}", Thread.currentThread().getName());
            try {
                Thread.sleep(2000); // 模拟耗时操作
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("异步任务完成，当前线程: {}", Thread.currentThread().getName());
            return "CompletableFuture result";
        }, executor)
        .thenApply(result -> {
            logger.info("处理结果，当前线程: {}", Thread.currentThread().getName());
            return result + " processed at " + System.currentTimeMillis();
        })
        .whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("任务执行出现异常", throwable);
            } else {
                logger.info("任务成功完成，结果: {}", result);
            }
        });
        
        logger.info("返回CompletableFuture，当前线程: {}", Thread.currentThread().getName());
        return future;
    }

    /**
     * 使用自定义执行器并添加更多调试信息
     */
    @GetMapping("/completable-future-detailed")
    public CompletableFuture<String> debugCompletableFutureDetailed() {
        logger.info("=== 开始处理请求 ===");
        logger.info("请求处理线程: {}", Thread.currentThread().getName());
        
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            String threadName = Thread.currentThread().getName();
            logger.info("任务1在线程 {} 中开始执行", threadName);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("任务1在线程 {} 中执行完成", threadName);
            return "Result from task 1";
        }, executor);
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            String threadName = Thread.currentThread().getName();
            logger.info("任务2在线程 {} 中开始执行", threadName);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("任务2在线程 {} 中执行完成", threadName);
            return "Result from task 2";
        }, executor);
        
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (result1, result2) -> {
            String threadName = Thread.currentThread().getName();
            logger.info("在线程 {} 中组合结果: {} 和 {}", threadName, result1, result2);
            return result1 + " + " + result2;
        });
        
        combinedFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("任务执行出现异常", throwable);
            } else {
                logger.info("=== 所有任务完成，最终结果: {} ===", result);
            }
        });
        
        logger.info("返回CompletableFuture给Spring处理");
        return combinedFuture;
    }
}