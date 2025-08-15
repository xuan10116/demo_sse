package com.example.demo.controller.lingmacode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class SchedulerDemoController {

    @GetMapping("/scheduler-demo")
    public Mono<String> schedulerDemo() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== subscribeOn 和 publishOn 实际应用场景 ===\n\n");
        
        // 场景1: 将阻塞操作移出主线程
        result.append("场景1: 将阻塞操作移出主线程\n");
        result.append("使用 subscribeOn(Schedulers.boundedElastic())\n");
        
        // 模拟阻塞操作
        Mono<String> blockingOperation = Mono.fromCallable(() -> {
            System.out.println("阻塞操作执行在线程: " + Thread.currentThread().getName());
            Thread.sleep(100); // 模拟阻塞
            return "阻塞操作完成";
        })
        .subscribeOn(Schedulers.boundedElastic()); // 将阻塞操作移到专用线程池
        
        // 场景2: 不同阶段使用不同线程池
        result.append("\n场景2: 不同处理阶段使用不同线程池\n");
        
        Flux<String> multiStageProcessing = Flux.range(1, 5)
            // 数据获取阶段 - 使用I/O优化线程池
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(i -> System.out.println("数据获取阶段: " + i + " 在线程 " + Thread.currentThread().getName()))
            
            // 数据处理阶段 - 切换到CPU优化线程池
            .publishOn(Schedulers.parallel())
            .map(i -> {
                System.out.println("数据处理阶段: " + i + " 在线程 " + Thread.currentThread().getName());
                // 模拟CPU密集型操作
                return "处理后的数据-" + i;
            })
            
            // 结果保存阶段 - 切换回I/O优化线程池
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(data -> System.out.println("结果保存阶段: " + data + " 在线程 " + Thread.currentThread().getName()));
        
        // 执行流
        blockingOperation.subscribe();
        multiStageProcessing.subscribe();
        
        result.append("查看控制台输出了解线程切换情况\n");
        
        return Mono.just(result.toString());
    }

    @GetMapping("/thread-info")
    public Mono<String> threadInfoDemo() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== 线程信息演示 ===\n\n");
        
        Flux.range(1, 3)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(i -> result.append("1. 数据源 [" + i + "] 在线程: " + Thread.currentThread().getName() + "\n"))
            
            .publishOn(Schedulers.parallel())
            .doOnNext(i -> result.append("2. 经过publishOn(Schedulers.parallel())后 [" + i + "] 在线程: " + Thread.currentThread().getName() + "\n"))
            
            .publishOn(Schedulers.single())
            .doOnNext(i -> result.append("3. 经过publishOn(Schedulers.single())后 [" + i + "] 在线程: " + Thread.currentThread().getName() + "\n"))
            
            .subscribe();
            
        try {
            Thread.sleep(100); // 等待异步操作完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return Mono.just(result.toString());
    }
}