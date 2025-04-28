package com.example.domain.util

import android.content.Context
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream

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


    fun encodeToBase64(text: String): String {
        return try {
            Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Text to Base64 error: ${e.message}")
            ""
        }
    }

}
