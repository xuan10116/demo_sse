package com.example.demo.controller.lingmacode;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 演示 reactor-core 和 reactive-streams 的关系
 */
public class ReactorAndReactiveStreamsDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Reactor-Core 和 Reactive-Streams 关系演示 ===\n");

//        // 1. Reactor-Core 实现了 Reactive-Streams 规范
//        System.out.println("1. Reactor-Core 实现了 Reactive-Streams 规范");
//        demonstrateReactorImplementsReactiveStreams();
//
//        Thread.sleep(1000);

        // 2. 使用 Reactive-Streams 原生接口
        System.out.println("\n2. 使用 Reactive-Streams 原生接口");
        demonstrateReactiveStreamsInterfaces();

        Thread.sleep(1000);
        
//        // 3. Reactor-Core 扩展功能
//        System.out.println("\n3. Reactor-Core 扩展功能");
//        demonstrateReactorExtensions();
        
        Thread.sleep(2000);
    }

    /**
     * 演示 Reactor-Core 如何实现 Reactive-Streams 规范
     */
    private static void demonstrateReactorImplementsReactiveStreams() {
        // Flux 实现了 Publisher 接口
        Flux<String> flux = Flux.just("Hello", "Reactive", "World");
        
        // 可以直接作为 Publisher 使用
        Publisher<String> publisher = flux;
        
        System.out.println("Flux 实现了 Publisher 接口: " + (publisher instanceof Publisher));
        System.out.println("Flux 类型: " + flux.getClass().getName());
        
        // 使用 Reactive-Streams 原生订阅方式
        publisher.subscribe(new Subscriber<String>() {
            private Subscription subscription;
            
            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                System.out.println("  原生订阅建立");
                subscription.request(10); // 请求10个元素
            }

            @Override
            public void onNext(String s) {
                System.out.println("  原生接收到: " + s);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("  原生错误: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("  原生完成");
            }
        });
    }

    /**
     * 演示使用 Reactive-Streams 原生接口
     */
    private static void demonstrateReactiveStreamsInterfaces() {
        // 创建一个简单的 Publisher 实现
        Publisher<Long> publisher = subscriber -> {
            subscriber.onSubscribe(new Subscription() {
                private boolean cancelled = false;
                private int counter = 0;
                private final int maxItems = 3;

                @Override
                public void request(long n) {
                    System.out.println("  Publisher 接收到请求: " + n + " 个元素");

                    // 模拟发送数据
                    for (int i = 0; i < n && i < maxItems - counter && !cancelled; i++) {
                        counter++;
                        subscriber.onNext((long) counter);
                    }

                    if (counter >= maxItems && !cancelled) {
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() {
                    System.out.println("  Publisher 订阅被取消");
                    cancelled = true;
                }
            });
        };

        // 订阅这个 Publisher
        publisher.subscribe(new Subscriber<Long>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                System.out.println("  自定义 Publisher 订阅建立");
                // 请求2个元素
                subscription.request(2);
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("  接收到数据: " + aLong);
                // 处理完一个元素后再请求下一个
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("  发生错误: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("  数据流完成");
            }
        });
    }

    /**
     * 演示 Reactor-Core 的扩展功能
     */
    private static void demonstrateReactorExtensions() {
        System.out.println("Reactor-Core 提供的额外功能:");
        
        // 1. 丰富的操作符
        System.out.println("  1. 丰富的操作符:");
        Flux.interval(Duration.ofMillis(200))
            .take(3)
            .map(i -> "Item " + i)
            .subscribe(
                data -> System.out.println("    处理数据: " + data),
                error -> System.err.println("    错误: " + error),
                () -> System.out.println("    完成")
            );

        // 2. 背压处理策略
        System.out.println("  2. 背压处理策略:");
        Flux.range(1, 1000)
            .onBackpressureBuffer(10) // 缓冲10个元素
            .limitRate(1) // 限制处理速率
            .take(5)
            .subscribe(
                data -> System.out.println("    限速处理: " + data)
            );

        // 3. 错误处理
        System.out.println("  3. 错误处理:");
        Flux.just(1, 2, 0, 4)
            .map(i -> 10 / i) // 除以0会产生错误
            .onErrorResume(e -> {
                System.out.println("    捕获错误: " + e.getMessage());
                return Mono.just(-1); // 返回默认值
            })
            .subscribe(
                data -> System.out.println("    错误处理后数据: " + data)
            );
    }
}