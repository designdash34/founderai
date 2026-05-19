import os
import json
import time
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage
from founderlens_ai.utils.logger import logger

class FounderLensBaseline:
    """Non-agentic baseline for comparison (mandatory hackathon requirement).
    Uses LangChain + OpenAI for a single, direct prompt analysis.
    """
    def __init__(self):
        self.llm = ChatOpenAI(
            model="gpt-4o-mini",
            temperature=0.3,
            api_key=os.getenv("OPENAI_API_KEY")
        )

    async def run(self, inputs: dict) -> dict:
        start_time = time.time()

        prompt = f"""You are a standard business analyst (Non-Agentic Baseline).
Analyze the following business startup/project idea and data for {inputs.get('startup_name', 'the startup')}:

Startup/Project Idea: {inputs.get('startup_idea', 'No idea specified')}
Data: {json.dumps(inputs.get('data_sources', {}))}

Tasks:
1. List key insights.
2. Detect any obvious contradictions.
3. Suggest a few actions.
4. Predict a basic outcome.

Respond ONLY with a valid JSON object (no markdown, no code block) containing:
{{
  "insights": [],
  "contradictions": [],
  "action_chain": [],
  "simulation_result": {{}},
  "summary": "string"
}}"""

        try:
            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            raw_text = response.content.strip()

            # Strip markdown code fences if present
            if raw_text.startswith("```"):
                raw_text = raw_text.split("```")[1]
                if raw_text.startswith("json"):
                    raw_text = raw_text[4:]
                raw_text = raw_text.strip()

            data = json.loads(raw_text)
            runtime = time.time() - start_time

            return {
                "status": "success",
                "result": data,
                "total_runtime_seconds": round(runtime, 2),
                "is_baseline": True
            }
        except Exception as e:
            logger.log_event("BASELINE", "ERROR", "System", {"message": str(e)})
            return {"status": "error", "error": str(e)}
