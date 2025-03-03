package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // 修改 flux 方法为 stream 方法，使用 SseEmitter 实现 SSE
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String clientId) {
        SseEmitter emitter = new SseEmitter();
        // 确保 clientId 存在于 clientPausedMap 中，默认状态为未暂停
        clientPausedMap.putIfAbsent(clientId, new AtomicBoolean(false));
        // 读取固定文件内容
        String fileContent = readFileContent("static/twgx.txt");

        new Thread(() -> {
            try {
                for (char c : fileContent.toCharArray()) {
                    if (!clientPausedMap.containsKey(clientId)) { // 检查客户端是否已终止
                        emitter.complete();
                        return;
                    }
                    if (!getClientStatus(clientId)) {
                        String json = processCharacter(c); // 调用提取的处理方法
                        if (json != null) { // 过滤掉无效的 JSON 数据
                            emitter.send(SseEmitter.event().data(json));
                        }
                    }
                    Thread.sleep(100);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
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

    // 提取的字符处理方法
    private String processCharacter(int c) {
        try {
            // 校验字符是否为空白字符
            char character = (char) c;
            if (Character.isWhitespace(character)) {
                return null; // 跳过空白字符
            }
            // 构造 JSON 格式的字符串
            String content = String.valueOf(character);
            return "{\"choices\": [{\"delta\": {\"role\": \"assistant\", \"content\": \"" + content + "\"}}]}";
        } catch (Exception e) {
            // 捕获异常并记录日志
            System.err.println("Error processing character: " + c + ", error: " + e.getMessage());
            return null; // 跳过异常数据
        }
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
            // 使用类加载器获取资源路径
            URL resourceUrl = getClass().getClassLoader().getResource(filePath);
            if (resourceUrl == null) {
                throw new RuntimeException("File not found: " + filePath);
            }
            Path path = Paths.get(resourceUrl.toURI());
            return new String(Files.readAllBytes(path));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}