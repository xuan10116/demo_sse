package com.example.demo.controller.lingmacode;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
@RequestMapping("/schedulers")
public class SchedulerExamplesController {

    /**
     * 示例1: 默认执行线程
     * 展示默认情况下操作符在哪个线程执行
     */
    @GetMapping(value = "/default-thread", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> defaultThread() {
        return Flux.range(1, 5)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "数据 " + i + " 在线程 " + threadName + " 上处理";
                })
                .delayElements(Duration.ofSeconds(1));
    }

    /**
     * 示例2: subscribeOn操作符
     * 改变整个流的执行线程
     */
    @GetMapping(value = "/subscribe-on", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> subscribeOnExample() {
        return Flux.range(1, 5)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    System.out.println("数据生成在: " + threadName);
                    return "数据 " + i + " 生成于 " + threadName;
                })
                .subscribeOn(Schedulers.newParallel("自定义并行调度器"))
                .map(data -> {
                    String threadName = Thread.currentThread().getName();
                    System.out.println("数据处理在: " + threadName);
                    return data + "，处理于 " + threadName;
                })
                .delayElements(Duration.ofSeconds(1));
    }

    /**
     * 示例3: publishOn操作符
     * 改变后续操作符的执行线程
     */
    @GetMapping(value = "/publish-on", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> publishOnExample() {
        return Flux.range(1, 5)
                .map(i -> {
                    String threadName = Thread.currentThread().getName();
                    return "步骤1: 数据 " + i + " 在 " + threadName + " 上";
                })
                .publishOn(Schedulers.boundedElastic())
                .map(data -> {
                    String threadName = Thread.currentThread().getName();
                    return data + " -> 步骤2: 在 " + threadName + " 上";
                })
                .delayElements(Duration.ofSeconds(1));
    }

    /**
     * 示例4: 多个调度器组合使用
     * 展示复杂场景下的调度器使用
     */
    @GetMapping(value = "/multiple-schedulers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> multipleSchedulers() {
        Scheduler scheduler1 = Schedulers.newParallel("并行调度器1", 2);
        Scheduler scheduler2 = Schedulers.newParallel("并行调度器2", 2);
        
        return Flux.range(1, 10)
                .subscribeOn(scheduler1)
                .map(i -> {
                    simulateSlowOperation();
                    String threadName = Thread.currentThread().getName();
                    return "并行处理1 - 数据 " + i + " 在 " + threadName;
                })
                .publishOn(scheduler2)
                .map(data -> {
                    simulateSlowOperation();
                    String threadName = Thread.currentThread().getName();
                    return data + " -> 并行处理2 在 " + threadName;
                })
                .delayElements(Duration.ofSeconds(1));
    }

    /**
     * 示例5: elastic调度器使用
     * 适用于需要阻塞I/O操作的场景
     */
    @GetMapping(value = "/elastic-scheduler", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> elasticScheduler() {
        return Flux.range(1, 5)
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    // 模拟阻塞操作
                    simulateSlowOperation();
                    String threadName = Thread.currentThread().getName();
                    return "阻塞操作 " + i + " 在 " + threadName + " 上完成";
                })
                .delayElements(Duration.ofSeconds(1));
    }

    /**
     * 模拟耗时操作
     */
    private void simulateSlowOperation() {
        try {
            Thread.sleep(500); // 模拟耗时500ms的操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}