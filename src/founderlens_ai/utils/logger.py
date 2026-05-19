import os
import datetime
import json

# Use environment variable for log directory
LOG_DIR = os.getenv("LOG_DIR", "./logs")

class AntigravityLogger:
    def __init__(self, log_dir=LOG_DIR):
        self.log_dir = log_dir
        os.makedirs(self.log_dir, exist_ok=True)
        
        self.files = {
            "master": "master_log.md",
            "observation": "agent_observations.md",
            "reasoning": "reasoning_trace.md",
            "tool": "tool_calls.md",
            "error": "error_recovery.md",
            "trace_json": "antigravity_trace.json"
        }
        
        # Initialize MD files
        for key, filename in self.files.items():
            if filename.endswith(".md"):
                path = os.path.join(self.log_dir, filename)
                if not os.path.exists(path):
                    with open(path, "w") as f:
                        f.write(f"# {filename.replace('_', ' ').replace('.md', '').title()}\n\n")
        
        # Initialize JSON trace file if it doesn't exist
        trace_path = os.path.join(self.log_dir, self.files["trace_json"])
        if not os.path.exists(trace_path):
            with open(trace_path, "w") as f:
                json.dump([], f)

    def _get_timestamp(self):
        return datetime.datetime.now().isoformat()

    def _append_to_json_trace(self, entry):
        trace_path = os.path.join(self.log_dir, self.files["trace_json"])
        try:
            with open(trace_path, "r") as f:
                trace_data = json.load(f)
            trace_data.append(entry)
            with open(trace_path, "w") as f:
                json.dump(trace_data, f, indent=2)
        except Exception as e:
            print(f"Error logging to JSON trace: {e}")

    def calculate_cost(self, prompt_tokens, completion_tokens):
        """Estimate cost in USD for OpenAI GPT-4o-mini."""
        # Pricing: $0.15 per 1M prompt tokens, $0.60 per 1M completion tokens
        prompt_cost = (prompt_tokens / 1_000_000) * 0.15
        completion_cost = (completion_tokens / 1_000_000) * 0.60
        return round(prompt_cost + completion_cost, 6)

    def log_event(self, layer, event_type, agent, data):
        """Structured logging for the Antigravity Trace Layer."""
        timestamp = self._get_timestamp()
        session_id = data.get("session_id") or "SYSTEM"
        
        entry = {
            "timestamp": timestamp,
            "layer": layer,
            "type": event_type,
            "agent": agent,
            "data": data
        }
        
        # 1. Local Persistence (JSON Trace)
        self._append_to_json_trace(entry)
        
        # 2. Local Persistence (Master MD)
        md_entry = f"[{timestamp}] [{layer}] [{event_type}] ({agent}) — {json.dumps(data)}\n"
        with open(os.path.join(self.log_dir, self.files["master"]), "a") as f:
            f.write(md_entry)
            
        # 3. Cloud Persistence (Supabase Mirroring)
        from founderlens_ai.db.database import db_manager
        if db_manager.is_active():
            try:
                # We fire and forget to avoid blocking agent execution
                db_manager.client.table("antigravity_trace").insert({
                    "session_id": session_id,
                    "layer": layer,
                    "type": event_type,
                    "agent": agent,
                    "data": data,
                    "timestamp": timestamp
                }).execute()
            except Exception as e:
                print(f"Supabase logging failed: {e}")

    def log(self, agent, action_type, message):
        """Legacy support for simple logging."""
        self.log_event("AGENT_LOG", action_type, agent, {"message": message})

    def log_observation(self, agent, text):
        timestamp = self._get_timestamp()
        self.log_event("REASONING", "OBSERVATION", agent, {"text": text})
        entry = f"### Agent: {agent}\n**Timestamp**: {timestamp}\n**Observed**: {text}\n\n"
        with open(os.path.join(self.log_dir, self.files["observation"]), "a") as f:
            f.write(entry)

    def log_reasoning(self, agent, steps):
        timestamp = self._get_timestamp()
        self.log_event("REASONING", "STEPS", agent, {"steps": steps})
        entry = f"### Agent: {agent}\n**Timestamp**: {timestamp}\n"
        for i, step in enumerate(steps, 1):
            entry += f"Step {i} → {step}\n"
        entry += "\n"
        with open(os.path.join(self.log_dir, self.files["reasoning"]), "a") as f:
            f.write(entry)

    def log_tool_call(self, agent, tool, input_data, output, ms=0, cost=0):
        self.log_event("EXECUTION", "TOOL_CALL", agent, {
            "tool": tool,
            "input": input_data,
            "output": output,
            "latency_ms": ms,
            "cost": cost
        })
        timestamp = self._get_timestamp()
        entry = f"[{timestamp}] [{agent}] TOOL_CALL — {tool} called | Input: {input_data} | Output: {output} | Latency: {ms}ms | Cost: ${cost}\n"
        with open(os.path.join(self.log_dir, self.files["tool"]), "a") as f:
            f.write(entry)

    def log_error(self, agent, error, recovery, outcome):
        self.log_event("RECOVERY", "ERROR", agent, {
            "error": error,
            "recovery_attempt": recovery,
            "outcome": outcome
        })
        timestamp = self._get_timestamp()
        entry = f"[{timestamp}] [{agent}] ERROR — {error} | Recovery: {recovery} | Outcome: {outcome}\n"
        with open(os.path.join(self.log_dir, self.files["error"]), "a") as f:
            f.write(entry)

logger = AntigravityLogger()
