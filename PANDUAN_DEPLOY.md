# Prosperity Tracker — Panduan Build & Install (Offline)

Aplikasi ini sepenuhnya offline: tidak ada izin INTERNET, tidak ada server, data disimpan lokal di HP lewat SQLite (Room). Berikut cara mengubah kode jadi APK yang bisa di-install.

## Catatan jujur soal lingkungan
APK **tidak bisa** saya hasilkan di tempat saya bekerja (tidak ada Android SDK, dan repositori Google Maven diblokir di jaringan saya). Jadi yang saya berikan adalah **kode proyek lengkap**. Build dilakukan di komputermu memakai Android Studio. Satu hal yang paling mungkin perlu disesuaikan saat pertama kali: **versi plugin/dependency** — Gradle Sync akan memberi tahu jika ada yang perlu diperbarui.

## Yang dibutuhkan
- Android Studio (versi terbaru, mis. Ladybug atau lebih baru).
- Saat pertama buka, biarkan Android Studio mengunduh Gradle, Android SDK (API 35), dan dependency. Ini butuh internet **di komputer** (bukan di HP). Aplikasi yang jadi tetap berjalan offline di HP.

## Langkah build
1. Ekstrak `ProsperityTracker.zip`.
2. Buka Android Studio → **Open** → pilih folder `ProsperityTracker`.
3. Tunggu **Gradle Sync** selesai. Jika ada peringatan versi (AGP/Kotlin/Compose), terima saran "Upgrade" dari Android Studio, atau sesuaikan angka versi di:
   - `build.gradle.kts` (level proyek) — versi plugin.
   - `app/build.gradle.kts` — Compose BOM dan dependency lain.
4. Setelah sync hijau (tanpa error), buat APK:
   - Menu **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
   - APK debug akan ada di: `app/build/outputs/apk/debug/app-debug.apk`.
   - (Debug sudah cukup untuk dipakai sendiri. Untuk versi release tertandatangani perlu langkah signing terpisah.)

## Install ke HP
1. Pindahkan `app-debug.apk` ke HP (kabel USB, atau cara lain).
2. Di HP, aktifkan **Install unknown apps** untuk aplikasi yang kamu pakai membuka file (Settings → Apps → akses khusus).
3. Buka file APK → Install.
4. Selesai. Aplikasi jalan offline; semua data (akun, transaksi, anggaran) tersimpan di HP.

## Alternatif: install langsung lewat USB (tanpa cari file APK)
1. Aktifkan **Developer Options** + **USB Debugging** di HP.
2. Colok HP ke komputer.
3. Di Android Studio, pilih perangkatmu di bar atas, tekan tombol **Run** (▶). Aplikasi langsung ter-install dan terbuka.

## Yang sudah termasuk di aplikasi
- Bahasa Indonesia penuh, semua nominal mulai Rp 0.
- Logo baru (koin Rp + panah masuk hijau / keluar merah) sebagai ikon launcher dan header.
- Beranda: total saldo (jumlah semua akun), pemasukan/pengeluaran bulan ini, donut ringkasan, transaksi terbaru.
- Tambah: toggle Pengeluaran/Pemasukan; Pengeluaran punya "Bayar dari", Pemasukan punya "Simpan ke"; bisa tambah kategori baru (nama + emoji).
- Riwayat: semua transaksi, bisa hapus (saldo akun otomatis dikoreksi).
- Anggaran: Daftar Akun (tambah/edit/hapus, saldo boleh minus) + Pembagian Anggaran (list sederhana per kategori + "Belum dialokasikan").
- Laporan: saldo bersih, grafik arus kas 6 bulan, kategori teratas.
- Tombol back HP kembali ke layar sebelumnya (bukan keluar aplikasi).

## Catatan teknis
- `minSdk = 26` (Android 8.0). HP di bawah itu tidak didukung (dipakai untuk `java.time`).
- Font Lexend dari desain tidak dibundel; aplikasi memakai font sistem. Bisa ditambahkan nanti sebagai resource font kalau mau identik.
- Tidak ada FK keras antar tabel — sengaja, supaya hapus akun/kategori tidak crash. Integritas diatur di kode (Repository).
- Database akan reset jika skema berubah (fallbackToDestructiveMigration) — aman untuk pemakaian pribadi, tapi artinya jangan andalkan untuk migrasi data antar versi tanpa menambah Migration.
