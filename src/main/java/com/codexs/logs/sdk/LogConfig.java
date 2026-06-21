package com.codexs.logs.sdk;

/** SDK 配置 */
public class LogConfig {
    public String endpoint, apiKey, apiSecret, projectSlug;
    public String environment = "production", serviceName = "";
    public int bufferSize = 1000, flushInterval = 5, maxRetries = 3;
    public int maxBodySize = 4096, maxStackSize = 8192;
}
