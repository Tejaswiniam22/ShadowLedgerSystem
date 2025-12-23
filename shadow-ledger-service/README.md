# Shadow Ledger Service

A high-performance, event-sourced ledger system that maintains shadow account balances by consuming transaction events from Kafka. Built with Spring Boot, this service ensures data integrity through idempotent event processing and enforces business rules like preventing negative balances.

## Features

- **Event-Driven Architecture**: Consumes transaction events from Kafka topics
- **Idempotent Processing**: Prevents duplicate event processing using unique event IDs
- **Real-time Balance Computation**: Calculates account balances on-the-fly using window functions
- **Negative Balance Prevention**: Enforces business rules to maintain account integrity
- **Append-Only Ledger**: Immutable transaction history for audit compliance
- **Distributed Tracing**: Built-in trace ID propagation for request tracking
- **Correction Handling**: Separate consumer for transaction corrections

## Architecture

### Components

- **EventConsumerService**: Kafka listeners for raw transactions and corrections
- **ShadowLedgerService**: Core business logic with idempotency and balance validation
- **LedgerEntryRepository**: Data access with optimized balance computation query
- **ShadowBalanceController**: REST API for retrieving account balances

### Data Flow

```
Kafka (transactions.raw) → EventConsumerService → ShadowLedgerService → PostgreSQL
                                                          ↓
                                                  Balance Validation
                                                          ↓
                                                  Append to Ledger
```

## Prerequisites

- Java 17+
- PostgreSQL 12+
- Apache Kafka 2.8+
- Maven 3.6+

## Quick Start

### 1. Start Dependencies

```bash
# Start PostgreSQL
docker run -d \
  --name postgres-ledger \
  -e POSTGRES_DB=ledgerdb \
  -e POSTGRES_USER=ledger \
  -e POSTGRES_PASSWORD=ledger \
  -p 5433:5432 \
  postgres:14

# Start Kafka (with Zookeeper)
docker run -d \
  --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  confluentinc/cp-kafka:latest
```

### 2. Build and Run

```bash
./gradlew clean install
./gradlew bootRun
```

The service will start on port **8082**.

### 3. Create Kafka Topics

```bash
kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic transactions.raw \
  --partitions 3 \
  --replication-factor 1

kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic transactions.corrections \
  --partitions 3 \
  --replication-factor 1
```

## API Reference

### Get Shadow Balance

Retrieve the current balance for an account.

```http
GET /accounts/{accountId}/shadow-balance
X-Internal-Call: true
X-Trace-Id: optional-trace-id
```

**Response:**
```json
{
  "accountId": "ACC123",
  "balance": 1500.50,
  "lastEvent": "evt-789"
}
```

## Event Schema

### EventRequest

```json
{
  "eventId": "evt-123",
  "accountId": "ACC123",
  "type": "credit",
  "amount": 100.00,
  "timestamp": 1703001234567
}
```

**Fields:**
- `eventId` (String): Unique identifier for idempotency
- `accountId` (String): Account identifier
- `type` (String): Either `"credit"` or `"debit"`
- `amount` (BigDecimal): Transaction amount
- `timestamp` (Long): Unix timestamp in milliseconds (optional)

## Configuration

Key configurations in `application.yml`:

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/ledgerdb
    username: ledger
    password: ledger

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: shadow-ledger-group
      auto-offset-reset: earliest
      enable-auto-commit: false
```

### Consumer Configuration

- **Concurrency**: 3 parallel consumers per topic
- **Auto-commit**: Disabled for manual offset management
- **Offset Reset**: `earliest` to process all events from topic start
- **Group ID**: `shadow-ledger-group` for raw transactions

## Business Rules

1. **Idempotency**: Duplicate events with the same `eventId` are silently ignored
2. **Negative Balance Prevention**: Debit transactions that would result in negative balance are rejected
3. **Append-Only**: Ledger entries are never updated or deleted
4. **Ordered Processing**: Events are processed in timestamp order per account

## Security

### Internal-Only Filter

All endpoints require the `X-Internal-Call: true` header to prevent unauthorized external access.

```bash
# This will fail (403 Forbidden)
curl http://localhost:8082/accounts/ACC123/shadow-balance

# This will succeed
curl -H "X-Internal-Call: true" \
  http://localhost:8082/accounts/ACC123/shadow-balance
```

## Monitoring

Health and metrics endpoints are exposed:

```bash
# Health check
curl http://localhost:8082/actuator/health

# Metrics
curl http://localhost:8082/actuator/metrics
```

## Database Schema

```sql
CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) UNIQUE NOT NULL,
    account_id VARCHAR(255) NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT uk_event_id UNIQUE (event_id)
);

CREATE INDEX idx_account_timestamp 
ON ledger_entries(account_id, timestamp, event_id);
```

## Testing

### Produce Test Events

```bash
# Credit event
kafka-console-producer --broker-list localhost:9092 \
  --topic transactions.raw << EOF
{"eventId":"evt-001","accountId":"ACC123","type":"credit","amount":1000.00,"timestamp":1703001234567}
EOF

# Debit event
kafka-console-producer --broker-list localhost:9092 \
  --topic transactions.raw << EOF
{"eventId":"evt-002","accountId":"ACC123","type":"debit","amount":50.00,"timestamp":1703001234568}
EOF
```

### Check Balance

```bash
curl -H "X-Internal-Call: true" \
  http://localhost:8082/accounts/ACC123/shadow-balance
```

## Error Handling

- **Duplicate Events**: Silently ignored (logged at INFO level)
- **Negative Balance**: Throws `IllegalStateException`, event not saved
- **Invalid Event Type**: Throws `IllegalArgumentException`
- **Processing Errors**: Logged and re-thrown to trigger Kafka retry

## Logging

Logs include trace IDs for distributed tracing:

```
2024-12-22 10:30:45 INFO [shadow-ledger-service] [traceId=abc-123] 
EventConsumerService - Consuming event: evt-001
```

## Performance Considerations

- Uses PostgreSQL window functions for efficient balance computation
- Unique constraint on `event_id` ensures database-level idempotency
- Concurrent Kafka consumers (configurable concurrency level)
- Manual offset commit for at-least-once delivery guarantees

## Troubleshooting

### Kafka Connection Issues
```bash
# Verify Kafka is running
kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Database Connection Issues
```bash
# Test PostgreSQL connection
psql -h localhost -p 5433 -U ledger -d ledgerdb
```

### View Consumer Lag
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group shadow-ledger-group --describe
```

## License

[Your License Here]

## Contributing

[Your Contributing Guidelines Here]