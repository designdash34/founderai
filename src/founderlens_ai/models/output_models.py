from pydantic import BaseModel
from typing import List, Dict, Any, Literal

class KPIState(BaseModel):
    metrics: Dict[str, Any]

class Action(BaseModel):
    step: int
    action: str
    target_metric: str
    expected_impact: str
    priority: Literal["HIGH", "MED", "LOW"]

class Contradiction(BaseModel):
    source_a: str
    source_b: str
    conflict: str
    resolution: str
    confidence: str

class SimulationResult(BaseModel):
    before_state: KPIState
    execution_log: List[str]
    after_state: KPIState
    improvement_summary: str
    confidence: Literal["HIGH", "MEDIUM", "LOW"]

class AgentOutput(BaseModel):
    agent_name: str
    status: Literal["success", "failed", "partial"]
    findings: List[str]
    confidence: Literal["HIGH", "MEDIUM", "LOW"]
    reasoning: str
    raw_output: Dict[str, Any]

class AnalyzeResponse(BaseModel):
    session_id: str
    insights: List[str]
    contradictions: List[Contradiction]
    action_chain: List[Action]
    simulation_result: SimulationResult
    agent_logs: List[AgentOutput]
    total_runtime_seconds: float
    cost_estimate_usd: float
