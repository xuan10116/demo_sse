package com.example.demo.controller.lingmacode;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SubscribeMethodController {

    @GetMapping("/subscribe-method")
    public String demonstrateSubscribeMethod() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== subscribe()方法简写形式及request信号控制 ===\n\n");
        
        // 1. 使用subscribe的简写形式
        result.append("1. 使用subscribe()简写形式:\n");
        System.out.println("=== 使用subscribe方法的多个参数 ===");
        Flux.just("x", "y", "z")
                .map(String::toUpperCase)
                .subscribe(
                        data -> {
                            System.out.println(data);
                            result.append("   接收数据: ").append(data).append("\n");
                        },           // onNext
                        error -> {
                            error.printStackTrace();
                            result.append("   错误: ").append(error.getMessage()).append("\n");
                        },    // onError
                        () -> {
                            System.out.println("Completed");
                            result.append("   完成\n");
                        } // onComplete
                );
        
        result.append("\n2. 背后的信号控制机制:\n");
        result.append("   - Reactor内部自动创建Subscriber实现\n");
        result.append("   - 自动调用request(Long.MAX_VALUE)请求所有数据\n");
        result.append("   - 适用于不需要背压控制的简单场景\n\n");
        
        // 3. 对比手动控制request信号
        result.append("3. 手动控制request信号的完整实现:\n");
        
        Flux.just("a", "b", "c")
                .map(String::toUpperCase)
                .subscribe(new Subscriber<String>() {
                    private Subscription subscription;
                    
                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.println("手动实现 - 收到onSubscribe");
                        this.subscription = s;
                        // 只请求2个元素，实现背压控制
                        System.out.println("手动实现 - 请求2个元素");
                        s.request(2);
                    }
                    
                    @Override
                    public void onNext(String data) {
                        System.out.println("手动实现 - 接收数据: " + data);
                        result.append("   手动控制接收数据: ").append(data).append("\n");
                        
                        // 处理完后可以继续请求更多数据
                        if ("B".equals(data)) {
                            System.out.println("手动实现 - 再请求1个元素");
                            subscription.request(1);
                        }
                    }
                    
                    @Override
                    public void onError(Throwable t) {
                        System.out.println("手动实现 - 错误: " + t.getMessage());
                    }
                    
                    @Override
                    public void onComplete() {
                        System.out.println("手动实现 - 完成");
                        result.append("   手动控制完成\n");
                    }
                });
                
        result.append("\n4. 两种方式的区别:\n");
        result.append("   简写形式: 自动请求所有数据，无背压控制\n");
        result.append("   手动实现: 精确控制请求数量，实现背压\n");
        
        return result.toString();
    }

    @GetMapping("/request-control")
    public String demonstrateRequestControl() {
        StringBuilder result = new StringBuilder();
        
        result.append("=== request信号精确控制演示 ===\n\n");
        
        // 演示不同request策略
        result.append("1. request(Long.MAX_VALUE) - 请求所有数据:\n");
        Flux.range(1, 5)
            .subscribe(new Subscriber<Integer>() {
                @Override
                public void onSubscribe(Subscription s) {
                    System.out.println("请求所有数据 - onSubscribe");
                    s.request(Long.MAX_VALUE); // 请求所有数据
                }
                
                @Override
                public void onNext(Integer data) {
                    System.out.println("请求所有数据 - onNext: " + data);
                    result.append("   ").append(data).append("\n");
                }
                
                @Override
                public void onError(Throwable t) {
                    System.out.println("请求所有数据 - onError: " + t.getMessage());
                }
                
                @Override
                public void onComplete() {
                    System.out.println("请求所有数据 - onComplete");
                    result.append("   完成\n");
                }
            });
            
        result.append("\n2. 分批请求数据:\n");
        Flux.range(1, 5)
            .subscribe(new Subscriber<Integer>() {
                private Subscription subscription;
                private int count = 0;
                
                @Override
                public void onSubscribe(Subscription s) {
                    System.out.println("分批请求 - onSubscribe");
                    this.subscription = s;
                    System.out.println("分批请求 - 初始请求2个元素");
                    s.request(2); // 初始请求2个元素
                }
                
                @Override
                public void onNext(Integer data) {
                    System.out.println("分批请求 - onNext: " + data);
                    result.append("   ").append(data).append("\n");
                    count++;
                    
                    // 每处理2个元素后，再请求2个
                    if (count % 2 == 0) {
                        System.out.println("分批请求 - 再请求2个元素");
                        subscription.request(2);
                    }
                }
                
                @Override
                public void onError(Throwable t) {
                    System.out.println("分批请求 - onError: " + t.getMessage());
                }
                
                @Override
                public void onComplete() {
                    System.out.println("分批请求 - onComplete");
                    result.append("   完成\n");
                }
            });
            
        result.append("\n3. 控制要点:\n");
        result.append("   - request(0)是无效的，不会请求数据\n");
        result.append("   - request(Long.MAX_VALUE)请求所有数据\n");
        result.append("   - 可以多次调用request()累加请求数量\n");
        result.append("   - Subscriber控制数据流速，实现背压\n");
        
        return result.toString();
    }
}