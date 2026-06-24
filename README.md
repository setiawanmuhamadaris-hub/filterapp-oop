# Aplikasi Filter Foto Pro

Aplikasi **Filter Foto Pro** adalah aplikasi desktop berbasis Java (JavaFX) yang memungkinkan pengguna untuk memanipulasi dan menambahkan berbagai efek filter serta penyesuaian estetika pada gambar atau foto dengan mudah dan cepat. 

Aplikasi ini menggunakan antarmuka pengguna (UI) modern dengan gaya *dark mode* yang intuitif dan elegan.

## ✨ Fitur Utama

- **Filter Siap Pakai (Preset Filters):**
  - Galeri visual dua kolom interaktif untuk memilih filter dengan mudah.
  - Pilihan efek: *Grayscale, Sepia, Blur, Sharpen, Watercolor Paint*, dan *BW Portrait*.
- **Penyesuaian Visual Manual (Adjustments):**
  - Slider interaktif untuk: *Brightness, Contrast, Saturation, Fade, Temperature*, dan *Vignette*.
- **Pemotongan Gambar Cerdas (Crop):**
  - Pilihan rasio presisi: 1:1 (Square), 4:5 (Portrait), 16:9 (Landscape).
  - Dilengkapi fitur *grid pembantu* (rule of thirds) yang dinamis mengikuti ukuran gambar.
  - Dukungan pergeseran (*drag & pan*) dan *zoom* menggunakan mouse.
- **Pengalaman Pengguna (UX) Nyaman:**
  - *Real-time rendering* saat menggeser slider.
  - **Pratinjau Sejajar (Preview):** Bandingkan langsung hasil foto mentah (raw) dengan hasil edit secara berdampingan melalui pop-up khusus.
  - Tombol **Hapus Foto** untuk mengosongkan kanvas tanpa harus me-restart aplikasi.
  - Placeholder unggah foto terintegrasi (drag & drop/klik) di tengah panel gambar.
- **Simpan Hasil Kualitas Tinggi:** Ekspor mahakarya Anda ke format `.jpg` atau `.png`.

## 🛠 Teknologi yang Digunakan

- **Java 21**: Bahasa pemrograman utama yang handal.
- **JavaFX**: Framework utama untuk mendesain GUI reaktif.
- **Maven**: Manajemen dependensi dan manajemen build (`javafx-maven-plugin`).
- **ONNX Runtime (Opsional)**: Framework deep learning inferensi (untuk integrasi AI seperti model AnimeGANv2 dan U2Net).

## 🤖 Kebutuhan Model AI (ONNX)

Aplikasi ini menggunakan teknologi *Machine Learning* untuk filter tingkat lanjut (`BW Portrait` dan `Watercolor Paint`). File model AI tersebut tidak disertakan di dalam repositori karena ukurannya yang besar. Untuk dapat menggunakan filter tersebut, Anda **wajib** mengunduh file model berikut dan meletakkannya tepat di **folder root/utama** project ini:

1. **Model U2-Net Portrait** (`u2net_portrait.onnx`)
   - Digunakan untuk filter **BW Portrait**.
   - Unduh dari repository resmi pembuatnya: [U-2-Net GitHub (Xuebinqin)](https://github.com/xuebinqin/U-2-Net) (Cari tautan unduhan untuk model *u2net_portrait*).
   - Pastikan nama filenya tetap `u2net_portrait.onnx`.

2. **Model AnimeGANv2** (`AnimeGANv2_Hayao.onnx`)
   - Digunakan untuk filter **Watercolor Paint** (gaya Hayao Miyazaki).
   - Unduh langsung dari HuggingFace: [AnimeGANv2_Hayao/tree/main](https://huggingface.co/vumichien/AnimeGANv2_Hayao/tree/main).
   - Pastikan nama filenya tetap `AnimeGANv2_Hayao.onnx`.

## 🚀 Cara Menjalankan Aplikasi

Pastikan sistem Anda sudah terinstal [Java JDK 21+](https://jdk.java.net/) dan [Apache Maven](https://maven.apache.org/).

1. **Clone repositori ke mesin lokal Anda:**
   ```bash
   git clone https://github.com/setiawanmuhamadaris-hub/filterapp-oop.git
   cd filterapp-oop
   ```

2. **Kompilasi proyek:**
   ```bash
   mvn clean compile
   ```

3. **Jalankan Aplikasi:**
   ```bash
   mvn javafx:run
   ```

## 📂 Struktur Direktori Penting

- `src/main/java/com/filterapp/` — Root *source code* (Controllers, Image Processing Algorithms, Utilities).
- `media/` — Kumpulan sampel gambar (*thumbnails*) yang digunakan untuk tampilan grid efek filter.
- `pom.xml` — Konfigurasi struktur Maven.

## 📝 Catatan Kontribusi
Aplikasi ini dikembangkan sebagai bagian dari Project Akhir semester 4 mata kuliah Pemrograman Berorientasi Objek (OOP).
