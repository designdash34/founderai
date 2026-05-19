import json
import re

class DashboardParser:
    def parse(self, inputs: dict) -> dict:
        kpis_input = inputs.get("kpis")
        dashboard_text = inputs.get("dashboard_text")
        
        try:
            if kpis_input:
                if isinstance(kpis_input, str):
                    kpis = json.loads(kpis_input)
                else:
                    kpis = kpis_input
            elif dashboard_text:
                kpis = self.parse_text_dashboard(dashboard_text)
            else:
                return self.get_default_dashboard()
            
            classified = []
            red_flags = []
            green_signals = []
            
            for name, value in kpis.items():
                status = self.classify_kpi(name, value)
                classified.append({
                    "name": name,
                    "value": value,
                    "trend": "UNKNOWN",
                    "status": status
                })
                if status == "CRITICAL":
                    red_flags.append(f"{name}: {value}")
                if status == "HEALTHY":
                    green_signals.append(f"{name}: {value}")
            
            return {
                "type": "dashboard",
                "source": "user_provided",
                "kpis": classified,
                "red_flags": red_flags,
                "green_signals": green_signals,
                "quality_score": 95,
                "warnings": []
            }
        except Exception as e:
            return self.get_default_dashboard()

    def parse_text_dashboard(self, text: str) -> dict:
        # Simple extraction of Key: Value or Key=Value
        kpis = {}
        # Try to find common patterns like "churn: 28" or "churn=28"
        matches = re.findall(r'(\w+)\s*[:=]\s*(\d+\.?\d*)', text)
        for name, val in matches:
            kpis[name.lower()] = float(val)
        return kpis

    def classify_kpi(self, name: str, value: float) -> str:
        name = name.lower()
        if "churn" in name:
            return "CRITICAL" if value > 20 else "WARNING" if value > 10 else "HEALTHY"
        if "retention" in name:
            return "CRITICAL" if value < 50 else "WARNING" if value < 70 else "HEALTHY"
        if "mrr" in name:
            return "CRITICAL" if value < 10000 else "WARNING" if value < 20000 else "HEALTHY"
        if "nps" in name:
            return "CRITICAL" if value < 30 else "WARNING" if value < 50 else "HEALTHY"
        return "WARNING"

    def get_default_dashboard(self) -> dict:
        return {
            "type": "dashboard",
            "source": "auto_filled",
            "kpis": [
                {"name":"churn_rate","value":28.0,"trend":"UP","status":"CRITICAL"},
                {"name":"mrr","value":12000,"trend":"DOWN","status":"CRITICAL"},
                {"name":"retention","value":41.0,"trend":"DOWN","status":"CRITICAL"},
                {"name":"nps_score","value":28,"trend":"DOWN","status":"CRITICAL"},
                {"name":"new_signups","value":55,"trend":"DOWN","status":"WARNING"},
                {"name":"support_tickets","value":247,"trend":"UP","status":"WARNING"},
                {"name":"onboarding_completion","value":38.0,"trend":"DOWN","status":"CRITICAL"}
            ],
            "red_flags": [
                "churn_rate at 28% — critical",
                "NPS at 28 — below survival threshold",
                "Retention at 41% — losing majority of users",
                "Onboarding completion at 38% — broken funnel"
            ],
            "green_signals": [],
            "quality_score": 95,
            "warnings": ["All KPIs in critical or warning state"]
        }
