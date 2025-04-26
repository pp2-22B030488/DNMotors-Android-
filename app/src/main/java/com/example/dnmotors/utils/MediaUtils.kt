package com.example.dnmotors.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object MediaUtils {

    private const val TAG = "MediaUtils"

    fun decodeBase64ToFile(base64: String?, mediaType: String, context: Context): File? {
        if (base64.isNullOrEmpty()) {
            Log.e(TAG, "Input Base64 string is null or empty.")
            return null
        }

        return try {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            val extension = when (mediaType.lowercase()) {
                "audio" -> ".3gp"
                "video" -> ".mp4"
                else -> ".tmp"
            }
            val file = File.createTempFile("media_${System.currentTimeMillis()}", extension, context.cacheDir)

            FileOutputStream(file).use { outputStream ->
                outputStream.write(bytes)
            }
            Log.d(TAG, "Successfully decoded Base64 to file: ${file.absolutePath}")
            file
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to decode Base64 string. It might be corrupted.", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create or write to temporary media file.", e)
            null
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError while decoding Base64 string.", e)
            Toast.makeText(context, "Media file is too large to process", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun playFile(file: File, mediaType: String, context: Context) {
        val authority = "${context.packageName}.provider"
        val uri = try {
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error getting FileProvider URI. Check authority ('$authority') and file path ('${file.absolutePath}'). Is the file path valid and within configured paths?", e)
            Toast.makeText(context, "Error accessing media file", Toast.LENGTH_SHORT).show()
            return
        }


        val mimeType = when (mediaType.lowercase()) {
            "audio" -> "audio/*"
            "video" -> "video/*"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            Log.d(TAG, "Starting activity to play media: URI=$uri, MIME=$mimeType")
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle media type: $mimeType", e)
            Toast.makeText(context, "No app found to play this type of media ($mediaType)", Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException trying to play media. Check URI permissions.", e)
            Toast.makeText(context, "Permission denied while trying to play media", Toast.LENGTH_SHORT).show()
        }
    }

    fun decodeFromBase64(base64: String?): String {
        if (base64.isNullOrEmpty()) return ""
        return try {
            String(Base64.decode(base64, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to decode Base64 string to text.", e)
            ""
        }
    }

}
