package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
public class DashboardStats {
    @SerializedName("active_agents") private int activeAgents;
    @SerializedName("analyses_run") private int analysesRun;
    @SerializedName("insights_found") private int insightsFound;
    @SerializedName("success_rate") private int successRate;
    @SerializedName("recent_activity") private List<ActivityItem> recentActivity;
    @SerializedName("agent_statuses") private Map<String, String> agentStatuses;
    public DashboardStats() {}
    public int getActiveAgents() { return activeAgents; } public void setActiveAgents(int a) { this.activeAgents = a; }
    public int getAnalysesRun() { return analysesRun; } public void setAnalysesRun(int a) { this.analysesRun = a; }
    public int getInsightsFound() { return insightsFound; } public void setInsightsFound(int i) { this.insightsFound = i; }
    public int getSuccessRate() { return successRate; } public void setSuccessRate(int s) { this.successRate = s; }
    public List<ActivityItem> getRecentActivity() { return recentActivity; } public void setRecentActivity(List<ActivityItem> r) { this.recentActivity = r; }
    public Map<String, String> getAgentStatuses() { return agentStatuses; } public void setAgentStatuses(Map<String, String> a) { this.agentStatuses = a; }
}
