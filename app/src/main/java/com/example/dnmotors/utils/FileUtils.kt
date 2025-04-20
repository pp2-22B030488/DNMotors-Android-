package com.example.dnmotors.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object FileUtils {

    private const val TAG = "FileUtils"

    fun getPath(context: Context, uri: Uri): String? {
        val fileName = getFileName(context, uri) ?: "temp_media_${System.currentTimeMillis()}" // Fallback filename
        val destinationFile = File(context.cacheDir, fileName)

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open InputStream for URI: $uri")
                return null
            }
            outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()

            Log.d(TAG, "Successfully copied URI content to cache file: ${destinationFile.absolutePath}")
            return destinationFile.absolutePath

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Permission denied for URI: $uri", e)
            return null
        } catch (e: IOException) {
            Log.e(TAG, "IOException during file copy from URI: $uri", e)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing InputStream", e)
            }
            try {
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing FileOutputStream", e)
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex)
                    } else {
                        Log.w(TAG,"DISPLAY_NAME column not found for URI: $uri")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying content resolver for file name", e)
                result = null
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        Log.d(TAG, "Resolved filename for URI $uri: $result")
        return result
    }
}
