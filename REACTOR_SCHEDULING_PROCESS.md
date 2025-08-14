# Reactor 调度过程详解

本文将以 [AllSchedulersController.java](src/main/java/com/example/demo/controller/AllSchedulersController.java) 中的 `boundedElasticScheduler` 示例为基础，详细说明任务从提交到执行的完整流程。

## 示例代码回顾

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

## 调度过程详解

### 1. 任务提交阶段

1. 客户端发起请求 `/bounded-elastic`
2. Spring MVC 调用 `boundedElasticScheduler()` 方法
3. 创建 `Flux.range(1, 5)` 数据流
4. 通过 `publishOn` 指定调度器

### 2. 调度器初始化

1. **调度器获取**：`Schedulers.boundedElastic()` 返回一个 `BoundedElasticScheduler` 实例
2. **Worker创建**：
   - 调用 `BoundedElasticScheduler.createWorker()`
   - 从 `workQueue` 中获取或创建新的 `BoundedElasticWorker`
   - 每个 Worker 绑定一个 `TaskScheduler` 线程池
3. **线程池配置**：
   - 默认线程数 = CPU 核心数 × 10
   - 最大任务队列容量 = 100,000
   - 空闲线程超时回收时间 = 60 秒

### 3. 任务处理流程

#### 3.1 任务提交

1. `Flux.range(1, 5)` 生成数据 1-5
2. `publishOn` 操作符将数据封装为任务提交给调度器
3. 任务被添加到 Worker 的任务队列中

#### 3.2 任务分发

1. **Worker选择**：采用轮询方式选择空闲 Worker
2. **任务队列**：
   - 使用 `Queue<Runnable>` 存储待执行任务
   - 当队列满时（超过 100,000），抛出 `Schedulers.RejectedExecutionException`
3. **线程分配**：
   - 如果所有线程都在忙，且未达到最大线程数，则创建新线程
   - 如果已达到最大线程数且队列已满，则拒绝任务

#### 3.3 任务执行

1. **线程获取任务**：
   - 线程从 Worker 的任务队列获取任务
   - 使用 `TaskScheduler` 执行任务
2. **执行上下文**：
   - 切换到 Worker 的执行上下文
   - 执行 `map` 操作符中的阻塞操作
3. **阻塞操作处理**：
   - `simulateBlockingOperation()` 被调用
   - 当前线程被占用，但调度器会自动创建新线程处理其他任务

#### 3.4 线程管理

1. **线程复用**：
   - 线程执行完任务后不会立即销毁
   - 等待 60 秒内是否有新任务，否则回收线程
2. **资源释放**：
   - 当 Worker 被取消时，释放相关线程资源
   - 空闲线程自动回收，防止资源浪费

### 4. 关键组件交互

```mermaid
graph LR
    A[Client] --> B[Controller.boundedElasticScheduler]
    B --> C[Schedulers.boundedElastic]
    C --> D[Worker: BoundedElasticWorker]
    D --> E[TaskQueue: 有界队列(100k)]
    E --> F[Thread: 执行 map 操作符]
    F --> G[执行 simulateBlockingOperation()]
    F --> H[Client: 返回处理结果]
    
    classDef core fill:#98fb98,stroke:#333;
    classDef diff fill:#e0ffff,stroke:#333;
    
    class A,B,C,D,E,F,G,H diff
```

### 5. 调度器差异对比

| 阶段 | boundedElasticScheduler | parallelScheduler |
|------|-------------------------|------------------|
| Worker管理 | 动态创建Worker实例 | 固定数量Worker实例 |
| 线程池 | 弹性线程池 | 固定线程池 |
| 任务队列 | 有界队列（100,000） | 无界队列 |
| 线程回收 | 空闲线程超时回收 | 线程永不回收 |
| 适用场景 | I/O密集型任务 | CPU密集型任务 |

### 6. 调度过程特点

1. **非阻塞提交**：任务提交是非阻塞的，立即返回
2. **上下文切换**：自动切换到调度器的执行上下文
3. **背压支持**：通过队列实现背压控制
4. **错误传播**：任务中的异常会传播到下游
5. **资源隔离**：不同调度器使用不同的线程池，避免相互影响

### 7. 典型执行时序

1. 客户端发起请求
2. 创建 Flux 数据流
3. 调用 `publishOn` 切换调度器
4. 获取或创建 Worker
5. Worker 将任务放入队列
6. 线程从队列获取任务
7. 执行 map 操作符
8. 处理阻塞操作
9. 返回结果给客户端

### 8. 线程生命周期管理

1. **线程创建**：
   - 初始时按需创建线程
   - 默认最大线程数 = CPU 核心数 × 10
2. **线程复用**：
   - 线程执行完任务后保持活跃状态
   - 等待新任务或超时回收
3. **线程回收**：
   - 空闲线程超过 60 秒自动回收
   - 保证线程池不会无限增长

### 9. 调度器生命周期

1. **创建**：
   - 通过 `Schedulers.boundedElastic()` 创建
   - 初始化线程池和 Worker 池
2. **运行**：
   - 处理任务提交和执行
   - 动态管理线程资源
3. **销毁**：
   - 调用 `dispose()` 方法
   - 关闭所有线程池
   - 释放资源

### 10. 总结

通过这个示例，我们可以看到 Reactor 调度器的完整工作流程：

1. **任务提交**：通过 `publishOn` 或 `subscribeOn` 提交任务
2. **Worker管理**：调度器选择或创建合适的 Worker
3. **任务排队**：任务被放入 Worker 的队列
4. **线程调度**：线程从队列获取任务执行
5. **资源管理**：线程复用和回收机制

理解这个过程有助于我们更好地选择和使用调度器，充分发挥 Reactor 的性能优势。