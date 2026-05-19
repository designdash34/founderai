package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
public class HealthResponse {
    @SerializedName("status") private String status;
    @SerializedName("version") private String version;
    @SerializedName("model") private String model;
    @SerializedName("gemini") private boolean gemini;
    @SerializedName("supabase") private boolean supabase;
    @SerializedName("timestamp") private String timestamp;
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public String getVersion() { return version; } public void setVersion(String v) { this.version = v; }
    public String getModel() { return model; } public void setModel(String m) { this.model = m; }
    public boolean isGemini() { return gemini; } public void setGemini(boolean g) { this.gemini = g; }
    public boolean isSupabase() { return supabase; } public void setSupabase(boolean s) { this.supabase = s; }
    public String getTimestamp() { return timestamp; } public void setTimestamp(String t) { this.timestamp = t; }
}
