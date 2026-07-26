package io.github.xiaohao0725.logs.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 服务端日志上报同步响应体。
 * SDK 的 sendBatch 解析服务端 JSON 响应后返回此对象，
 * 包含批次追踪信息（batch_id）和 UUID 列表。
 */
public class IngestResponse {
    /** 接收到的日志数量 */
    @JsonProperty("received")
    public int received;

    /** 服务端确认收到的日志 UUID 列表 */
    @JsonProperty("uuids")
    public List<String> uuids;

    /** 批次追踪 ID，用于查询处理状态或匹配 Webhook 回调 */
    @JsonProperty("batch_id")
    public String batchId;

    public IngestResponse() {}

    public IngestResponse(int received, List<String> uuids, String batchId) {
        this.received = received;
        this.uuids = uuids;
        this.batchId = batchId;
    }
}
