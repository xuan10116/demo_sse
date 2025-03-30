package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

@RestController
@RequestMapping("/flux-demo")
public class FluxWebDemoController {

    @GetMapping("/stream-normal")
    public String fluxStreamNormalDemo() {
        Flux<String> flux = createFlux();
        flux.subscribe(
            data -> System.out.println("[Subscriber] Received: " + data),
            error -> System.err.println("[Subscriber] Error: " + error.getMessage()),
            () -> System.out.println("[Subscriber] Completed")
        );

        return "Check the console for normal Flux stream output!";
    }

    @GetMapping("/stream-error")
    public String fluxStreamErrorDemo() {
        // 案例2：模拟异常情况
        Flux<String> errorFlux = createFluxWithError();
        // 订阅异常流
        errorFlux.subscribe(
            data -> System.out.println("[Subscriber] Received: " + data), // 数据处理
            error -> System.err.println("[Subscriber] Error: " + error.getMessage()), // 错误处理
            () -> System.out.println("[Subscriber] Completed") // 完成回调
        );

        return "Check the console for error Flux stream output!";
    }

    @GetMapping("/stream-cancel")
    public String fluxStreamCancelDemo() {
        // 案例3：模拟取消情况
        Flux<String> cancelFlux = createFlux();
        // 订阅并取消流
        cancelFlux.subscribe(
            data -> {
                System.out.println("[Subscriber] Received: " + data); // 数据处理
                if (data.equals("Flux")) {
                    throw new RuntimeException("Simulated cancellation");
                }
            },
            error -> System.err.println("[Subscriber] Error: " + error.getMessage()), // 错误处理
            () -> System.out.println("[Subscriber] Completed") // 完成回调
        ).dispose();

        return "Check the console for cancel Flux stream output!";
    }

    private Flux<String> createFlux() {
        return Flux.just("Hello", "Flux", "Stream")
            .map(data -> "Transformed: " + data)
            .doOnNext(data -> System.out.println("[Stream] Next: " + data))
            .doOnError(error -> System.err.println("[Stream] Error: " + error.getMessage()))
            .doOnComplete(() -> System.out.println("[Stream] Complete"))
            .doFinally(signalType -> handleSignal(signalType));
    }

    private Flux<String> createFluxWithError() {
        return Flux.just("Hello", "Flux", "Stream")
            .map(data -> {
                if (data.equals("Flux")) {
                    throw new RuntimeException("Simulated error");
                }
                return "Transformed: " + data;
            })
            .doOnNext(data -> System.out.println("[Stream] Next: " + data))
            .doOnError(error -> System.err.println("[Stream] Error: " + error.getMessage()))
            .doOnComplete(() -> System.out.println("[Stream] Complete"))
            .doFinally(signalType -> handleSignal(signalType));
    }

    private void handleSignal(SignalType signalType) {
        System.out.println("[Stream] Finally: " + signalType);
        // 根据信号类型实现不同的处理逻辑
        switch (signalType) {
            case ON_COMPLETE:
                System.out.println("Handling completion...");
                break;
            case ON_ERROR:
                System.err.println("Handling error...");
                break;
            case CANCEL:
                System.out.println("Handling cancellation...");
                break;
            default:
                System.out.println("Handling other signal...");
                break;
        }
    }
}