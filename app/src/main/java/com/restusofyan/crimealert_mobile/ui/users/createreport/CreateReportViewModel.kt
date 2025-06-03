package com.restusofyan.crimealert_mobile.ui.users.createreport

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.*
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.data.response.createreport.AddNewReportResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val storyRepository: CrimeAlertRepository
) : ViewModel() {

    private val _uploadResult = MutableLiveData<UploadResult>()
    val uploadResult: LiveData<UploadResult> = _uploadResult

    sealed class UploadResult {
        object Loading : UploadResult()
        data class Success(val message: String) : UploadResult()
        data class Error(val message: String) : UploadResult()
    }

    fun compressImage(filePath: String, maxSize: Long = 1 * 1024 * 1024): File? {
        val file = File(filePath)
        if (!file.exists()) return null

        if (file.length() <= maxSize) {
            return file
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap == null) {
            Log.e("AddStoryViewModel", "Failed to create bitmap")
            return null
        }

        val tempFile = File(file.parent, "compressed_${file.name}")

        var quality = 100
        var fileSize: Long

        do {
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            fileSize = tempFile.length()

            quality -= 10
        } while (fileSize > maxSize && quality > 10)
        return if (fileSize <= maxSize) tempFile else null
    }

    fun uploadReport(
        token: String,
        description: String,
        title: String,
        file: File,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        if (title.isBlank()) {
            _uploadResult.value = UploadResult.Error("Title cannot be empty")
            return
        }

        if (description.isBlank()) {
            _uploadResult.value = UploadResult.Error("Description cannot be empty")
            return
        }

        if (!file.exists()) {
            _uploadResult.value = UploadResult.Error("File not found")
            return
        }

        _uploadResult.value = UploadResult.Loading


        val titlePart = RequestBody.create("text/plain".toMediaTypeOrNull(), title)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val latitudePart =
            latitude?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString()) }
        val longitudePart =
            longitude?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it.toString()) }

        val filePart = MultipartBody.Part.createFormData(
            "picture",
            file.name,
            RequestBody.create("image/*".toMediaTypeOrNull(), file)
        )

        storyRepository.createNewReport(token, titlePart, descriptionPart, latitudePart, longitudePart, filePart)
            .enqueue(object : Callback<AddNewReportResponse> {
                override fun onResponse(
                    call: Call<AddNewReportResponse>,
                    response: Response<AddNewReportResponse>
                ) {
                    if (response.isSuccessful && response.body()?.error == false) {
                        _uploadResult.value = UploadResult.Success("Story uploaded successfully")
                    } else {
                        _uploadResult.value = UploadResult.Error("Failed to upload story")
                    }
                }

                override fun onFailure(call: Call<AddNewReportResponse>, t: Throwable) {
                    _uploadResult.value = UploadResult.Error("Network error: ${t.message}")
                }
            })
    }
}
