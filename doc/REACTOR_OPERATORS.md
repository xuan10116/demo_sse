# Reactor 操作符详解笔记

Project Reactor 是一个用于构建响应式应用程序的库，它提供了丰富的操作符来处理异步数据流。这些操作符可以分为多个类别，每个类别都有其特定的用途和行为。本笔记将详细介绍 Reactor 中常用的操作符及其使用方法。

## 1. 创建操作符

创建操作符用于创建 Flux 或 Mono 流。

### 1.1 基础创建操作符

#### Flux.just()
从指定的元素创建一个 Flux 流。适用于已知数据源的情况。

```java
Flux<String> flux = Flux.just("A", "B", "C");
```

#### Mono.just()
从单个元素创建一个 Mono 流。

```java
Mono<String> mono = Mono.just("Hello");
```

#### Flux.interval()
创建一个按指定时间间隔发射递增 Long 值的 Flux 流。

```java
Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1));
```

#### Flux.range()
创建一个发射指定范围内整数序列的 Flux 流。

```java
Flux<Integer> rangeFlux = Flux.range(1, 5); // 发射 1, 2, 3, 4, 5
```

#### Flux.fromIterable()
从 Iterable 创建 Flux 流。

```java
List<String> list = Arrays.asList("A", "B", "C");
Flux<String> flux = Flux.fromIterable(list);
```

### 1.2 特殊状态操作符

#### Mono.empty()
创建一个不包含任何元素且立即完成的 Mono。

```java
Mono<String> emptyMono = Mono.empty();
```

#### Mono.never()
创建一个不包含任何元素且永远不会完成的 Mono。

```java
Mono<String> neverMono = Mono.never();
```

## 2. 转换操作符

转换操作符用于对流中的元素进行转换。

### 2.1 基础转换操作符

#### map()
对 Flux/Mono 中的每个元素应用同步函数进行转换。

```java
Flux<Integer> numbers = Flux.just(1, 2, 3);
Flux<String> strings = numbers.map(n -> "Number: " + n);
```

#### flatMap()
将每个元素转换为 Publisher，然后将所有 Publisher 合并为一个 Flux。这是一个异步操作，不保证顺序。

```java
Flux<String> letters = Flux.just("A", "B", "C");
Flux<String> flatMapped = letters.flatMap(letter -> 
    Mono.just(letter + "!").delayElement(Duration.ofMillis(100)));
```

### 2.2 异步转换操作符

#### delayElements()
延迟发射 Flux 中的每个元素。

```java
Flux<Integer> delayedFlux = Flux.just(1, 2, 3).delayElements(Duration.ofSeconds(1));
```

#### delayElement()
延迟发射 Mono 中的元素。

```java
Mono<String> delayedMono = Mono.just("Hello").delayElement(Duration.ofSeconds(1));
```

## 3. 过滤操作符

过滤操作符用于筛选流中的元素。

### 3.1 限制数量操作符

#### take()
限制 Flux 发射元素的数量。

```java
Flux<Integer> limitedFlux = Flux.range(1, 100).take(10); // 只取前10个元素
```

#### takeUntil()
从源 Flux 获取元素直到条件谓词匹配。

```java
Flux<Integer> randomNumbers = Flux.interval(Duration.ofMillis(500))
    .map(i -> new Random().nextInt(100))
    .takeUntil(i -> i > 90); // 当遇到大于90的数时停止
```

### 3.2 条件过滤操作符

#### skipWhile()
丢弃源 Flux 中的元素直到条件谓词不匹配。

```java
Flux<Integer> skippedFlux = Flux.interval(Duration.ofMillis(500))
    .map(i -> new Random().nextInt(100))
    .skipWhile(i -> i < 50) // 跳过小于50的数
    .takeUntil(i -> i > 90); // 当遇到大于90的数时停止
```

#### filter()
过滤满足条件的元素。

```java
Flux<Integer> evenNumbers = Flux.range(1, 10).filter(n -> n % 2 == 0);
```

## 4. 组合操作符

组合操作符用于将多个流合并为一个流。

### 4.1 并行组合操作符

#### Flux.merge()
将多个 Publisher 合并为一个，按元素发射的时间顺序。

```java
Flux<String> flux1 = Flux.interval(Duration.ofSeconds(1))
    .map(i -> "Flux1: " + i)
    .take(5);

Flux<String> flux2 = Flux.interval(Duration.ofSeconds(1))
    .map(i -> "Flux2: " + i)
    .take(5);

Flux<String> merged = Flux.merge(flux1, flux2);
```

#### Flux.zip()
将多个 Publisher 组合在一起，当所有源 Publisher 都发射了数据时，将这些数据组合起来。

```java
Flux<String> flux1 = Flux.interval(Duration.ofSeconds(1))
    .map(i -> "Flux1: " + i)
    .take(5);

Flux<String> flux2 = Flux.interval(Duration.ofMillis(500))
    .map(i -> "Flux2: " + i)
    .take(10);

Flux<Tuple2<String, String>> zipped = Flux.zip(flux1, flux2);
```

### 4.2 串行组合操作符

#### concatMap()
顺序订阅每个内部 Publisher，保持顺序输出。

```java
Flux<String> letters = Flux.just("A", "B", "C");
Flux<String> concatMapped = letters.concatMap(letter -> 
    Mono.just(letter + "!").delayElement(Duration.ofMillis(100)));
```

## 5. 聚合操作符

聚合操作符用于将流中的多个元素聚合成更少的元素或者单个值。

### 5.1 缓冲聚合操作符

#### buffer()
将 Flux 中的元素按指定大小分组为 List。

```java
List<String> data = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
Flux<List<String>> buffered = Flux.fromIterable(data)
    .delayElements(Duration.ofMillis(200))
    .buffer(3); // 每3个元素组成一个List
```

### 5.2 窗口聚合操作符

#### window()
将 Flux 中的元素按指定大小分组为内部 Flux 流。

```java
Flux<Flux<Long>> windowed = Flux.interval(Duration.ofMillis(100))
    .take(20)
    .window(5); // 每5个元素分为一组窗口

Flux<String> result = windowed.flatMap(window -> 
    window.reduce("", (acc, value) -> acc + " " + value)
         .map(reduced -> "窗口数据: [" + reduced.trim() + "]"));
```

#### window(Duration)
按时间窗口将 Flux 中的元素分组为内部 Flux 流。

```java
Flux<String> timeWindowed = Flux.interval(Duration.ofMillis(100))
    .take(30)
    .window(Duration.ofSeconds(1)) // 每1秒创建一个新窗口
    .flatMap(window -> 
        window.count()
             .map(count -> "在1秒时间窗口内收到 " + count + " 个元素"));
```

> 根据项目规范，window操作符适用于需要立即处理窗口内数据的场景，内存效率更高，实时性更好；而buffer操作符适用于需要等待窗口填满后处理整个集合的场景。

#### count()
计算 Flux 中元素的数量。

```java
Mono<Long> count = Flux.range(1, 10).count();
```

## 6. 错误处理操作符

错误处理操作符用于处理流中的异常情况。

### 6.1 错误恢复操作符

#### onErrorResume()
出错时切换到备用的 Publisher 流。

```java
Flux<String> flux = Flux.interval(Duration.ofSeconds(1))
    .flatMap(i -> {
        if (i == 3) {
            return Mono.error(new RuntimeException("模拟错误"));
        }
        return Mono.just("正常数据: " + i);
    })
    .onErrorResume(e -> Mono.just("错误已处理: " + e.getMessage()));
```

#### onErrorContinue()
出错时忽略错误元素并继续处理流中其他元素。

```java
Flux<String> flux = Flux.interval(Duration.ofSeconds(1))
    .flatMap(i -> {
        if (i == 3) {
            return Mono.error(new RuntimeException("模拟错误"));
        }
        return Mono.just("正常数据: " + i);
    })
    .onErrorContinue(RuntimeException.class, 
        (e, v) -> System.out.println("错误已处理: " + e.getMessage()));
```

## 7. 背压处理操作符

背压处理操作符用于处理生产者和消费者速率不匹配的情况。

### 7.1 背压策略操作符

#### onBackpressureBuffer()
缓冲所有多余的元素直到达到指定的限制。

```java
Flux<String> buffered = Flux.interval(Duration.ofMillis(100))
    .onBackpressureBuffer(10, 
        dropped -> System.out.println("丢弃的数据: " + dropped))
    .take(30)
    .map(i -> "处理后的数据: " + i);
```

#### onBackpressureDrop()
当下游无法跟上时，丢弃多余的元素。

```java
Flux<String> dropped = Flux.interval(Duration.ofMillis(1))
    .onBackpressureDrop(item -> logger.info("丢弃的项目: " + item))
    .take(30)
    .map(i -> "Drop策略处理数据: " + i);
```

#### onBackpressureLatest()
当下游无法跟上时，只保留最新的元素。

```java
Flux<String> latest = Flux.interval(Duration.ofMillis(1))
    .onBackpressureLatest()
    .take(30)
    .map(i -> "Latest策略处理数据: " + i);
```

#### onBackpressureError()
当下游无法跟上时，发出一个错误信号。

```java
Flux<String> errorBackpressure = Flux.interval(Duration.ofMillis(1))
    .onBackpressureError()
    .map(i -> "Error策略处理数据: " + i)
    .onErrorResume(throwable -> {
        logger.severe("背压错误: " + throwable.getMessage());
        return Flux.just("背压错误发生，流已终止");
    });
```

### 7.2 速率控制操作符

#### limitRate()
限制请求速率。

```java
Flux<String> limited = Flux.interval(Duration.ofMillis(1))
    .limitRate(10) // 限制请求速率
    .take(100)
    .map(i -> "限速策略处理数据: " + i);
```

#### sample()
以指定的时间间隔采样数据流。

```java
Flux<String> sampled = Flux.interval(Duration.ofMillis(1))
    .sample(Duration.ofMillis(100)) // 每100ms采样一次
    .take(100)
    .map(i -> "采样策略处理数据: " + i);
```

## 8. 调度器操作符

调度器操作符用于控制操作执行的线程。

### 8.1 线程调度操作符

#### subscribeOn()
指定整个订阅链的执行调度器。

```java
Flux<Integer> flux = Flux.range(1, 5)
    .map(i -> {
        String threadName = Thread.currentThread().getName();
        return "数据 " + i + " 在线程 " + threadName;
    })
    .subscribeOn(Schedulers.immediate()) // 在当前线程执行
    .map(data -> data + " -> 使用 immediate 调度器");
```

#### publishOn()
指定后续操作符的执行调度器。

```java
Flux<Integer> flux = Flux.range(1, 5)
    .publishOn(Schedulers.single()) // 使用单线程调度器
    .map(i -> {
        String threadName = Thread.currentThread().getName();
        return "数据 " + i + " 在线程 " + threadName;
    });
```

## 9. 调试操作符

调试操作符用于调试和监控流的执行。

### 9.1 日志操作符

#### log()
记录流的订阅、请求、元素、错误和完成事件。

```java
Flux<Long> loggedFlux = Flux.interval(Duration.ofSeconds(1))
    .log()
    .take(10)
    .map(i -> "Flux item: " + i);
```

## 10. 组合操作符

组合操作符用于组合多个流的完成信号。

### 10.1 组合完成信号操作符

#### Mono.when()
当所有 Mono 都完成时发出完成信号。

```java
Mono<String> emptyMono = Mono.empty();
Mono<String> justMono = Mono.just("立即完成的Mono");

Mono<Void> when = Mono.when(emptyMono, justMono);
```

#### then()
忽略源流中的元素，仅在完成时发出指定值。

```java
Mono<String> result = Mono.when(emptyMono, justMono)
    .then(Mono.just("多个Mono处理完成"));
```

## 总结

Reactor 提供了丰富的操作符来处理响应式流。正确选择和使用这些操作符对于构建高效、可靠的响应式应用程序至关重要。在实际使用中，需要根据具体需求选择合适类型的操作符，并注意操作符之间的组合和顺序，以达到最佳效果。