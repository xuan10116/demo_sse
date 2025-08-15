package com.example.demo.controller.sharedemo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

/**
 * @author lvxuan
 * @version 1.0
 * @description: TODO
 */
public class FlatmapDemo {
	public static void main(String[] args) throws InterruptedException {
		final Flux<String> stringFlux = Flux.just("A", "B", "C")
				// flatMap 并行处理，不保证顺序
				.flatMap(s ->  Mono.just(s + "!").delayElement(Duration.ofMillis(new Random().nextInt(100)))
				// concatMap 内部顺序订阅每个 inner publisher，按顺序合并输出。
//				.concatMap(s ->  Mono.just(s + "!").delayElement(Duration.ofMillis(new Random().nextInt(100)))
				);

		stringFlux.subscribe(result -> System.out.println("Received: " + result));
		Thread.sleep(1000);
	}
}
