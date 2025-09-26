#!/usr/bin/env bash
set -euo pipefail
echo "[build] Starting Maven build (skip tests)..."
mvn -B -DskipTests clean package
echo "[build] Build finished. Artifact(s) in target/:"
ls -la target/*.jar || true