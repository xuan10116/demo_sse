# Reactor & WebFlux 学习项目

本项目旨在帮助开发者深入理解 Project Reactor 和 Spring WebFlux 的核心概念、设计理念和最佳实践。

## 项目概述

这是一个基于 Spring Boot 的演示项目，展示了响应式编程的核心概念。通过一系列实际代码示例，帮助开发者掌握 Reactor 和 WebFlux 的使用方法。

## 技术栈

- Spring Boot 2.2.0.RELEASE
- Spring WebFlux
- Project Reactor
- Java 8+

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/example/demo/
│   │       ├── controller/
│   │       │   ├── ChatStreamController.java      # 原始SSE聊天流示例
│   │       │   ├── ReactorExamplesController.java  # Reactor核心概念示例
│   │       │   ├── WebFluxVsMvcController.java    # WebFlux与传统MVC对比
│   │       │   ├── SchedulerExamplesController.java # 调度器使用示例
│   │       │   ├── ParallelSchedulerController.java # 并行调度器使用示例
│   │       │   ├── AllSchedulersController.java    # 所有调度器使用示例
│   │       │   ├── DefaultSchedulerController.java # 默认调度器行为示例
│   │       │   ├── ReactorBestPracticesController.java # 最佳实践示例
│   │       │   ├── CommonIssuesController.java    # 常见问题及解决方案
│   │       │   └── BackpressureStrategiesController.java # 背压策略示例
│   │       └── DemoSseApplication.java            # 应用启动类
│   └── resources/
│       ├── static/
│       │   ├── index.html                         # SSE演示页面
│       │   └── twgx.txt                           # 示例文本文件
│       └── application.properties                 # 应用配置
└── test/
    └── java/
        └── com/example/demo/
            └── ReactorTestingExamples.java        # Reactor测试示例
```

## 核心学习内容

### 1. Reactor 基础概念

通过 [ReactorExamplesController](src/main/java/com/example/demo/controller/ReactorExamplesController.java) 类学习 Reactor 的核心概念：

- **Flux 和 Mono**: 0...N 和 0...1 的异步序列
- **操作符**: map, flatMap, filter 等常用操作符
- **错误处理**: onErrorResume, onErrorContinue 等错误处理机制
- **背压处理**: onBackpressureBuffer 等背压处理策略
- **流合并**: zip, merge 等流合并操作符
- **缓存和批处理**: buffer, window 等批处理操作符

访问以下端点查看示例：
- `/reactor/flux-basic` - 基础Flux示例
- `/reactor/mono-basic` - 基础Mono示例
- `/reactor/error-handling` - 错误处理示例
- `/reactor/backpressure` - 背压处理示例
- `/reactor/combining` - 流合并示例
- `/reactor/buffering` - 缓存和批处理示例
- `/reactor/conditional` - 条件操作符示例
- `/reactor/transforming` - 转换操作符示例
- `/reactor/chat-example` - 实际聊天场景示例

### 2. WebFlux 与传统 Spring MVC 对比

通过 [WebFluxVsMvcController](src/main/java/com/example/demo/controller/WebFluxVsMvcController.java) 类理解 WebFlux 与传统 Spring MVC 的差异：

- **阻塞 vs 非阻塞**: 线程使用效率的对比
- **流式处理**: 实时数据流处理能力
- **背压支持**: 自然的背压处理机制

访问以下端点查看示例：
- `/comparison/mvc-blocking` - 传统MVC阻塞式处理
- `/comparison/webflux-nonblocking` - WebFlux非阻塞式处理
- `/comparison/webflux-stream` - 流式数据处理对比
- `/comparison/backpressure-demo` - 背压处理对比

### 3. 调度器 (Schedulers) 使用

通过 [SchedulerExamplesController](src/main/java/com/example/demo/controller/SchedulerExamplesController.java) 类学习调度器的使用：

- **subscribeOn**: 改变整个流的执行线程
- **publishOn**: 改变后续操作符的执行线程
- **不同类型调度器**: parallel, elastic, single 等调度器的使用场景

通过 [ParallelSchedulerController](src/main/java/com/example/demo/controller/ParallelSchedulerController.java) 类深入学习并行调度器：

- **CPU密集型任务处理**: 如何使用并行调度器处理计算密集型任务
- **并行处理**: 如何利用并行调度器同时处理多个任务
- **线程切换**: 并行调度器与其他调度器的线程差异

通过 [AllSchedulersController](src/main/java/com/example/demo/controller/AllSchedulersController.java) 类学习所有类型的调度器：

- **immediate调度器**: 在当前线程执行任务
- **single调度器**: 使用全局单线程执行任务
- **boundedElastic调度器**: 处理I/O密集型和阻塞操作
- **parallel调度器**: 处理CPU密集型任务
- **自定义调度器**: 创建和使用自定义线程池

通过 [DefaultSchedulerController](src/main/java/com/example/demo/controller/DefaultSchedulerController.java) 类学习默认调度器行为：

- **默认调度器**: 了解各种操作符的默认调度器行为
- **线程继承**: 理解线程如何在操作符间传递

访问以下端点查看示例：
- `/schedulers/default-thread` - 默认线程执行示例
- `/schedulers/subscribe-on` - subscribeOn 使用示例
- `/schedulers/publish-on` - publishOn 使用示例
- `/schedulers/multiple-schedulers` - 多调度器组合使用
- `/schedulers/elastic-scheduler` - elastic 调度器使用
- `/parallel-scheduler/cpu-intensive` - CPU密集型任务处理
- `/parallel-scheduler/parallel-processing` - 并行处理任务
- `/parallel-scheduler/thread-comparison` - 线程切换对比
- `/all-schedulers/immediate` - immediate 调度器使用
- `/all-schedulers/single` - single 调度器使用
- `/all-schedulers/bounded-elastic` - boundedElastic 调度器使用
- `/all-schedulers/parallel` - parallel 调度器使用
- `/all-schedulers/custom` - 自定义调度器使用
- `/all-schedulers/comparison` - 调度器对比
- `/default-scheduler/behavior` - 默认调度器行为

### 4. 背压处理策略

通过 [BackpressureStrategiesController](src/main/java/com/example/demo/controller/BackpressureStrategiesController.java) 类学习背压处理策略：

- **Buffer策略**: 缓冲多余的元素直到达到指定的限制
- **Drop策略**: 当下游无法跟上时，丢弃多余的元素
- **Latest策略**: 当下游无法跟上时，只保留最新的元素
- **Error策略**: 当下游无法跟上时，发出一个错误信号
- **LimitRate策略**: 控制上游发布者的请求速率
- **自定义策略**: 组合使用不同的背压策略
- **Sample策略**: 以指定的时间间隔采样数据流
- **Window策略**: 将数据流分组处理

访问以下端点查看示例：
- `/backpressure/buffer` - Buffer策略示例
- `/backpressure/drop` - Drop策略示例
- `/backpressure/latest` - Latest策略示例
- `/backpressure/error` - Error策略示例
- `/backpressure/limit-rate` - LimitRate策略示例
- `/backpressure/custom` - 自定义策略示例
- `/backpressure/sample` - Sample策略示例
- `/backpressure/window-buffer` - Window策略示例

### 5. 最佳实践和常见问题

通过 [ReactorBestPracticesController](src/main/java/com/example/demo/controller/ReactorBestPracticesController.java) 类学习 Reactor 最佳实践：

- **避免阻塞操作**: 如何正确处理阻塞操作
- **资源共享**: 正确处理共享可变状态
- **缓存**: 合理使用缓存提高性能
- **错误处理**: 正确处理和恢复错误

通过 [CommonIssuesController](src/main/java/com/example/demo/controller/CommonIssuesController.java) 类学习如何解决常见问题：

- **线程安全**: 避免共享可变状态引发的问题
- **错误处理**: 正确处理和恢复错误
- **背压处理**: 正确处理背压问题
- **资源管理**: 正确管理订阅和资源

访问以下端点查看示例：
- `/best-practices/avoid-blocking` - 避免阻塞操作示例
- `/best-practices/shared-state` - 共享状态处理示例
- `/best-practices/caching` - 缓存使用示例
- `/common-issues/mutable-state-issue` - 可变状态问题示例
- `/common-issues/mutable-state-solution` - 可变状态解决方案示例
- `/common-issues/error-handling-issue` - 错误处理问题示例
- `/common-issues/error-handling-solution` - 错误处理解决方案示例
- `/common-issues/backpressure-issue` - 背压处理问题示例
- `/common-issues/backpressure-solution` - 背压处理解决方案示例

### 6. Reactor 测试

通过 [ReactorTestingExamples](src/test/java/com/example/demo/ReactorTestingExamples.java) 类学习如何测试响应式流：

- **StepVerifier**: Reactor 测试的核心工具
- **期望值验证**: expectNext, expectError 等验证方法
- **时间相关测试**: 带超时的测试方法
- **背压测试**: 测试不同请求数量下的行为

运行测试：
```bash
./mvnw test
```

## 项目运行

### 环境要求
- JDK 8 或更高版本
- Maven 3.x

### 构建和运行
```bash
# 构建项目
./mvnw clean package

# 运行项目
./mvnw spring-boot:run

# 或者直接运行jar包
java -jar target/demo-sse-0.0.1-SNAPSHOT.jar
```

访问 [http://localhost:8080](http://localhost:8080) 查看演示页面。

## 学习资源

1. [Project Reactor 官方文档](https://projectreactor.io/docs/core/release/reference/)
2. [Reactor 中文文档](https://htmlpreview.github.io/?https://github.com/get-set/reactor-core/blob/master-zh/src/docs/index.html)
3. [Reactive Streams 规范](https://www.reactive-streams.org/)
