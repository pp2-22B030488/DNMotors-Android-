package com.example.domain.util

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

    fun playFile(fileOrBase64: String, mediaType: String, context: Context) {
        val file = if (fileOrBase64.startsWith("/") || fileOrBase64.startsWith("file:/")) {
            File(fileOrBase64)
        } else {
            decodeBase64ToTempFile(fileOrBase64, mediaType, context)
        }

        if (file == null || !file.exists()) {
            Log.e(TAG, "Media file not found or failed to create temporary file.")
            Toast.makeText(context, "Media file not found", Toast.LENGTH_SHORT).show()
            return
        }

        val authority = "${context.packageName}.provider"
        val uri = try {
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error getting FileProvider URI", e)
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
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle media type: $mimeType", e)
            Toast.makeText(context, "No app found to play this type of media ($mediaType)", Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException trying to play media", e)
            Toast.makeText(context, "Permission denied while trying to play media", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeBase64ToTempFile(base64: String, mediaType: String, context: Context): File? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val suffix = when (mediaType.lowercase()) {
                "audio" -> ".mp3"
                "video" -> ".mp4"
                else -> ".dat"
            }

            val tempFile = File.createTempFile("media_${System.currentTimeMillis()}", suffix, context.cacheDir)
            tempFile.writeBytes(decodedBytes)
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode base64 and write to file", e)
            null
        }
    }
}
