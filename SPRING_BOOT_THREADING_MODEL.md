# Spring Boot线程模型详解

## 阻塞方式线程模型（使用Future）

```mermaid
sequenceDiagram
    title Spring Boot阻塞方式线程模型（传统Future）
    
    participant Client as 客户端
    participant WebServer as Web服务器 (Tomcat)
    participant BusinessLogic as 业务逻辑
    
    Note over WebServer: Servlet线程池
    Note over BusinessLogic: 应用业务逻辑
    
    Client->>WebServer: HTTP请求
    WebServer->>BusinessLogic: 执行业务逻辑
    Note over WebServer,BusinessLogic: Servlet线程直接执行业务逻辑
    
    WebServer->>WebServer: 线程阻塞直到完成
    Note over WebServer: Servlet线程被长时间占用
    
    BusinessLogic-->>WebServer: 返回结果
    WebServer->>Client: 返回HTTP响应 (200 OK)
    
    Note over WebServer: 每个请求占用一个Servlet线程直到完成
```

## 非阻塞方式线程模型（使用CompletableFuture）

```mermaid
sequenceDiagram
    title Spring Boot非阻塞方式线程模型（CompletableFuture）
    
    participant Client as 客户端
    participant WebServer as Web服务器 (Tomcat)
    participant AppThreadPool as 应用线程池
    participant BusinessLogic as 业务逻辑
    participant Spring as Spring框架
    
    Note over WebServer: Servlet线程池
    Note over AppThreadPool: 应用自定义线程池
    
    Client->>WebServer: HTTP请求
    WebServer->>AppThreadPool: 提交异步任务
    Note over WebServer,AppThreadPool: CompletableFuture.supplyAsync(...)
    
    WebServer-->>WebServer: 立即释放Servlet线程
    Note over WebServer: Servlet线程可处理其他请求
    
    AppThreadPool->>BusinessLogic: 执行业务逻辑
    BusinessLogic-->>AppThreadPool: 返回结果
    AppThreadPool->>Spring: 任务完成通知
    Spring->>Client: 返回HTTP响应 (200 OK)
    
    Note over WebServer: Servlet线程快速释放，提高并发能力
```

## 响应式编程线程模型（使用Reactor）

```mermaid
sequenceDiagram
    title Spring Boot响应式编程线程模型（Reactor - WebFlux）
    
    participant Client as 客户端
    participant WebServer as Web服务器 (Netty)
    participant ReactorScheduler as Reactor调度器
    participant BusinessLogic as 业务逻辑
    
    Note over WebServer: Netty事件循环线程
    Note over ReactorScheduler: Reactor内置调度器
    
    Client->>WebServer: HTTP请求
    WebServer->>ReactorScheduler: 订阅数据流
    Note over WebServer,ReactorScheduler: Flux.just("data").subscribeOn(Schedulers.boundedElastic())
    
    WebServer-->>WebServer: 立即释放事件循环线程
    Note over WebServer: 事件循环线程可处理其他请求
    
    ReactorScheduler->>BusinessLogic: 执行业务逻辑
    BusinessLogic-->>ReactorScheduler: 返回结果
    
    loop 数据流推送 (SSE)
        ReactorScheduler->>WebServer: 推送数据
        WebServer->>Client: SSE数据推送
    end
    
    Note over WebServer: 事件循环线程仅用于IO操作
```

## 线程池配置和使用对比

```mermaid
graph LR
    A[Spring Boot应用] --> B[Web服务器线程池]
    A --> C[应用自定义线程池]
    A --> D[Reactor调度器]
    
    B --> B1["Servlet线程池<br/>(Tomcat)"]
    B --> B2["事件循环线程池<br/>(Netty)"]
    B --> B3[用于接收和<br/>协调HTTP请求]
    
    C --> C1[CompletableFuture<br/>线程池]
    C --> C2[@Async注解<br/>线程池]
    C --> C3[执行耗时<br/>业务逻辑]
    
    D --> D1["Schedulers<br/>.parallel()"]
    D --> D2["Schedulers<br/>.boundedElastic()"]
    D --> D3["Schedulers<br/>.single()"]
    
    D1 --> D11[CPU密集型任务]
    D2 --> D21[I/O密集型任务]
    D3 --> D31[轻量级任务]
    
    style A fill:#f0f8ff,stroke:#333
    style B fill:#ffe4c4,stroke:#333
    style C fill:#e0ffff,stroke:#333
    style D fill:#98fb98,stroke:#333
    
    classDef webServer fill:#ffe4c4,stroke:#333;
    classDef appThreadPool fill:#e0ffff,stroke:#333;
    classDef reactorScheduler fill:#98fb98,stroke:#333;
```

## 线程模型对比

```mermaid
graph LR
    A[线程模型] --> B[阻塞方式]
    A --> C[CompletableFuture方式]
    A --> D[Reactor方式]
    
    B --> B1[Web服务器线程直接执行业务逻辑]
    B --> B2[线程长时间占用]
    B --> B3[并发能力受限]
    
    C --> C1[Web服务器线程快速释放]
    C --> C2[应用线程池执行业务逻辑]
    C --> C3[提高并发能力]
    
    D --> D1[事件循环线程仅处理IO]
    D --> D2[调度器线程执行业务逻辑]
    D --> D3[响应式流处理]
    
    style B fill:#ffe4c4
    style C fill:#e0ffff
    style D fill:#98fb98
```

## 实际应用中的线程使用情况

```mermaid
gantt
    title 阻塞方式 vs 非阻塞方式线程使用情况对比
    dateFormat  HH:mm:ss
    axisFormat  %H:%M:%S
    
    section 阻塞方式 - Web服务器线程
    请求1处理: 00:00:00, 3s
    请求2处理: 00:00:00, 3s
    请求3处理: 00:00:00, 3s
    请求4处理: 00:00:00, 3s
    请求5处理: 00:00:00, 3s
    
    section 非阻塞方式 - Web服务器线程
    接收请求1: 00:00:00, 0s
    接收请求2: 00:00:00, 0s
    接收请求3: 00:00:00, 0s
    接收请求4: 00:00:00, 0s
    接收请求5: 00:00:00, 0s
    空闲时间: 00:00:00, 3s
    
    section 非阻塞方式 - 应用线程池
    任务1执行: 00:00:00, 3s
    任务2执行: 00:00:00, 3s
    任务3执行: 00:00:00, 3s
    任务4执行: 00:00:00, 3s
    任务5执行: 00:00:00, 3s
   
```

## 总结

通过以上图表可以清楚地看到不同线程模型的特点：

1. **阻塞方式**：
   - Web服务器线程（Servlet线程/事件循环线程）直接执行业务逻辑
   - 线程被长时间占用，直到请求处理完成
   - 并发处理能力受限于Web服务器线程池大小

2. **CompletableFuture非阻塞方式**：
   - Web服务器线程快速释放，可以处理其他请求
   - 应用自定义线程池执行具体的业务逻辑
   - 提高了系统的并发处理能力

3. **Reactor响应式方式**：
   - Web服务器事件循环线程仅处理IO操作
   - Reactor调度器线程执行业务逻辑
   - 通过响应式流实现高效的异步处理

关键区别：
- **Web服务器线程**：负责接收请求和IO操作，应尽量避免执行耗时业务逻辑
- **应用线程池/调度器线程**：专门用于执行耗时的业务逻辑，不影响Web服务器的并发处理能力