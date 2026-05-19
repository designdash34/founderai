from fastapi import APIRouter, UploadFile, File, HTTPException
import shutil
import os
import uuid
from pydantic import BaseModel, Field
from typing import Dict, Any
from founderlens_ai.parsers.pdf_parser import parse_pdf
from founderlens_ai.parsers.csv_parser import parse_csv_json
from founderlens_ai.utils.logger import logger

router = APIRouter()

# Unified uploads directory
UPLOAD_DIR = os.getenv("UPLOAD_DIR", "uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)

# 15MB Size limit
MAX_FILE_SIZE = 15 * 1024 * 1024 

class UploadResponse(BaseModel):
    file_id: str = Field(..., description="Unique generated ID for the uploaded session")
    file_path: str = Field(..., description="Path to the saved file on disk")
    filename: str = Field(..., description="Original name of the uploaded file")
    status: str = Field("success", description="Status of the upload action")
    parsed_preview: Dict[str, Any] = Field(..., description="Preview dictionary parsed from the file content")

@router.post("", response_model=UploadResponse, summary="Upload PDF or CSV business reports")
async def upload_file(file: UploadFile = File(...)):
    """Uploads, validates, and parses business reports (PDF, CSV, or JSON)."""
    
    # 1. Validate File Extension
    filename = file.filename or ""
    extension = os.path.splitext(filename)[1].lower()
    if extension not in [".pdf", ".csv", ".json"]:
        logger.log_event("API_ROUTE", "ERROR", "UploadSystem", {"message": f"Invalid extension: {extension}"})
        raise HTTPException(
            status_code=400, 
            detail="Unsupported file format. Only PDF, CSV, and JSON files are supported."
        )

    # 2. Validate File Size
    # Read file size in a stream-safe way
    file.file.seek(0, 2)
    file_size = file.file.tell()
    file.file.seek(0) # Reset stream pointer
    
    if file_size > MAX_FILE_SIZE:
        logger.log_event("API_ROUTE", "ERROR", "UploadSystem", {"message": f"File size too large: {file_size} bytes"})
        raise HTTPException(
            status_code=400,
            detail="File is too large. Maximum allowed size is 15MB."
        )

    file_id = str(uuid.uuid4()).upper()
    safe_filename = f"{file_id}_{filename}"
    file_path = os.path.join(UPLOAD_DIR, safe_filename)
    
    logger.log_event("API_ROUTE", "TASK_START", "UploadSystem", {"message": f"Uploading: {filename}"})
    
    try:
        # Save file to disk
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
        
        # Parse preview based on format
        parsed_preview = {}
        if filename.endswith(".pdf"):
            parsed_preview = parse_pdf(file_path)
        elif filename.endswith((".csv", ".json")):
            parsed_preview = parse_csv_json(file_path)

        logger.log_event("API_ROUTE", "TASK_COMPLETE", "UploadSystem", {
            "message": f"Uploaded successfully: {filename}",
            "file_id": file_id
        })

        return UploadResponse(
            file_id=file_id,
            file_path=file_path,
            filename=filename,
            status="success",
            parsed_preview=parsed_preview
        )
    except Exception as e:
        # Proper error logging & clean exception raising (Resolves Flaw 7)
        logger.log_event("API_ROUTE", "ERROR", "UploadSystem", {"message": str(e)})
        if os.path.exists(file_path):
            try:
                os.remove(file_path)
            except:
                pass
        raise HTTPException(status_code=500, detail=f"File saving failed: {str(e)}")
