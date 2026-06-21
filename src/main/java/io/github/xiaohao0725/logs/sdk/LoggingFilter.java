package io.github.xiaohao0725.logs.sdk;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.Instant;
import java.util.*;

/** Servlet Filter — 一行代码注册，自动采集所有 HTTP 请求日志 */
public class LoggingFilter implements Filter {
    private final LogSDK sdk;

    public LoggingFilter(LogSDK sdk) { this.sdk = sdk; }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        CachedBodyWrapper wrappedReq = new CachedBodyWrapper(request);
        ResponseWrapper wrappedRes = new ResponseWrapper(response);

        LogEntry entry = new LogEntry();
        entry.uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        entry.requestId = entry.uuid.substring(0, 8);
        entry.traceId = entry.uuid;
        entry.spanId = entry.uuid;
        long start = System.currentTimeMillis();

        // 请求信息
        entry.method = request.getMethod();
        entry.scheme = request.getScheme();
        entry.fullUrl = request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        entry.hostHeader = request.getHeader("Host");
        entry.path = request.getRequestURI();
        entry.queryString = request.getQueryString() != null ? request.getQueryString() : "";
        entry.contentType = request.getContentType();
        entry.userAgent = request.getHeader("User-Agent");
        entry.clientIp = request.getHeader("X-Forwarded-For") != null ? request.getHeader("X-Forwarded-For").split(",")[0].trim() : request.getRemoteAddr();
        entry.clientIpChain = request.getHeader("X-Forwarded-For");
        entry.clientType = detectClientType(request);
        entry.origin = detectOrigin(request);
        entry.requestHeaders = sanitizeHeaders(request);
        entry.requestBodySize = wrappedReq.getBody().length;
        entry.requestBody = new String(wrappedReq.getBody(), 0, Math.min(wrappedReq.getBody().length, sdk.config.maxBodySize));
        entry.referer = request.getHeader("Referer") != null ? request.getHeader("Referer") : "";
        entry.traceId = request.getHeader("X-Trace-ID") != null ? request.getHeader("X-Trace-ID") : entry.uuid;
        entry.parentSpanId = request.getHeader("X-Parent-Span-ID") != null ? request.getHeader("X-Parent-Span-ID") : "";
        entry.userId = request.getHeader("X-User-ID") != null ? request.getHeader("X-User-ID") : "";
        entry.sessionId = request.getSession(false) != null ? request.getSession(false).getId() : "";
        entry.proto = request.getProtocol();

        try {
            chain.doFilter(wrappedReq, wrappedRes);
        } catch (Exception e) {
            entry.isError = true;
            entry.errorType = "panic";
            entry.errorMessage = e.getMessage();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            entry.errorStack = sw.toString();
            throw e;
        } finally {
            entry.durationMs = System.currentTimeMillis() - start;
            entry.statusCode = wrappedRes.getStatus();
            entry.responseHeaders = sanitizeHeaders(wrappedRes);
            entry.responseBodySize = wrappedRes.getBody().length;
            entry.responseBody = new String(wrappedRes.getBody(), 0, Math.min(wrappedRes.getBody().length, sdk.config.maxBodySize));
            if (entry.statusCode >= 500) { entry.isError = true; entry.errorType = "http_error"; }
            sdk.send(entry);
        }
    }

    private String detectClientType(HttpServletRequest req) {
        String ct = req.getHeader("X-Client-Type");
        if (ct != null) return ct;
        String ua = req.getHeader("User-Agent") != null ? req.getHeader("User-Agent").toLowerCase() : "";
        if (ua.contains("micromessenger") || ua.contains("miniprogram")) return "miniprogram";
        if (req.getHeader("X-Caller-Service") != null) return "server";
        if ((req.getHeader("Referer") != null || req.getHeader("Origin") != null) &&
            (ua.contains("mozilla") || ua.contains("chrome"))) return "web";
        return "other";
    }

    private String detectOrigin(HttpServletRequest req) {
        switch (detectClientType(req)) {
            case "web": return req.getHeader("Referer") != null ? req.getHeader("Referer") : req.getHeader("Origin");
            case "miniprogram": return "miniprogram:" + req.getHeader("X-MiniProgram-AppId") + req.getHeader("X-MiniProgram-Path");
            case "app": return "app:" + req.getHeader("X-App-Name") + "/" + req.getHeader("X-App-Version");
            case "server": return "server:" + req.getHeader("X-Caller-Service") + "/" + req.getHeader("X-Caller-Version");
            default: return "unknown";
        }
    }

    private String sanitizeHeaders(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder("{");
        Enumeration<String> names = req.getHeaderNames();
        boolean first = true;
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String val = req.getHeader(name);
            if (name.equalsIgnoreCase("authorization") || name.equalsIgnoreCase("cookie")) {
                val = val.length() > 20 ? val.substring(0, 15) + "..." : "***";
            }
            if (!first) sb.append(",");
            sb.append("\"").append(name).append("\":\"").append(val.replace("\"", "\\\"")).append("\"");
            first = false;
        }
        return sb.append("}").toString();
    }

    private String sanitizeHeaders(HttpServletResponse resp) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (String name : resp.getHeaderNames()) {
            if (!first) sb.append(",");
            sb.append("\"").append(name).append("\":\"").append(resp.getHeader(name)).append("\"");
            first = false;
        }
        return sb.append("}").toString();
    }

    /** 缓存请求体 */
    static class CachedBodyWrapper extends HttpServletRequestWrapper {
        private final byte[] body;
        public CachedBodyWrapper(HttpServletRequest req) throws IOException {
            super(req);
            this.body = req.getInputStream().readAllBytes();
        }
        public byte[] getBody() { return body; }
        @Override public ServletInputStream getInputStream() { return new ByteArrayServletInputStream(body); }
        @Override public BufferedReader getReader() { return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body))); }
    }

    /** 缓存响应体 */
    static class ResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private int status = 200;
        public ResponseWrapper(HttpServletResponse resp) { super(resp); }
        public byte[] getBody() { return body.toByteArray(); }
        public int getStatus() { return status; }
        @Override public void setStatus(int status) { this.status = status; super.setStatus(status); }
        @Override public ServletOutputStream getOutputStream() {
            try { return new TeeServletOutputStream(super.getOutputStream(), body); }
            catch (IOException e) { throw new RuntimeException(e); }
        }
        @Override public PrintWriter getWriter() { return new PrintWriter(new OutputStreamWriter(getOutputStream())); }
    }

    static class ByteArrayServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream bais;
        public ByteArrayServletInputStream(byte[] data) { this.bais = new ByteArrayInputStream(data); }
        @Override public int read() { return bais.read(); }
        @Override public boolean isFinished() { return bais.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(jakarta.servlet.ReadListener l) {}
    }

    static class TeeServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream original;
        private final ByteArrayOutputStream copy;
        public TeeServletOutputStream(ServletOutputStream original, ByteArrayOutputStream copy) { this.original = original; this.copy = copy; }
        @Override public void write(int b) throws IOException { original.write(b); copy.write(b); }
        @Override public boolean isReady() { return true; }
        @Override public void setWriteListener(jakarta.servlet.WriteListener l) {}
    }
}
