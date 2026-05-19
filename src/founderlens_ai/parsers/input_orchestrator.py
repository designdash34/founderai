import os
import json
import uuid
import datetime
from typing import Dict, List, Any

from founderlens_ai.parsers.pdf_parser import PDFParser
from founderlens_ai.parsers.csv_parser import CSVParser
from founderlens_ai.parsers.web_parser import WebParser
from founderlens_ai.parsers.dashboard_parser import DashboardParser
from founderlens_ai.parsers.realtime_generator import RealtimeFeedGenerator
from founderlens_ai.utils.logger import logger

class InputOrchestrator:
    def __init__(self):
        self.pdf_parser = PDFParser()
        self.csv_parser = CSVParser()
        self.web_parser = WebParser()
        self.dashboard_parser = DashboardParser()
        self.realtime_generator = RealtimeFeedGenerator()
        
        # Determine the demo directory path relative to this file
        current_dir = os.path.dirname(os.path.abspath(__file__))
        self.demo_dir = os.path.join(os.path.dirname(current_dir), "demo")

    def _load_scenario(self, scenario_name: str) -> Dict[str, Any]:
        """Loads a scenario JSON file from the demo directory."""
        if not scenario_name:
            return {}
            
        file_path = os.path.join(self.demo_dir, f"scenario_{scenario_name}.json")
        if os.path.exists(file_path):
            try:
                with open(file_path, "r") as f:
                    return json.load(f)
            except Exception as e:
                logger.log_event("INPUT_ORCHESTRATOR", "ERROR", "Orchestrator", {
                    "message": f"Failed to load scenario {scenario_name}",
                    "error": str(e)
                })
        return {}

    def process(self, user_inputs: dict, scenario: str = "saas_churn") -> dict:
        session_id = user_inputs.get("session_id", str(uuid.uuid4()))
        
        # 0. Load Scenario Data
        scenario_data = self._load_scenario(scenario)
        scenario_inputs = scenario_data.get("inputs", {})
        
        logger.log_event("INPUT_ORCHESTRATOR", "TASK_START", "Orchestrator", {
            "session_id": session_id,
            "scenario": scenario,
            "scenario_loaded": bool(scenario_data)
        })

        results = {}
        source_map = {}

        # TYPE 1 — DOCUMENT (PDF)
        if user_inputs.get("pdf_path") or user_inputs.get("document_text"):
            results["document"] = self.pdf_parser.parse(user_inputs)
            source_map["document"] = "user_provided"
        elif scenario_inputs.get("document"):
            results["document"] = scenario_inputs["document"]
            source_map["document"] = "demo_scenario"
        else:
            # No PDF uploaded — do NOT auto-fill with generic default data
            results["document"] = None
            source_map["document"] = "not_provided"

        # TYPE 2 — STRUCTURED (CSV / JSON)
        if user_inputs.get("csv_path") or user_inputs.get("json_data"):
            results["structured"] = self.csv_parser.parse(user_inputs)
            source_map["structured"] = "user_provided"
        elif scenario_inputs.get("structured"):
            results["structured"] = scenario_inputs["structured"]
            source_map["structured"] = "demo_scenario"
        else:
            # No CSV uploaded — do NOT auto-fill with generic default data
            results["structured"] = None
            source_map["structured"] = "not_provided"

        # TYPE 3 — WEB INTEL (URL)
        if user_inputs.get("url"):
            results["web_intel"] = self.web_parser.parse(user_inputs["url"])
            source_map["web_intel"] = "user_provided"
        elif scenario_inputs.get("web_intel"):
            results["web_intel"] = scenario_inputs["web_intel"]
            source_map["web_intel"] = "demo_scenario"
        else:
            # No URL provided — do NOT auto-fill
            results["web_intel"] = None
            source_map["web_intel"] = "not_provided"

        # TYPE 4 — DASHBOARD (KPIs / Metrics table)
        if user_inputs.get("kpis") or user_inputs.get("dashboard_text"):
            results["dashboard"] = self.dashboard_parser.parse(user_inputs)
            source_map["dashboard"] = "user_provided"
        elif scenario_inputs.get("dashboard"):
            results["dashboard"] = scenario_inputs["dashboard"]
            source_map["dashboard"] = "demo_scenario"
        else:
            # No dashboard data — do NOT auto-fill
            results["dashboard"] = None
            source_map["dashboard"] = "not_provided"

        # TYPE 5 — REAL-TIME FEED
        if user_inputs.get("realtime_url"):
            dashboard_kpis = (results.get("dashboard") or {}).get("kpis")
            results["realtime"] = self.realtime_generator.generate(dashboard_kpis)
            source_map["realtime"] = "live_feed"
        else:
            # No live feed — do NOT generate simulated metrics
            results["realtime"] = None
            source_map["realtime"] = "not_provided"

        # Only run conflict detection on sources that were actually provided
        actual_results = {k: v for k, v in results.items() if v is not None}
        conflicts = self.detect_conflicts(actual_results, scenario_data)

        provided_count = sum(1 for v in source_map.values() if v in ["user_provided", "demo_scenario"])
        avg_quality = (
            sum([actual_results[t].get("quality_score", 70) for t in actual_results]) / len(actual_results)
            if actual_results else 0
        )

        unified = {
            "session_id": session_id,
            "scenario": scenario,
            "startup_name": user_inputs.get("startup_name") or scenario_data.get("startup_name", "Startup"),
            "problem_statement": user_inputs.get("problem_statement") or scenario_data.get("problem_statement"),
            "timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "sources": actual_results,
            "source_map": source_map,
            "inputs_provided": provided_count,
            "inputs_not_provided": sum(1 for v in source_map.values() if v == "not_provided"),
            "conflicts": conflicts,
            "overall_quality": round(avg_quality, 1),
            "ready_for_analysis": True,
            "source_badges": {k: self.get_source_badge(v) for k, v in source_map.items()}
        }

        return unified

    def detect_conflicts(self, results: dict, scenario_data: dict = None) -> list:
        conflicts = []
        
        # 1. Check for preset contradictions in the scenario
        if scenario_data and "planted_contradiction" in scenario_data:
            conflicts.append(scenario_data["planted_contradiction"])
            
        # 2. Dynamic check (Churn)
        doc_churn = results["document"].get("key_metrics", {}).get("churn_rate")
        struct_churn = 0
        if results["structured"].get("sample_rows"):
            struct_churn = results["structured"]["sample_rows"][-1].get("churn_rate", 0)
        
        if doc_churn and struct_churn:
            diff_pct = abs(doc_churn - struct_churn) / doc_churn
            if diff_pct > 0.10:
                # Avoid duplicates if it's already in planted contradictions
                if not any(c.get("metric") == "churn_rate" for c in conflicts):
                    conflicts.append({
                        "metric": "churn_rate",
                        "source_a": {"type": "document", "value": doc_churn},
                        "source_b": {"type": "structured", "value": struct_churn},
                        "difference_pct": round(diff_pct * 100, 1),
                        "resolution": "PENDING — sent to conflict agent"
                    })
        return conflicts

    def get_source_badge(self, source_type: str) -> dict:
        badges = {
            "user_provided": {"label": "User Uploaded", "icon": "✅", "color": "green"},
            "demo_scenario": {"label": "Scenario Data", "icon": "📚", "color": "indigo"},
            "not_provided":  {"label": "Not Provided",  "icon": "⬜", "color": "grey"},
            "live_feed":     {"label": "Live Feed",     "icon": "⚡", "color": "orange"}
        }
        return badges.get(source_type, badges["not_provided"])
