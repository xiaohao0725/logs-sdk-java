package io.github.xiaohao0725.logs.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfraLogEntry {
    private static final ObjectMapper mapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    public String uuid, timestamp;
    @JsonProperty("project_slug") public String projectSlug;
    @JsonProperty("source_type") public String sourceType;
    @JsonProperty("source_name") public String sourceName;
    public String host;
    public String level;
    public String message;
    public String metadata;
    @JsonProperty("trace_id") public String traceId;
    @JsonProperty("related_api_uuid") public String relatedApiUuid;
    @JsonProperty("is_error") public boolean isError;
    @JsonProperty("error_detail") public String errorDetail;

    public InfraLogEntry() {}

    public String toJson() {
        try { return mapper.writeValueAsString(this); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}