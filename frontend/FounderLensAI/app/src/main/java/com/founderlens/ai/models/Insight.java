package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
public class Insight {
    @SerializedName("id") private String id;
    @SerializedName("type") private String type; // risk, opportunity, trend, action
    @SerializedName("title") private String title;
    @SerializedName("description") private String description;
    @SerializedName("confidence") private int confidence; // 0-100
    @SerializedName("impact") private String impact; // high, medium, low
    @SerializedName("agent") private String agent;
    @SerializedName("timestamp") private String timestamp;
    @SerializedName("tags") private java.util.List<String> tags;
    public Insight() {}
    public Insight(String id, String type, String title, String description, int confidence, String impact, String agent) {
        this.id = id; this.type = type; this.title = title;
        this.description = description; this.confidence = confidence;
        this.impact = impact; this.agent = agent;
    }
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public int getConfidence() { return confidence; } public void setConfidence(int c) { this.confidence = c; }
    public String getImpact() { return impact; } public void setImpact(String impact) { this.impact = impact; }
    public String getAgent() { return agent; } public void setAgent(String agent) { this.agent = agent; }
    public String getTimestamp() { return timestamp; } public void setTimestamp(String t) { this.timestamp = t; }
    public java.util.List<String> getTags() { return tags; } public void setTags(java.util.List<String> tags) { this.tags = tags; }
}
