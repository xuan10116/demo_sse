package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class ChatStreamController {

    private final ConcurrentHashMap<String, AtomicBoolean> clientPausedMap;

    public ChatStreamController() {
        this.clientPausedMap = new ConcurrentHashMap<>(); // 初始化客户状态存储
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> flux(@RequestParam String clientId) {
        return Flux.interval(Duration.ofSeconds(1))
                   .filter(sequence -> !getClientStatus(clientId)) // 调用提取的公共方法
                   .map(sequence -> "Chat Message - " + sequence);
    }

    @GetMapping("/chat/pause")
    public String pause(@RequestParam String clientId) {
        updateClientStatus(clientId, true); // 调用提取的公共方法
        return "Message stream paused for client: " + clientId;
    }

    @GetMapping("/chat/resume")
    public String resume(@RequestParam String clientId) {
        updateClientStatus(clientId, false); // 调用提取的公共方法
        return "Message stream resumed for client: " + clientId;
    }

    @GetMapping("/chat/clients")
    public String getAllClientsStatus() {
        StringBuilder status = new StringBuilder();
        clientPausedMap.forEach((clientId, paused) ->
            status.append("Client ID: ").append(clientId)
                  .append(", Status: ").append(paused.get() ? "Paused" : "Active")
                  .append("\n"));
        return status.toString();
    }

    // 提取公共方法：获取客户状态
    private boolean getClientStatus(String clientId) {
        return clientPausedMap.getOrDefault(clientId, new AtomicBoolean(false)).get();
    }

    // 提取公共方法：更新客户状态
    private void updateClientStatus(String clientId, boolean isPaused) {
        clientPausedMap.put(clientId, new AtomicBoolean(isPaused));
    }
}