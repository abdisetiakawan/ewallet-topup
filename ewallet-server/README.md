# ewallet-server

Backend REST API untuk aplikasi e-wallet topup berbasis Spring Boot.

## Tech Stack

- **Java 21** + **Spring Boot 3.5**
- **PostgreSQL** — database utama
- **Redis** — idempotency key & refresh token storage
- **Flyway** — database migration
- **Spring Security + JWT** — autentikasi via HTTP-only cookie

## Prasyarat

- Java 21+
- Maven 3.8+
- PostgreSQL (database: `ewallet`)
- Redis

## Menjalankan Aplikasi

### Development

```bash
mvn spring-boot:run
```

Profile `dev` aktif secara default. Pastikan PostgreSQL dan Redis berjalan di localhost.

### Production

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Konfigurasi

| File | Keterangan |
|---|---|
| `application.properties` | Config base (port, JWT, Flyway location) |
| `application-dev.properties` | DB & Redis localhost, logging verbose |
| `application-prod.properties` | DB & Redis localhost, logging minimal, cookie secure |

Sesuaikan `spring.datasource.*`, `spring.data.redis.*`, dan `app.jwt.secret` sebelum deploy.

## Database Migration

Migration dikelola Flyway dengan dua folder:

```
db/migration/   ← DDL (struktur tabel)
db/seed/        ← DML (data awal)
```

Jika terjadi checksum mismatch (misal setelah edit migration yang sudah applied):

```bash
mvn flyway:repair \
  -Dflyway.url=jdbc:postgresql://localhost:5432/ewallet \
  -Dflyway.user=postgres \
  -Dflyway.password=postgres
```

## API Endpoints

Base URL: `http://localhost:8080`

### Auth — `/api/auth`

| Method | Endpoint | Keterangan |
|---|---|---|
| POST | `/api/auth/register` | Registrasi user baru |
| POST | `/api/auth/login` | Login, set JWT cookie |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout, hapus cookie |

### Wallet — `/api/wallet`

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/wallet/balance` | Cek saldo wallet |
| POST | `/api/wallet/topup` | Top up saldo |

### Transaksi — `/api/transactions`

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/transactions` | Riwayat transaksi |
| POST | `/api/transactions/pay` | Bayar ke merchant |

### Merchant — `/api/merchants`

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/merchants` | Daftar merchant |

### Admin Merchant — `/api/admin/merchants`

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/admin/merchants` | Daftar semua merchant |
| GET | `/api/admin/merchants/{id}` | Detail merchant |
| POST | `/api/admin/merchants` | Tambah merchant |
| PUT | `/api/admin/merchants/{id}` | Update merchant |

## Struktur Project

```
src/main/java/com/berijalan/ewallet/
├── config/         # Security, CORS, Redis, Idempotency
├── controller/     # REST controllers
├── entity/         # JPA entities
├── exception/      # Global exception handler
├── repository/     # Spring Data JPA repositories
├── security/       # JWT utils & filters
└── service/        # Business logic
```
