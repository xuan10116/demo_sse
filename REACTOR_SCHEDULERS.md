# Reactor 调度器详解

在 Reactor 中，调度器（Scheduler）是执行模型的核心，它决定了代码在哪个线程上执行。Reactor 提供了多种调度器以适应不同的使用场景。理解这些调度器的特性和使用方式对于正确使用 Reactor 至关重要。

## 调度器概述

调度器是 Reactor 中用于控制操作执行线程和执行方式的组件。它类似于 `ExecutorService`，但提供了更丰富的功能，特别是在响应式流的上下文中。调度器可以用于切换执行上下文，可将耗时操作从主线程中移出，提高应用程序的响应性和吞吐量。

## 四种主要调度器

### 1. Schedulers.immediate()

[Schedulers.immediate()](file://reactor/core/scheduler/Schedulers.java#L59-L59) 是最简单的调度器，它不进行任何线程切换。提交给这个调度器的任务会在当前线程立即执行。

#### 实现原理：
- 不创建新的线程，直接在当前线程执行任务
- 适用于快速、非阻塞的操作
- 实现简单，开销最小

#### 使用场景：
- 快速计算或转换操作
- 不涉及任何阻塞或耗时操作的场景

#### 示例代码：
```java
@GetMapping(value = "/immediate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> immediateScheduler() {
    return Flux.range(1, 5)
            .map(i -> {
                String threadName = Thread.currentThread().getName();
                return "数据 " + i + " 在线程 " + threadName;
            })
            .subscribeOn(Schedulers.immediate()) // 在当前线程执行
            .map(data -> data + " -> 使用 immediate 调度器");
}
```

### 2. Schedulers.single()

[Schedulers.single()](file://reactor/core/scheduler/Schedulers.java#L96-L96) 是一个全局单线程调度器，所有使用该调度器的任务都会在同一个线程中执行。

#### 实现原理：
- 使用单个可重用线程执行所有任务
- 所有调用共享同一个线程，直到调度器被销毁
- 任务按顺序执行，保证了任务间的顺序性

#### 使用场景：
- 轻量级、非并行的任务
- 需要全局顺序执行的场景

#### 示例代码：
```java
@GetMapping(value = "/single", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> singleScheduler() {
    return Flux.range(1, 5)
            .publishOn(Schedulers.single()) // 使用单线程调度器
            .map(i -> {
                String threadName = Thread.currentThread().getName();
                return "数据 " + i + " 在线程 " + threadName;
            })
            .delayElements(Duration.ofSeconds(1)); // 添加延迟以便观察
}
```

### 3. Schedulers.boundedElastic()

`Schedulers.boundedElastic()` 是一个有界弹性线程池，用于处理 I/O 密集型和阻塞操作。

#### 实现原理：
- 创建可重用的线程池，如果线程长时间空闲会被回收
- 有线程数量和任务队列大小的限制（默认线程数为 CPU 核心数 × 10，最大任务队列数为 100,000）
- 当所有线程都在忙时，新任务会被放入队列等待
- 是处理阻塞操作的首选调度器，替代了旧的 `Schedulers.elastic()`

#### 使用场景：
- I/O 密集型任务（如文件读写、数据库操作、网络请求）
- 阻塞操作
- 需要弹性伸缩线程数量的场景

#### 示例代码：
```java
@GetMapping(value = "/bounded-elastic", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> boundedElasticScheduler() {
    return Flux.range(1, 5)
            .publishOn(Schedulers.boundedElastic()) // 使用有界弹性调度器
            .map(i -> {
                // 模拟阻塞操作
                simulateBlockingOperation();
                String threadName = Thread.currentThread().getName();
                return "阻塞操作 " + i + " 在线程 " + threadName + " 完成";
            })
            .delayElements(Duration.ofSeconds(1));
}
```

### 4. Schedulers.parallel()

`Schedulers.parallel()` 是一个固定大小的线程池，线程数与 CPU 核心数相同，适用于 CPU 密集型任务。

#### 实现原理：
- 创建固定数量的线程（通常等于 CPU 核心数）
- 适用于并行处理任务
- 线程会一直存在，不会被回收

#### 使用场景：
- CPU 密集型计算任务
- 可以并行处理的任务
- 需要充分利用 CPU 资源的场景

#### 示例代码：
```java
@GetMapping(value = "/parallel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> parallelScheduler() {
    return Flux.range(1, 10)
            .publishOn(Schedulers.parallel()) // 使用并行调度器
            .map(i -> {
                // 模拟CPU密集型操作
                simulateCpuIntensiveOperation();
                String threadName = Thread.currentThread().getName();
                return "CPU计算 " + i + " 在线程 " + threadName + " 完成";
            })
            .delayElements(Duration.ofMillis(500));
}
```



## 调度器内部实现机制



### Worker 模式

Reactor 的调度器采用 Worker 模式实现，每个调度器可以创建多个 Worker 实例。**所有调度器的核心实现都基于 Worker 模式**，但不同调度器的 Worker 管理策略和线程模型存在显著差异。

#### Worker 的核心作用：
1. **任务执行单元**：Worker 是调度器的工作单元，负责实际执行任务
2. **线程绑定**：每个 Worker 绑定一个线程或线程池
3. **串行保障**：同一个 Worker 保证任务串行执行
4. **资源管理**：管理线程生命周期和任务队列

#### 不同调度器实现对比

| 特性/调度器 | immediate | single | boundedElastic | parallel |
|------------|---------|--------|----------------|----------|
| **Worker数量** | 无Worker，直接在调用线程执行任务 | 全局共享一个Worker | 动态可变 | 固定(CPU核心数) |
| **线程模型** | - | 单线程 | 弹性线程池，线程数可变（默认CPU核心数×10） | 固定线程池，线程数等于CPU核心数 |
| **任务队列** | - | 无界队列 | 有界队列，最大任务队列100,000（防止内存溢出） | 无界队列（保证任务不丢失） |
| **任务分发策略** | 直接在调用线程执行 | 始终使用同一个Worker | 轮询选择空闲Worker | 按任务哈希选择Worker |
| **线程回收** | -                                | 线程永不回收                   | 空闲线程超时回收                              | 线程永不回收                      |
| **适用场景** | 快速操作 | 顺序任务 | IO密集型 | CPU密集型 |
| **典型用例** | 简单转换 | 定时任务 | 网络请求 | 并行计算 |
| **特点** | 零开销，无上下文切换 | 保证任务顺序执行，避免线程竞争 | 支持并发执行，防止资源耗尽 | 最大化CPU利用率，保证任务公平执行 |



## 调度器使用最佳实践

### 选择合适的调度器

1. **对于快速、简单操作**：使用 [Schedulers.immediate()](file://reactor/core/scheduler/Schedulers.java#L59-L59)
2. **对于轻量级、顺序执行任务**：使用 [Schedulers.single()](file://reactor/core/scheduler/Schedulers.java#L96-L96)
3. **对于 I/O 密集型或阻塞操作**：使用 `Schedulers.boundedElastic()`
4. **对于 CPU 密集型并行计算**：使用 [Schedulers.parallel()](file://reactor/core/scheduler/Schedulers.java#L58-L58)

### 避免阻塞主线程

在响应式编程中，避免在事件循环线程或主线程上执行阻塞操作非常重要。应该使用合适的调度器将这些操作移到后台线程执行。

```
// 错误示例：在主线程上执行阻塞操作
@GetMapping("/wrong-blocking")
public Mono<String> wrongBlocking() {
    return Mono.just("result")
               .map(data -> {
                   // 阻塞操作会阻塞主线程
                   Thread.sleep(1000);
                   return data;
               });
}

// 正确示例：使用调度器处理阻塞操作
@GetMapping("/correct-blocking")
public Mono<String> correctBlocking() {
    return Mono.just("result")
               .publishOn(Schedulers.boundedElastic())
               .map(data -> {
                   // 阻塞操作在后台线程执行
                   Thread.sleep(1000);
                   return data;
               });
}
```

### 合理使用 subscribeOn 和 publishOn

- **subscribeOn**：影响源头的执行线程，通常用于指定数据流的初始执行线程
- **publishOn**：改变后续操作符的执行线程，可以多次使用

```
Flux.range(1, 10)
    .map(i -> {
        // 在主线程执行
        return i * 2;
    })
    .subscribeOn(Schedulers.boundedElastic()) // 指定整个流的执行线程
    .publishOn(Schedulers.parallel()) // 改变后续操作的执行线程
    .map(i -> {
        // 在parallel线程池执行
        return i + 1;
    });
```
