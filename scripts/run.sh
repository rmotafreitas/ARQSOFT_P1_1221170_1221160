#!/usr/bin/env bash
set -euo pipefail
PORT="${1:-8080}"
echo "[run] Starting application on port $PORT..."

# Prefer runnable jar if present
JAR=$(ls target/*.jar 2>/dev/null | grep -v "original" | head -n1 || true)
if [[ -n "$JAR" ]]; then
  echo "[run] Found jar: $JAR"
  java -Dserver.port="$PORT" -jar "$JAR"
else
  echo "[run] No jar found, falling back to 'mvn spring-boot:run'"
  mvn -Dspring-boot.run.arguments="--server.port=$PORT" spring-boot:run
fi