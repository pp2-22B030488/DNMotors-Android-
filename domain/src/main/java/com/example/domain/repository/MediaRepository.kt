package com.example.domain.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MediaRepository(private val context: Context) {
    private val TAG = "Media Repository"

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var recordedFile: File? = null

    fun startRecording(onStart: () -> Unit, onFailure: (String) -> Unit) {
        if (isRecording) return

        val fileName = "recorded_audio_${System.currentTimeMillis()}.mp3"
        val file = File(context.cacheDir, fileName)
        audioFilePath = file.absolutePath
        recordedFile = file
        var recorder: MediaRecorder? = null

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            onStart()
        } catch (e: Exception) {
            recorder?.release()
            cleanupRecorder()
            onFailure("Recording failed: ${e.message}")
        }
    }

    fun stopRecording(onSuccess: (File) -> Unit, onFailure: () -> Unit) {
        val recorder = mediaRecorder
        val file = recordedFile

        if (recorder == null || file == null) {
            onFailure()
            cleanupRecorder()
            return
        }

        if (!isRecording) {
            file.delete()
            onFailure()
            cleanupRecorder()
            return
        }

        try {
            recorder.stop()
            onSuccess(file)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException when stopping recorder: ${e.message}", e)
            file.delete()
            onFailure()
        } catch (e: RuntimeException) {
            Log.e(TAG, "RuntimeException when stopping recorder: ${e.message}", e)
            file.delete()
            onFailure()
        } finally {
            cleanupRecorder()
        }
    }

    fun cleanupRecorder() {
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        audioFilePath = null
        recordedFile = null
    }

    fun isRecording(): Boolean = isRecording

    fun requestAudioPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            onPermissionGranted()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun createImageFile(): File {
        val timeStamp: String = System.currentTimeMillis().toString()
        val fileName = "JPEG_${timeStamp}_.jpg"
        return File(context.cacheDir, fileName)
    }

    fun createVideoFile(): File {
        val timeStamp: String = System.currentTimeMillis().toString()
        val fileName = "VIDEO_${timeStamp}_.mp4"
        return File(context.cacheDir, fileName)
    }
}


object MediaRepoUseCase {
    const val REQUEST_PERMISSION_CODE = 1234
    fun createImageFile(context: Context): File? {
        // Check if the app has the necessary permissions
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CODE
            )
            return null
        }

        // If permissions are granted, proceed with creating the file
        val imagesDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
        val timeStamp = System.currentTimeMillis()
        return File(imagesDir, "IMG_${timeStamp}.jpg")
    }

    fun createVideoFile(context: Context): File? {
        // Check if the app has the necessary permissions
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CODE
            )
            return null
        }

        // If permissions are granted, proceed with creating the file
        val videosDir = File(context.cacheDir, "videos").apply { if (!exists()) mkdirs() }
        val timeStamp = System.currentTimeMillis()
        return File(videosDir, "VID_${timeStamp}.mp4")
    }


    fun decodeSampledBitmapFromFile(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


}