package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
public class ActivityItem {
    @SerializedName("id") private String id;
    @SerializedName("title") private String agent;
    @SerializedName("description") private String message;
    @SerializedName("timestamp") private String timestamp;
    @SerializedName("type") private String type;
    public ActivityItem() {}
    public ActivityItem(String id, String agent, String message, String timestamp, String type) {
        this.id = id; this.agent = agent; this.message = message;
        this.timestamp = timestamp; this.type = type;
    }
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getAgent() { return agent; } public void setAgent(String agent) { this.agent = agent; }
    public String getMessage() { return message; } public void setMessage(String message) { this.message = message; }
    public String getTimestamp() { return timestamp; } public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
}
