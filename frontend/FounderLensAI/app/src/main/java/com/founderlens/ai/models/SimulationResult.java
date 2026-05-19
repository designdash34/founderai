package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class SimulationResult {
    @SerializedName("revenue_impact") private String revenueImpact;
    @SerializedName("roi") private String roi;
    @SerializedName("growth_rate") private String growthRate;
    @SerializedName("kpi_revenue") private String kpiRevenue;
    @SerializedName("kpi_customers") private String kpiCustomers;
    @SerializedName("kpi_market_share") private String kpiMarketShare;
    @SerializedName("kpi_ebitda") private String kpiEbitda;
    @SerializedName("projected_data") private List<Float> projectedData;
    @SerializedName("confidence") private int confidence;
    @SerializedName("recommendations") private List<String> recommendations;
    public String getRevenueImpact() { return revenueImpact; } public void setRevenueImpact(String r) { this.revenueImpact = r; }
    public String getRoi() { return roi; } public void setRoi(String r) { this.roi = r; }
    public String getGrowthRate() { return growthRate; } public void setGrowthRate(String g) { this.growthRate = g; }
    public String getKpiRevenue() { return kpiRevenue; } public void setKpiRevenue(String k) { this.kpiRevenue = k; }
    public String getKpiCustomers() { return kpiCustomers; } public void setKpiCustomers(String k) { this.kpiCustomers = k; }
    public String getKpiMarketShare() { return kpiMarketShare; } public void setKpiMarketShare(String k) { this.kpiMarketShare = k; }
    public String getKpiEbitda() { return kpiEbitda; } public void setKpiEbitda(String k) { this.kpiEbitda = k; }
    public List<Float> getProjectedData() { return projectedData; } public void setProjectedData(List<Float> p) { this.projectedData = p; }
    public int getConfidence() { return confidence; } public void setConfidence(int c) { this.confidence = c; }
    public List<String> getRecommendations() { return recommendations; } public void setRecommendations(List<String> r) { this.recommendations = r; }
}
