package com.example.dnmotors.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import android.util.Base64

object MediaUtils {
    fun playBase64Media(base64: String, mediaType: String, context: Context) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val file = File.createTempFile("temp", if (mediaType == "audio") ".3gp" else ".mp4", context.cacheDir)
            file.writeBytes(bytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (mediaType == "audio") "audio/*" else "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to play media", Toast.LENGTH_SHORT).show()
        }
    }

    fun decodeFromBase64(base64: String): String {
        return String(Base64.decode(base64, Base64.DEFAULT), Charsets.UTF_8)
    }
    fun decodeBase64ToFile(base64: String, mediaType: String, context: Context): File? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val file = File.createTempFile("media", if (mediaType == "audio") ".3gp" else ".mp4", context.cacheDir)
            file.writeBytes(bytes)
            file
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
    fun playFile(file: File, mediaType: String, context: Context) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, if (mediaType == "audio") "audio/*" else "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
