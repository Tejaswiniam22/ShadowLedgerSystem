# API Gateway

Enterprise-grade **Spring Cloud API Gateway** providing secure access,
centralized routing, and service discovery for ShadowLedger microservices.

---

## Overview

This API Gateway acts as the single entry point for all client requests.
It handles:

- Authentication and authorization
- Request routing and load balancing
- Trace ID propagation
- Security enforcement
- Observability hooks

---

## Architecture

Client  
→ API Gateway (8080)  
→ Downstream Services  
↳ Eureka Service Discovery

---

## Core Capabilities

- Stateless JWT authentication
- Role-based access control (RBAC)
- Reactive WebFlux security
- Global request tracing
- Service discovery via Eureka
- Non-blocking, scalable I/O

---

## Security Model

### Authentication
- JWT (HMAC SHA-256)
- Token validity: **1 hour**
- Stateless (no sessions)

### Authorization

| Endpoint | Required Authority |
|--------|--------------------|
| `/auth/**` | Public |
| `POST /events/**` | ROLE_user |
| `/drift-check/**` | ROLE_auditor |
| `/correct/**` | ROLE_admin |
| All others | Authenticated |

---

## Token Issuance

POST /auth/token?role=<role>


Response:
{
  "token": "<JWT_TOKEN>"
}

## Request Requirements
All protected requests must include:

Authorization: Bearer <JWT_TOKEN>
Distributed Tracing
Each request is enriched with the following headers:

X-Trace-Id: <UUID>
X-Internal-Call: true
These headers are propagated downstream for log correlation.

## Gateway Routing
Path Pattern	Target Service
/events/**	event-service
/accounts/**	shadow-ledger-service
/drift-check/**	drift-correction-service
/correct/**	drift-correction-service

## Routing is resolved dynamically via Eureka.

## Configuration Requirements
Environment Variables (Recommended)
JWT_SECRET=<secure-256-bit-key>
EUREKA_SERVER_URL=http://eureka-server:8761/eureka

## Observability
Logging
Gateway routing: TRACE

Security events: DEBUG

Reactor Netty: DEBUG

## Actuator Endpoints

/actuator/health
/actuator/metrics
Deployment Guidelines


Run Eureka before Gateway

Deploy Gateway as stateless service

Use multiple Gateway instances behind a load balancer

Externalize secrets

Enable HTTPS termination at ingress

