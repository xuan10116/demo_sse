package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 性能对比控制器
 * 
 * 提供四组不同的API端点，用于比较阻塞式和响应式编程在不同场景下的性能表现：
 * 1. 阻塞式一次性返回数据
 * 2. 响应式一次性返回数据
 * 3. 阻塞式流式返回数据
 * 4. 响应式流式返回数据
 * 
 * 通过JMeter等工具测试这些端点，可以观察到：
 * - 线程利用率差异
 * - 内存使用情况
 * - 并发处理能力
 * - 响应时间表现
 * - 背压处理能力
 */
@RestController
@RequestMapping("/performance")
public class PerformanceComparisonController {

    /**
     * 传统阻塞式实现 - 一次性返回所有数据
     * 
     * 特点：
     * 1. 每个请求会占用一个Tomcat线程
     * 2. 线程在整个处理过程中被阻塞，无法处理其他请求
     * 3. 内存使用较高（每个线程默认栈空间约1MB）
     * 4. 在高并发场景下容易出现线程耗尽问题
     * 
     * 适用场景：
     * - CPU密集型任务
     * - 简单的Web应用
     * - 不需要高并发处理的场景
     */
    @GetMapping("/blocking")
    public List<String> blocking() {
        // 模拟一些处理逻辑
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            // 模拟一些计算或处理
            String processed = processData(i);
            data.add(processed);
        }
        return data;
    }

    /**
     * 传统阻塞式实现 - 流式返回数据（模拟）
     * 
     * 特点：
     * 1. 使用Thread.sleep模拟处理时间，线程被阻塞
     * 2. 无法真正实现流式传输
     * 3. 客户端需要等待所有数据处理完成后才能接收到响应
     * 
     * 适用场景：
     * - 不适合实际的流式数据处理
     * - 仅用于与真正的流式处理进行性能对比
     */
    @GetMapping("/blocking-stream")
    public List<String> blockingStream() throws InterruptedException {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            // 模拟处理时间
            Thread.sleep(10);
            String processed = processData(i);
            data.add(processed);
        }
        return data;
    }

    /**
     * 响应式实现 - 使用Flux一次性返回
     * 
     * 特点：
     * 1. 使用少量事件循环线程处理所有请求
     * 2. 非阻塞式处理，线程利用率高
     * 3. 内存使用相对较低
     * 4. 更好的并发处理能力
     * 
     * 适用场景：
     * - 需要高并发处理能力的应用
     * - IO密集型操作
     * - 需要节省线程资源的场景
     */
    @GetMapping("/reactive")
    public Mono<List<String>> reactive() {
        return Flux.range(0, 1000)
                .map(this::processData)
                .collectList();
    }

    /**
     * 响应式实现 - 使用Flux流式返回
     * 
     * 特点：
     * 1. 真正的非阻塞流式处理
     * 2. 支持背压处理，能够在系统过载时保护系统
     * 3. 客户端可以逐步接收数据，无需等待所有数据处理完成
     * 4. 更好的用户体验和资源利用率
     * 
     * 适用场景：
     * - 实时数据推送
     * - 大数据量传输
     * - 需要背压处理的场景
     * - Server-Sent Events (SSE) 或 WebSocket 应用
     */
    @GetMapping(value = "/reactive-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> reactiveStream() {
        return Flux.range(0, 100)
                .delayElements(Duration.ofMillis(10))
                .map(this::processData);
    }

    /**
     * 模拟数据处理函数
     * 
     * 模拟一些CPU计算，用于消耗一定的处理时间
     * 这样可以更好地观察阻塞式和响应式实现的差异
     */
    private String processData(int i) {
        // 模拟一些CPU计算
        int result = 0;
        for (int j = 0; j < 1000; j++) {
            result += Math.sqrt(j) * Math.sin(i);
        }
        return "Data-" + i + "-Result-" + result;
    }
}