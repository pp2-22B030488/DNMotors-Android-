package com.example.domain.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
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
        recordedFile = file // <<< ADD THIS
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
            // Not actually recording, nothing to stop
            file.delete()
            onFailure()
            cleanupRecorder()
            return
        }

        try {
            recorder.stop() // Only if truly recording
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
        recordedFile = null // << ADD THIS
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

}
