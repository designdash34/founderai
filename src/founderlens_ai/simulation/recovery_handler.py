from ..utils.logger import logger

def handle_recovery(failure_type, context):
    logger.log("RECOVERY_HANDLER", "RECOVERY", f"Handling {failure_type}")
    
    if failure_type == "missing_data":
        logger.log("RECOVERY_HANDLER", "DECISION", "Using industry defaults for missing data")
        return {"status": "ESTIMATED", "data": {"mrr": 5000, "churn": 8.0}}
        
    elif failure_type == "json_failure":
        logger.log("RECOVERY_HANDLER", "DECISION", "Returning partial results due to JSON failure")
        return {"status": "PARTIAL", "warning": "Some data could not be parsed"}
        
    elif failure_type == "api_timeout":
        logger.log("RECOVERY_HANDLER", "DECISION", "Using low-confidence fallback for timeout")
        return {"status": "LOW_CONFIDENCE", "message": "API took too long, showing cached analysis"}
        
    elif failure_type == "contradiction":
        logger.log("RECOVERY_HANDLER", "DECISION", "Resolving contradiction via recency weighting")
        return {"status": "RESOLVED", "action": "Flagging conflict in UI"}
        
    return {"status": "UNRESOLVED"}
