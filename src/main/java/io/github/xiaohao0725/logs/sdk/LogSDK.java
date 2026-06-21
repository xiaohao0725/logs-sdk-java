package io.github.xiaohao0725.logs.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/** 核心客户端 — 缓冲管理、定时刷新、HTTP 上报、离线缓存 */
public class LogSDK {
    final LogConfig config;
    private final RingBuffer buffer;
    private final OfflineCache offlineCache;
    private final ScheduledExecutorService scheduler;
    private final HttpClient httpClient;
    private final String hostname, pid;
    private volatile boolean closed = false;
    private static final ObjectMapper mapper = new ObjectMapper();

    public LogSDK(LogConfig config) {
        this.config = config;
        this.hostname = getHostname();
        this.pid = getPid();
        this.offlineCache = new OfflineCache();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.buffer = new RingBuffer(config.bufferSize, this::flushEntries);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "logs-sdk-flush");
            t.setDaemon(true); return t;
        });
        this.scheduler.scheduleAtFixedRate(() -> {
            List<LogEntry> entries = buffer.flush();
            if (!entries.isEmpty()) flushEntries(entries);
        }, config.flushInterval, config.flushInterval, TimeUnit.SECONDS);
    }

    /** 异步发送一条日志 */
    public void send(LogEntry entry) {
        if (closed) return;
        entry.host = hostname;
        entry.processId = pid;
        entry.environment = config.environment;
        entry.projectSlug = config.projectSlug;
        entry.serviceName = config.serviceName;
        entry.timestamp = java.time.Instant.now().toString();
        buffer.push(entry);
    }

    /** 优雅关闭 */
    public void close() {
        closed = true;
        scheduler.shutdown();
        List<LogEntry> remaining = buffer.flush();
        if (!remaining.isEmpty()) {
            try { sendBatch(remaining); }
            catch (Exception e) { offlineCache.save(remaining); }
        }
        offlineCache.flushAll(this::sendBatch);
    }

    private void flushEntries(List<LogEntry> entries) {
        for (int i = 0; i <= config.maxRetries; i++) {
            try {
                sendBatch(entries);
                return;
            } catch (Exception e) {
                if (i == config.maxRetries) {
                    System.err.println("[logs-sdk] 上报失败(重试" + config.maxRetries + "次): " + e.getMessage());
                    offlineCache.save(entries);
                } else {
                    try { Thread.sleep(500L * (1 << i)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                }
            }
        }
    }

    private void sendBatch(List<LogEntry> entries) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("logs", entries);
        String json = mapper.writeValueAsString(body);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.endpoint))
                .header("Content-Type", "application/json")
                .header("X-API-Key", config.apiKey)
                .header("X-SDK-Type", "java")
                .header("X-SDK-Version", "0.3.0")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200 && resp.statusCode() != 201) {
            throw new Exception("服务端返回 " + resp.statusCode());
        }
    }

    private static String getHostname() {
        try { return InetAddress.getLocalHost().getHostName(); } catch (Exception e) { return ""; }
    }

    private static String getPid() {
        return ProcessHandle.current().pid() + "";
    }
}
