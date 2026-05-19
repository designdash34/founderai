import os
import json
import random
from fastapi import APIRouter, HTTPException, Body, BackgroundTasks
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
import uuid
import datetime

from founderlens_ai.config import BOARDROOM_SIZE
from founderlens_ai.crew import FounderLensCrew
from founderlens_ai.parsers.input_orchestrator import InputOrchestrator
from founderlens_ai.utils.logger import logger


router = APIRouter()
orchestrator = InputOrchestrator()

class AnalysisRequest(BaseModel):
    business_problem: Optional[str] = Field(None, alias="problem_statement")
    website_url: Optional[str] = Field(None, alias="url")
    use_demo_data: bool = True
    input_types: List[str] = ["document", "structured", "web_intel"]
    pdf_path: Optional[str] = None
    csv_path: Optional[str] = None
    
    # Keep legacy fields for compatibility
    scenario: str = "saas_churn"
    startup_name: str = "Startup"
    baseline: bool = Field(False, description="Toggle to run in Non-Agentic Baseline Mode (single direct LLM call)")

async def trigger_live_discord_webhook(session_id: str, startup_name: str, insights: list, action_chain: list):
    """Triggers an outbound Discord Webhook to simulate real-world system notification."""
    webhook_url = os.getenv("DISCORD_WEBHOOK_URL", "").strip()
    if not webhook_url:
        logger.log_event("API", "INFO", "WebhookService", {
            "message": "DISCORD_WEBHOOK_URL is empty. Outbound webhook alert skipped."
        })
        return
        
    try:
        import requests
        
        # Format the embed description of proposed action steps
        action_steps = ""
        if isinstance(action_chain, list) and action_chain:
            for idx, action in enumerate(action_chain, 1):
                if isinstance(action, dict):
                    title = action.get("title", f"Step {idx}")
                    action_steps += f"**{idx}.** {title}\n"
                elif isinstance(action, str):
                    action_steps += f"**{idx}.** {action}\n"
        else:
            action_steps = "*No sequential actions generated.*"
            
        # Format top insight
        top_insight = "*No active insights identified.*"
        if isinstance(insights, list) and insights:
            top_insight = insights[0].get("description", insights[0].get("title", ""))
            
        payload = {
            "username": "FounderLens AI Boardroom",
            "avatar_url": "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=128&auto=format&fit=crop&q=80",
            "embeds": [
                {
                    "title": "🚀 FounderLens AI Boardroom Executive Alert",
                    "description": "A dynamic business analysis session has completed successfully with multi-agent orchestration.",
                    "color": 725996,  # Sleek Dark Indigo color decimal (matching HSL brand #0B1020 / hex #0B102C)
                    "fields": [
                        {
                            "name": "🏢 Startup Name",
                            "value": f"**{startup_name}**",
                            "inline": True
                        },
                        {
                            "name": "🔑 Session ID",
                            "value": f"`{session_id}`",
                            "inline": True
                        },
                        {
                            "name": "💡 Primary Boardroom Insight",
                            "value": top_insight[:1000]
                        },
                        {
                            "name": "🛠️ Simulated Action Chain Steps",
                            "value": action_steps[:1000]
                        }
                    ],
                    "footer": {
                        "text": "FounderLens AI • Google Antigravity Hackathon 2026",
                        "icon_url": "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=64&auto=format&fit=crop&q=80"
                    },
                    "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat()
                }
            ]
        }
        
        response = requests.post(webhook_url, json=payload, timeout=8)
        if response.status_code in [200, 204]:
            logger.log_event("API", "SUCCESS", "WebhookService", {
                "message": f"Successfully triggered outbound Discord webhook for session {session_id}."
            })
        else:
            logger.log_event("API", "WARNING", "WebhookService", {
                "message": f"Discord webhook returned status {response.status_code}: {response.text}"
            })
    except Exception as e:
        logger.log_event("API", "ERROR", "WebhookService", {
            "message": f"Outbound Discord webhook execution failed: {str(e)}"
        })

def format_insights_for_client(insights_strs: List[str], agent_name: str, baseline_tag: str) -> List[dict]:
    formatted = []
    for text in insights_strs:
        # Determine some logical fields based on the text content
        is_risk = any(word in text.lower() for word in ["churn", "risk", "decline", "pressure", "drop", "critical", "complaint"])
        is_opp = any(word in text.lower() for word in ["opportunity", "pivot", "implement", "market", "launch", "pricing"])
        insight_type = "risk" if is_risk else ("opportunity" if is_opp else "trend")
        
        # Build clean title from first few words
        words = text.split()
        title = " ".join(words[:4]) + ("..." if len(words) > 4 else "")
        if title.endswith(".") or title.endswith(","):
            title = title[:-1]
            
        formatted.append({
            "id": str(uuid.uuid4()),
            "type": insight_type,
            "title": title,
            "description": text,
            "confidence": random.randint(85, 95),
            "impact": "high" if is_risk or "high" in text.lower() or "severe" in text.lower() else "medium",
            "agent": agent_name,
            "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "tags": [baseline_tag, insight_type.capitalize()]
        })
    return formatted

@router.post("/baseline", summary="Simple chatmodel baseline analysis")
async def baseline_analysis(request: AnalysisRequest):
    """Starts the simple chatmodel baseline analysis pipeline."""
    session_id = str(uuid.uuid4()).upper()
    
    user_inputs = {
        "session_id": session_id,
        "problem_statement": request.business_problem or "Standard Analysis",
        "url": request.website_url,
        "startup_name": request.startup_name,
        "pdf_path": request.pdf_path,
        "csv_path": request.csv_path
    }
    
    try:
        # 1. Orchestrate Inputs
        unified_data = orchestrator.process(user_inputs, scenario=request.scenario)
        
        # 2. Run Non-Agentic Baseline
        from founderlens_ai.baseline import FounderLensBaseline
        baseline_engine = FounderLensBaseline()
        
        baseline_result = await baseline_engine.run({
            "startup_name": request.startup_name,
            "startup_idea": request.business_problem or "Standard Analysis",
            "data_sources": unified_data["sources"]
        })
        
        if baseline_result["status"] == "error":
            raise HTTPException(status_code=500, detail=baseline_result["error"])
            
        result_data = baseline_result["result"]
        insights_list = result_data.get("insights", [])
        runtime = baseline_result["total_runtime_seconds"]
        
        # Format the raw string insights into structured InsightModel-compatible dictionaries!
        formatted_insights = format_insights_for_client(insights_list, "DirectLLM", "Baseline")
        
        analysis_record = {
            "session_id": session_id,
            "startup_name": f"{request.startup_name} (Baseline)",
            "insights": formatted_insights,
            "contradictions": result_data.get("contradictions", []),
            "action_chain": result_data.get("action_chain", []),
            "simulation_result": result_data.get("simulation_result", {}),
            "raw_result": json.dumps(result_data),
            "total_runtime_seconds": runtime,
            "cost_estimate_usd": 0.005 # Baseline runs use fewer tokens
        }
        
        # Save to database if active
        from founderlens_ai.db.database import db_manager
        if db_manager.is_active():
            try:
                db_manager.client.table("analysis_results").insert(analysis_record).execute()
            except Exception as db_err:
                logger.log_event("API", "ERROR", "System", {"message": f"Supabase baseline save failed: {str(db_err)}"})
                
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
            logger.log_event("API", "ERROR", "System", {"message": f"Local baseline save failed: {str(file_err)}"})
            
        return {
            "id": session_id,
            "trace_id": session_id,
            "status": "success",
            "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "agents_involved": 1, # Direct LLM
            "execution_time_ms": int(runtime * 1000),
            "insights": formatted_insights,
            "raw_result": json.dumps(result_data)
        }
    except Exception as e:
        logger.log_event("API", "ERROR", "System", {"message": f"Simple baseline chat failed: {str(e)}"})
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/start", summary="Start a new business analysis")
async def start_analysis(request: AnalysisRequest, background_tasks: BackgroundTasks):
    """Starts the multi-agent analysis pipeline."""
    session_id = str(uuid.uuid4()).upper()
    start_time = datetime.datetime.now()
    
    # Map Android fields to orchestrator format
    user_inputs = {
        "session_id": session_id,
        "problem_statement": request.business_problem or "Standard Analysis",
        "url": request.website_url,
        "startup_name": request.startup_name,
        "pdf_path": request.pdf_path,
        "csv_path": request.csv_path
    }
    
    try:
        # 1. Orchestrate Inputs
        unified_data = orchestrator.process(user_inputs, scenario=request.scenario)
        
        if request.baseline:
            # 2. Run Non-Agentic Baseline
            from founderlens_ai.baseline import FounderLensBaseline
            baseline_engine = FounderLensBaseline()
            baseline_result = await baseline_engine.run({
                "startup_name": request.startup_name,
                "data_sources": unified_data["sources"]
            })
            
            if baseline_result["status"] == "error":
                raise HTTPException(status_code=500, detail=baseline_result["error"])
                
            # 3. Format Baseline Data for DB and Response
            result_data = baseline_result["result"]
            insights_list = result_data.get("insights", [])
            
            # Format the raw string insights into structured InsightModel-compatible dictionaries!
            formatted_insights = format_insights_for_client(insights_list, "DirectLLM", "Baseline")
            
            analysis_record = {
                "session_id": session_id,
                "startup_name": f"{request.startup_name} (Baseline)",
                "insights": formatted_insights,
                "contradictions": result_data.get("contradictions", []),
                "action_chain": result_data.get("action_chain", []),
                "simulation_result": result_data.get("simulation_result", {}),
                "raw_result": json.dumps(result_data),
                "total_runtime_seconds": baseline_result["total_runtime_seconds"],
                "cost_estimate_usd": 0.005 # Baseline runs use fewer tokens
            }
            
            # Save to database if active
            from founderlens_ai.db.database import db_manager
            if db_manager.is_active():
                try:
                    db_manager.client.table("analysis_results").insert(analysis_record).execute()
                except Exception as db_err:
                    logger.log_event("API", "ERROR", "System", {"message": f"Supabase baseline save failed: {str(db_err)}"})
                    
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
                logger.log_event("API", "ERROR", "System", {"message": f"Local baseline save failed: {str(file_err)}"})
                
            return {
                "id": session_id,
                "trace_id": session_id,
                "status": "success",
                "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "agents_involved": 1, # Direct LLM
                "execution_time_ms": int(baseline_result["total_runtime_seconds"] * 1000),
                "insights": formatted_insights,
                "raw_result": json.dumps(result_data)
            }
            
        # 2. Kickoff Crew
        boardroom = FounderLensCrew()
        result = boardroom.crew().kickoff(inputs={
            "data_sources": str(unified_data["sources"]),
            "session_id": session_id
        })
        
        end_time = datetime.datetime.now()
        execution_time_ms = int((end_time - start_time).total_seconds() * 1000)
        
        # Parse dynamic insights, contradictions, action plan, and simulation outcomes from boardroom Crew output
        result_str = str(result)
        
        # Clean markdown code block if wrapped in LLM format
        clean_str = result_str.strip()
        if clean_str.startswith("```json"):
            clean_str = clean_str[7:]
        if clean_str.endswith("```"):
            clean_str = clean_str[:-3]
        clean_str = clean_str.strip()
        
        parsed_output = {}
        try:
            parsed_output = json.loads(clean_str)
        except Exception as e:
            logger.log_event("API", "WARNING", "System", {"message": f"Failed to parse Crew json output directly: {str(e)}"})
            
        # Extract from container (handling "recovered_data" or root nesting gracefully)
        data_container = parsed_output.get("recovered_data", parsed_output)
        
        formatted_insights = []
        contradictions = []
        action_chain = []
        simulation_result = {}
        
        if isinstance(data_container, dict):
            # 1. Parse Insights (Trends, Risks, Opportunities, Anomalies)
            insight_data = data_container.get("insight", {})
            if isinstance(insight_data, dict):
                # Trends
                trends = insight_data.get("trends", [])
                if isinstance(trends, list):
                    for item in trends:
                        if isinstance(item, dict):
                            formatted_insights.append({
                                "id": str(uuid.uuid4()),
                                "type": "trend",
                                "title": item.get("title", "Trend Flagged"),
                                "description": item.get("description", "Dynamic trend detected in boardroom run."),
                                "confidence": item.get("confidence", 90),
                                "impact": item.get("impact_level", "medium"),
                                "agent": "InsightAgent",
                                "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                                "tags": ["Courtroom", "Trend"]
                            })
                
                # Risks
                risks = insight_data.get("risks", [])
                if isinstance(risks, list):
                    for item in risks:
                        if isinstance(item, dict):
                            formatted_insights.append({
                                "id": str(uuid.uuid4()),
                                "type": "risk",
                                "title": item.get("title", "Risk Detected"),
                                "description": item.get("description", "Potential business risk identified."),
                                "confidence": item.get("confidence", 90),
                                "impact": "high",
                                "agent": "InsightAgent",
                                "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                                "tags": ["Courtroom", "Risk"]
                            })

                # Opportunities
                opps = insight_data.get("opportunities", [])
                if isinstance(opps, list):
                    for item in opps:
                        if isinstance(item, dict):
                            formatted_insights.append({
                                "id": str(uuid.uuid4()),
                                "type": "opportunity",
                                "title": item.get("title", "Opportunity Identified"),
                                "description": item.get("description", "Strategic operational opportunity."),
                                "confidence": item.get("confidence", 90),
                                "impact": item.get("impact_level", "medium"),
                                "agent": "InsightAgent",
                                "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                                "tags": ["Courtroom", "Opportunity"]
                            })

                # Anomalies
                anomalies = insight_data.get("anomalies", [])
                if isinstance(anomalies, list):
                    for item in anomalies:
                        if isinstance(item, dict):
                            formatted_insights.append({
                                "id": str(uuid.uuid4()),
                                "type": "trend",
                                "title": item.get("title", "Anomaly Flagged"),
                                "description": item.get("description", "Atypical behavior or benchmark break."),
                                "confidence": item.get("confidence", 90),
                                "impact": "high",
                                "agent": "InsightAgent",
                                "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                                "tags": ["Courtroom", "Anomaly"]
                            })
            
            # 2. Parse Conflict Contradictions
            conflict_data = data_container.get("conflict", {})
            if isinstance(conflict_data, dict):
                contradictions = conflict_data.get("contradictions", [])
                
            # 3. Parse Planning Action Chain
            planning_data = data_container.get("planning", {})
            if isinstance(planning_data, dict):
                action_chain = planning_data.get("action_chain", [])
                
            # 4. Parse Simulation Data
            simulation_result = data_container.get("simulation", {})
            
        # Fallback to legacy parser if json parsing failed to yield any insights
        if not formatted_insights:
            legacy_trends = unified_data.get("sources", {}).get("insight", {}).get("trends", [])
            formatted_insights = format_insights_for_client(legacy_trends, "InsightAgent", "Courtroom")
            
        analysis_record = {
            "session_id": session_id,
            "startup_name": request.startup_name,
            "insights": formatted_insights,
            "contradictions": contradictions,
            "action_chain": action_chain,
            "simulation_result": simulation_result,
            "raw_result": str(result),
            "total_runtime_seconds": float(execution_time_ms / 1000.0),
            "cost_estimate_usd": 0.05
        }
        
        # Save to database if active
        from founderlens_ai.db.database import db_manager
        if db_manager.is_active():
            try:
                db_manager.client.table("analysis_results").insert(analysis_record).execute()
            except Exception as db_err:
                logger.log_event("API", "ERROR", "System", {"message": f"Supabase analysis_results save failed: {str(db_err)}"})
                
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
            logger.log_event("API", "ERROR", "System", {"message": f"Local analysis_results save failed: {str(file_err)}"})
        
        # Trigger outbound webhook asynchronously in the background pool after response is sent!
        background_tasks.add_task(trigger_live_discord_webhook, session_id, request.startup_name, formatted_insights, action_chain)
        
        # 3. Format response for Android
        return {
            "id": session_id,
            "trace_id": session_id,
            "status": "success",
            "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "agents_involved": BOARDROOM_SIZE,
            "execution_time_ms": execution_time_ms,
            "insights": formatted_insights,
            "raw_result": str(result)
        }
        
    except Exception as e:
        logger.log_event("API", "ERROR", "System", {"message": str(e)})
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/{id}", summary="Get analysis details")
async def get_analysis_detail(id: str):
    """Fetch details for a specific analysis."""
    # Placeholder: In a real app, this would fetch from Supabase
    return {
        "id": id,
        "status": "completed",
        "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat()
    }
