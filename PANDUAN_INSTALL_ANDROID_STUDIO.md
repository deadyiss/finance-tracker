# Panduan Install Aplikasi via Android Studio

## Prasyarat

- Android Studio (versi stabil terbaru, contoh: Ladybug atau lebih baru).
- HP Android dengan **minSdk 26** (Android 8.0 Oreo) atau lebih baru.
- Kabel USB untuk menghubungkan HP ke komputer.
- Koneksi internet di komputer (untuk download Gradle dan dependency saat sync pertama).

## Langkah 1 — Buka proyek di Android Studio

1. Clone atau download proyek ini.
2. Jalankan Android Studio.
3. Di halaman welcome → **File → Open** → pilih folder proyek.
4. Klik **Trust Project** jika muncul dialog.

## Langkah 2 — Gradle Sync

Android Studio otomatis menjalankan Gradle Sync setelah proyek terbuka. Tunggu sampai selesai (status bar bawah menunjukkan "Gradle sync finished").

Jika muncul **Upgrade Assistant** menawarkan naik versi AGP (Android Gradle Plugin), klik **Upgrade**. AGP 8.13.2 + Gradle 8.13 sudah diverifikasi kompatibel.

## Langkah 3 — Aktifkan USB Debugging di HP

1. Buka **Settings** di HP.
2. Pergi ke **About Phone** (atau **Tentang Telepon**).
3. Tap **Build Number** sebanyak **7 kali** sampai muncul pesan "You are now a developer".
4. Kembali ke Settings → cari **Developer Options**.
5. Aktifkan toggle **USB Debugging**.

## Langkah 4 — Hubungkan HP via USB

1. Colok HP ke komputer dengan kabel USB.
2. Di HP akan muncul popup "Allow USB debugging?" — pilih **Allow**.
3. Di Android Studio toolbar atas, HP-mu sudah terdeteksi di dropdown device.

Kalau HP tidak terdeteksi:
- Cek kabel USB (sebagian kabel hanya untuk charging, tidak data).
- Di HP, geser notifikasi USB → pilih **File Transfer**.
- Install driver USB OEM (terutama untuk Windows + HP non-Pixel/Samsung).

## Langkah 5 — Run aplikasi

Klik tombol **Run ▶** (atau Shift+F10). Tunggu proses build (~1–3 menit pertama, lebih cepat di build berikutnya). Aplikasi otomatis ter-install dan terbuka di HP.

## Langkah 6 — (Opsional) Build APK manual

Untuk membuat file `.apk` untuk di-share atau install di HP lain:

1. Menu **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
2. APK ada di `app/build/outputs/apk/debug/app-debug.apk`.
3. Pindahkan APK ke HP, install via file manager. Aktifkan **Install unknown apps** untuk file manager yang dipakai.

## Troubleshooting

### Gradle Sync gagal terus

- Cek **File → Settings → Build, Execution, Deployment → Gradle** — pastikan Gradle JDK pakai JDK 17.
- Terima saran upgrade dari Upgrade Assistant kalau muncul.

### HP tidak terdeteksi di Android Studio

- Aktifkan **File Transfer** mode di notifikasi USB di HP.
- Install driver USB OEM (Windows: terutama untuk Xiaomi/Vivo/Oppo).
- Coba kabel USB lain.
