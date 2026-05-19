"""
FounderLens AI API Routes Package.
This module exposes all individual endpoint routers for a clean namespace package.
"""

from .analyze import router as analyze_router
from .trace import router as trace_router
from .health import router as health_router
from .dashboard import router as dashboard_router
from .insights import router as insights_router
from .simulation import router as simulation_router
from .upload import router as upload_router

__all__ = [
    "analyze_router",
    "trace_router",
    "health_router",
    "dashboard_router",
    "insights_router",
    "simulation_router",
    "upload_router",
]
