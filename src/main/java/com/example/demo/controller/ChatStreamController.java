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

    @GetMapping("/chat/stop")
    public String stop(@RequestParam String clientId) {
        clientPausedMap.remove(clientId); // 移除客户端状态
        return "Message stream stopped for client: " + clientId;
    }

    // 修改 flux 方法，增加对已移除客户端的检查
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> flux(@RequestParam String clientId) {
        // 确保 clientId 存在于 clientPausedMap 中，默认状态为未暂停
        clientPausedMap.putIfAbsent(clientId, new AtomicBoolean(false));
        // 读取固定文件内容
        String fileContent = readFileContent("static/twgx.txt");

        return Flux.fromIterable(fileContent.chars()
                    .mapToObj(c -> String.valueOf((char) c))
                    .toList())
            .delayElements(Duration.ofMillis(100))
            .filter(sequence -> {
                if (!clientPausedMap.containsKey(clientId)) { // 检查客户端是否已终止
                    throw new RuntimeException("Client stream terminated: " + clientId);
                }
                return !getClientStatus(clientId);
            });
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

    // 新增方法：读取文件内容
    private String readFileContent(String filePath) {
        try {
            // 使用类加载器读取资源文件
            return new String(getClass().getClassLoader().getResourceAsStream(filePath).readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}