# Drift Correction Service

A reconciliation service that detects and automatically corrects balance discrepancies between the Core Banking System (CBS) and the Shadow Ledger. When drift is detected, the service publishes correction events to Kafka to bring the shadow ledger back into alignment with the authoritative CBS balances.

## Features

- **Automated Drift Detection**: Compares CBS balances against shadow ledger balances
- **Automatic Correction**: Publishes correction events to reconcile discrepancies
- **Manual Correction API**: Allows manual intervention for specific accounts
- **Batch Processing**: Handles multiple account balance checks in a single request
- **Audit Trail**: All corrections are logged with unique event IDs
- **Distributed Tracing**: Built-in trace ID propagation for debugging

## Architecture

### Components

- **DriftCheckController**: REST endpoint for batch balance reconciliation
- **ManualCorrectionController**: REST endpoint for manual corrections
- **DriftDetectionService**: Core logic for detecting and correcting drift
- **CorrectionPublisher**: Publishes correction events to Kafka
- **ShadowLedgerRepository**: Queries shadow balances from the ledger database

### Data Flow

```
CBS Balance Report → DriftCheckController → DriftDetectionService
                                                    ↓
                                            Compare Balances
                                                    ↓
                                         Drift Detected? → Yes → Publish Correction
                                                    ↓              ↓
                                                   No          Kafka Topic
                                                    ↓              ↓
                                                 Done      Shadow Ledger Service
```

## Prerequisites

- Java 17+
- PostgreSQL 12+ (shared with Shadow Ledger Service)
- Apache Kafka 2.8+
- Maven 3.6+

## Quick Start

### 1. Ensure Dependencies are Running

This service requires the same PostgreSQL and Kafka instances as the Shadow Ledger Service:

```bash
# PostgreSQL should already be running on port 5433
# Kafka should already be running on port 9092
```

### 2. Build and Run

```bash
./gradlew clean install
./gradlew bootRun
```

The service will start on port **8083**.

## API Reference

### Batch Drift Check

Check multiple account balances and automatically correct any drift.

```http
POST /drift-check
Content-Type: application/json
X-Internal-Call: true
X-Trace-Id: optional-trace-id

[
  {
    "accountId": "ACC123",
    "reportedBalance": 1000.00
  },
  {
    "accountId": "ACC456",
    "reportedBalance": 2500.50
  }
]
```

**Response:** `200 OK` (no body)

**Behavior:**
- Compares each CBS balance against the shadow ledger
- If drift is detected, publishes a correction event to `transactions.corrections`
- Logs all actions for audit purposes

### Manual Correction

Manually apply a correction to a specific account.

```http
POST /correct/{accountId}?amount=100.00
X-Internal-Call: true
X-Trace-Id: optional-trace-id
```

**Example:**
```bash
curl -X POST \
  -H "X-Internal-Call: true" \
  "http://localhost:8083/correct/ACC123?amount=100.00"
```

**Response:** `200 OK` (no body)

**Note:** This always creates a credit correction. For debit corrections, use a negative amount or implement via drift-check.

## Event Schema

### CorrectionEvent

Published to `transactions.corrections` topic:

```json
{
  "eventId": "CORR-a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "accountId": "ACC123",
  "type": "credit",
  "amount": 50.00,
  "timestamp": 1703001234567
}
```

**Fields:**
- `eventId` (String): Unique identifier (prefixed with `CORR-` for auto-corrections, `MANUAL-` for manual)
- `accountId` (String): Account identifier
- `type` (String): `"credit"` if CBS > Shadow, `"debit"` if CBS < Shadow
- `amount` (BigDecimal): Absolute value of the drift amount
- `timestamp` (Long): Unix timestamp in milliseconds

## Configuration

Key configurations in `application.yml`:

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ledgerdb
    username: ledger
    password: ledger

  kafka:
    bootstrap-servers: localhost:9092

logging:
  level:
    com.shadowledger.drift: DEBUG
```

## How Drift Detection Works

### 1. Balance Comparison

```
CBS Reported Balance:  $1,050.00
Shadow Ledger Balance: $1,000.00
Drift:                 $   50.00 (positive = CBS higher)
```

### 2. Correction Logic

| Scenario | CBS Balance | Shadow Balance | Drift | Correction Type | Amount |
|----------|-------------|----------------|-------|-----------------|--------|
| CBS Higher | $1,050 | $1,000 | +$50 | **credit** | $50 |
| CBS Lower | $950 | $1,000 | -$50 | **debit** | $50 |
| Match | $1,000 | $1,000 | $0 | none | - |

### 3. Correction Event

The service publishes a correction event that the Shadow Ledger Service consumes and processes like any other transaction.

## Usage Examples

### Scenario 1: Daily Reconciliation

Run a nightly batch job to check all accounts:

```bash
#!/bin/bash
# daily-reconciliation.sh

# Get balances from CBS (example)
CBS_BALANCES='[
  {"accountId":"ACC001","reportedBalance":1500.00},
  {"accountId":"ACC002","reportedBalance":2300.50},
  {"accountId":"ACC003","reportedBalance":750.25}
]'

curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-Internal-Call: true" \
  -d "$CBS_BALANCES" \
  http://localhost:8083/drift-check
```

### Scenario 2: Manual Correction

An operator notices an issue and manually corrects it:

```bash
# Add $100 credit to account ACC123
curl -X POST \
  -H "X-Internal-Call: true" \
  "http://localhost:8083/correct/ACC123?amount=100.00"
```

### Scenario 3: Integration Testing

```bash
# 1. Create shadow ledger entries
curl -X POST \
  -H "X-Internal-Call: true" \
  http://localhost:8082/process-event \
  -d '{"eventId":"evt-001","accountId":"ACC123","type":"credit","amount":1000}'

# 2. Simulate CBS reporting different balance
curl -X POST \
  -H "X-Internal-Call: true" \
  -H "Content-Type: application/json" \
  http://localhost:8083/drift-check \
  -d '[{"accountId":"ACC123","reportedBalance":1050}]'

# 3. Verify correction was published to Kafka
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic transactions.corrections --from-beginning

# 4. Check shadow balance updated
curl -H "X-Internal-Call: true" \
  http://localhost:8082/accounts/ACC123/shadow-balance
```

## Security

### Internal-Only Filter

All endpoints require the `X-Internal-Call: true` header to prevent unauthorized external access.

```bash
# This will fail (403 Forbidden)
curl -X POST http://localhost:8083/drift-check -d '[]'

# This will succeed
curl -X POST \
  -H "X-Internal-Call: true" \
  -H "Content-Type: application/json" \
  http://localhost:8083/drift-check \
  -d '[]'
```

## Monitoring

### Health Check

```bash
curl http://localhost:8083/actuator/health
```

### Metrics

```bash
curl http://localhost:8083/actuator/metrics
```
