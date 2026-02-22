# Detail Report dengan Status History - Implementation Summary

## Overview
Implementasi fitur untuk menampilkan detail laporan lengkap dengan riwayat status di bottom sheet, baik untuk pengguna masyarakat maupun polisi.

## API Endpoint
**GET /reports/:id**

Response structure sesuai dengan spesifikasi yang diberikan, termasuk:
- Detail laporan (title, description, picture, status_kasus, created_at)
- Informasi pelapor (User)
- Lokasi (Map dengan latitude & longitude)
- Riwayat status (statusHistory) dengan informasi updater

## Files Created/Modified

### 1. Data Models
**File:** `ReportDetailResponse.kt`
- `ReportDetailResponse` - Response wrapper
- `ReportDetailData` - Data laporan lengkap
- `ReportUser` - Informasi user pelapor
- `ReportMap` - Informasi lokasi
- `StatusHistoryItem` - Item riwayat status
- `StatusUpdater` - Informasi user yang mengupdate status

### 2. API Service
**File:** `ApiService.kt`
- Menambahkan endpoint `getReportDetail(token, reportId)` untuk mengambil detail laporan

### 3. Repository
**File:** `CrimeAlertRepository.kt`
- Menambahkan method `getReportDetail(token, reportId)` untuk memanggil API

### 4. UI Layout
**File:** `item_status_history.xml`
- Layout untuk menampilkan setiap item riwayat status
- Menampilkan:
  - Avatar dan nama updater
  - Role updater (Polisi/Masyarakat)
  - Tanggal update
  - Status baru
  - Catatan
  - Foto bukti (jika ada)

**Files:** `activity_detail_cases.xml` & `activity_detail_cases_police.xml`
- Menambahkan section "Riwayat Status"
- RecyclerView untuk menampilkan list status history

### 5. Adapter
**File:** `StatusHistoryAdapter.kt`
- RecyclerView adapter untuk menampilkan riwayat status
- Format tanggal ke GMT+7
- Format status ke bahasa Indonesia
- Load foto bukti dengan Glide

### 6. Activities

**File:** `DetailCasesActivity.kt` (Masyarakat)
- Menambahkan `@AndroidEntryPoint` untuk Hilt injection
- Inject `CrimeAlertRepository`
- Mengubah `setupDetailCasesData()` untuk fetch data dari API
- Menambahkan `fetchReportDetail()` untuk mengambil detail dari server
- Menambahkan `setupStatusHistory()` untuk menampilkan riwayat status
- Menambahkan `updateMapLocation()` untuk update marker di map

**File:** `DetailCasesPoliceActivity.kt` (Polisi)
- Sama seperti DetailCasesActivity
- Menambahkan refresh data setelah update status berhasil
- Status history akan otomatis terupdate setelah polisi mengubah status

## Flow Penggunaan

### Untuk Masyarakat:
1. User membuka detail laporan dengan passing `report_id` via Intent
2. Activity fetch detail laporan dari API
3. Menampilkan informasi lengkap di bottom sheet:
   - Foto laporan
   - Judul & deskripsi
   - Waktu & tanggal kejadian
   - Informasi pelapor
   - **Riwayat Status** (baru)
4. Map menampilkan lokasi kejadian

### Untuk Polisi:
1. Polisi membuka detail laporan dengan passing `report_id` via Intent
2. Activity fetch detail laporan dari API
3. Menampilkan informasi lengkap di bottom sheet (sama seperti masyarakat)
4. Polisi dapat mengubah status melalui Spinner
5. Setelah status berhasil diupdate, data akan di-refresh otomatis
6. Riwayat status akan menampilkan update terbaru

## Fitur Status History

Setiap item di riwayat status menampilkan:
- **Updater Info**: Avatar, nama, dan role (Polisi/Masyarakat)
- **Timestamp**: Tanggal dan waktu update (format: dd/MM/yyyy HH:mm GMT+7)
- **Status**: Status kasus yang diupdate (format Indonesia)
- **Catatan**: Notes dari updater
- **Foto Bukti**: Jika ada evidence_photo, akan ditampilkan

## Status Format Mapping
- `belum_ditangani` → "Belum Ditangani"
- `sedang_ditangani` → "Sedang Ditangani"
- `sudah_ditangani` → "Sudah Ditangani"
- `tidak_dapat_ditangani` → "Tidak Dapat Ditangani"

## Technical Notes

1. **Dependency Injection**: Menggunakan Hilt untuk inject repository
2. **Coroutines**: Semua API calls menggunakan Kotlin Coroutines dengan lifecycleScope
3. **Error Handling**: Proper error handling dengan Toast messages
4. **Image Loading**: Menggunakan Glide untuk load images dengan placeholder
5. **Date Formatting**: Semua timestamp dikonversi ke GMT+7
6. **RecyclerView**: Menggunakan LinearLayoutManager dengan nestedScrollingEnabled=false untuk smooth scrolling di dalam bottom sheet

## Testing Checklist

- [ ] Detail laporan muncul dengan benar
- [ ] Status history ditampilkan dalam urutan yang benar
- [ ] Foto bukti muncul jika ada
- [ ] Format tanggal dan waktu sesuai GMT+7
- [ ] Status dalam bahasa Indonesia
- [ ] Map marker muncul di lokasi yang benar
- [ ] Polisi dapat update status
- [ ] Status history terupdate setelah polisi mengubah status
- [ ] Error handling berfungsi dengan baik
