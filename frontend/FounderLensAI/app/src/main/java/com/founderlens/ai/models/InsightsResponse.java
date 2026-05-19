package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class InsightsResponse {
    @SerializedName("insights") private List<Insight> insights;
    @SerializedName("total") private int total;
    @SerializedName("analysis_id") private String analysisId;
    public List<Insight> getInsights() { return insights; } public void setInsights(List<Insight> i) { this.insights = i; }
    public int getTotal() { return total; } public void setTotal(int t) { this.total = t; }
    public String getAnalysisId() { return analysisId; } public void setAnalysisId(String a) { this.analysisId = a; }
}
