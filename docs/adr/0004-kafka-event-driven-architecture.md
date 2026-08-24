# ADR 0004: Event-Driven Architecture with Kafka

## Context
Synchronous REST calls between microservices create tight runtime coupling, cascade failures, and increase overall request latency.

## Decision
We implement an asynchronous event bus using **Apache Kafka**. Domain events use a standardized `DomainEvent<T>` envelope containing metadata (`eventId`, `eventType`, `eventVersion`, `rideId`, `timestamp`, `correlationId`, `producer`).

## Consequences
- **Pros**: Decoupled producers and consumers, fault isolation, reliable event replay capabilities.
- **Cons**: Requires idempotent consumer logic and dead-letter topic (DLT) management.
