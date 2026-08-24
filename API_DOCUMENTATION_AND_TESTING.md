# 🧪 Uber Microservices API Documentation & Automated Test Guide

All API endpoints are accessible via the **API Gateway** on port `8080`.  
The Gateway automatically injects a `X-Correlation-ID` into request/response headers for end-to-end distributed tracing.

---

## 🔐 1. Auth Service APIs (`/api/v1/auth`)

### 1.1 Passenger Signup
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/auth/signup/passenger`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "email": "rider@example.com",
  "password": "SecurePassword123!",
  "phoneNumber": "9876543210",
  "name": "John Rider"
}
```
- **cURL Command**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup/passenger \
  -H "Content-Type: application/json" \
  -d '{"email":"rider@example.com","password":"SecurePassword123!","phoneNumber":"9876543210","name":"John Rider"}'
```

### 1.2 Passenger Signin
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/auth/signin/passenger`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "email": "rider@example.com",
  "password": "SecurePassword123!"
}
```
- **Response**: `200 OK` with HTTP-Only JWT Cookie (`jwt=...`) and `{"success": true}` body.
- **cURL Command**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/signin/passenger \
  -H "Content-Type: application/json" \
  -i -d '{"email":"rider@example.com","password":"SecurePassword123!"}'
```

---

## 📍 2. Location Service APIs (`/api/location` / `/api/v1/location`)

### 2.1 Save Driver Location (Redis GEO)
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/location/drivers`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "driverId": "1001",
  "latitude": 37.774929,
  "longitude": -122.419416
}
```
- **cURL Command**:
```bash
curl -X POST http://localhost:8080/api/location/drivers \
  -H "Content-Type: application/json" \
  -d '{"driverId":"1001","latitude":37.774929,"longitude":-122.419416}'
```

### 2.2 Get Nearby Drivers (within 5 km radius)
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/location/nearby/drivers`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "latitude": 37.7749,
  "longitude": -122.4194
}
```
- **Response**: List of nearby driver IDs and coordinates.
- **cURL Command**:
```bash
curl -X POST http://localhost:8080/api/location/nearby/drivers \
  -H "Content-Type: application/json" \
  -d '{"latitude":37.7749,"longitude":-122.4194}'
```

---

## 🚖 3. Booking Service APIs (`/api/v1/booking` / `/api/v1/bookings`)

### 3.1 Create Ride Booking (with Idempotency Support)
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/booking`
- **Headers**: 
  - `Content-Type: application/json`
  - `Idempotency-Key: ride-req-unique-uuid-001`
- **Request Body**:
```json
{
  "passengerId": 1,
  "startLocation": {
    "latitude": 37.7749,
    "longitude": -122.4194
  },
  "endLocation": {
    "latitude": 37.7833,
    "longitude": -122.4167
  }
}
```
- **cURL Command**:
```bash
curl -X POST http://localhost:8080/api/v1/booking \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: ride-req-unique-uuid-001" \
  -d '{"passengerId":1,"startLocation":{"latitude":37.7749,"longitude":-122.4194},"endLocation":{"latitude":37.7833,"longitude":-122.4167}}'
```

### 3.2 Update Booking Status & Driver Assignment (State Machine Validated)
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/booking/1`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "status": "DRIVER_ASSIGNED",
  "driverId": 1001
}
```
- **cURL Command**:
```bash
curl -X POST http://localhost:8080/api/v1/booking/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"DRIVER_ASSIGNED","driverId":1001}'
```

---

## ⭐ 4. Review Service APIs (`/api/v1/reviews`)

### 4.1 Publish Review
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/v1/reviews`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "bookingId": 1,
  "rating": 5.0,
  "content": "Great ride! Very clean car and professional driver."
}
```
- **cURL Command**:
```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H "Content-Type: application/json" \
  -d '{"bookingId":1,"rating":5.0,"content":"Great ride! Very clean car and professional driver."}'
```

### 4.2 Get Review by ID
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/v1/reviews/1`
- **cURL Command**:
```bash
curl -X GET http://localhost:8080/api/v1/reviews/1
```

### 4.3 Get All Reviews
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/v1/reviews`
- **cURL Command**:
```bash
curl -X GET http://localhost:8080/api/v1/reviews
```

---

## ⚡ 5. Client Socket Service (WebSockets)

- **Protocol**: STOMP / WebSocket
- **URL**: `ws://localhost:8080/ws`
- **Topics**:
  - `/topic/ride-request` (Subscribes to ride requests)
  - `/topic/location-update` (Subscribes to real-time driver coordinates)
