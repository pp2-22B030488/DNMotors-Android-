package com.example.domain.util

import android.content.Context
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream

object FileUtils {

    private const val TAG = "FileUtils"

    fun fileToBase64(filePath: String): String {
        return try {
            val file = File(filePath)
            val bytes = FileInputStream(file).use { it.readBytes() }
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "File to Base64 error: ${e.message}")
            ""
        }
    }

    fun encodeToBase64(text: String): String {
        return try {
            Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Text to Base64 error: ${e.message}")
            ""
        }
    }

    fun createTempFile(context: Context, type: String): File? {
        return try {
            val (prefix, suffix) = when (type.lowercase()) {
                "audio" -> Pair("audio_", ".3gp")
                "image" -> Pair("image_", ".jpg")
                "video" -> Pair("video_", ".mp4")
                else -> Pair("file_", ".tmp")
            }

            File.createTempFile(prefix, suffix, context.cacheDir).apply {
                createNewFile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating temp file: ${e.message}")
            null
        }
    }
}
