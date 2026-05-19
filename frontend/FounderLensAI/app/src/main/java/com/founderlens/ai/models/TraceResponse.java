package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class TraceResponse {
    @SerializedName("entries") private List<TraceEntry> entries;
    @SerializedName("total_steps") private int totalSteps;
    @SerializedName("duration") private String duration;
    @SerializedName("agents_involved") private int agentsInvolved;
    @SerializedName("analysis_id") private String analysisId;
    public List<TraceEntry> getEntries() { return entries; } public void setEntries(List<TraceEntry> e) { this.entries = e; }
    public int getTotalSteps() { return totalSteps; } public void setTotalSteps(int t) { this.totalSteps = t; }
    public String getDuration() { return duration; } public void setDuration(String d) { this.duration = d; }
    public int getAgentsInvolved() { return agentsInvolved; } public void setAgentsInvolved(int a) { this.agentsInvolved = a; }
    public String getAnalysisId() { return analysisId; } public void setAnalysisId(String a) { this.analysisId = a; }
}
