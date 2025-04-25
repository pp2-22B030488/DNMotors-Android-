package com.example.dnmotors.utils

import android.util.Base64
import android.util.Log
import android.widget.Toast
import java.io.File
import androidx.core.content.ContextCompat

object MultimediaUtils {
    internal fun fileToBase64(path: String?): String {
        if (path == null) {
            Log.e("MessagesFragment", "fileToBase64: Input path is null")
            return ""
        }

        val file = File(path)
        if (!file.exists()) {
            Log.e("MessagesFragment", "fileToBase64: File does not exist at path: $path")
            return ""
        }

        val fileSizeInMB = file.length() / (1024.0 * 1024.0)
        val maxSizeMB = 2500
        if (fileSizeInMB > maxSizeMB) {
            Log.w("MessagesFragment", "fileToBase64: File too large ($fileSizeInMB MB), max allowed is $maxSizeMB MB")
            return ""
        }

        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("MessagesFragment", "fileToBase64: Failed to encode file", e)
            ""
        }
    }


    fun encodeToBase64(input: String): String {
        return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

}