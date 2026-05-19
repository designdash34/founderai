import random
import math
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import List
from founderlens_ai.utils.logger import logger

router = APIRouter()

class SimulationRequest(BaseModel):
    scenario_type: str = Field(..., description="Classification category: churn, growth, marketing")
    investment_amount: float = Field(..., ge=0, description="Capital allocation target in USD")
    target_growth_rate: float = Field(..., description="Target percentage increase")
    time_horizon_months: int = Field(..., gt=0, description="Duration benchmark in months")

class SimulationResult(BaseModel):
    revenue_impact: str = Field(..., description="Forecasted revenue changes")
    roi: str = Field(..., description="Estimated return on capital multiplier percentage")
    growth_rate: str = Field(..., description="Calculated target metric increase")
    kpi_revenue: str = Field(..., description="Standard Projected Gross Revenue")
    kpi_customers: str = Field(..., description="Projected customer volume count")
    kpi_market_share: str = Field(..., description="Estimated market share penetration percentage")
    kpi_ebitda: str = Field(..., description="Estimated operational profitability metrics")
    projected_data: List[float] = Field(..., description="Historical trend projection numbers")
    confidence: int = Field(..., ge=0, le=100, description="Simulation outcome probability confidence index")
    recommendations: List[str] = Field(..., description="AI boardroom recommendation directives")

@router.post("/run", response_model=SimulationResult, summary="Run a business strategic simulation")
async def run_simulation(request: SimulationRequest):
    """Runs scenario-based cashflow, churn, and retention forecasting models dynamically."""
    
    logger.log_event("API_ROUTE", "TASK_START", "SimulationEngine", {
        "message": f"Kicking off dynamic strategic simulation model: {request.scenario_type}",
        "investment": request.investment_amount,
        "target_growth": request.target_growth_rate,
        "horizon": request.time_horizon_months
    })
    
    try:
        I = float(request.investment_amount)
        G = float(request.target_growth_rate)
        H = int(request.time_horizon_months)
        S = request.scenario_type.lower()
        
        # 1. Calculate ROI based on Scenario, Investment, and Growth Target
        if "marketing" in S:
            roi_val = 120.0 + G * 2.5 + (I / 10000.0)
        elif "pricing" in S:
            roi_val = 80.0 + G * 4.0 + (I / 20000.0)
        elif "growth" in S or "churn" in S:
            roi_val = 150.0 + G * 1.8 + (I / 8000.0)
        else: # expansion or default
            roi_val = 110.0 + G * 3.0 + (I / 12000.0)
            
        # Keep ROI within realistic commercial boundaries (15% to 850%)
        roi_val = max(15.0, min(roi_val, 850.0))
        roi_str = f"{int(roi_val)}%"
        
        # 2. Revenue Impact (ROI * Investment)
        impact_val = I * (roi_val / 100.0)
        if impact_val >= 1_000_000:
            revenue_impact = f"+${impact_val / 1_000_000:.2f}M"
        else:
            revenue_impact = f"+${int(impact_val):,}"
            
        # 3. Projected Gross Revenue (Baseline $1M + impact)
        gross_val = 1_000_000.0 + impact_val
        if gross_val >= 1_000_000:
            kpi_revenue = f"${gross_val / 1_000_000:.2f}M"
        else:
            kpi_revenue = f"${int(gross_val):,}"
            
        # 4. Customer Growth (baseline 5,000 customers, $200 LTV average per new customer)
        new_customers = impact_val / 200.0
        total_customers = int(5000 + new_customers)
        kpi_customers = f"{total_customers:,}"
        
        # 5. Market Share (baseline 5.0%, +0.5% for every $50k of growth impact)
        share_increase = (impact_val / 50000.0) * 0.5
        total_share = max(1.0, min(45.0, 5.0 + share_increase))
        kpi_market_share = f"{total_share:.1f}%"
        
        # 6. EBITDA (Profitability - estimated at 25% of gross revenue)
        ebitda_val = gross_val * 0.25
        if ebitda_val >= 1_000_000:
            kpi_ebitda = f"${ebitda_val / 1_000_000:.2f}M"
        else:
            kpi_ebitda = f"${int(ebitda_val / 1000.0)}K"
            
        # 7. Projected Data Curve (Natural growth path with slight noise over H months)
        projected_data = []
        current_val = 100.0
        monthly_rate = (G / 100.0) / 12.0
        
        # Seed generator deterministically using request parameters for stability
        random_gen = random.Random(int(I + G + H))
        
        for m in range(H + 1):
            projected_data.append(round(current_val, 1))
            noise = random_gen.uniform(-0.012, 0.016)  # Simulate realistic month-on-month market volatility
            current_val = current_val * (1.0 + monthly_rate + noise)
            
        # 8. Confidence Score
        base_conf = 88
        conf_penalty = (G - 10.0) * 0.6 if G > 10 else 0
        if I > 200000:
            conf_penalty += (I - 200000) / 40000.0
        confidence = int(max(40, min(96, base_conf - conf_penalty)))
        
        # 9. Smart Strategic Recommendations
        if "marketing" in S:
            recommendations = [
                f"Allocate at least 45% of the ${I:,.0f} investment towards low-friction digital acquisition channels.",
                f"Deploy multivariate testing on checkout pages to capture the projected {roi_str} return on sales.",
                "Build retention programs to maximize the lifetime value (LTV) of newly acquired customer contacts."
            ]
        elif "pricing" in S:
            recommendations = [
                "Implement utility-based tiered pricing to capture consumer surplus without alienating churn-sensitive accounts.",
                "Conduct customer price elasticity trials before scaling adjustments globally.",
                f"Grandfather legacy accounts for 6 months to offset immediate attrition risks."
            ]
        elif "growth" in S or "churn" in S:
            recommendations = [
                f"Onboard strategic customer success advisors to support the aggressive {G:.1f}% growth target.",
                "Optimize critical service onboarding funnels to prevent early retention leaks.",
                "Upgrade technical support capacity to sustain expanded daily client interactions."
            ]
        else: # expansion
            recommendations = [
                "Localize digital assets, localized payment systems, and search profiles for targeted expansion zones.",
                f"Apportion ${I * 0.35:,.0f} specifically towards market-specific awareness campaigns.",
                "Forge partnerships with localized strategic vendors to bypass early distribution barriers."
            ]
            
        res = SimulationResult(
            revenue_impact=revenue_impact,
            roi=roi_str,
            growth_rate=f"+{G:.1f}%",
            kpi_revenue=kpi_revenue,
            kpi_customers=kpi_customers,
            kpi_market_share=kpi_market_share,
            kpi_ebitda=kpi_ebitda,
            projected_data=projected_data,
            confidence=confidence,
            recommendations=recommendations
        )
        
        # Record this simulation run as a business analysis so it updates the dashboard!
        import uuid
        import os
        import json
        
        sim_id = f"SIM_{str(uuid.uuid4())[:8].upper()}"
        analysis_record = {
            "session_id": sim_id,
            "startup_name": f"Simulation ({request.scenario_type.replace('_', ' ').title()})",
            "insights": res.recommendations,
            "contradictions": [],
            "action_chain": [],
            "simulation_result": {
                "scenario_type": request.scenario_type,
                "investment_amount": request.investment_amount,
                "target_growth_rate": request.target_growth_rate,
                "time_horizon_months": request.time_horizon_months,
                "roi": res.roi,
                "revenue_impact": res.revenue_impact
            },
            "raw_result": f"Simulation run for scenario '{request.scenario_type}' with investment ${request.investment_amount:,.0f} and target growth rate {request.target_growth_rate}%.",
            "total_runtime_seconds": 0.5,
            "cost_estimate_usd": 0.005
        }
        
        # Save to database if active
        from founderlens_ai.db.database import db_manager
        if db_manager.is_active():
            try:
                db_manager.client.table("analysis_results").insert(analysis_record).execute()
            except Exception as db_err:
                logger.log_event("API", "ERROR", "SimulationEngine", {"message": f"Supabase simulation save failed: {str(db_err)}"})
                
        # Save locally as fallback
        try:
            log_dir = os.getenv("LOG_DIR", "./logs")
            os.makedirs(log_dir, exist_ok=True)
            results_path = os.path.join(log_dir, "analysis_results.json")
            
            local_results = []
            if os.path.exists(results_path):
                with open(results_path, "r") as f:
                    local_results = json.load(f)
            local_results.append(analysis_record)
            with open(results_path, "w") as f:
                json.dump(local_results, f, indent=2)
        except Exception as file_err:
            logger.log_event("API", "ERROR", "SimulationEngine", {"message": f"Local simulation save failed: {str(file_err)}"})
            
        logger.log_event("API_ROUTE", "TASK_COMPLETE", "SimulationEngine", {
            "message": "Strategic simulation projection model finished successfully.",
            "roi": res.roi,
            "impact": res.revenue_impact
        })
        
        return res
        
    except Exception as e:
        logger.log_event("API_ROUTE", "ERROR", "SimulationEngine", {"message": str(e)})
        raise HTTPException(status_code=500, detail=f"Simulation failed: {str(e)}")
