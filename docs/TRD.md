# Ewallet Topup 0.0.1

## 1. Gambaran Umum Sistem

Sistem dompet digital sederhana yang memungkinkan pengguna untuk registrasi, login, melihat saldo, top-up, melakukan pembayaran ke *merchant*, dan melihat riwayat transaksi.

**Teknologi:**

- **Backend:** Java Spring Boot (N-Tier Architecture)
- **Frontend:** Angular dengan Tailwind CSS
- **Database:** Relasional (PostgreSQL)
- **Keamanan:** JWT (JSON Web Token)
- **Response Format:** BaseResponse standar

---

## 2. Entity Relationship Diagram (ERD)

**Deskripsi Tabel:**

### `users`

| Kolom      | Tipe      | Keterangan               |
| ---------- | --------- | ------------------------ |
| id         | INT (PK)  | Auto increment           |
| name       | VARCHAR   |                          |
| email      | VARCHAR   | UNIQUE, NOT NULL         |
| password   | VARCHAR   | NOT NULL (sudah di-hash) |
| created_at | TIMESTAMP | DEFAULT now()            |

### `wallets`

| Kolom      | Tipe                       | Keterangan                           |
| ---------- | -------------------------- | ------------------------------------ |
| id         | INT (PK)                   | Auto increment                       |
| user_id    | INT (UNIQUE, FK→users.id) | One-to-one: 1 user punya 1 wallet    |
| balance    | BIGINT                     | Saldo dalam satuan terkecil (Rupiah) |
| updated_at | TIMESTAMP                  |                                      |

### `merchants`

| Kolom      | Tipe         | Keterangan     |
| ---------- | ------------ | -------------- |
| id         | INT (PK)     | Auto increment |
| name       | VARCHAR(255) | NOT NULL       |
| created_at | TIMESTAMP    |                |

### `transactions`

| Kolom        | Tipe                             | Keterangan                           |
| ------------ | -------------------------------- | ------------------------------------ |
| id           | INT (PK)                         | Auto increment                       |
| user_id      | INT (FK→users.id)               | One-to-many                          |
| merchant_id  | INT (FK→merchants.id, nullable) | Null untuk TOPUP                     |
| amount       | BIGINT                           | NOT NULL                             |
| type         | VARCHAR(20)                      | `TOPUP`, `PAYMENT`, `TRANSFER` |
| status       | VARCHAR(20)                      | `PENDING`, `SUCCESS`, `FAILED` |
| reference_id | VARCHAR(255)                     | UNIQUE, ID dari payment gateway/bank |
| created_at   | TIMESTAMP                        | DEFAULT now()                        |

**Relasi:**

- `users` 1 : 1 `wallets`
- `users` 1 : M `transactions`
- `merchants` 1 : M `transactions` (opsional)

---

## 3. Indexing Strategy

Untuk performa query yang optimal, index dibuat pada kolom-kolom yang sering digunakan di klausa `WHERE`, `JOIN`, dan sorting.

| Tabel            | Index                                 | Tipe                    | Alasan                                                                                                 |
| ---------------- | ------------------------------------- | ----------------------- | ------------------------------------------------------------------------------------------------------ |
| `users`        | `idx_users_email`                   | UNIQUE                  | Login / pencarian user by email                                                                        |
| `wallets`      | `idx_wallets_user_id`               | UNIQUE (sudah otomatis) | Join dan lookup saldo user                                                                             |
| `transactions` | `idx_transactions_user_id`          | B-Tree                  | Riwayat transaksi per user                                                                             |
| `transactions` | `idx_transactions_merchant_id`      | B-Tree                  | Laporan transaksi per merchant                                                                         |
| `transactions` | `idx_transactions_reference_id`     | UNIQUE                  | Verifikasi duplikasi transaksi                                                                         |
| `transactions` | `idx_transactions_user_status_date` | Composite               | `user_id`, `status`, `created_at DESC` (untuk list transaksi user dengan filter status & paging) |
| `transactions` | `idx_transactions_created_at`       | B-Tree                  | Jika perlu laporan harian/mingguan                                                                     |

**Catatan:** Index `created_at` saja sering kurang efektif karena jarang digunakan tanpa user_id; composite index pada `(user_id, status, created_at)` menjadi covering index terbaik.

---

## 4. Format Base Response

Setiap respons API, baik sukses maupun error, akan dibungkus dalam format JSON yang seragam.

```json
{
  "requestId": "UUID (string)",
  "status": true/false,
  "message": "deskripsi singkat",
  "data": { ... } atau null
}
```

- `requestId`: UUID unik untuk tracing request, dihasilkan di backend (MDC filter).
- `status`: `true` jika sukses, `false` jika terjadi error (validasi, authorization, server error).
- `message`: Pesan yang dapat ditampilkan ke pengguna.
- `data`: Payload respons (objek, list, atau null).

Implementasi di Spring Boot: buat class `BaseResponse<T>` generic, dan `GlobalExceptionHandler` untuk handling error.

---

## 5. Arsitektur Backend (Spring Boot N-Tier)

**Lapisan (Tier):**

```
Controller Layer (API REST) 
    ↓
Service Layer (Business Logic, Transaksi) 
    ↓
Repository Layer (Spring Data JPA) 
    ↓
Database
```

**Struktur Paket:**

```
com.example.ewallet
├── config        // Security, JWT, CORS
├── controller    // REST Controllers
├── dto           // Request/Response DTOs
├── entity        // JPA Entities (User, Wallet, Transaction, Merchant)
├── exception     // Custom Exceptions, GlobalHandler
├── repository    // Spring Data repositories
├── security      // JwtUtils, UserDetailsServiceImpl
├── service       // Interfaces & Implementations
└── util          // BaseResponse builder, helper
```

**Komponen Penting:**

- `AuthController`: `/api/auth/register`, `/api/auth/login`
- `WalletController`: `/api/wallet/balance`, `/api/wallet/topup`
- `TransactionController`: `/api/transactions`, `/api/transactions/{id}`
- `MerchantController` (opsional): `/api/merchants`
- `JwtAuthFilter`: Memvalidasi setiap request terproteksi
- `GlobalExceptionHandler` dengan `@RestControllerAdvice` untuk menghasilkan BaseResponse yang konsisten saat error.

### Contoh Alur Top-Up

1. Request masuk: `POST /api/wallet/topup`
2. `WalletController` menerima, memanggil `WalletService.topUp(userId, amount, referenceId)`
3. Service: validasi user & amount, panggil Payment Gateway eksternal (mock), buat `Transaction` dengan status `SUCCESS`/`FAILED`, update `wallet.balance` jika sukses, simpan transaksi, kembalikan DTO.
4. Controller mengembalikan `BaseResponse<TransactionDto>`

---

## 6. Arsitektur Frontend (Angular + Tailwind)

**Struktur Folder Singkat:**

```
src/app/
├── core/
│   ├── services/       // auth.service.ts, wallet.service.ts, transaction.service.ts
│   ├── interceptors/   // auth.interceptor.ts (attach JWT), error.interceptor.ts
│   ├── models/         // interfaces (User, Wallet, Transaction, BaseResponse)
│   └── guards/         // auth.guard.ts
├── features/
│   ├── auth/           // login, register components
│   ├── dashboard/      // home, balance display
│   ├── topup/          // top-up form
│   ├── payment/        // merchant payment form
│   └── transactions/   // list & detail
├── shared/
│   └── components/     // re-usable UI components (button, card)
└── app-routing.module.ts
```

**Flow:**

- Gunakan `AuthGuard` untuk melindungi route yang memerlukan login.
- Simpan JWT di `localStorage` (atau lebih baik `HttpOnly cookie` jika backend sesuai).
- Semua HTTP request dilewatkan `AuthInterceptor` untuk menyertakan header `Authorization: Bearer <token>`.
- `ErrorInterceptor` menangkap error HTTP, menampilkan toast/modal berdasarkan `message` dari BaseResponse.
- Design UI menggunakan utility-class Tailwind (responsive, modern).

---

## 7. Keamanan dan Validasi

- Password di-hash menggunakan **BCrypt**.
- Endpoint dilindungi JWT; token expired singkat (misal 1 jam) + refresh token (jika diperlukan).
- Validasi input menggunakan Bean Validation (`@NotBlank`, `@Email`, `@Min`), error dikembalikan sebagai BaseResponse.
- Enkripsi HTTPS di production.
- Idempotency key: gunakan `reference_id` untuk mencegah transaksi ganda.

---
