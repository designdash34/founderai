import os
from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from founderlens_ai.api.routes import (
    analyze_router,
    trace_router,
    health_router,
    dashboard_router,
    insights_router,
    simulation_router,
    upload_router,
)

# Metadata for Swagger (Resolves Flaw 13 - added Simulation and Insights tags descriptions)
tags_metadata = [
    {
        "name": "Analysis",
        "description": "The core Byte Minds boardroom engine. Use this to start autonomous multi-agent business analysis.",
    },
    {
        "name": "Trace",
        "description": "Antigravity Trace Layer access. Fetch real-time reasoning and agent tool calls.",
    },
    {
        "name": "Dashboard",
        "description": "Statistics and activity timeline charts for the FounderLens app.",
    },
    {
        "name": "Insights",
        "description": "Dynamic trends, opportunities, B2B expansions, and strategic risk observations.",
    },
    {
        "name": "Simulation",
        "description": "Scenario forecasting, Customer acquisition cost and customer churn projection metrics.",
    },
    {
        "name": "System",
        "description": "Administrative endpoints, high-safety file uploads, and operational health checks.",
    },
]

app = FastAPI(
    title="FounderLens AI — Antigravity API",
    description="Autonomous multi-agent business intelligence and action orchestration engine for the Byte Minds team.",
    version="1.0.0",
    openapi_tags=tags_metadata
)

# Scope origins to an env-configurable list.
_raw_origins = os.getenv("ALLOWED_ORIGINS", "*")
if _raw_origins == "*":
    ALLOWED_ORIGINS = ["*"]
else:
    ALLOWED_ORIGINS = [o.strip() for o in _raw_origins.split(",") if o.strip()]
    if "null" not in ALLOWED_ORIGINS:
        ALLOWED_ORIGINS.append("null")

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 1. Include Routers with standard v1 prefix for Android Client Retrofit interfaces
app.include_router(analyze_router, prefix="/api/v1/analysis", tags=["Analysis"])
app.include_router(trace_router, prefix="/api/v1/trace", tags=["Trace"])
app.include_router(dashboard_router, prefix="/api/v1/dashboard", tags=["Dashboard"])
app.include_router(insights_router, prefix="/api/v1/insights", tags=["Insights"])
app.include_router(simulation_router, prefix="/api/v1/simulation", tags=["Simulation"])
app.include_router(upload_router, prefix="/api/v1/upload", tags=["System"]) # Included proper upload router (Resolves Flaw 5)

# 2. Register Health routers (Resolves Flaw 6 - No triple duplicate health decorators!)
app.include_router(health_router, prefix="", tags=["System"])     # Binds /health
app.include_router(health_router, prefix="/api", tags=["System"]) # Binds /api/health

@app.get("/", tags=["System"])
async def root():
    return {
        "status": "online",
        "system": "FounderLens AI",
        "identity": "Byte Minds",
        "version": "1.0.0",
        "docs": "/docs"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
