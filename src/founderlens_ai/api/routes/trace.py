import os
import json
from datetime import datetime
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import List, Optional
from founderlens_ai.config import BOARDROOM_SIZE
from founderlens_ai.db.database import db_manager
from founderlens_ai.utils.logger import logger

router = APIRouter()

LOG_DIR = os.getenv("LOG_DIR", "./logs")
TRACE_FILE = "antigravity_trace.json"

class TraceEntryModel(BaseModel):
    id: str = Field(..., description="Unique generated ID for the trace entry")
    type: str = Field(..., description="Mapped type matching Chip filters: decision, agent_action, conflict, error, recovery")
    agent: str = Field(..., description="Name of the courtroom agent responsible for this step")
    timestamp: str = Field(..., description="ISO 8601 formatted action timestamp")
    message: str = Field(..., description="Action log descriptive detail summary")
    reasoning: str = Field(..., description="Intermediate reasoning observations and tool context outputs")
    duration_ms: int = Field(0, description="Latency duration of the execution step in milliseconds")
    state_before: str = Field("", description="Pre-execution environment state")
    state_after: str = Field("", description="Post-execution outcome state projection")

class TraceResponseModel(BaseModel):
    entries: List[TraceEntryModel] = Field(..., description="List of audit trace entry models")
    total_steps: int = Field(..., description="Total completed trace timeline events")
    duration: str = Field("N/A", description="Total boardroom execution latency summary string")
    agents_involved: int = Field(6, description="Total agents that participated")
    analysis_id: str = Field(..., description="Associated session or analysis ID")

def calculate_total_duration(logs) -> str:
    """Calculates boardroom execution duration by finding min/max timestamp delta of the latest session."""
    if not logs:
        return "0s"
    try:
        # 1. Group logs by session_id to isolate runs
        session_logs = {}
        for e in logs:
            if not isinstance(e, dict):
                continue
            sid = e.get("session_id") or "GLOBAL"
            if sid not in session_logs:
                session_logs[sid] = []
            session_logs[sid].append(e)

        # 2. Identify the most recent active session ID by finding the latest logged timestamp
        latest_session_id = None
        latest_timestamp = None
        for sid, s_entries in session_logs.items():
            for e in s_entries:
                ts_str = e.get("timestamp")
                if ts_str:
                    ts_str = ts_str.replace("Z", "+00:00")
                    try:
                        dt = datetime.fromisoformat(ts_str)
                        if latest_timestamp is None or dt > latest_timestamp:
                            latest_timestamp = dt
                            latest_session_id = sid
                    except ValueError:
                        pass

        if not latest_session_id or latest_session_id not in session_logs:
            return "0s"

        # 3. Calculate execution duration exclusively for this single, latest session
        target_logs = session_logs[latest_session_id]
        timestamps = []
        for e in target_logs:
            ts_str = e.get("timestamp")
            if ts_str:
                ts_str = ts_str.replace("Z", "+00:00")
                try:
                    dt = datetime.fromisoformat(ts_str)
                    timestamps.append(dt)
                except ValueError:
                    pass

        if not timestamps:
            return "0s"
        min_ts = min(timestamps)
        max_ts = max(timestamps)
        diff = max_ts - min_ts
        total_seconds = int(diff.total_seconds())
        if total_seconds < 60:
            return f"{total_seconds}s"
        minutes = total_seconds // 60
        seconds = total_seconds % 60
        if minutes >= 60:
            hours = minutes // 60
            remaining_minutes = minutes % 60
            return f"{hours}h {remaining_minutes}m"
        return f"{minutes}m {seconds}s"
    except Exception as e:
        logger.log_event("API_ROUTE", "ERROR", "TraceDuration", {"message": f"Failed to parse duration: {str(e)}"})
        return "N/A"

def flatten_entry(entry):
    """Flattens a nested log entry into the flat schema expected by the Android app."""
    # Ensure entry and entry.get("data") are dicts
    if not isinstance(entry, dict):
        entry = {}
    data = entry.get("data")
    if not isinstance(data, dict):
        data = {}
        
    event_type = str(entry.get("type") or "info").lower()
    
    # Map raw event types to the filters expected by Android Chips:
    # decision, agent_action, conflict, error, recovery
    mapped_type = event_type
    if event_type in ["observation", "steps", "reasoning"]:
        mapped_type = "decision"
    elif event_type in ["tool_call", "action", "agent_action"]:
        mapped_type = "agent_action"
    elif event_type in ["error"]:
        mapped_type = "error"
    elif event_type in ["recovery"]:
        mapped_type = "recovery"
    elif event_type in ["conflict"]:
        mapped_type = "conflict"
        
    flat = {
        "id": str(entry.get("id")) if entry.get("id") is not None else str(entry.get("timestamp") or ""),
        "type": str(mapped_type or "info"),
        "agent": str(entry.get("agent") or "System"),
        "timestamp": str(entry.get("timestamp") or ""),
        "message": str(data.get("message") or ""),
        "reasoning": str(data.get("reasoning") or ""),
        "duration_ms": int(data.get("latency_ms") or 0),
        "state_before": "",
        "state_after": ""
    }
    
    # Perform event-specific flattening depending on how it was logged
    if event_type == "observation":
        flat["message"] = "Observed business dynamics and environmental indicators."
        flat["reasoning"] = str(data.get("text") or "")
    elif event_type == "steps":
        steps = data.get("steps", [])
        flat["message"] = "Formulating boardroom strategic timeline."
        flat["reasoning"] = "\n".join(steps) if isinstance(steps, list) else str(steps or "")
    elif event_type == "tool_call":
        tool = str(data.get("tool") or "")
        input_val = str(data.get("input") or "")
        output_val = str(data.get("output") or "")
        cost = data.get("cost") or 0
        
        flat["message"] = f"Triggered tool execution: {tool}"
        flat["reasoning"] = f"Input: {input_val}\nOutput: {output_val}\nCost: ${cost}"
    elif event_type == "error":
        error = str(data.get("error") or "")
        recovery = str(data.get("recovery_attempt") or "")
        outcome = str(data.get("outcome") or "")
        
        flat["message"] = f"System Error Detected: {error}"
        flat["reasoning"] = f"Recovery Action: {recovery}\nOutcome: {outcome}"
        
    # Standard backup message mapping if empty
    if not flat["message"] and data.get("text"):
        flat["message"] = str(data.get("text"))
    if not flat["message"]:
        layer_val = entry.get('layer') or 'SYSTEM'
        flat["message"] = f"Orchestrating boardroom operations under {layer_val} layer."
        
    return flat

@router.get("", response_model=TraceResponseModel, summary="Get all boardroom audit traces")
async def get_all_traces():
    """Fetch all traces, flattened for Android compatibility."""
    logger.log_event("API_ROUTE", "TASK_START", "TraceService", {"message": "Fetching all traces"})
    logs = []
    
    # 1. Try Supabase
    if db_manager.is_active():
        try:
            result = db_manager.client.table("antigravity_trace")\
                .select("*")\
                .order("timestamp", desc=False)\
                .execute()
            if result.data:
                logs = result.data
        except Exception as e:
            logger.log_event("API_ROUTE", "ERROR", "TraceService", {"message": f"Supabase trace fetch failed: {str(e)}"})

    # 2. Fallback to local
    if not logs:
        trace_path = os.path.join(LOG_DIR, TRACE_FILE)
        if os.path.exists(trace_path):
            try:
                with open(trace_path, "r") as f:
                    logs = json.load(f)
            except Exception as e:
                logger.log_event("API_ROUTE", "ERROR", "TraceService", {"message": f"Local log read failed: {str(e)}"})

    # Flatten logs for Android consumption
    flattened_logs = [TraceEntryModel(**flatten_entry(e)) for e in logs]

    logger.log_event("API_ROUTE", "TASK_COMPLETE", "TraceService", {
        "message": f"Successfully retrieved {len(flattened_logs)} trace steps."
    })

    return TraceResponseModel(
        entries=flattened_logs,
        total_steps=len(flattened_logs),
        duration=calculate_total_duration(logs),
        agents_involved=BOARDROOM_SIZE,
        analysis_id="GLOBAL"
    )

@router.get("/{analysis_id}", response_model=TraceResponseModel, summary="Get trace for specific analysis session")
async def get_trace_for_analysis(analysis_id: str):
    """Fetch trace for a specific session, flattened for Android compatibility."""
    search_id = analysis_id.upper()
    logger.log_event("API_ROUTE", "TASK_START", "TraceService", {
        "message": f"Fetching trace for session: {search_id}",
        "session_id": search_id
    })
    
    logs = []
    
    if db_manager.is_active():
        try:
            result = db_manager.client.table("antigravity_trace")\
                .select("*")\
                .eq("session_id", search_id)\
                .order("timestamp", desc=False)\
                .execute()
            if result.data:
                logs = result.data
        except Exception as e:
            logger.log_event("API_ROUTE", "ERROR", "TraceService", {"message": f"Supabase trace query failed: {str(e)}"})

    # Fallback to local
    if not logs:
        trace_path = os.path.join(LOG_DIR, TRACE_FILE)
        if os.path.exists(trace_path):
            try:
                with open(trace_path, "r") as f:
                    full_trace = json.load(f)
                logs = [e for e in full_trace if e.get("session_id") == search_id]
            except Exception as e:
                logger.log_event("API_ROUTE", "ERROR", "TraceService", {"message": f"Local log parsing failed: {str(e)}"})

    # If no logs found anywhere, raise proper HTTPException instead of returning silent empties! (Resolves Flaw 12)
    if not logs:
        logger.log_event("API_ROUTE", "ERROR", "TraceService", {"message": f"No trace session found: {search_id}"})
        raise HTTPException(status_code=404, detail=f"No trace timeline found for session ID {search_id}")

    # Flatten logs for Android consumption
    flattened_logs = [TraceEntryModel(**flatten_entry(e)) for e in logs]

    logger.log_event("API_ROUTE", "TASK_COMPLETE", "TraceService", {
        "message": f"Successfully loaded {len(flattened_logs)} trace steps.",
        "session_id": search_id
    })

    return TraceResponseModel(
        entries=flattened_logs,
        total_steps=len(flattened_logs),
        duration=calculate_total_duration(logs),
        agents_involved=BOARDROOM_SIZE,
        analysis_id=search_id
    )
