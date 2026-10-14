#!/usr/bin/env bash
# Usage: scripts/health-check.sh [base-url]   e.g. scripts/health-check.sh http://localhost:8080/certificate-portal
set -euo pipefail
BASE="${1:-http://localhost:8080/certificate-portal}"
echo "GET $BASE/health"
curl --fail --silent --show-error --retry 10 --retry-delay 3 --retry-all-errors --retry-connrefused "$BASE/health"
echo
