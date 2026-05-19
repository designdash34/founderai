package com.founderlens.ai.utils;

import com.founderlens.ai.models.ActivityItem;
import com.founderlens.ai.models.Agent;
import com.founderlens.ai.models.DashboardStats;
import com.founderlens.ai.models.Insight;
import com.founderlens.ai.models.SimulationResult;
import com.founderlens.ai.models.TraceEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoDataProvider {

    private static final Gson gson = new Gson();

    public static List<Agent> getAgents() {
        Type type = new TypeToken<List<Agent>>(){}.getType();
        return gson.fromJson(AGENTS_JSON, type);
    }

    public static DashboardStats getDashboardStats() {
        DashboardStats stats = gson.fromJson(STATS_JSON, DashboardStats.class);
        stats.setRecentActivity(getRecentActivity());
        return stats;
    }

    public static List<ActivityItem> getRecentActivity() {
        Type type = new TypeToken<List<ActivityItem>>(){}.getType();
        return gson.fromJson(ACTIVITY_JSON, type);
    }

    public static List<Insight> getInsights() {
        Type type = new TypeToken<List<Insight>>(){}.getType();
        return gson.fromJson(INSIGHTS_JSON, type);
    }

    public static List<TraceEntry> getTraceEntries() {
        Type type = new TypeToken<List<TraceEntry>>(){}.getType();
        return gson.fromJson(TRACE_JSON, type);
    }

    public static SimulationResult getSimulationResult(String scenarioType) {
        SimulationResult result = new SimulationResult();
        switch (scenarioType) {
            case "pricing_change":
                result.setRevenueImpact("+$287K ARR"); result.setRoi("186%"); result.setGrowthRate("+23%");
                result.setKpiRevenue("$1.53M"); result.setKpiCustomers("+340"); result.setKpiMarketShare("+4.2%"); result.setKpiEbitda("+18%");
                break;
            case "growth_plan":
                result.setRevenueImpact("+$540K ARR"); result.setRoi("210%"); result.setGrowthRate("+43%");
                result.setKpiRevenue("$1.78M"); result.setKpiCustomers("+680"); result.setKpiMarketShare("+7.8%"); result.setKpiEbitda("+22%");
                break;
            case "expansion":
                result.setRevenueImpact("+$820K ARR"); result.setRoi("175%"); result.setGrowthRate("+65%");
                result.setKpiRevenue("$2.06M"); result.setKpiCustomers("+1,200"); result.setKpiMarketShare("+12.4%"); result.setKpiEbitda("+9%");
                break;
            default:
                result.setRevenueImpact("+$420K ARR"); result.setRoi("320%"); result.setGrowthRate("+34%");
                result.setKpiRevenue("$1.66M"); result.setKpiCustomers("+520"); result.setKpiMarketShare("+5.6%"); result.setKpiEbitda("+27%");
        }
        result.setConfidence(90);
        result.setRecommendations(Arrays.asList(
                "Launch in Q1 for maximum seasonal impact",
                "Allocate 60% budget to digital channels",
                "Set 90-day review checkpoint for course correction",
                "Prioritize top 20% accounts for white-glove onboarding"
        ));
        List<Float> projected = new ArrayList<>();
        float base = 100f;
        for (int i = 0; i < 13; i++) {
            projected.add(base * (float)(1 + (0.03 + (Math.random() * 0.01)) * i));
        }
        result.setProjectedData(projected);
        return result;
    }

    private static final String AGENTS_JSON = "[" +
            "{\"id\":\"intake\",\"name\":\"Intake Agent\",\"description\":\"Collects and normalizes data from PDFs, CSVs, and websites\",\"status\":\"idle\",\"icon\":\"upload\",\"color\":\"#4F8CFF\"}," +
            "{\"id\":\"insight\",\"name\":\"Insight Agent\",\"description\":\"Detects trends, opportunities, and risks\",\"status\":\"idle\",\"icon\":\"insights\",\"color\":\"#38DDF8\"}," +
            "{\"id\":\"conflict\",\"name\":\"Conflict Agent\",\"description\":\"Resolves contradictory or mismatched data\",\"status\":\"idle\",\"icon\":\"conflict\",\"color\":\"#F59E0B\"}," +
            "{\"id\":\"planner\",\"name\":\"Action Planner\",\"description\":\"Creates business strategies and execution plans\",\"status\":\"idle\",\"icon\":\"plan\",\"color\":\"#22C55E\"}," +
            "{\"id\":\"simulation\",\"name\":\"Simulation Agent\",\"description\":\"Simulates impact on KPIs and business growth\",\"status\":\"idle\",\"icon\":\"simulation\",\"color\":\"#A855F7\"}," +
            "{\"id\":\"recovery\",\"name\":\"Recovery Agent\",\"description\":\"Handles failures and restores system stability\",\"status\":\"idle\",\"icon\":\"recovery\",\"color\":\"#EF4444\"}" +
            "]";

    private static final String STATS_JSON = "{" +
            "\"activeAgents\":6,\"analysesRun\":12,\"insightsFound\":47,\"successRate\":98," +
            "\"agentStatuses\":{\"intake\":\"idle\",\"insight\":\"idle\",\"conflict\":\"idle\",\"planner\":\"idle\",\"simulation\":\"idle\",\"recovery\":\"idle\"}" +
            "}";

    private static final String ACTIVITY_JSON = "[" +
            "{\"id\":\"a1\",\"title\":\"Intake Agent\",\"description\":\"Demo scenario loaded — 5 data sources ingested (PDF, CSV, JSON, website, real-time feed)\",\"timestamp\":\"Just now\",\"type\":\"success\"}," +
            "{\"id\":\"a2\",\"title\":\"Insight Agent\",\"description\":\"Detected 3 high-impact risks and 4 growth opportunities in Q3 financial data\",\"timestamp\":\"2m ago\",\"type\":\"info\"}," +
            "{\"id\":\"a3\",\"title\":\"Conflict Agent\",\"description\":\"Resolved data discrepancy between CSV revenue figures and PDF report (±12% variance)\",\"timestamp\":\"3m ago\",\"type\":\"warning\"}," +
            "{\"id\":\"a4\",\"title\":\"Action Planner\",\"description\":\"Generated 5-step growth strategy roadmap with Q4 execution milestones\",\"timestamp\":\"5m ago\",\"type\":\"success\"}," +
            "{\"id\":\"a5\",\"title\":\"Simulation Agent\",\"description\":\"Projected +34% revenue uplift for marketing expansion scenario (90% confidence)\",\"timestamp\":\"7m ago\",\"type\":\"success\"}," +
            "{\"id\":\"a6\",\"title\":\"Recovery Agent\",\"description\":\"All agents nominal — system health check passed\",\"timestamp\":\"10m ago\",\"type\":\"success\"}" +
            "]";

    private static final String INSIGHTS_JSON = "[" +
            "{\"id\":\"i1\",\"type\":\"risk\",\"title\":\"Customer Churn Risk — Q3 Spike\",\"description\":\"Customer churn increased by 18% in Q3. Sentiment analysis from support tickets indicates product-fit issues in the SMB segment. Immediate retention intervention recommended.\",\"confidence\":88,\"impact\":\"high\",\"agent\":\"Insight Agent\",\"timestamp\":\"2024-01-15 09:23\",\"tags\":[\"churn\",\"SMB\",\"retention\"]}," +
            "{\"id\":\"i2\",\"type\":\"risk\",\"title\":\"Cash Runway — 7 Month Warning\",\"description\":\"At current burn rate of $180K/month, cash reserves will deplete in approximately 7 months. Fundraising or cost optimization required within 60 days.\",\"confidence\":92,\"impact\":\"high\",\"agent\":\"Insight Agent\",\"timestamp\":\"2024-01-15 09:24\",\"tags\":[\"finance\",\"runway\",\"burn-rate\"]}," +
            "{\"id\":\"i3\",\"type\":\"risk\",\"title\":\"Competitive Pressure — New Market Entrant\",\"description\":\"Analysis of website traffic data shows 3 new competitors launched in the last 60 days targeting the same ICP. Market share erosion risk is estimated at 8-12% in H1.\",\"confidence\":74,\"impact\":\"medium\",\"agent\":\"Insight Agent\",\"timestamp\":\"2024-01-15 09:25\",\"tags\":[\"competition\",\"market-share\"]}," +
            "{\"id\":\"i4\",\"type\":\"opportunity\",\"title\":\"Enterprise Segment Expansion\",\"description\":\"Usage data shows 23% of SMB customers are exhibiting enterprise-scale usage patterns. Upsell opportunity estimated at $420K ARR with targeted account expansion program.\",\"confidence\":85,\"impact\":\"high\",\"agent\":\"Insight Agent\",\"timestamp\":\"2024-01-15 09:26\",\"tags\":[\"upsell\",\"enterprise\",\"ARR\"]}," +
            "{\"id\":\"i5\",\"type\":\"opportunity\",\"title\":\"APAC Market Entry Window\",\"description\":\"Real-time market feed shows 40% YoY growth in APAC SaaS adoption. Low competition density and high willingness-to-pay in Singapore and Australia markets.\",\"confidence\":78,\"impact\":\"high\",\"agent\":\"Insight Agent\",\"timestamp\":\"2024-01-15 09:27\",\"tags\":[\"APAC\",\"expansion\",\"SaaS\"]}," +
            "{\"id\":\"i6\",\"type\":\"opportunity\",\"title\":\"Partnership Channel — 2x CAC Reduction\",\"description\":\"Channel partnership with 3 identified integration partners could reduce Customer Acquisition Cost by 50% while maintaining LTV. Deal velocity data supports this projection.\",\"confidence\":82,\"impact\":\"medium\",\"agent\":\"Action Planner\",\"timestamp\":\"2024-01-15 09:28\",\"tags\":[\"partnerships\",\"CAC\",\"channels\"]}," +
            "{\"id\":\"i7\",\"type\":\"trend\",\"title\":\"AI Feature Adoption — 340% Growth\",\"description\":\"AI-powered features usage grew 340% QoQ. Users who activate AI features have 2.8x higher retention and 1.6x higher NPS scores. Prioritize AI roadmap investment.\",\"confidence\":91,\"impact\":\"high\",\"agent\":\"Insight Agent\",\"timestamp\":\"2024-01-15 09:29\",\"tags\":[\"AI\",\"product\",\"retention\"]}," +
            "{\"id\":\"i8\",\"type\":\"trend\",\"title\":\"Mobile Usage Surpassing Desktop\",\"description\":\"Mobile sessions exceeded desktop for the first time (54% vs 46%). Mobile conversion rate lags 31% behind desktop — significant optimization opportunity.\",\"confidence\":87,\"impact\":\"medium\",\"agent\":\"Insight Agent\",\"timestamp\":\"2024-01-15 09:30\",\"tags\":[\"mobile\",\"UX\",\"conversion\"]}," +
            "{\"id\":\"i9\",\"type\":\"action\",\"title\":\"Launch Retention Campaign — Week 1\",\"description\":\"Deploy targeted re-engagement campaign for at-risk SMB accounts. Offer 2-month discount + dedicated CSM. Expected recovery: 60% of churning accounts. ROI positive at 45-day mark.\",\"confidence\":89,\"impact\":\"high\",\"agent\":\"Action Planner\",\"timestamp\":\"2024-01-15 09:31\",\"tags\":[\"retention\",\"campaign\",\"SMB\"]}," +
            "{\"id\":\"i10\",\"type\":\"action\",\"title\":\"Optimize Pricing — Freemium Tier\",\"description\":\"Introduce usage-based pricing tier between free and pro. Conversion modeling shows 22% improvement in free-to-paid conversion. Simulation confidence: 87%.\",\"confidence\":87,\"impact\":\"medium\",\"agent\":\"Action Planner\",\"timestamp\":\"2024-01-15 09:32\",\"tags\":[\"pricing\",\"freemium\",\"conversion\"]}" +
            "]";

    private static final String TRACE_JSON = "[" +
            "{\"id\":\"t1\",\"type\":\"decision\",\"agent\":\"Antigravity Orchestrator\",\"message\":\"Execution plan created. 6-agent workflow initialized for multi-source business analysis.\",\"details\":\"Evaluated task complexity: 5 data sources × 6 agent pipeline = full orchestration required. Deterministic execution order: intake → insight → conflict → planner → simulation → recovery.\",\"timestamp\":\"09:20:01.001\"}," +
            "{\"id\":\"t2\",\"type\":\"agent_action\",\"agent\":\"Intake Agent\",\"message\":\"Ingesting PDF report: Q3_Financial_Report.pdf (2.4MB)\",\"details\":\"PDF parsed using PyMuPDF. Extracted 47 tables, 12 charts, and 8,200 words of financial narrative. Revenue figures normalized to USD. Date formats standardized to ISO 8601.\",\"timestamp\":\"09:20:01.234\"}," +
            "{\"id\":\"t3\",\"type\":\"agent_action\",\"agent\":\"Intake Agent\",\"message\":\"Ingesting CSV data: sales_data_2024.csv (3,200 rows)\",\"details\":\"CSV parsed. Detected 4 column type mismatches. Auto-corrected numeric columns with string contamination. Applied rolling 7-day smoothing to daily sales figures.\",\"timestamp\":\"09:20:01.891\"}," +
            "{\"id\":\"t4\",\"type\":\"agent_action\",\"agent\":\"Intake Agent\",\"message\":\"Ingesting JSON feed: metrics.json — 156 KPI entries\",\"details\":\"Real-time metrics feed processed. Timestamp alignment applied across 3 timezone discrepancies. 2 stale entries (>24h) flagged and excluded from analysis.\",\"timestamp\":\"09:20:02.102\"}," +
            "{\"id\":\"t5\",\"type\":\"agent_action\",\"agent\":\"Intake Agent\",\"message\":\"Scraping website: https://demo.founderlens.ai/data\",\"details\":\"Website content fetched. Extracted pricing table, product changelog, and customer testimonials. Sentiment analysis queued.\",\"timestamp\":\"09:20:02.567\"}," +
            "{\"id\":\"t6\",\"type\":\"agent_action\",\"agent\":\"Intake Agent\",\"message\":\"Processing real-time market feed — 89 events ingested\",\"details\":\"Market intelligence feed processed. Competitor pricing changes (3), industry news (12), and customer signal events (74) classified and tagged.\",\"timestamp\":\"09:20:03.211\"}," +
            "{\"id\":\"t7\",\"type\":\"conflict\",\"agent\":\"Conflict Agent\",\"message\":\"CONFLICT DETECTED: Revenue discrepancy between PDF and CSV sources\",\"details\":\"PDF reports Q3 revenue as $1.24M. CSV data aggregates to $1.09M (Δ=$150K, 12.2% variance). Investigating cause: likely includes deferred revenue not reflected in CSV bookings data.\",\"timestamp\":\"09:20:04.001\"}," +
            "{\"id\":\"t8\",\"type\":\"decision\",\"agent\":\"Conflict Agent\",\"message\":\"RESOLVED: Applied PDF figure as canonical — deferred revenue confirmed via JSON metrics\",\"details\":\"Cross-referenced JSON metrics feed which shows $152K in deferred revenue matching the discrepancy. PDF accrual accounting is correct. CSV reflects cash bookings only. Both figures preserved with labels.\",\"timestamp\":\"09:20:04.445\"}," +
            "{\"id\":\"t9\",\"type\":\"agent_action\",\"agent\":\"Insight Agent\",\"message\":\"Analyzing 5 normalized data sources — generating business intelligence\",\"details\":\"Running anomaly detection, trend analysis, and risk scoring across unified dataset. Applying industry benchmarks from market feed for comparative analysis.\",\"timestamp\":\"09:20:05.001\"}," +
            "{\"id\":\"t10\",\"type\":\"agent_action\",\"agent\":\"Insight Agent\",\"message\":\"Generated 10 insights: 3 risks (2 high), 3 opportunities (2 high), 2 trends, 2 actions\",\"details\":\"Confidence scores calculated using ensemble of pattern recognition + LLM reasoning. All insights above 70% confidence threshold. Top risk: customer churn (88% confidence, HIGH impact).\",\"timestamp\":\"09:20:06.234\"}," +
            "{\"id\":\"t11\",\"type\":\"agent_action\",\"agent\":\"Action Planner\",\"message\":\"Building 5-step strategic execution roadmap\",\"details\":\"Priority matrix applied: High impact × High confidence insights escalated to Week 1 actions. Dependencies mapped. Resource requirements estimated. Timeline: 90-day sprint structure.\",\"timestamp\":\"09:20:07.001\"}," +
            "{\"id\":\"t12\",\"type\":\"agent_action\",\"agent\":\"Simulation Agent\",\"message\":\"Running 1,000-iteration Monte Carlo simulation for marketing expansion scenario\",\"details\":\"Parameters: $100K investment, 12-month horizon, 20% growth target. Baseline: current trajectory. Testing: marketing expansion scenario. Sampling revenue distributions from historical data.\",\"timestamp\":\"09:20:07.891\"}," +
            "{\"id\":\"t13\",\"type\":\"agent_action\",\"agent\":\"Simulation Agent\",\"message\":\"Simulation complete — projected +34% revenue uplift (90% confidence interval: +28% to +41%)\",\"details\":\"1,000 iterations complete. Median outcome: +34% ARR growth. 90th percentile: +41%. 10th percentile: +28%. Recommendation threshold exceeded. Scenario approved for action planning.\",\"timestamp\":\"09:20:08.654\"}," +
            "{\"id\":\"t14\",\"type\":\"agent_action\",\"agent\":\"Recovery Agent\",\"message\":\"System health check — all agents nominal\",\"details\":\"Memory usage: 234MB/512MB. API latency: 124ms avg. No errors detected in this run. Execution logs written to antigravity_trace.json. Master log updated.\",\"timestamp\":\"09:20:09.001\"}," +
            "{\"id\":\"t15\",\"type\":\"decision\",\"agent\":\"Antigravity Orchestrator\",\"message\":\"EXECUTION COMPLETE — Analysis pipeline finished in 7.4 seconds\",\"details\":\"All 6 agents completed their tasks. 10 insights generated. 1 conflict resolved. 1 simulation run. Total tokens processed: 48,200. Trace ID: FL-2024-0115-001. Full log available for export.\",\"timestamp\":\"09:20:09.445\"}" +
            "]";
}
