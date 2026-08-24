# ADR 0003: Redis GEO for High-Frequency Location Tracking

## Context
Driver GPS coordinates are updated every few seconds. Writing every coordinate update to a relational database (MySQL) causes excessive disk I/O, table lock contention, and high latency.

## Decision
We utilize **Redis GEO** data structures (`GEOADD` and `GEOSEARCH`) in `UberProject-LocationService`. Coordinate Points are stored as `Point(longitude, latitude)`.

## Consequences
- **Pros**: Sub-millisecond write latency, efficient spatial radius queries (`within X km`), zero database write amplification.
- **Cons**: Requires Redis cluster memory management and TTL location expiration strategies.
