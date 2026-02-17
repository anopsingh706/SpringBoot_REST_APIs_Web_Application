🚀 Wallet REST API

A high-concurrency Wallet Service built with Spring Boot 3.4.3 and Java 17.
Designed to handle 1000+ requests per second per wallet with zero 50x errors, ensuring strong consistency and safe concurrent transactions.

🧱 Tech Stack

Java 17

Spring Boot 3.4.3

PostgreSQL 15

Liquibase (DB Migrations)

Docker & Docker Compose

⚡ Quick Start
🐳 Run with Docker Compose
git clone https://github.com/anopsingh706/SpringBoot_REST_APIs_Web_Application.git
cd SpringBoot_REST_APIs_Web_Application
docker-compose up --build


Application will be available at:

http://localhost:8080


No manual database setup required — everything runs inside containers.

📌 API Endpoints
💰 Deposit or Withdraw

POST /api/v1/wallet

{
  "valletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 1000
}


Supported operations:

DEPOSIT

WITHDRAW

💳 Get Wallet Balance

GET /api/v1/wallets/{WALLET_UUID}

Example:

GET /api/v1/wallets/550e8400-e29b-41d4-a716-446655440000

🧪 Test Using curl
➕ Deposit
curl -X POST http://localhost:8080/api/v1/wallet \
-H "Content-Type: application/json" \
-d "{\"valletId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operationType\":\"DEPOSIT\",\"amount\":1000}"

➖ Withdraw
curl -X POST http://localhost:8080/api/v1/wallet \
-H "Content-Type: application/json" \
-d "{\"valletId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operationType\":\"WITHDRAW\",\"amount\":500}"

🔍 Check Balance
curl http://localhost:8080/api/v1/wallets/550e8400-e29b-41d4-a716-446655440000

❗ Error Handling
Scenario	HTTP Status	Response Example
Wallet not found	404	{"message": "Wallet not found"}
Insufficient funds	400	{"message": "Insufficient funds"}
Invalid JSON request	400	{"message": "Invalid request"}

All invalid requests return structured and meaningful error responses.

🧪 Running Tests
mvnw.cmd test


Expected output:

Tests run: 22, Failures: 0, Errors: 0, Skipped: 0


Includes:

Unit tests

Integration tests

Concurrency stress tests

🔒 Concurrency & Consistency Design

Pessimistic locking (SELECT FOR UPDATE) to prevent race conditions

Optimistic locking (@Version) as secondary safety layer

Automatic retry with exponential backoff for lock conflicts

Designed to handle 1000+ RPS per wallet

No 50x server errors under concurrent load

📦 Deployment

Fully containerized application and database

Configuration via environment variables

One-command startup using Docker Compose
