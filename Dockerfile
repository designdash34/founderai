FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim AS builder

# Enable bytecode compilation and use copy mode for portability
ENV UV_COMPILE_BYTECODE=1 UV_LINK_MODE=copy

WORKDIR /app

# 1. Install dependencies first (layer cache optimization)
# Removed --mount=type=cache to resolve persistent build environment validation issues
COPY uv.lock pyproject.toml ./
RUN uv sync --frozen --no-install-project --no-dev

# 2. Copy the rest of the source code
COPY . .

# 3. Install the project itself
RUN uv sync --frozen --no-dev


# ── Runtime Stage ─────────────────────────────────────────────────────────
FROM python:3.12-slim-bookworm

# Set essential production environment variables
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONPATH="/app/src" \
    PATH="/app/.venv/bin:$PATH" \
    LOG_DIR="/app/logs" \
    ENVIRONMENT=production

WORKDIR /app

# Copy the application and virtual environment from the builder stage
COPY --from=builder /app /app

# Create persistent directories for logs and uploads
RUN mkdir -p logs uploads && \
    chmod -R 777 logs uploads

# Railway provides the $PORT environment variable at runtime
EXPOSE 8000

# Health check using Python's built-in urllib
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python3 -c "import urllib.request; urllib.request.urlopen('http://localhost:${PORT:-8000}/api/health')" || exit 1

# Start the FastAPI server
CMD ["sh", "-c", "uvicorn founderlens_ai.api.main:app --host 0.0.0.0 --port ${PORT:-8000} --workers 1 --log-level info"]
