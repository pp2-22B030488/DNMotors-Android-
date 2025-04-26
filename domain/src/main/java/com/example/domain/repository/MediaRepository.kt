package com.example.domain.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import java.io.File

class MediaRepository(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false

    fun startRecording(onStart: () -> Unit, onFailure: (String) -> Unit) {
        if (isRecording) return

        val fileName = "recorded_audio_${System.currentTimeMillis()}.3gp"
        val file = File(context.cacheDir, fileName)
        audioFilePath = file.absolutePath
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

    fun stopRecording(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val recorder = mediaRecorder ?: return onFailure()
        val path = audioFilePath ?: return onFailure()

        try {
            recorder.stop()
            onSuccess(path)
        } catch (e: Exception) {
            File(path).delete()
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
