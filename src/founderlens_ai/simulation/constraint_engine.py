class ConstraintEngine:
    def validate_action(self, action: dict, constraints: dict) -> dict:
        violations = []
        feasible = True
        modified_action = action.copy()
        
        # Budget check
        estimated_cost = action.get("estimated_cost_usd")
        budget_limit = constraints.get("budget_usd")
        
        if estimated_cost and budget_limit:
            if estimated_cost > budget_limit:
                violations.append({
                    "type": "BUDGET_EXCEEDED",
                    "detail": f"Action costs ${estimated_cost} but budget is ${budget_limit}",
                    "resolution": "MODIFY — reduce scope or skip"
                })
                feasible = False
        
        # Time check
        estimated_days = action.get("estimated_days")
        time_limit = constraints.get("time_days")
        
        if estimated_days and time_limit:
            if estimated_days > time_limit:
                violations.append({
                    "type": "TIME_EXCEEDED",
                    "detail": f"Action needs {estimated_days} days but deadline is {time_limit} days",
                    "resolution": "MODIFY — fast-track version"
                })
                feasible = False
        
        # Urgency override
        if constraints.get("urgency") == "HIGH":
            modified_action["priority"] = "HIGH"
        
        return {
            "action": modified_action,
            "feasible": feasible,
            "violations": violations,
            "recommendation": "EXECUTE" if feasible else "MODIFY/SKIP"
        }
