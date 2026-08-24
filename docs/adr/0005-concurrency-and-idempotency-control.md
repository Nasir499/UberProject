# ADR 0005: Concurrency Control & Idempotency Strategy

## Context
When multiple riders request rides in the same vicinity simultaneously, race conditions can cause two riders to be assigned the same driver (double assignment). Additionally, retried HTTP requests could create duplicate bookings.

## Decision
1. **Atomic Conditional Updates**: `DriverRepository.reserveDriverIfAvailable` executes atomic SQL updates `UPDATE driver SET driver_state='RESERVED', is_available=false WHERE id=? AND is_available=true AND (driver_state='AVAILABLE' OR driver_state IS NULL)`.
2. **Optimistic Locking**: `@Version` annotation on JPA entities.
3. **Idempotency Keys**: Support `Idempotency-Key` header on booking APIs with `IdempotencyService`.

## Consequences
- **Pros**: Zero double driver assignments under high concurrency, deterministic API retry behavior.
- **Cons**: Requires handling `OptimisticLockingFailureException` and state transition validation exceptions gracefully.
