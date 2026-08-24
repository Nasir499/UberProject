# ADR 0001: Microservices Architecture & Eureka Service Discovery

## Context
The application handles discrete domain concerns including authentication, booking management, driver location tracking, reviews, and client notifications. A monolithic structure leads to tight coupling, deployment bottlenecks, and database contention.

## Decision
We adopt a microservices architecture with **Netflix Eureka** (`UberServiceDiscovery`) as the central service registry. Each microservice registers dynamically at startup and uses client-side load balancing.

## Consequences
- **Pros**: Independent scaling, decoupled deployments, domain isolation.
- **Cons**: Requires service discovery overhead and distributed network monitoring.
