#!/bin/bash
set -e

echo "==============================================="
echo " Shadow Ledger System - Acceptance Test Runner "
echo "==============================================="

GATEWAY_URL=http://localhost:8080

# Generate unique suffix for this run
RUN_ID=$(date +%s)

EVENT1_ID="E-${RUN_ID}-1"
EVENT2_ID="E-${RUN_ID}-2"

echo ""
echo "▶ Starting infrastructure (Docker Compose)..."
docker-compose up -d

echo ""
echo "▶ Waiting for infrastructure to stabilize..."
sleep 25

# Optional: Uncomment ONLY if you want a clean DB every run
# echo "▶ Resetting Postgres data..."
# docker-compose stop
# docker-compose rm -f postgres
# docker-compose up -d
# sleep 20

echo ""
echo "▶ Generating JWT tokens..."

USER_TOKEN=$(curl -s -X POST "$GATEWAY_URL/auth/token?role=user" | jq -r .token)
AUDITOR_TOKEN=$(curl -s -X POST "$GATEWAY_URL/auth/token?role=auditor" | jq -r .token)
ADMIN_TOKEN=$(curl -s -X POST "$GATEWAY_URL/auth/token?role=admin" | jq -r .token)

echo "✔ Tokens generated"

echo ""
echo "▶ Submitting events ($EVENT1_ID, $EVENT2_ID)..."

curl -s -X POST "$GATEWAY_URL/events" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
        \"eventId\": \"$EVENT1_ID\",
        \"accountId\": \"A10\",
        \"type\": \"credit\",
        \"amount\": 1000,
        \"timestamp\": $(date +%s000)
      }"

curl -s -X POST "$GATEWAY_URL/events" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
        \"eventId\": \"$EVENT2_ID\",
        \"accountId\": \"A10\",
        \"type\": \"debit\",
        \"amount\": 250,
        \"timestamp\": $(date +%s000)
      }"

echo "✔ Events submitted"

echo ""
echo "▶ Waiting for Kafka consumption..."
sleep 10

echo ""
echo "▶ Fetching shadow balance (before drift)..."

curl -s "$GATEWAY_URL/accounts/A10/shadow-balance" \
  -H "Authorization: Bearer $USER_TOKEN" | jq .

echo ""
echo "▶ Running drift check (CBS balance = 800)..."

curl -s -X POST "$GATEWAY_URL/drift-check" \
  -H "Authorization: Bearer $AUDITOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
        { "accountId": "A10", "reportedBalance": 800 }
      ]'

echo "✔ Drift check executed"

echo ""
echo "▶ Waiting for correction processing..."
sleep 10

echo ""
echo "▶ Fetching shadow balance (after drift correction)..."

curl -s "$GATEWAY_URL/accounts/A10/shadow-balance" \
  -H "Authorization: Bearer $USER_TOKEN" | jq .

echo ""
echo "▶ Running manual correction (admin +50)..."

curl -s -X POST "$GATEWAY_URL/correct/A10?amount=50" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

sleep 5

echo ""
echo "▶ Final shadow balance..."

curl -s "$GATEWAY_URL/accounts/A10/shadow-balance" \
  -H "Authorization: Bearer $USER_TOKEN" | jq .

echo ""
echo "==============================================="
echo " ✅ Acceptance tests completed successfully"
echo "==============================================="
