import os
import datetime
from fastapi import APIRouter
from pydantic import BaseModel, Field
from founderlens_ai.utils.logger import logger

router = APIRouter()

# Read model name from env — same source of truth as crew.py
_MODEL = os.getenv("MODEL", "gpt-4o-mini")

class HealthResponse(BaseModel):
    status: str = Field(..., description="Overall operational status of backend services")
    model: str = Field(..., description="Active orchestrator model version")
    gemini: bool = Field(..., description="Operational status of API connection (Mapped to OpenAI)")
    supabase: bool = Field(..., description="Operational status of Supabase database connection")
    timestamp: str = Field(..., description="ISO 8601 formatted health timestamp")
    version: str = Field(..., description="Application version label")

@router.get("/health", response_model=HealthResponse, summary="Operational Health Check")
async def health_check():
    """Returns the operational status of all key microservices and dependencies."""
    logger.log_event("HEALTH", "TASK_START", "SystemHealth", {"message": "Conducting endpoint health audit"})

    # Evaluate OpenAI API status (mapped to gemini field for Android frontend compatibility)
    openai_status = bool(os.getenv("OPENAI_API_KEY"))

    # Supabase status — check if the db_manager is active without importing at module load
    supabase_status = False
    try:
        from founderlens_ai.db.database import db_manager
        supabase_status = db_manager.is_active()
    except Exception:
        pass

    overall = "ok" if openai_status else "degraded"

    res = HealthResponse(
        status=overall,
        model=_MODEL,
        gemini=openai_status,
        supabase=supabase_status,
        timestamp=datetime.datetime.now(datetime.timezone.utc).isoformat(),
        version="1.0.0"
    )

    logger.log_event("HEALTH", "TASK_COMPLETE", "SystemHealth", {
        "message": "Health audit complete",
        "status": overall
    })

    return res
