# 🚗 Uber Backend Microservices — Interview & Technical Showcase Guide

---

## 📌 Executive Summary

This project is an **enterprise-grade, event-driven microservices platform** mimicking core functionality of Uber, built with **Java 21, Spring Boot 3, Spring Cloud Gateway, Netflix Eureka, MySQL 8, Redis GEO, Apache Kafka, WebSockets (STOMP)**, and **Docker Compose**.

It handles high-throughput driver location tracking, real-time spatial proximity queries, transactionally safe ride state transitions, and asynchronous driver notification streaming.

---

## 🏗️ System Architecture

```
                               ┌─────────────────────────┐
                               │  Postman / Mobile App   │
                               └────────────┬────────────┘
                                            │
                                 http://localhost:8080
                                            │
                                            ▼
                                ┌───────────────────────┐
                                │   API Gateway         │ ◄─── Service Discovery
                                │ (Spring Cloud Gateway)│      (Netflix Eureka :8761)
                                └───────────┬───────────┘
                                            │
         ┌────────────────────┬─────────────┴────────────┬────────────────────┐
         │                    │                          │                    │
         ▼                    ▼                          ▼                    ▼
┌────────────────┐   ┌────────────────┐         ┌────────────────┐   ┌───────────────────┐
│  Auth Service  │   │ BookingService │         │ Review Service │   │ Location Service  │
│    (:7474)     │   │    (:7777)     │         │    (:7475)     │   │     (:7478)       │
└───────┬────────┘   └───────┬────────┘         └───────┬────────┘   └─────────┬─────────┘
        │                    │                          │                      │
        ▼                    ▼                          ▼                      ▼
    ┌───────┐            ┌───────┐                  ┌───────┐              ┌───────┐
    │ MySQL │            │ MySQL │                  │ MySQL │              │ Redis │
    │(:3307)│            │(:3307)│                  │(:3307)│              │(:6379)│
    └───────┘            └───┬───┘                  └───────┘              └───────┘
                             │
                             ▼
                     ┌──────────────┐
                     │ Apache Kafka │
                     │ Event Stream │
                     └───────┬──────┘
                             │
                             ▼
                   ┌──────────────────┐
                   │  Socket Service  │ ◄─── WebSocket STOMP
                   │     (:8086)      │      (Driver Real-Time)
                   └──────────────────┘
```

---

## ⚙️ Service Inventory & Tech Stack

| Service | Port | Primary Tech | Role |
|---------|------|--------------|------|
| **`UberServiceDiscovery`** | `8761` | Spring Cloud Netflix Eureka | Service Registry & Health Monitoring |
| **`UberApiGateway`** | `8080` | Spring Cloud Gateway, Netty | Unified Entry Point, Reverse Proxy, Route Load-Balancing |
| **`UberProject-AuthService`** | `7474` | Spring Security, JWT, BCrypt, MySQL | Passenger/Driver Auth, Session Cookies |
| **`UberBookingService`** | `7777` | Spring Data JPA, MySQL, Flyway | Ride State Machine, Booking Lifecycle, Idempotency |
| **`UberProject-LocationService`**| `7478` | Redis GEO, Spring Data Redis | Real-time Driver GPS Spatial Indexing & Radius Search |
| **`UberReviewService`** | `7475` | Spring Data JPA, Flyway, MySQL | Rating & Review CRUD operations |
| **`ClientSocketService`** | `8086` | WebSockets, STOMP, Kafka | Real-time Bi-directional Driver Notification Streaming |
| **Shared Domain Library** | N/A | JPA, Flyway Migrations | Shared Database Schemas (`Uber-entityService`) |

---

## 🎯 Live Interview Demo Script (Using Postman & WebSockets)

### **Phase 1: Infrastructure Verification**
1. Open Eureka Dashboard in browser: [`http://localhost:8761`](http://localhost:8761)
   - **Point out to interviewer**: All 6 microservices (`UBERAUTHSERVICE`, `UBERBOOKINGSERVICE`, `UBERREVIEWSERVICE`, `UBERPROJECT-LOCATIONSERVICE`, `CLIENTSOCKETSERVICE`, `UBERAPIGATEWAY`) are registered with `UP` status.

---

### **Phase 2: Passenger & Driver Onboarding**
2. **Passenger Signup**
   - **Method**: `POST`
   - **URL**: `http://localhost:8080/api/v1/auth/signup/passenger`
   - **Body**:
     ```json
     {
       "name": "John Doe",
       "email": "john.doe@example.com",
       "password": "SecurePass@123",
       "phoneNumber": "+919876543210"
     }
     ```
   - **Talking point**: Password is BCrypt-hashed before persistence. Duplicate email/phone triggers a handled `409 Conflict`.

3. **Passenger Sign-In**
   - **Method**: `POST`
   - **URL**: `http://localhost:8080/api/v1/auth/signin/passenger`
   - **Body**: `{"email": "john.doe@example.com", "password": "SecurePass@123"}`
   - **Talking point**: Generates a stateless JWT token set in an `HttpOnly` response cookie to prevent XSS attacks.

4. **Driver Signup**
   - **Method**: `POST`
   - **URL**: `http://localhost:8080/api/v1/auth/driver/signup`
   - **Body**:
     ```json
     {
       "name": "Ravi Kumar",
       "email": "ravi.driver@example.com",
       "password": "DriverPass@123",
       "phoneNumber": "+919876543211",
       "licenseNumber": "DL-1420110012345",
       "carModel": "Swift Dzire",
       "carColor": "White",
       "carPlateNumber": "DL 01 AB 1234"
     }
     ```

---

### **Phase 3: Real-Time Geospatial Driver Location**
5. **Update Driver GPS Location**
   - **Method**: `POST`
   - **URL**: `http://localhost:8080/api/location/drivers`
   - **Body**: `{"driverId": "1", "latitude": 28.6139, "longitude": 77.2090}`
   - **Talking point**: Pushes coordinates into **Redis GEO spatial index** (`GEOADD`), enabling sub-millisecond proximity calculations.

6. **Find Nearby Drivers**
   - **Method**: `POST`
   - **URL**: `http://localhost:8080/api/location/nearby/drivers`
   - **Body**: `{"latitude": 28.6139, "longitude": 77.2090}`
   - **Talking point**: Executes a `GEORADIUS` query searching drivers within a 5 km bounding circle.

---

### **Phase 4: Real-Time Nearby Driver Broadcast (Live Demo ✨)**
7. **Open Real-Time Driver Dashboard**:
   - Double click [`driver_socket_demo.html`](file:///d:/UberBackend/driver_socket_demo.html) in your workspace to open in browser.
   - Open a **second tab** with the same HTML file.
   - Set Tab 1 as **Driver #1** (Click **Connect STOMP**).
   - Set Tab 2 as **Driver #2** (Click **Connect STOMP**).
8. **Broadcast Ride Request to Nearby Drivers**:
   - **Method**: `POST`
   - **URL**: `http://localhost:8086/api/socket/newride` *(or via Gateway `http://localhost:8080/api/socket/newride`)*
   - **Body**:
     ```json
     {
       "passengerId": 101,
       "bookingId": 1,
       "driverIds": [1, 2, 3],
       "startLocation": { "latitude": 28.6139, "longitude": 77.2090 },
       "endLocation": { "latitude": 28.5355, "longitude": 77.3910 }
     }
     ```
   - **Result**: The new ride offer card **pops up simultaneously on both Driver tabs in real time** over WebSockets!
   - **Accepting**: Click **"Accept Ride as Driver #1"** on Tab 1. It sends a STOMP message to `/app/rideResponse/1`, updating `UberBookingService` (`status: DRIVER_ASSIGNED`) and publishing an audit log to **Kafka** (`sample-topic`).

---

### **Phase 5: Ride Request & State Machine Lifecycle**
9. **Create Booking Request (REST)**
   - **Method**: `POST`
   - **URL**: `http://localhost:8080/api/v1/booking`
   - **Header**: `Idempotency-Key: <UUID>`
   - **Body**:
     ```json
     {
       "passengerId": 1,
       "startLocation": { "latitude": 28.6139, "longitude": 77.2090 },
       "endLocation": { "latitude": 28.5355, "longitude": 77.3910 }
     }
     ```
   - **Talking point**: Supports **Idempotency keys** to prevent duplicate booking creation on network retries. Initial status: `REQUESTED`.

10. **Progress Ride Lifecycle**:
    - `DRIVER_ASSIGNED` → `DRIVER_ARRIVING` → `IN_RIDE` → `COMPLETED`
    - `POST http://localhost:8080/api/v1/booking/1` with body `{"status": "COMPLETED", "driverId": 1}`

11. **Post-Ride Review**:
    - **Method**: `POST`
    - **URL**: `http://localhost:8080/api/v1/reviews`
    - **Body**: `{"bookingId": 1, "content": "Excellent ride!", "rating": 5.0}`

---

## 💡 Key Architectural Design Patterns to Highlight

1. **Service Discovery Pattern**: Decouples microservice networking using Eureka; services register dynamically without hardcoded IP addresses.
2. **API Gateway Pattern**: Single entry point (`:8080`) providing request routing, load balancing, and centralized cross-cutting concerns.
3. **Database-per-Service & Shared Entity Library**: Domain models and Flyway migrations are version-controlled in `Uber-entityService` for consistency.
4. **In-Memory Spatial Indexing**: Using Redis GEO instead of R-Tree indexed relational queries for high-frequency GPS writes.
5. **Real-time Bi-directional Streaming**: Using WebSockets STOMP for instantaneous driver notifications instead of polling.
6. **Idempotency Pattern**: Network retry protection on booking creation using unique `Idempotency-Key` headers.
7. **Graceful Container Startup**: Orchestrated startup via `docker-compose` healthchecks (`mysqladmin ping`, Eureka health check).

---

## ❓ Frequently Asked Interview Questions & Answers

### **Q1: Why did you use Redis GEO for Location Service instead of MySQL?**
> **Answer**: Driver GPS locations are updated every few seconds. Writing high-frequency updates to MySQL causes disk I/O bottlenecks and index fragmentation. Redis GEO stores spatial coordinates in memory using a Geohash sorted set, allowing us to perform proximity queries (`GEORADIUS`) in under 2 milliseconds.

### **Q2: How does API Gateway handle load balancing?**
> **Answer**: Spring Cloud Gateway integrates with Eureka. When a request hits `http://localhost:8080/api/v1/booking/**`, the Gateway uses Spring Cloud LoadBalancer to look up `UBERBOOKINGSERVICE` in Eureka's registry and routes the request across available instances using round-robin load balancing.

### **Q3: How do you handle real-time ride broadcasting to nearby drivers?**
> **Answer**: `UberProject-LocationService` performs a `GEORADIUS` lookup to get nearby driver IDs. Then `ClientSocketService` broadcasts a `RideRequestDto` via STOMP over WebSockets to `/topic/rideRequest`. All online driver clients subscribed to this topic receive the push notification instantly without HTTP polling.

### **Q4: How do you handle schema changes across multiple services?**
> **Answer**: We use **Flyway Database Migrations** versioned SQL files (`V1__init_db.sql` through `V7__add_driver_fields.sql`). When any microservice boots up, Flyway executes pending migrations transactionally against MySQL, ensuring schema consistency across environments.

---

## 🛠️ Local Execution & Verification Quick Reference

```bash
# Rebuild all service JARs
.\gradlew.bat :Uber-entityService:clean :Uber-entityService:jar :UberProject-AuthService:clean :UberProject-AuthService:bootJar :UberBookingService:clean :UberBookingService:bootJar :UberProject-LocationService:clean :UberProject-LocationService:bootJar :UberReviewService:clean :UberReviewService:bootJar :ClientSocketService:clean :ClientSocketService:bootJar

# Spin up complete container stack
docker compose up -d --build

# Open real-time driver dashboard
start d:\UberBackend\driver_socket_demo.html

# Verify running containers
docker ps

# Check Eureka discovery status
curl -s http://localhost:8761/eureka/apps -H "Accept: application/json"
```
