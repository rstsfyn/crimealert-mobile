package com.restusofyan.crimealert_mobile.ui.users.createreport

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.data.response.createreport.AddNewReportResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: CrimeAlertRepository
) : ViewModel() {

    var capturedImage: Bitmap? = null

    private val _uploadState = MutableLiveData<Result<AddNewReportResponse>>()
    val uploadState: LiveData<Result<AddNewReportResponse>> = _uploadState

    fun uploadReport(
        token: String,
        title: String,
        description: String,
        latitude: String?,
        longitude: String?
    ) {
        viewModelScope.launch {
            try {
                val imageBitmap = capturedImage ?: throw Exception("No image captured")
                val imagePart = bitmapToMultipart(imageBitmap)

                val titleBody = title.toRequestBody()
                val descBody = description.toRequestBody()
                val latBody = latitude?.toRequestBody()
                val lonBody = longitude?.toRequestBody()

                val response = repository.createNewReport(
                    token,
                    imagePart,
                    titleBody,
                    descBody,
                    latBody,
                    lonBody
                )
                if (response.isSuccessful) {
                    _uploadState.value = Result.success(response.body()!!)
                } else {
                    _uploadState.value = Result.failure(Exception("Upload failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                _uploadState.value = Result.failure(e)
            }
        }
    }

    private fun bitmapToMultipart(bitmap: Bitmap): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), stream.toByteArray())
        return MultipartBody.Part.createFormData("image", "report.jpg", requestFile)
    }

    private fun String.toRequestBody(): RequestBody =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)
}
