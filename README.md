# Prosperity Tracker

Aplikasi pencatat keuangan pribadi untuk Android. Berjalan **sepenuhnya offline** — tidak ada izin INTERNET, tidak ada server, tidak ada akun, semua data tersimpan lokal di SQLite (Room) pada perangkat.

Antarmuka berbahasa Indonesia. Mata uang Rupiah (IDR), disimpan sebagai bilangan bulat (boleh negatif untuk akun seperti kartu kredit).

## Preview Aplikasi
Link : https://deadyiss.github.io/finance-tracker/
## Fitur

- **Beranda** — total saldo (jumlah seluruh akun), ringkasan pemasukan/pengeluaran bulan berjalan, donut kategori, daftar transaksi terbaru.
- **Tambah Transaksi** — toggle Pengeluaran/Pemasukan; Pengeluaran memilih "Bayar dari" akun, Pemasukan memilih "Simpan ke" akun; saldo akun terkait otomatis ter-update.
- **Kategori kustom** — dapat ditambah dari layar Tambah (nama + emoji), terpisah untuk tipe Pengeluaran dan Pemasukan.
- **Riwayat** — daftar seluruh transaksi, dapat dihapus (saldo akun otomatis dikoreksi balik).
- **Anggaran**
  - Daftar Akun: tambah/edit/hapus akun, saldo boleh minus.
  - Pembagian Anggaran: alokasi nominal per kategori pengeluaran per bulan + tampilan "Belum dialokasikan".
- **Laporan**
  - Granularitas pilihan: **Tahun**, **Bulan**, **Hari**.
  - Toggle Pemasukan/Pengeluaran mengontrol donut, kalender, dan daftar kategori.
  - Saldo bersih + Pemasukan + Pengeluaran untuk periode terpilih.
  - **Donut chart** distribusi kategori sesuai tipe + periode.
  - **Kalender interaktif**:
    - Mode Bulan/Hari: grid harian dengan nominal hari itu di tiap sel; tap tanggal → drill ke mode Hari.
    - Mode Tahun: grid 12 bulan dengan total per bulan; tap bulan → drill ke mode Bulan.
  - **Daftar Kategori** lengkap untuk periode terpilih, urut nominal terbesar.
- **Navigasi**
  - Tombol back perangkat kembali ke layar sebelumnya (bukan keluar aplikasi).
  - Transisi antar tab tanpa animasi (instan).

## Teknologi

| Komponen | Versi |
|---|---|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.7.0 (Upgrade Assistant akan menyarankan naik, lihat catatan di bawah) |
| Compose BOM | 2024.10.01 |
| Compose Compiler Plugin | `org.jetbrains.kotlin.plugin.compose` 2.0.21 |
| Room | 2.6.1 (via KSP) |
| Navigation Compose | 2.8.3 |
| Lifecycle ViewModel/Runtime Compose | 2.8.7 |
| `compileSdk` / `targetSdk` | 35 |
| `minSdk` | 26 (Android 8.0 Oreo) |
| Java | 17 |

Tidak ada library chart eksternal. Donut dan grid kalender digambar/ditata pakai Compose Canvas dan layout primitives.

## Persyaratan Build

- Android Studio versi stabil terbaru.
- JDK 17 (dibundel oleh Android Studio).
- Koneksi internet **di komputer build** untuk Gradle Sync pertama (mengunduh dependency Android SDK & Maven). Aplikasi hasilnya tetap offline di perangkat.

## Build & Install

Detail langkah-per-langkah ada di [`PANDUAN_DEPLOY.md`](PANDUAN_DEPLOY.md). Ringkasnya:

1. Ekstrak proyek, buka folder root di Android Studio (**File → Open**).
2. Tunggu Gradle Sync. Jika Upgrade Assistant menawarkan naik versi AGP, terima sarannya (AGP 8.13.2 + Gradle 8.13 sudah diverifikasi kompatibel).
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. APK debug ada di `app/build/outputs/apk/debug/app-debug.apk`.
5. Pindahkan APK ke perangkat dan install (aktifkan **Install unknown apps** untuk file manager / browser yang dipakai membuka APK).

Alternatif lebih cepat: aktifkan USB debugging di HP, colok ke komputer, tekan tombol **Run** di Android Studio.

## Struktur Proyek

```
ProsperityTracker/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/prosperity/tracker/
│       │   ├── MainActivity.kt              # Setup Scaffold, NavHost, bottom nav
│       │   ├── ProsperityApp.kt             # Application class, seed defaults
│       │   ├── data/
│       │   │   ├── Entities.kt              # Account, Category, Transaction, Budget
│       │   │   ├── Daos.kt                  # Akses DB per entitas
│       │   │   ├── AppDatabase.kt           # Room database (singleton)
│       │   │   └── FinanceRepository.kt     # Single access point ke data
│       │   ├── viewmodel/
│       │   │   ├── FinanceViewModel.kt      # State + commands
│       │   │   └── Aggregations.kt          # Helper agregasi/filter list transaksi
│       │   ├── ui/
│       │   │   ├── theme/Theme.kt           # Palette & ProsperityTheme
│       │   │   ├── components/Charts.kt     # DonutChart, IncomeExpenseBarChart
│       │   │   └── screens/
│       │   │       ├── CommonUi.kt          # Komposable yang dipakai banyak layar
│       │   │       ├── HomeScreen.kt
│       │   │       ├── HistoryScreen.kt
│       │   │       ├── AddScreen.kt
│       │   │       ├── BudgetScreen.kt
│       │   │       └── ReportsScreen.kt
│       │   └── util/Format.kt               # formatRupiah, helper tanggal
│       └── res/                              # drawable launcher, themes, mipmap, strings
├── assets/logo.svg                          # Logo full-color
├── preview.html                              # Prototipe HTML interaktif
├── PANDUAN_DEPLOY.md                         # Panduan build/install bahasa Indonesia
├── build.gradle.kts                          # Root build script
├── settings.gradle.kts
├── gradle.properties
└── gradle/wrapper/gradle-wrapper.properties
```

## Arsitektur

- **MVVM** sederhana: Composable → ViewModel (StateFlow) → Repository → Room DAO.
- **Reactive flow**: Room mengeluarkan `Flow<List<...>>`, dikonversi jadi `StateFlow` di ViewModel, dikonsumsi di Composable lewat `collectAsStateWithLifecycle`. UI otomatis update saat data berubah.
- **Tidak ada DI framework.** ViewModel menerima Repository lewat factory manual (`FinanceViewModelFactory`).
- **Tanpa foreign key keras antar tabel.** Disengaja agar hapus akun/kategori tidak crash karena constraint. Integritas (mis. balance accounting) dijaga di Repository.
- **Migrasi DB**: `fallbackToDestructiveMigration` aktif — saat skema berubah, database direset. Aman untuk pemakaian pribadi, tapi data lama hilang jika struktur tabel diubah tanpa Migration manual.
- **Format uang**: disimpan sebagai `Long` (Rupiah utuh, tanpa pecahan). Tidak ada perhitungan pajak/inflasi.

## Catatan Teknis

- **Font**: desain memakai Lexend. Font tidak dibundel; aplikasi memakai font sistem. Untuk tampilan persis sesuai desain, tambahkan Lexend sebagai resource font (`res/font/`) dan set di `Theme.kt`.
- **Tema gelap**: tidak diimplementasikan. Hanya light theme.
- **Tanggal & waktu**: pakai `java.time` (dibutuhkan `minSdk 26`).
- **Currency lock**: hard-coded ke Rupiah.
- **Belum ada**: unit test, instrumentation test, signing config untuk release build, ProGuard rules kustom.

## Lisensi

Tidak ditentukan. Proyek pribadi.
