from pydantic import BaseModel, Field, field_validator
from typing import List, Literal, Optional
import uuid

class DataSource(BaseModel):
    type: Literal["pdf", "csv", "json", "text", "url", "realtime"]
    content: Optional[str] = None
    file_path: Optional[str] = None
    url: Optional[str] = None

    @field_validator('type')
    @classmethod
    def validate_type(cls, v):
        allowed = ["pdf", "csv", "json", "text", "url", "realtime"]
        if v not in allowed:
            raise ValueError(f"type must be one of {allowed}")
        return v

class AnalyzeRequest(BaseModel):
    session_id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    startup_name: str = Field(..., min_length=1, max_length=200)
    problem_statement: str = Field(..., min_length=1)
    data_sources: List[DataSource] = Field(default_factory=list, min_length=0)

    @field_validator('startup_name')
    @classmethod
    def validate_startup_name(cls, v):
        if not v.strip():
            raise ValueError("startup_name cannot be empty or whitespace only")
        return v.strip()

class HealthResponse(BaseModel):
    status: str
    version: str = "1.0.0"
    environment: str

class ErrorResponse(BaseModel):
    error: str
    detail: Optional[str] = None
    session_id: Optional[str] = None