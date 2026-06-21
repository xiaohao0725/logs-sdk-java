# 日志管理平台 Java SDK

[English Documentation](https://github.com/xiaohao0725/logs-sdk-java/blob/main/README_EN.md) | [Maven Central](https://central.sonatype.com/artifact/com.codexs/logs-sdk-java)

`logs-sdk-java` 是日志管理平台的 Java SDK，提供 Jakarta Servlet 6 Filter（兼容 Spring Boot 3），一行配置即可自动采集 HTTP 请求的完整日志，异步批量上报。

## 功能特性

- ✅ **一行配置接入**：注册 `LoggingFilter` 即可
- ✅ **完整采集**：60+ 字段——请求/响应头体、客户端信息、设备信息、TLS 版本
- ✅ **自动识别**：客户端类型（Web / 小程序 / App / 服务端）、请求来源
- ✅ **异常捕获**：HTTP 5xx + Java 异常堆栈自动采集
- ✅ **UUID v7**：32 位十六进制无连字符
- ✅ **敏感脱敏**：Authorization / Cookie 自动脱敏
- ✅ **异步非阻塞**：环形缓冲区 + 后台定时刷新
- ✅ **离线缓存**：断网本地存储，恢复自动重传
- ✅ **优雅关闭**：`close()` 确保缓冲日志全部上报
- ✅ **Spring Boot 3 兼容**：Jakarta Servlet 6 + JDK 17+

## 安装

### Maven

```xml
<dependency>
  <groupId>com.codexs</groupId>
  <artifactId>logs-sdk-java</artifactId>
  <version>0.3.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.codexs:logs-sdk-java:0.3.0'
```

要求 JDK 17+，Jakarta Servlet 6.1+。

## 快速开始

### Spring Boot 3

```java
import com.codexs.logs.sdk.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfig {

    @Bean
    public LogSDK logSDK() {
        LogConfig cfg = new LogConfig();
        cfg.endpoint = "https://api.logs.codexs.cn/api/v1/ingest/logs";
        cfg.apiKey = "clog_pk_xxx";
        cfg.apiSecret = "clog_sk_xxx";
        cfg.projectSlug = "my-project";
        cfg.environment = "production";
        return new LogSDK(cfg);
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter(LogSDK sdk) {
        FilterRegistrationBean<LoggingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new LoggingFilter(sdk));
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }
}
```

### 传统 Servlet (web.xml)

```xml
<filter>
    <filter-name>loggingFilter</filter-name>
    <filter-class>com.codexs.logs.sdk.LoggingFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>loggingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

```java
// 在 ServletContextListener 中初始化
LogConfig cfg = new LogConfig();
cfg.endpoint = "https://api.logs.codexs.cn/api/v1/ingest/logs";
cfg.apiKey = "clog_pk_xxx";
cfg.projectSlug = "my-project";
LogSDK sdk = new LogSDK(cfg);
// 注册到全局上下文
```

## 配置参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `endpoint` | `String` | **必填** | 日志上报地址 |
| `apiKey` | `String` | **必填** | SDK 认证密钥（公钥） |
| `apiSecret` | `String` | **必填** | SDK 认证密钥（私钥） |
| `projectSlug` | `String` | **必填** | 项目短标识 |
| `environment` | `String` | `"production"` | 运行环境 |
| `serviceName` | `String` | `""` | 微服务名称 |
| `bufferSize` | `int` | `1000` | 缓冲区容量 |
| `flushInterval` | `int` | `5` | 刷新间隔（秒） |
| `maxRetries` | `int` | `3` | 最大重试次数 |
| `maxBodySize` | `int` | `4096` | 请求/响应体最大采集大小 |
| `maxStackSize` | `int` | `8192` | 错误堆栈最大采集大小 |

## 采集字段一览

与 Go/Node.js/Python SDK 完全对齐，详见 [LogEntry.java](./src/main/java/com/codexs/logs/sdk/LogEntry.java)。

## 架构设计

```
HTTP 请求进入
  │
  ├─ ① LoggingFilter.doFilter()
  │     ├─ 生成 UUID v7
  │     ├─ CachedBodyWrapper 缓存请求体
  │     └─ 记录开始时间
  │
  ├─ ② chain.doFilter(wrappedReq, wrappedRes)  # 业务处理
  │
  ├─ ③ ResponseWrapper 捕获响应体 + 状态码
  │     └─ 构建 LogEntry（60+ 字段）
  │
  ├─ ④ buffer.push(entry)  # 非阻塞
  │
  └─ ⑤ ScheduledExecutorService 定时刷新（每 5s）
        └─ 批量 POST 到 Ingestion API → 重试 → 离线缓存
```

## 离线缓存

断网时自动缓存到 `java.io.tmpdir/logs-sdk-offline/`，恢复后自动重传。

## 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| v0.3.0 | 2026-06-21 | 初始版本：Servlet Filter、异步缓冲、重试、离线缓存 |

## License

UNLICENSED — 内部使用
