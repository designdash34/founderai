import os
import warnings
import json
from typing import Optional
from dotenv import load_dotenv
load_dotenv()

from crewai import Agent, Crew, Process, Task, LLM
from crewai.project import CrewBase, agent, crew, task, before_kickoff, after_kickoff

from founderlens_ai.utils.logger import logger
from founderlens_ai.tools.custom_tool import CompetitorSearchTool, ConstraintValidationTool, SafeSearchTool

warnings.filterwarnings("ignore", category=SyntaxWarning, module="pysbd")

def task_log_callback(output):
    """Named callback for task completion logging to ensure serialization support."""
    task_name = output.description[:20] if output.description else "Task"
    logger.log_event("TASK", "COMPLETE", task_name, {"output": output.raw[:200]})

# Load environment variables
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
ENVIRONMENT = os.getenv("ENVIRONMENT", "development")

@CrewBase
class FounderLensCrew:
    """FounderLens AI Crew — Powered by OpenAI gpt-4o-mini"""

    agents_config = "config/agents.yaml"
    tasks_config = "config/tasks.yaml"

    def __init__(self) -> None:
        if not OPENAI_API_KEY:
            raise ValueError("OPENAI_API_KEY is missing.")
        
        model_name = os.getenv("MODEL", "gpt-4o-mini")
        if "/" in model_name:
            model_name = model_name.split("/")[-1]
            
        self.openai_llm = LLM(
            model=model_name,
            api_key=OPENAI_API_KEY,
            temperature=0.3
        )
        
        self.search_tool = SafeSearchTool()
        self._initialized = False

    @before_kickoff
    def log_workplan(self, inputs):
        """Log the initial Workplan as the Antigravity Orchestrator Layer."""
        logger.log_event("ORCHESTRATOR", "WORKPLAN_START", "FounderLens_System", {
            "mission": "Consolidated OpenAI GPT-4o-mini Execution",
            "agents": ["intake", "insight", "conflict", "planner", "simulation", "recovery"],
            "platform": "Google Cloud AI",
            "inputs": {k: (v[:100] + '...' if isinstance(v, str) and len(v) > 100 else v) for k, v in inputs.items()}
        })
        return inputs

    @after_kickoff
    def log_final_state(self, result):
        """Log the Final State Diff and execution summary."""
        logger.log_event("ORCHESTRATOR", "EXECUTION_COMPLETE", "FounderLens_System", {
            "status": "Success",
            "summary": "Hybrid chain completed successfully.",
            "raw_result_preview": str(result)[:500] + "..."
        })
        return result

    def _ensure_initialized(self):
        """Lazily initialize agents to ensure decorators are processed."""
        if not self._initialized:
            _ = self.agents
            self._initialized = True

    @agent
    def intake_agent(self) -> Agent:
        """Data processing agent — Powered by OpenAI for structured execution."""
        return Agent(
            config=self.agents_config["intake_agent"],
            llm=self.openai_llm,
            verbose=True,
            allow_delegation=False,
            memory=False
        )

    @agent
    def insight_agent(self) -> Agent:
        """Strategic analysis agent — Powered by OpenAI for reasoning depth."""
        return Agent(
            config=self.agents_config["insight_agent"],
            llm=self.openai_llm,
            tools=[self.search_tool, CompetitorSearchTool()],
            reasoning=True,
            verbose=True,
            allow_delegation=False,
            memory=False
        )

    @agent
    def conflict_agent(self) -> Agent:
        """Conflict resolution agent — Powered by OpenAI for precision."""
        return Agent(
            config=self.agents_config["conflict_agent"],
            llm=self.openai_llm,
            verbose=True,
            allow_delegation=False,
            memory=False
        )

    @agent
    def action_planner_agent(self) -> Agent:
        """Action sequencing agent — Powered by OpenAI."""
        return Agent(
            config=self.agents_config["action_planner_agent"],
            llm=self.openai_llm,
            tools=[self.search_tool, ConstraintValidationTool()],
            reasoning=True,
            inject_date=True,
            verbose=True,
            allow_delegation=False,
            memory=False
        )

    @agent
    def simulation_agent(self) -> Agent:
        """Outcome simulation agent — Powered by OpenAI."""
        return Agent(
            config=self.agents_config["simulation_agent"],
            llm=self.openai_llm,
            verbose=True,
            allow_delegation=False,
            memory=False
        )

    @agent
    def recovery_agent(self) -> Agent:
        """System resilience agent — Powered by OpenAI for reliability."""
        return Agent(
            config=self.agents_config["recovery_agent"],
            llm=self.openai_llm,
            verbose=True,
            allow_delegation=False,
            memory=False
        )

    @task
    def intake_task(self) -> Task:
        return Task(
            config=self.tasks_config["intake_task"],
            agent=self.intake_agent(),
            callback=task_log_callback
        )

    @task
    def insight_task(self) -> Task:
        return Task(
            config=self.tasks_config["insight_task"],
            agent=self.insight_agent(),
            context=[self.intake_task()],
            callback=task_log_callback
        )

    @task
    def conflict_task(self) -> Task:
        return Task(
            config=self.tasks_config["conflict_task"],
            agent=self.conflict_agent(),
            context=[self.intake_task()],
            callback=task_log_callback
        )

    @task
    def planning_task(self) -> Task:
        return Task(
            config=self.tasks_config["planning_task"],
            agent=self.action_planner_agent(),
            context=[self.insight_task(), self.conflict_task()],
            callback=task_log_callback
        )

    @task
    def simulation_task(self) -> Task:
        return Task(
            config=self.tasks_config["simulation_task"],
            agent=self.simulation_agent(),
            context=[self.planning_task()],
            callback=task_log_callback
        )

    @task
    def recovery_task(self) -> Task:
        return Task(
            config=self.tasks_config["recovery_task"],
            agent=self.recovery_agent(),
            context=[
                self.intake_task(),
                self.insight_task(),
                self.conflict_task(),
                self.planning_task(),
                self.simulation_task()
            ],
            callback=task_log_callback
        )

    @crew
    def crew(self) -> Crew:
        """Creates the FounderLens AI Crew with Hybrid LLM setup"""
        self._ensure_initialized()

        return Crew(
            agents=self.agents,
            tasks=self.tasks,
            process=Process.sequential,
            verbose=True,
            memory=False
        )

    def run_with_inputs(self, inputs: dict) -> dict:
        """Run crew with given inputs and handle errors gracefully."""
        try:
            result = self.crew().kickoff(inputs=inputs)
            
            metrics = {
                "total_tokens": 0,
                "prompt_tokens": 0,
                "completion_tokens": 0,
                "successful_requests": 0
            }
            
            if hasattr(result, 'usage_metrics'):
                metrics = {
                    "total_tokens": result.usage_metrics.total_tokens,
                    "prompt_tokens": result.usage_metrics.prompt_tokens,
                    "completion_tokens": result.usage_metrics.completion_tokens,
                    "successful_requests": result.usage_metrics.successful_requests
                }
            
            cost = logger.calculate_cost(metrics["prompt_tokens"], metrics["completion_tokens"])
            
            return {
                "status": "success", 
                "result": str(result),
                "metrics": metrics,
                "cost_estimate_usd": cost,
                "tasks_output": result.tasks_output if hasattr(result, 'tasks_output') else []
            }
        except Exception as e:
            logger.log_error("Orchestrator", str(e), "Hybrid Recovery", "Failed")
            return {
                "status": "error",
                "error": str(e),
                "message": f"Crew execution failed: {str(e)}"
            }