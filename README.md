# Shadow Ledger System

## Overview
The Shadow Ledger System is a simplified but realistic banking backend built using
Spring Boot microservices. It processes financial events (credits and debits),
maintains an immutable shadow ledger, validates balances against a Core Banking
System (CBS), and generates correction events when mismatches occur.

The system is designed to demonstrate:
- Event-driven architecture
- Idempotent processing
- Deterministic ordering
- Kafka-based messaging
- Role-based security
- Observability and traceability
- Basic AWS deployment capability

## Microservices
The system consists of exactly four components:

1. **API Gateway**
    - Single entry point
    - JWT authentication
    - Role-based access control (RBAC)
    - Trace ID propagation

2. **Event Service**
    - Accepts incoming financial events
    - Validates and deduplicates events
    - Publishes events to Kafka

3. **Shadow Ledger Service**
    - Consumes Kafka events
    - Maintains an append-only ledger
    - Computes shadow balances
    - Prevents negative balances

4. **Drift & Correction Service**
    - Compares CBS balances with shadow balances
    - Detects drift
    - Publishes correction events

## Kafka Topics
- `transactions.raw`
- `transactions.corrections`

## Security
- JWT-based authentication
- RBAC enforced at API Gateway
- All services accessible only through the gateway

## Observability
Each service exposes:
- `/actuator/health`
- `/actuator/metrics`

Logs include:
- timestamp
- service name
- log level
- X-Trace-Id

## Repository Structure

/api-gateway
/event-service
/shadow-ledger-service
/drift-correction-service
docker-compose.yml
/scripts/run-acceptance.sh

 
