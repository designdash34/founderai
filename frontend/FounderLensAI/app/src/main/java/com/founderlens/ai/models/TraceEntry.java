package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
public class TraceEntry {
    @SerializedName("id") private String id;
    @SerializedName("type") private String type; // decision, agent_action, conflict, error, recovery
    @SerializedName("agent") private String agent;
    @SerializedName("message") private String message;
    @SerializedName("reasoning") private String reasoning;
    @SerializedName("timestamp") private String timestamp;
    @SerializedName("duration_ms") private long durationMs;
    @SerializedName("state_before") private String stateBefore;
    @SerializedName("state_after") private String stateAfter;
    public TraceEntry() {}
    public TraceEntry(String id, String type, String agent, String message, String reasoning, String timestamp) {
        this.id = id; this.type = type; this.agent = agent;
        this.message = message; this.reasoning = reasoning; this.timestamp = timestamp;
    }
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getAgent() { return agent; } public void setAgent(String agent) { this.agent = agent; }
    public String getMessage() { return message; } public void setMessage(String message) { this.message = message; }
    public String getReasoning() { return reasoning; } public void setReasoning(String r) { this.reasoning = r; }
    public String getTimestamp() { return timestamp; } public void setTimestamp(String t) { this.timestamp = t; }
    public long getDurationMs() { return durationMs; } public void setDurationMs(long d) { this.durationMs = d; }
    public String getStateBefore() { return stateBefore; } public void setStateBefore(String s) { this.stateBefore = s; }
    public String getStateAfter() { return stateAfter; } public void setStateAfter(String s) { this.stateAfter = s; }
}
