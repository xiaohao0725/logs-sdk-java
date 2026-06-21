package com.codexs.logs.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;

/** 日志条目 — 与 Go/Node.js/Python SDK 完全对齐 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry {
    private static final ObjectMapper mapper = new ObjectMapper();

    public String uuid, timestamp, projectSlug, environment, serviceName, host, processId;
    public String method, scheme, fullUrl, hostHeader, path, queryString, origin;
    public String requestHeaders, requestBody, contentType;
    public int requestBodySize, statusCode, responseBodySize;
    public String responseHeaders, responseBody;
    public String clientIp, clientIpChain, clientType, userAgent;
    public int clientPort;
    public String clientCountry, clientProvince, clientCity, clientIsp;
    public String deviceType, browser, browserVersion, osName, osVersion;
    public String tlsVersion, tlsCipher, proto, apiVersion, referer;
    public int upstreamStatus;
    public String latencyBreakdown, requestId;
    public String traceId, spanId, parentSpanId, userId, sessionId;
    public boolean isError;
    public String errorMessage, errorType, errorStack, panicLocation;
    public String rootCause, faultLevel, suggestedAction, analysisStatus;
    public long uid, durationMs;
    public String tags;

    public LogEntry() {}

    public String toJson() {
        try { return mapper.writeValueAsString(this); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
