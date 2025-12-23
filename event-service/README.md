# Event Service

A Spring Boot microservice that accepts financial transaction events via REST API, stores them in PostgreSQL with deduplication, and publishes them to Kafka for downstream processing.

## Overview

This service acts as an event ingestion gateway for a transaction processing system. It validates incoming events, ensures idempotency through database deduplication, and forwards events to Kafka for further processing.

## Features

- **REST API** for event ingestion
- **Idempotent processing** - prevents duplicate events using unique `eventId`
- **Request validation** - validates event structure and business rules
- **Kafka integration** - publishes events to `transactions.raw` topic
- **PostgreSQL persistence** - stores all events with unique constraints
- **Distributed tracing** - automatic trace ID generation and propagation
- **Access control** - internal-only filter for service-to-service calls
- **Structured logging** - consistent log format with trace IDs

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Kafka
- Logback

## Prerequisites

- Java 11+
- PostgreSQL (running on port 5433)
- Kafka (running on port 9092)
- Maven or Gradle

## Configuration

The service runs on port **8081** with the following configuration:

### Database
```yaml
URL: jdbc:postgresql://localhost:5433/ledgerdb
Username: ledger
Password: ledger
```

### Kafka
```yaml
Bootstrap servers: localhost:9092
Topic: transactions.raw
```

## API Endpoints

### Create Event
```http
POST /events
Content-Type: application/json
X-Internal-Call: true
X-Trace-Id: <optional-trace-id>
```

**Request Body:**
```json
{
  "eventId": "evt_12345",
  "accountId": "acc_67890",
  "type": "debit",
  "amount": 100.50,
  "timestamp": 1703001234567
}
```

**Validation Rules:**
- `eventId`: Required, non-blank, must be unique
- `accountId`: Required, non-blank
- `type`: Required, must be "debit" or "credit"
- `amount`: Required, must be positive
- `timestamp`: Required

**Response:**
- `202 Accepted` - Event accepted and processed
- `400 Bad Request` - Validation error or duplicate eventId
- `403 Forbidden` - Missing or invalid `X-Internal-Call` header

## Security

### Internal-Only Filter
All requests must include the header:
```
X-Internal-Call: true
```

This prevents direct external access and ensures the service is only called by trusted internal services.

## Tracing

The service automatically generates or propagates trace IDs:
- If `X-Trace-Id` header is present, it's used for correlation
- If absent, a new UUID is generated
- Trace ID is included in all log statements via MDC

## Database Schema

```sql
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL UNIQUE,
    account_id VARCHAR(255),
    type VARCHAR(255),
    amount DOUBLE PRECISION,
    timestamp BIGINT,
    CONSTRAINT uk_event_id UNIQUE (event_id)
);
```

## Kafka Publishing

Events are published to the `transactions.raw` topic with:
- **Key**: `accountId` (enables partitioning by account)
- **Value**: Complete event object (JSON serialized)

## Error Handling

The service includes global exception handling:

- **IllegalArgumentException**: Returns 400 with error message
- **MethodArgumentNotValidException**: Returns 400 with validation details
- **Duplicate eventId**: Returns 400 with "Duplicate eventId" message

## Logging

Structured logging format:
```
yyyy-MM-dd HH:mm:ss LEVEL [event-service] [traceId=<uuid>] logger - message
```

Example:
```
2024-12-22 10:30:45 INFO [event-service] [traceId=abc-123] EventProducerService - Processing event evt_12345
```

## Running the Service

1. Start PostgreSQL:
```bash
docker run -d -p 5433:5432 \
  -e POSTGRES_DB=ledgerdb \
  -e POSTGRES_USER=ledger \
  -e POSTGRES_PASSWORD=ledger \
  postgres:latest
```

2. Start Kafka:
```bash
docker-compose up -d kafka
```

3. Run the application:
```bash
./gradlew bootRun
```

## Health Checks

Health and metrics endpoints are exposed at:
- `http://localhost:8081/actuator/health`
- `http://localhost:8081/actuator/metrics`

## Example Usage

```bash
curl -X POST http://localhost:8081/events \
  -H "Content-Type: application/json" \
  -H "X-Internal-Call: true" \
  -H "X-Trace-Id: test-trace-123" \
  -d '{
    "eventId": "evt_001",
    "accountId": "acc_123",
    "type": "debit",
    "amount": 50.00,
    "timestamp": 1703001234567
  }'
```

## Architecture Notes

This service follows the **Event Sourcing** pattern:
1. Receives events via REST API
2. Validates and deduplicates using PostgreSQL
3. Publishes to Kafka for asynchronous processing
4. Downstream consumers process from Kafka topic

The database serves as both:
- **Deduplication store** - prevents duplicate processing
- **Event audit log** - maintains history of all received events