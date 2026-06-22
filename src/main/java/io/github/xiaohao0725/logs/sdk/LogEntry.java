package io.github.xiaohao0725.logs.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.time.Instant;
import java.util.*;

/** 日志条目 — 与 Go/Node.js/Python SDK 完全对齐。
 *  Java 字段使用 camelCase，序列化为 JSON 时自动转换为 snake_case 以匹配服务端 API。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {
    private static final ObjectMapper mapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    public String uuid, timestamp;
    @JsonProperty("project_slug")    public String projectSlug;
    public String environment;
    @JsonProperty("service_name")    public String serviceName;
    public String host;
    @JsonProperty("process_id")      public String processId;
    public String method, scheme;
    @JsonProperty("full_url")        public String fullUrl;
    @JsonProperty("host_header")     public String hostHeader;
    public String path;
    @JsonProperty("query_string")    public String queryString;
    public String origin;
    @JsonProperty("request_headers") public String requestHeaders;
    @JsonProperty("request_body")    public String requestBody;
    @JsonProperty("content_type")    public String contentType;
    @JsonProperty("request_body_size")  public int requestBodySize;
    @JsonProperty("status_code")        public int statusCode;
    @JsonProperty("response_body_size") public int responseBodySize;
    @JsonProperty("response_headers")   public String responseHeaders;
    @JsonProperty("response_body")      public String responseBody;
    @JsonProperty("client_ip")       public String clientIp;
    @JsonProperty("client_ip_chain") public String clientIpChain;
    @JsonProperty("client_type")     public String clientType;
    @JsonProperty("user_agent")      public String userAgent;
    @JsonProperty("client_port")     public int clientPort;
    @JsonProperty("client_country")  public String clientCountry;
    @JsonProperty("client_province") public String clientProvince;
    @JsonProperty("client_city")     public String clientCity;
    @JsonProperty("client_isp")      public String clientIsp;
    @JsonProperty("device_type")     public String deviceType;
    public String browser;
    @JsonProperty("browser_version") public String browserVersion;
    @JsonProperty("os_name")         public String osName;
    @JsonProperty("os_version")      public String osVersion;
    @JsonProperty("tls_version")     public String tlsVersion;
    @JsonProperty("tls_cipher")      public String tlsCipher;
    public String proto;
    @JsonProperty("api_version")     public String apiVersion;
    public String referer;
    @JsonProperty("upstream_status")    public int upstreamStatus;
    @JsonProperty("latency_breakdown")  public String latencyBreakdown;
    @JsonProperty("request_id")         public String requestId;
    @JsonProperty("trace_id")        public String traceId;
    @JsonProperty("span_id")         public String spanId;
    @JsonProperty("parent_span_id")  public String parentSpanId;
    @JsonProperty("user_id")         public String userId;
    @JsonProperty("session_id")      public String sessionId;
    @JsonProperty("is_error")        public boolean isError;
    @JsonProperty("error_message")   public String errorMessage;
    @JsonProperty("error_type")      public String errorType;
    @JsonProperty("error_stack")     public String errorStack;
    @JsonProperty("panic_location")  public String panicLocation;
    @JsonProperty("root_cause")      public String rootCause;
    @JsonProperty("fault_level")     public String faultLevel;
    @JsonProperty("suggested_action") public String suggestedAction;
    @JsonProperty("analysis_status") public String analysisStatus;
    public long uid;
    @JsonProperty("duration_ms")     public long durationMs;
    public String tags;

    public LogEntry() {}

    public String toJson() {
        try { return mapper.writeValueAsString(this); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
