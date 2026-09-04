package com.waterrec.recorder.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.Environment
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecorderService : Service() {

    companion object {
        private const val CHANNEL_ID = "WaterRecServiceChannel"
        private const val NOTIFICATION_ID = 1
        var mediaProjection: MediaProjection? = null
        var resultCode: Int = 0
        var resultData: Intent? = null
    }

    private var mediaRecorder: MediaRecorder? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var isRunning = false
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_START_RECORDING" -> startRecordingProcess()
            "ACTION_PAUSE_RECORDING" -> pauseRecordingProcess()
            "ACTION_STOP_RECORDING" -> stopRecordingProcess()
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "WaterRec Recording Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WaterRec is Recording")
            .setContentText("Tap the floating bubble to manage recording")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    private fun startRecordingProcess() {
        if (isRunning) return
        startForeground(NOTIFICATION_ID, createNotification())
        
        try {
            initMediaRecorder()
            initVirtualDisplay()
            mediaRecorder?.start()
            isRunning = true
            isPaused = false
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun initMediaRecorder() {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "WaterRec_${dateFormat.format(Date())}.mp4"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName)

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncodingBitRate(512 * 1000 * 8)
            setVideoFrameRate(30)
            setVideoSize(screenWidth, screenHeight)
            setOutputFile(file.absolutePath)
            prepare()
        }
    }

    private fun initVirtualDisplay() {
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (mediaProjection == null && resultCode != 0 && resultData != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData!!)
        }

        mediaProjection?.let {
            virtualDisplay = it.createVirtualDisplay(
                "WaterRecScreenRecorder",
                metrics.widthPixels,
                metrics.heightPixels,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface,
                null,
                null
            )
        }
    }

    private fun pauseRecordingProcess() {
        if (!isRunning) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isPaused) {
                mediaRecorder?.resume()
                isPaused = false
            } else {
                mediaRecorder?.pause()
                isPaused = true
            }
        }
    }

    private fun stopRecordingProcess() {
        if (!isRunning) return
        try {
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        virtualDisplay?.release()
        mediaProjection?.stop()
        mediaProjection = null

        isRunning = false
        isPaused = false
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopRecordingProcess()
    }
}

