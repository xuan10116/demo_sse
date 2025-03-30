package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@RestController
@RequestMapping("/mono-demo")
public class MonoWebDemoController {

    @GetMapping("/normal")
    public String monoNormalDemo() {
        Mono<String> mono = createMono();
        mono.subscribe(
            data -> System.out.println("[Subscriber] Received: " + data),
            error -> System.err.println("[Subscriber] Error: " + error.getMessage()),
            () -> System.out.println("[Subscriber] Completed")
        );

        return "Check the console for normal Mono output!";
    }

    @GetMapping("/error")
    public String monoErrorDemo() {
        Mono<String> errorMono = createMonoWithError();
        errorMono.subscribe(
            data -> System.out.println("[Subscriber] Received: " + data),
            error -> System.err.println("[Subscriber] Error: " + error.getMessage()),
            () -> System.out.println("[Subscriber] Completed")
        );

        return "Check the console for error Mono output!";
    }

    @GetMapping("/cancel")
    public String monoCancelDemo() {
        Mono<String> cancelMono = createMono();
        cancelMono.subscribe(
            data -> {
                System.out.println("[Subscriber] Received: " + data);
                if (data.equals("Mono")) {
                    throw new RuntimeException("Simulated cancellation");
                }
            },
            error -> System.err.println("[Subscriber] Error: " + error.getMessage()),
            () -> System.out.println("[Subscriber] Completed")
        ).dispose();

        return "Check the console for cancel Mono output!";
    }

    private Mono<String> createMono() {
        return Mono.just("Hello Mono")
            .map(data -> "Transformed: " + data)
            .doOnNext(data -> System.out.println("[Mono] Next: " + data))
            .doOnError(error -> System.err.println("[Mono] Error: " + error.getMessage()))
            .doOnSuccess(data -> System.out.println("[Mono] Success: " + data))
            .doOnTerminate(() -> System.out.println("[Mono] Terminate"))
            .doFinally(signalType -> handleSignal(signalType));
    }

    private Mono<String> createMonoWithError() {
        return Mono.just("Hello Mono")
            .map(data -> {
                if (data.equals("Mono")) {
                    throw new RuntimeException("Simulated error");
                }
                return "Transformed: " + data;
            })
            .doOnNext(data -> System.out.println("[Mono] Next: " + data))
            .doOnError(error -> System.err.println("[Mono] Error: " + error.getMessage()))
            .doOnSuccess(data -> System.out.println("[Mono] Success: " + data))
            .doOnTerminate(() -> System.out.println("[Mono] Terminate"))
            .doFinally(signalType -> handleSignal(signalType));
    }

    private void handleSignal(SignalType signalType) {
        System.out.println("[Mono] Finally: " + signalType);
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