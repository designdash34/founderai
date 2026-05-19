import sys
import os
import warnings
from datetime import datetime

os.environ.setdefault("ENVIRONMENT", "development")

from founderlens_ai.crew import FounderLensCrew
from founderlens_ai.utils.logger import logger

warnings.filterwarnings("ignore", category=SyntaxWarning, module="pysbd")

def get_test_inputs():
    """Load a comprehensive set of 5-source inputs for the Byte Minds Intake Protocol."""
    return {
        'startup_name': "FounderLens AI",
        'problem_statement': "Optimizing multi-agent workflows for autonomous business intelligence.",
        'data_sources': [
            {
                "type": "DOCUMENT",
                "content": "Q1 Operational Report: Churn has increased to 12% due to onboarding friction. Revenue grew by 5% but customer satisfaction is at an all-time low of 64/100."
            },
            {
                "type": "STRUCTURED",
                "content": {
                    "monthly_metrics": [
                        {"month": "Jan", "revenue": 10000, "churn": 0.05},
                        {"month": "Feb", "revenue": 11000, "churn": 0.07},
                        {"month": "Mar", "revenue": 10500, "churn": 0.12}
                    ],
                    "anomaly_detected": "Revenue dip in March correlates with churn spike."
                }
            },
            {
                "type": "WEB_INTEL",
                "content": "Article from TechCrunch: 'FounderLens AI competitor raises $50M to automate SaaS BI.' Sentiment: Neutral. Relevance: High."
            },
            {
                "type": "DASHBOARD",
                "content": "| Metric | Value | Trend | Status | \n| MRR | $10,500 | DOWN | WARNING | \n| Churn | 12% | UP | CRITICAL | \n| NPS | 15 | DOWN | CRITICAL |"
            },
            {
                "type": "REAL-TIME",
                "content": "simulate"
            }
        ]
    }

def run():
    """Run the crew locally with the Byte Minds test suite."""
    inputs = get_test_inputs()
    logger.log("SYSTEM", "TASK_START", f"Starting Byte Minds Intake Protocol for: {inputs['startup_name']}")

    try:
        result = FounderLensCrew().crew().kickoff(inputs=inputs)
        logger.log("SYSTEM", "TASK_COMPLETE", f"Full courtroom execution completed.")
        print(f"\n✅ Final Boardroom Decision Package: {result}")
        return result
    except Exception as e:
        logger.log("SYSTEM", "TASK_FAILED", str(e))
        raise Exception(f"An error occurred while running the crew: {e}")



if __name__ == "__main__":
    if len(sys.argv) > 1:
        command = sys.argv[1]
        if command == "run":
            run()
        else:
            run()
    else:
        run()