# Log Management Platform Java SDK

[中文文档](./README.md) | [Maven Central](https://central.sonatype.com/artifact/com.codexs/logs-sdk-java)

`logs-sdk-java` provides a Jakarta Servlet 6 Filter (Spring Boot 3 compatible) with one-config integration for automatic HTTP request log collection.

## Features

- ✅ **One-config**: Register `LoggingFilter` bean
- ✅ **60+ fields**: request/response headers & body, client device info, TLS version
- ✅ **Auto-detect**: client type (Web/MiniProgram/App/Server), request origin
- ✅ **Error capture**: HTTP 5xx + Java exception stack trace
- ✅ **UUID v7**: 32-char hex without hyphens
- ✅ **Sanitization**: Authorization/Cookie auto-masking
- ✅ **Async**: ring buffer + ScheduledExecutor, non-blocking
- ✅ **Offline cache**: local file cache on failure, auto-retransmit
- ✅ **Spring Boot 3**: Jakarta Servlet 6 + JDK 17+

## Installation

```xml
<dependency>
  <groupId>com.codexs</groupId>
  <artifactId>logs-sdk-java</artifactId>
  <version>0.3.0</version>
</dependency>
```

JDK 17+, Jakarta Servlet 6.1+.

## Quick Start

```java
@Configuration
public class LogConfig {
    @Bean
    public LogSDK logSDK() {
        LogConfig cfg = new LogConfig();
        cfg.endpoint = "https://api.logs.codexs.cn/api/v1/ingest/logs";
        cfg.apiKey = "clog_pk_xxx";
        cfg.projectSlug = "my-project";
        return new LogSDK(cfg);
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter(LogSDK sdk) {
        FilterRegistrationBean<LoggingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new LoggingFilter(sdk));
        reg.addUrlPatterns("/*");
        return reg;
    }
}
```

## Configuration / Collected Fields / Architecture

See [Go SDK README_EN.md](https://github.com/xiaohao0725/logs-sdk-go/blob/main/README_EN.md) — all SDKs share identical field definitions and architecture.

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| v0.3.0 | 2026-06-21 | Initial release: Servlet Filter, async buffer, retry, offline cache |

## License

UNLICENSED — Internal use
