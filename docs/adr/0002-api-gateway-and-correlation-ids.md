# ADR 0002: API Gateway & Request Correlation Tracing

## Context
Exposing individual microservice ports directly to external clients creates security risks, complex CORS configurations, and difficulties in tracing distributed requests across service boundaries.

## Decision
We introduce **Spring Cloud Gateway** (`UberApiGateway`) as the single public entry point on port `8080`.
All incoming requests pass through a global `CorrelationIdFilter` which inspects or injects a `X-Correlation-ID` HTTP header.

## Consequences
- **Pros**: Centralized security, unified CORS policy, seamless end-to-end log correlation across services.
- **Cons**: Gateway acts as a critical path component requiring high availability.
