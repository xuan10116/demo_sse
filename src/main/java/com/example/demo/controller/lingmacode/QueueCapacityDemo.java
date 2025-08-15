package com.example.demo.controller.lingmacode;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 演示线程池队列容量的工作原理
 * 展示队列是动态增长的，而不是一开始就分配最大容量
 */
public class QueueCapacityDemo {
    
    private static final Logger logger = Logger.getLogger(QueueCapacityDemo.class.getName());
    
    public static void main(String[] args) throws InterruptedException {
        logger.info("开始演示队列容量工作原理");
        
        // 创建一个计数器来跟踪任务执行情况
        AtomicInteger submittedTasks = new AtomicInteger(0);
        AtomicInteger executedTasks = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        // 使用boundedElastic调度器，其默认队列容量为100,000
        logger.info("使用boundedElastic调度器，其默认最大队列容量为100,000");
        
        // 提交大量快速任务来填充队列
        logger.info("提交任务到调度器...");
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            Schedulers.boundedElastic().schedule(() -> {
                submittedTasks.incrementAndGet();
                // 模拟非常快速的任务
                try {
                    Thread.sleep(10); // 极短的执行时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                int executed = executedTasks.incrementAndGet();
                if (executed % 100 == 0) {
                    logger.info("已执行任务数: " + executed);
                }
                if (executed >= 1000) {
                    latch.countDown();
                }
            });
        }
        
        logger.info("已提交任务数: " + submittedTasks.get());
        logger.info("等待任务执行完成...");
        
        // 等待所有任务执行完成
        if (latch.await(30, java.util.concurrent.TimeUnit.SECONDS)) {
            logger.info("所有任务执行完成");
        } else {
            logger.warning("任务执行超时");
        }
        
        logger.info("最终统计 - 提交任务: " + submittedTasks.get() + ", 执行任务: " + executedTasks.get());
        logger.info("演示完成");
        
        // 演示队列溢出情况
        demonstrateQueueOverflow();
    }
    
    /**
     * 演示队列溢出的情况
     */
    private static void demonstrateQueueOverflow() throws InterruptedException {
        logger.info("\n开始演示队列溢出情况");
        
        AtomicInteger successfulTasks = new AtomicInteger(0);
        AtomicInteger rejectedTasks = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        // 创建一个会阻塞的工作线程，使得队列快速填满
        Flux.range(1, 150000) // 超过默认队列容量的任务数
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(i -> {
                successfulTasks.incrementAndGet();
                // 模拟阻塞操作，让线程无法快速处理任务
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                if (i % 10000 == 0) {
                    logger.info("成功处理任务数: " + i);
                }
            })
            .doOnError(error -> {
                rejectedTasks.incrementAndGet();
                logger.warning("任务被拒绝: " + error.getMessage());
            })
            .doOnComplete(() -> latch.countDown())
            .subscribe();
        
        // 等待一段时间观察效果
        Thread.sleep(5000);
        
        logger.info("队列溢出演示结果 - 成功任务: " + successfulTasks.get() + 
                   ", 拒绝任务: " + rejectedTasks.get());
        logger.info("注意：由于背压处理，实际可能不会出现RejectedExecutionException");
    }
}