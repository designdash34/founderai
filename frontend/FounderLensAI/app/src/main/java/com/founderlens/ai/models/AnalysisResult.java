package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class AnalysisResult {
    @SerializedName("id") private String id;
    @SerializedName("status") private String status;
    @SerializedName("timestamp") private String timestamp;
    @SerializedName("agents_involved") private int agentsInvolved;
    @SerializedName("insights") private List<Insight> insights;
    @SerializedName("execution_time_ms") private long executionTimeMs;
    @SerializedName("trace_id") private String traceId;
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public String getTimestamp() { return timestamp; } public void setTimestamp(String t) { this.timestamp = t; }
    public int getAgentsInvolved() { return agentsInvolved; } public void setAgentsInvolved(int a) { this.agentsInvolved = a; }
    public List<Insight> getInsights() { return insights; } public void setInsights(List<Insight> i) { this.insights = i; }
    public long getExecutionTimeMs() { return executionTimeMs; } public void setExecutionTimeMs(long e) { this.executionTimeMs = e; }
    public String getTraceId() { return traceId; } public void setTraceId(String t) { this.traceId = t; }
}
