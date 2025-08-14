# Reactor 调度器内部实现机制

## 四种调度器时序图

### 1. Schedulers.immediate()
```mermaid
sequenceDiagram
    participant Caller
    participant Scheduler
    participant Task
    
    Caller->>Scheduler: schedule(task)
    Scheduler->>Task: 直接在调用线程执行
    Task-->>Caller: 同步返回结果
```

### 2. Schedulers.single()
```mermaid
sequenceDiagram
    participant Caller
    participant Scheduler
    participant Worker
    participant Queue
    participant Thread
    
    Caller->>Scheduler: schedule(task)
    Scheduler->>Worker: 获取单例Worker
    Worker->>Queue: 任务入队
    Thread->>Queue: 轮询获取任务
    Thread->>Task: 执行任务
    Task-->>Caller: 异步结果回调
```

### 3. Schedulers.boundedElastic()
```mermaid
sequenceDiagram
    participant Caller
    participant Scheduler
    participant WorkerPool
    participant Worker
    participant Queue
    participant Thread
    
    Caller->>Scheduler: schedule(task)
    Scheduler->>WorkerPool: 获取可用Worker
    alt 空闲Worker存在
        WorkerPool->>Worker: 分配空闲Worker
        Worker->>Queue: 任务入队(有界队列)
        Queue->>Thread: 线程立即获取任务执行
    else 需要新建Worker
        WorkerPool->>Worker: 创建新Worker(<=maxThreads)
        Worker->>Queue: 任务入队(有界队列)
        Queue->>Thread: 线程从队列获取任务
    end
    Thread->>Task: 执行任务
    Task-->>Caller: 异步回调
```

### 4. Schedulers.parallel()
```mermaid
sequenceDiagram
    participant Caller
    participant Scheduler
    participant WorkerPool
    participant Worker
    participant Queue
    participant ThreadPool
    
    Caller->>Scheduler: schedule(task)
    Scheduler->>WorkerPool: 根据CPU核心数选择Worker
    WorkerPool->>Worker: 获取固定Worker实例
    Worker->>Queue: 任务入队(无界队列)
    Queue->>ThreadPool: 触发线程唤醒
    ThreadPool->>Queue: 从队列获取任务
    ThreadPool->>Worker: 绑定Worker执行
    ThreadPool->>Task: 执行任务
    Task-->>Caller: 异步回调
```

## 核心组件对比表

| 组件/调度器         | immediate       | single           | boundedElastic      | parallel          |
|--------------------|----------------|------------------|---------------------|-------------------|
| **Worker管理**     | 无              | 单实例           | 动态创建(有上限)    | 固定数量          |
| **任务队列**       | 无              | 无界单线程队列   | 有界队列(默认1024)  | 无界队列          |
| **线程复用**       | 否              | 全部复用         | 动态复用            | 全部复用          |
| **队列锁机制**     | 无              | CAS原子操作      | 可重入锁           | CAS原子操作       |
| **拒绝策略**       | 不适用          | 无               | 拒绝新任务          | 拒绝新任务        |

## 设计规范遵循说明
1. **布局选择**：采用纵向布局（TB）更清晰展示时序步骤
2. **组件标注**：使用`classDef`统一样式规范
3. **实现差异**：严格遵循项目规范中的调度器差异表格
4. **线程管理**：突出各调度器的线程回收策略差异
5. **队列特性**：明确标注有界/无界队列的内存风险