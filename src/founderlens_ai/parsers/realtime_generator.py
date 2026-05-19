import random
import datetime

class RealtimeFeedGenerator:
    def generate(self, base_metrics: list = None) -> dict:
        now = datetime.datetime.now(datetime.timezone.utc)
        
        # Convert base_metrics list to dict if provided
        metrics_map = {}
        if base_metrics:
            for item in base_metrics:
                metrics_map[item["name"]] = item["value"]
        
        churn_base = metrics_map.get("churn_rate", 28.0)
        mrr_base = metrics_map.get("mrr", 12000)
        
        data_points = []
        for i in range(5, 0, -1):
            timestamp = now - datetime.timedelta(minutes=i)
            # Add realistic noise
            noise = random.uniform(-0.02, 0.02)
            data_points.append({
                "timestamp": timestamp.isoformat(),
                "time_label": f"T-{i}min",
                "metrics": {
                    "active_users": {
                        "value": int(1240 * (1 + noise)),
                        "change_pct": round(noise * 100, 1)
                    },
                    "churn_rate": {
                        "value": round(churn_base * (1 + noise * 0.5), 1),
                        "change_pct": round(noise * 50, 1)
                    },
                    "mrr": {
                        "value": int(mrr_base * (1 + noise)),
                        "change_pct": round(noise * 100, 1)
                    },
                    "support_tickets": {
                        "value": int(47 * (1 - noise)),
                        "change_pct": round(-noise * 100, 1)
                    }
                }
            })
        
        # Detect alerts
        latest = data_points[-1]["metrics"]
        alerts = []
        if latest["churn_rate"]["value"] > 25:
            alerts.append({
                "level": "CRITICAL",
                "metric": "churn_rate",
                "message": f"Churn spike detected: {latest['churn_rate']['value']}%",
                "timestamp": now.isoformat()
            })
            
        return {
            "type": "realtime",
            "source": "simulated",
            "feed_label": "FounderLens AI Synthetic Feed",
            "generated_at": now.isoformat(),
            "data_points": data_points,
            "latest_snapshot": data_points[-1],
            "active_alerts": alerts,
            "quality_score": 88,
            "warnings": ["Feed is simulated — not live data"]
        }
