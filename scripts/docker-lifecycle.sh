#!/usr/bin/env bash
# Task 11: walks through the whole container life cycle and prints every command, so you can save it as evidence:
#   scripts/docker-lifecycle.sh 2>&1 | tee docs/evidence/docker-command-log.txt
set -uo pipefail
cd "$(dirname "$0")/.."
IMAGE=certificate-portal
TAG=1.0.0
NAME=certportal-demo
PORT=8082

run() { echo; echo "\$ $*"; "$@"; }

run mvn -B clean package -DskipTests
run docker build -t "$IMAGE:$TAG" -t "$IMAGE:latest" .
run docker image ls "$IMAGE"
run docker image inspect "$IMAGE:$TAG" --format '{{.Id}} {{.Size}} bytes, created {{.Created}}'
run docker rm -f "$NAME" 2>/dev/null
run docker run -d --name "$NAME" -p "$PORT:8080" -v certportal-demo-data:/data "$IMAGE:$TAG"
run docker ps --filter "name=$NAME"
echo; echo "Waiting for the application to start..."
for i in $(seq 1 30); do curl -fs "http://localhost:$PORT/certificate-portal/health" >/dev/null && break; sleep 2; done
run curl -s "http://localhost:$PORT/certificate-portal/health"
run docker logs --tail 20 "$NAME"
run docker inspect --format '{{.State.Status}} (health: {{.State.Health.Status}})' "$NAME"
run docker stop "$NAME"
run docker ps -a --filter "name=$NAME"
run docker start "$NAME"
run docker restart "$NAME"
run docker ps --filter "name=$NAME"
run docker rm -f "$NAME"
run docker ps -a --filter "name=$NAME"
echo; echo "Lifecycle complete. Remove the demo volume with: docker volume rm certportal-demo-data"
