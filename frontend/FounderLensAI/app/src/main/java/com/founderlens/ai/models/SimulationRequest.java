package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
public class SimulationRequest {
    @SerializedName("scenario_type") private String scenarioType;
    @SerializedName("investment_amount") private double investmentAmount;
    @SerializedName("target_growth_rate") private double targetGrowthRate;
    @SerializedName("time_horizon_months") private int timeHorizonMonths;
    public String getScenarioType() { return scenarioType; } public void setScenarioType(String s) { this.scenarioType = s; }
    public double getInvestmentAmount() { return investmentAmount; } public void setInvestmentAmount(double i) { this.investmentAmount = i; }
    public double getTargetGrowthRate() { return targetGrowthRate; } public void setTargetGrowthRate(double t) { this.targetGrowthRate = t; }
    public int getTimeHorizonMonths() { return timeHorizonMonths; } public void setTimeHorizonMonths(int t) { this.timeHorizonMonths = t; }
}
