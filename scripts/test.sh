#!/usr/bin/env bash
set -euo pipefail
echo "[test] Running Maven tests..."
mvn -B test
echo "[test] Tests finished. See surefire-reports/ for details."
ls -la target/surefire-reports || true