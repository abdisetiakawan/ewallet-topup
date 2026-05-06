# 📘 Dokumen Kolaborasi GitHub

## 1. Gambaran Umum Repositori

Proyek menggunakan **monorepo** atau **dua repositori terpisah**:

- **Monorepo**: Satu repo dengan folder   `ewallet-server/` dan `ewallet-client/`.
  Struktur direktori utama:

```
ewallet-topup/
├── ewallet-server/           # Spring Boot (Java)
├── ewallet-client/          # Angular + Tailwind
├── docs/              # Dokumentasi (TRD, API spec, dll.)
├── .github/           # Template issue, workflows CI/CD
└── README.md
```

---

## 2. Branching Strategy

Menggunakan model **Trunk-Based Development** yang disesuaikan dengan **GitHub Flow**:

- `main` – Kode yang sudah siap *production*. Hanya menerima PR dari `develop` atau `hotfix/*`.
- `development` – Cabang integrasi utama. Semua fitur dan perbaikan digabung ke sini lebih dulu.
- `feature/<nama-fitur>` – Pengembangan fitur baru.
- `bugfix/<deskripsi-bug>` – Perbaikan bug yang ditemukan di `develop`.
- `hotfix/<deskripsi-hotfix>` – Perbaikan darurat langsung ke `main` (kemudian di-merge ke `develop`).
- `release/<versi>` – (Opsional) Cabang persiapan rilis untuk pengujian akhir.

**Aturan penamaan:**

- Gunakan **kebab-case**.
- Gunakan bahasa Inggris, singkat dan deskriptif.
- Contoh: `feature/user-registration`, `bugfix/wallet-balance-negative`, `hotfix/jwt-expiry-fix`.

💡 **Branch protection** diterapkan pada `main` dan `develop`:

- Tidak boleh push langsung.
- Wajib melalui Pull Request.
- Minimal 1 reviewer (untuk `develop`), 2 reviewer (untuk `main`).
- Status checks (CI build, test) harus lolos.

---

## 3. Penamaan Branch Berdasarkan Tipe Pekerjaan

| Tipe Pekerjaan     | Awalan Branch    | Contoh                              |
| ------------------ | ---------------- | ----------------------------------- |
| Fitur Baru         | `feature/`     | `feature/topup-wallet`            |
| Perbaikan Bug      | `bugfix/`      | `bugfix/transaction-duplicate`    |
| Peningkatan Teknis | `improvement/` | `improvement/add-logging`         |
| Hotfix Produksi    | `hotfix/`      | `hotfix/database-connection-leak` |
| Eksperimen         | `experiment/`  | `experiment/new-payment-gateway`  |
| Dokumentasi        | `docs/`        | `docs/api-spec-update`            |

---

## 4. Konvensi Commit Message

Mengadopsi **[Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/)** agar riwayat commit mudah dibaca dan bisa otomatis menghasilkan changelog.

**Format:**

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Type (wajib):**

- `feat` – Fitur baru
- `fix` – Perbaikan bug
- `docs` – Dokumentasi
- `style` – Format, penamaan, whitespace (tidak mengubah logika)
- `refactor` – Perubahan kode tanpa menambah fitur atau memperbaiki bug
- `test` – Menambah atau memperbaiki pengujian
- `chore` – Tugas rutin, build tool, konfigurasi
- `ci` – Perubahan konfigurasi CI/CD
- `perf` – Peningkatan performa

**Scope (opsional):** Modul yang terpengaruh, misal `auth`, `wallet`, `transaction`, `ui`.

**Contoh:**

```
feat(wallet): add top-up endpoint with external reference ID validation
```

```
fix(transaction): resolve duplicate payment when reference ID already exists

Return 409 Conflict and proper BaseResponse when reference_id is reused.
Closes #42
```

```
docs(readme): add setup instructions for backend and frontend
```

**Aturan tambahan:**

- Gunakan bahasa Inggris.
- Deskripsi maksimal 72 karakter, gaya imperatif (e.g., "add", not "added").
- Gunakan `!` setelah type/scope untuk menandakan **breaking change**, misal `feat(api)!: change base response structure`.

---

## 5. Alur Pull Request (PR)

1. Buat branch dari `develop` (atau `main` untuk hotfix).
2. Kerjakan perubahan dan commit secara teratur.
3. Sebelum push, **pull perubahan terbaru** dari `develop` dan selesaikan konflik jika ada.
4. Push branch ke remote: `git push origin feature/namanya`.
5. Buka Pull Request ke `develop` (atau `main`) melalui GitHub.
6. Isi template PR yang sudah disediakan (deskripsi, langkah pengujian, screenshot jika UI).
7. Minta review kepada minimal 1 rekan setim.
8. Setelah review disetujui dan semua check (CI/build) hijau, **merge dengan opsi "Squash and Merge"**.
9. Hapus branch fitur setelah di-merge.

**Template PR (`.github/pull_request_template.md`):**

```markdown
## Deskripsi
Jelaskan perubahan yang dilakukan.

## Tipe Perubahan
- [ ] Fitur baru
- [ ] Perbaikan bug
- [ ] Peningkatan teknis
- [ ] Dokumentasi

## Daftar Pengujian
- [ ] Unit test ditambahkan/diperbarui
- [ ] Manual test dilakukan (sebutkan langkah)
- [ ] API response sesuai BaseResponse

## Checklist
- [ ] Branch saya up-to-date dengan develop
- [ ] Saya sudah mengikuti konvensi commit
- [ ] Tidak ada warning atau error baru
```

---

## 6. Code Review

- Reviewer wajib memeriksa: fungsionalitas, keamanan, kesesuaian dengan format `BaseResponse`, potensi bug, dan gaya kode.
- Komentar harus konstruktif dan berbasis kode.
- Pengusul PR berhak memberikan klarifikasi atau revisi.
- Setelah semua komentar terselesaikan, reviewer **Approve** dan pengusul atau reviewer melakukan **merge**.

---

## 7. Versioning dan Tag Rilis

Menggunakan **[Semantic Versioning 2.0.0](https://semver.org/)** → `MAJOR.MINOR.PATCH`

- **MAJOR**: Perubahan yang tidak kompatibel dengan versi sebelumnya (breaking changes).
- **MINOR**: Penambahan fitur baru yang backward-compatible.
- **PATCH**: Perbaikan bug yang backward-compatible.

Tag dibuat di branch `main` setelah merge dari `develop` atau `release/*`.

**Contoh penamaan tag:**

```
v1.0.0   # Rilis pertama
v1.1.0   # Fitur baru (top-up, riwayat transaksi)
v1.1.1   # Perbaikan bug minor
v2.0.0   # Perubahan API signifikan
```

**Proses rilis:**

1. Buat branch `release/v1.2.0` dari `develop`.
2. Lakukan pengujian akhir, perbaiki bug, update versi di `pom.xml` (backend) dan `package.json` (frontend).
3. Merge release ke `main` dan `develop`.
4. Buat tag `v1.2.0` di `main`.
5. Otomatis (atau manual) deploy ke production.

---

## 8. Pengelolaan Issue

Setiap pekerjaan (fitur, bug, task) wajib dibuatkan **Issue** terlebih dahulu sebelum coding.

**Label standar:**

- `bug` – Bug
- `enhancement` – Peningkatan fitur/teknis
- `documentation` – Dokumentasi
- `good first issue` – Cocok untuk kontributor baru
- `backend` / `frontend` – Menunjukkan area kerja

**Issue template** (contoh `bug_report.md`) tersedia di `.github/ISSUE_TEMPLATE/`.

Contoh alur: `Issue #12` → Branch `feature/topup` → Commit `feat(wallet): implement top-up ... closes #12` → PR → Merge.

---

## 9. CI/CD (Opsional)

Dengan GitHub Actions, definisikan workflow untuk:

- **Backend:** Build Maven, run unit test (JUnit), build Docker image.
- **Frontend:** Lint, build Angular, run test (Jasmine/Karma).
- **CD:** Deploy ke staging/production setelah merge ke `develop`/`main`.

Pastikan semua pipeline menghasilkan artefak atau setidaknya mengecek bahwa kode dapat dibangun sebelum PR di-merge.

---

## 10. Aturan Tambahan

- Jangan commit file sensitif (`.env`, `application-prod.properties`) ke repositori; gunakan environment variable atau GitHub Secrets.
- Selalu jalankan test lokal sebelum push: `mvn test` dan `ng test`.
- Gunakan `git rebase` hanya untuk membersihkan history di branch lokal; jangan rebase branch yang sudah dipush publik.
- Lakukan komunikasi melalui komentar Issue/PR, bukan chat pribadi, agar tertelusur.

