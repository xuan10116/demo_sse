package com.example.demo.controller.sharedemo;

import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author lvxuan
 * @version 1.0
 * @description: TODO
 */
public class SimpleChainDemo {
	public static void main(String[] args) {
//		final Stream<Integer> integerStream = Stream.of(1, 2, 3).map(value -> value * 10).filter(value -> value > 15);
//		final List<Integer> collect = integerStream.collect(Collectors.toList());
//		System.out.println(collect);

//		Flux<Integer> simpleFlux = Flux.just(1, 2, 3).delayElements(Duration.ofSeconds(1)).map(value -> value * 10).filter(value -> value > 15);
//		simpleFlux.subscribe(System.out::println);

		Flux<Integer> sourceFlux = Flux.just(1, 2, 3);
		Flux<Integer> mappedFlux = sourceFlux.map(value -> value * 10);
		Flux<Integer> filteredFlux = mappedFlux.filter(value -> value > 15);

		filteredFlux.subscribe(
				data -> {
					System.out.printf("订阅者接收到数据: %d\n", data);
				},
				error -> {},
				() -> {
					System.out.println("订阅者接收到完成信号\n");
				}
		);
	}
}
