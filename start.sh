#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
cd backend
java -jar target/backend-0.0.1-SNAPSHOT.jar