package com.example.demo.controller.sharedemo;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

/**
 * @author lvxuan
 * @version 1.0
 * @description: TODO
 * @date 2025/8/12
 */
public class SubscriberDemo {
	public static void main(String[] args) {
		// 方法1：使用自定义的SampleSubscriber
		System.out.println("=== 使用SampleSubscriber ===");
		SampleSubscriber<String> subscriber = new SampleSubscriber<>();
		Flux.just("a", "b", "c")
				.map(String::toUpperCase)
				.subscribe(subscriber);

		// 等待足够的时间让异步操作完成
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// 方法2：使用带有多个参数的subscribe方法
		System.out.println("\n=== 使用subscribe方法的多个参数 ===");
		Flux.just("x", "y", "z")
				.map(String::toUpperCase)
				.subscribe(
						System.out::println,           // onNext
						Throwable::printStackTrace,    // onError
						() -> System.out.println("Completed") // onComplete
				);

		// 方法3：使用带有Subscription Consumer的subscribe方法
		System.out.println("\n=== 使用Subscription Consumer的subscribe方法 ===");
		Flux.just("1", "2", "3")
				.map(s -> "Number: " + s)
				.subscribe(
						System.out::println,           // onNext
						Throwable::printStackTrace,    // onError
						() -> System.out.println("Completed"), // onComplete
						subscription -> {              // onSubscribe
							System.out.println("Subscribed to stream with manual request");
							subscription.request(10);   // 手动请求10个元素
						}
				);
	}

	/**
	 * 自定义Subscriber，演示如何控制request信号
	 */
	static class SampleSubscriber<T> extends BaseSubscriber<T> {

		@Override
		public void hookOnSubscribe(Subscription subscription) {
			System.out.println("SampleSubscriber: Subscribed");
			request(1); // 初始请求1个元素
		}

		@Override
		public void hookOnNext(T value) {
			System.out.println("SampleSubscriber received: " + value);
			request(1); // 每处理完一个元素后，再请求下一个
		}

		@Override
		public void hookOnError(Throwable throwable) {
			System.err.println("SampleSubscriber error: " + throwable.getMessage());
		}

		@Override
		public void hookOnComplete() {
			System.out.println("SampleSubscriber: Completed");
		}
	}
}