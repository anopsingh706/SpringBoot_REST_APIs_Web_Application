# Wallet REST API

A high-concurrency wallet service built with Spring Boot 3.4.3 and Java 17.
Handles 1000+ RPS per wallet with zero 50x errors using pessimistic locking.

## Tech Stack
- Java 17, Spring Boot 3.4.3, PostgreSQL 15, Liquibase, Docker

## Quick Start

### Run with Docker Compose
```bash
git clone https://github.com/anopsingh706/SpringBoot_REST_APIs_Web_Application.git
cd SpringBoot_REST_APIs_Web_Application
docker-compose up --build
```
App starts at: `http://localhost:8080`

## API Endpoints

### Deposit or Withdraw
```
POST /api/v1/wallet
Content-Type: application/json

{
  "valletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 1000
}
```

### Get Balance
```
GET /api/v1/wallets/{WALLET_UUID}
```

## Test with curl

### Deposit
```bash
curl -X POST http://localhost:8080/api/v1/wallet -H "Content-Type: application/json" -d "{\"valletId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operationType\":\"DEPOSIT\",\"amount\":1000}"
```

### Check Balance
```bash
curl http://localhost:8080/api/v1/wallets/550e8400-e29b-41d4-a716-446655440000
```

### Withdraw
```bash
curl -X POST http://localhost:8080/api/v1/wallet -H "Content-Type: application/json" -d "{\"valletId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operationType\":\"WITHDRAW\",\"amount\":500}"
```

## Error Responses

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Wallet not found | 404 | `{"message": "Wallet not found"}` |
| Insufficient funds | 400 | `{"message": "Insufficient funds"}` |
| Invalid JSON | 400 | `{"message": "Invalid request"}` |

## Run Tests
```bash
mvnw.cmd test
```
Expected: `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`

## Concurrency Design
- Pessimistic locking (`SELECT FOR UPDATE`) prevents race conditions
- Optimistic locking (`@Version`) as secondary protection
- Automatic retry with exponential backoff for lock conflicts
- Handles 1000+ RPS per wallet with no 50x errors
