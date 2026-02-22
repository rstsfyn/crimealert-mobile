package com.restusofyan.crimealert_mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object CloudinaryUploader {
    
    private const val CLOUD_NAME = "dwnu2kuuf" // From the example URL
    private const val UPLOAD_PRESET = "reports" // From the example URL path
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    
    /**
     * Upload image to Cloudinary
     * @param context Application context
     * @param imageUri URI of the image to upload
     * @return Cloudinary URL of uploaded image, or null if failed
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Read and compress image
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                // Compress bitmap
                val compressedBitmap = compressBitmap(bitmap)
                val byteArrayOutputStream = ByteArrayOutputStream()
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()
                
                // Convert to base64
                val base64Image = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                
                // Create request body
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", base64Image)
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", "reports")
                    .build()
                
                // Create request
                val request = Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build()
                
                // Execute request
                val client = OkHttpClient()
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody ?: "")
                    val secureUrl = jsonObject.optString("secure_url")
                    
                    Log.d("CloudinaryUploader", "Upload successful: $secureUrl")
                    secureUrl
                } else {
                    Log.e("CloudinaryUploader", "Upload failed: ${response.code} - ${response.message}")
                    null
                }
            } catch (e: Exception) {
                Log.e("CloudinaryUploader", "Error uploading image", e)
                null
            }
        }
    }
    
    /**
     * Compress bitmap to reduce file size
     */
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 1024
        val maxHeight = 1024
        
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val ratio = Math.min(
            maxWidth.toFloat() / width.toFloat(),
            maxHeight.toFloat() / height.toFloat()
        )
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
