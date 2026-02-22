# Update Status Kasus dengan Foto Bukti dan Catatan

## Perubahan yang Dilakukan

### 1. **UpdateStatusRequest.kt**
- Menambahkan field `evidencePhoto` (opsional) untuk URL foto bukti
- Menambahkan field `notes` (opsional) untuk catatan polisi
- Menggunakan `@SerializedName` untuk mapping JSON

### 2. **ApiService.kt**
- Mengubah endpoint `updateStatus` dari `@Body` menjadi `@Multipart`
- Menerima parameter:
  - `status_kasus`: RequestBody (wajib)
  - `evidence_photo`: MultipartBody.Part (opsional)
  - `notes`: RequestBody (opsional)

### 3. **CrimeAlertRepository.kt**
- Update method `updateReportStatus` untuk menerima:
  - `evidencePhoto`: MultipartBody.Part (opsional)
  - `notes`: String (opsional)
- Konversi parameter ke RequestBody sebelum dikirim ke API

### 4. **DetailCasesPoliceActivity.kt**
- Menambahkan import untuk:
  - `ActivityResultContracts` untuk photo picker
  - `File` untuk file handling
  - `okhttp3` untuk multipart
- Update `showUpdateStatusDialog`:
  - Menampilkan custom dialog dengan layout `dialog_update_status`
  - Fitur upload foto bukti (opsional)
  - Input catatan (opsional)
  - Preview foto yang dipilih
  - Tombol hapus foto
- Update `updateCaseStatus`:
  - Menerima parameter `photoUri` dan `notes`
  - Konversi URI foto ke File
  - Membuat MultipartBody.Part dari file
  - Mengirim ke backend

### 5. **dialog_update_status.xml** (New File)
Layout dialog untuk update status dengan:
- TextView untuk menampilkan status baru
- CardView untuk preview foto (hidden by default)
- Button untuk pilih foto
- TextInputEditText untuk catatan
- Button Cancel dan Confirm

## Response API

Endpoint: `PUT /api/reports/{id}/status`

Request (Multipart):
- `status_kasus`: String (wajib)
- `evidence_photo`: File (opsional)
- `notes`: String (opsional)

Response:
```json
{
    "error": false,
    "message": "Report status updated successfully",
    "data": {
        "status_kasus": "sudah_ditangani",
        "evidence_photo": "https://res.cloudinary.com/dwnu2kuuf/image/upload/v1770883189/reports/ydyqyjtzwkluty4jzy5u.jpg"
    }
}
```

## Cara Penggunaan

1. Polisi membuka detail kasus
2. Memilih status baru dari dropdown
3. Dialog muncul dengan opsi:
   - Upload foto bukti (opsional)
   - Tambah catatan (opsional)
4. Klik "Update Status"
5. Status history akan di-refresh otomatis

## Catatan

- Foto bukti dan catatan bersifat opsional
- Backend akan handle upload ke Cloudinary
- Status history akan menampilkan foto bukti jika ada
- Force close issue sudah diperbaiki dengan proper null handling
