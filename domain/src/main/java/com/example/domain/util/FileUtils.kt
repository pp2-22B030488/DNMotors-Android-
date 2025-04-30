package com.example.domain.util

import android.util.Base64
import android.util.Log
import java.io.File

object FileUtils {

    private const val TAG = "FileUtils"

    fun fileToBase64(file: File): String {
        return try {
            Base64.encodeToString(file.readBytes(), Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "File to Base64 error: ${e.message}", e)
            ""
        }
    }
    fun base64ToFile(base64: String, type: String, cacheDir: File): File? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val fileExtension = when (type.lowercase()) {
                "audio" -> ".mp3"
                "video" -> ".mp4"
                else -> ".tmp"
            }
            val tempFile = File.createTempFile("temp_media", fileExtension, cacheDir)
            tempFile.outputStream().use { it.write(bytes) }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
