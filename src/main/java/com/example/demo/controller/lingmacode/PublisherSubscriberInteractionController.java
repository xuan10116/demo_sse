package com.example.demo.controller.lingmacode;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class PublisherSubscriberInteractionController {

    @GetMapping("/publisher-subscriber")
    public String demonstratePublisherSubscriberInteraction() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== Publisher 和 Subscriber 信号交换演示 ===\n\n");
        
        // 创建一个Publisher (Flux)
        Flux<String> publisher = Flux.just("A", "B", "C")
                .doOnSubscribe(sub -> System.out.println("[Publisher] 发送 onSubscribe 信号"))
                .doOnNext(data -> System.out.println("[Publisher] 准备发送 onNext: " + data))
                .doOnComplete(() -> System.out.println("[Publisher] 发送 onComplete 信号"));
        
        System.out.println("1. 创建Publisher: Flux.just(\"A\", \"B\", \"C\")");
        
        // 创建一个Subscriber
        Subscriber<String> subscriber = new Subscriber<String>() {
            private Subscription subscription;
            
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("[Subscriber] 收到 onSubscribe 信号");
                this.subscription = s;
                System.out.println("[Subscriber] 调用 request(2) 请求2个元素");
                s.request(2); // 请求2个元素
            }
            
            @Override
            public void onNext(String data) {
                System.out.println("[Subscriber] 收到 onNext: " + data);
                // 处理完数据后可以继续请求更多数据
                if ("B".equals(data)) {
                    System.out.println("[Subscriber] 处理完B后，调用 request(1) 请求更多元素");
                    subscription.request(1);
                }
            }
            
            @Override
            public void onError(Throwable t) {
                System.out.println("[Subscriber] 收到 onError: " + t.getMessage());
            }
            
            @Override
            public void onComplete() {
                System.out.println("[Subscriber] 收到 onComplete 信号");
            }
        };
        
        System.out.println("2. 创建Subscriber实现");
        System.out.println("3. 开始订阅过程:");
        
        // 订阅过程
        publisher.subscribe(subscriber);
        
        result.append("查看控制台输出了解完整的 Publisher-Subscriber 信号交换过程\n\n");
        
        result.append("信号交换顺序:\n");
        result.append("1. Publisher.subscribe() 被调用\n");
        result.append("2. Publisher 发送 onSubscribe(Subscription) 给 Subscriber\n");
        result.append("3. Subscriber 调用 Subscription.request(n) 请求数据\n");
        result.append("4. Publisher 根据请求数量发送 onNext 数据\n");
        result.append("5. Publisher 发送 onComplete 或 onError 信号结束流\n");
        
        return result.toString();
    }

    @GetMapping("/manual-subscription")
    public String demonstrateManualSubscription() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== 手动实现完整的订阅过程 ===\n\n");
        
        // 手动实现一个简单的Publisher
        Publisher<Integer> manualPublisher = new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                System.out.println("[ManualPublisher] 收到订阅请求");
                
                // 创建并发送Subscription
                Subscription subscription = new Subscription() {
                    private boolean cancelled = false;
                    private int requested = 0;
                    private int sent = 0;
                    private final int[] data = {1, 2, 3, 4, 5};
                    
                    @Override
                    public void request(long n) {
                        System.out.println("[ManualPublisher.Subscription] 收到request(" + n + ")请求");
                        if (cancelled) return;
                        
                        requested += n;
                        
                        // 发送数据直到满足请求数量或数据耗尽
                        while (sent < requested && sent < data.length) {
                            System.out.println("[ManualPublisher] 发送 onNext: " + data[sent]);
                            s.onNext(data[sent]);
                            sent++;
                        }
                        
                        // 如果所有数据都已发送，发送完成信号
                        if (sent == data.length) {
                            System.out.println("[ManualPublisher] 发送 onComplete");
                            s.onComplete();
                        }
                    }
                    
                    @Override
                    public void cancel() {
                        System.out.println("[ManualPublisher.Subscription] 订阅被取消");
                        cancelled = true;
                    }
                };
                
                System.out.println("[ManualPublisher] 发送 onSubscribe");
                s.onSubscribe(subscription);
            }
        };
        
        // 手动实现Subscriber
        Subscriber<Integer> manualSubscriber = new Subscriber<Integer>() {
            private Subscription subscription;
            
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("[ManualSubscriber] 收到 onSubscribe");
                this.subscription = s;
                System.out.println("[ManualSubscriber] 请求1个元素");
                s.request(1);
            }
            
            @Override
            public void onNext(Integer data) {
                System.out.println("[ManualSubscriber] 收到 onNext: " + data);
                // 处理完后请求下一个元素
                System.out.println("[ManualSubscriber] 请求下一个元素");
                subscription.request(1);
            }
            
            @Override
            public void onError(Throwable t) {
                System.out.println("[ManualSubscriber] 收到 onError: " + t.getMessage());
            }
            
            @Override
            public void onComplete() {
                System.out.println("[ManualSubscriber] 收到 onComplete");
            }
        };
        
        System.out.println("开始手动Publisher和Subscriber的交互:");
        manualPublisher.subscribe(manualSubscriber);
        
        result.append("查看控制台输出了解手动实现的完整信号交换过程\n");
        
        return result.toString();
    }
}