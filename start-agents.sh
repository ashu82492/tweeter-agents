#!/usr/bin/env bash
set -euo pipefail

count="${1:-100}"
: "${AGENT_CREDENTIAL_ENCRYPTION_KEY:?Set AGENT_CREDENTIAL_ENCRYPTION_KEY to a Base64 16, 24, or 32-byte AES key}"

export THINK9_API_BASE_URL="${THINK9_API_BASE_URL:-http://localhost:8081/api/v1}"
export KAFKA_BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:29092}"

AGENT_BOOTSTRAP_ENABLED=true \
AGENT_BOOTSTRAP_COUNT="$count" \
mvn -pl agent-management spring-boot:run