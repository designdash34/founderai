import os
from enum import Enum

from ..utils.logger import logger


class ActionType(str, Enum):
    """Enumeration of all supported simulation action types.

    Using an Enum instead of raw strings prevents silent no-ops from typos.
    Pass ActionType.LAUNCH_RETENTION_CAMPAIGN instead of the raw string.
    """
    LAUNCH_RETENTION_CAMPAIGN = "launch_retention_campaign"
    FIX_ONBOARDING = "fix_onboarding"
    ADJUST_PRICING = "adjust_pricing"
    NOTIFY_STAKEHOLDERS = "notify_stakeholders"
    INCREASE_SUPPORT = "increase_support"


class StateManager:
    def __init__(self, initial_state=None):
        self.state = initial_state or {
            "mrr": 10000,
            "churn": 5.0,
            "retention": 90.0,
            "status": "stable"
        }
        self.history = [self.state.copy()]

    def apply_action(self, action_type: ActionType | str):
        # Accept both enum values and plain strings for backward compatibility
        if isinstance(action_type, str):
            try:
                action_type = ActionType(action_type)
            except ValueError:
                logger.log_event(
                    "STATE_MANAGER", "WARNING", "StateManager",
                    {"message": f"Unknown action_type '{action_type}' — no state change applied."}
                )
                return self.state

        logger.log("STATE_MANAGER", "DECISION", f"Applying action: {action_type.value}")
        new_state = self.state.copy()

        if action_type == ActionType.LAUNCH_RETENTION_CAMPAIGN:
            new_state["churn"] *= 0.65
            new_state["retention"] *= 1.25
        elif action_type == ActionType.FIX_ONBOARDING:
            new_state["churn"] *= 0.80
            new_state["retention"] *= 1.15
        elif action_type == ActionType.ADJUST_PRICING:
            new_state["mrr"] *= 1.18
            new_state["churn"] *= 0.90
        elif action_type == ActionType.NOTIFY_STAKEHOLDERS:
            new_state["status"] = "alerted"
        elif action_type == ActionType.INCREASE_SUPPORT:
            new_state["retention"] *= 1.10
            new_state["churn"] *= 0.85

        self.state = new_state
        self.history.append(self.state.copy())
        return self.state

    def rollback(self, steps: int = 1):
        for _ in range(min(steps, len(self.history) - 1)):
            self.history.pop()
        self.state = self.history[-1]
        return self.state

    def get_history(self):
        return self.history

    def calculate_delta(self, before: dict, after: dict) -> dict:
        deltas = {}
        for key in before:
            if isinstance(before[key], (int, float)) and isinstance(after.get(key), (int, float)):
                deltas[key] = round(((after[key] - before[key]) / before[key]) * 100, 2)
        return deltas


state_manager = StateManager()
