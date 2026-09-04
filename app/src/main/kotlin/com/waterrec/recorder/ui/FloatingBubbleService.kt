package com.waterrec.app

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.waterrec.app.R
import com.waterrec.app.recorder.RecorderService

class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var menuView: View? = null
    private var isMenuExpanded = false
    private var isRecording = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createBubble()
        return START_STICKY
    }

    private fun createBubble() {
        bubbleView = createBubbleView()
        val params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            width = 120
            height = 120
            gravity = Gravity.BOTTOM or Gravity.END
            x = 20
            y = 20
        }

        windowManager.addView(bubbleView, params)
    }

    private fun createBubbleView(): FrameLayout {
        val bubbleContainer = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        val bubbleButton = View(this).apply {
            background = createGradientDrawable()
            setOnTouchListener(BubbleTouchListener())
        }

        val params = FrameLayout.LayoutParams(
            120,
            120,
            Gravity.CENTER
        )
        bubbleContainer.addView(bubbleButton, params)

        return bubbleContainer
    }

    private fun createGradientDrawable(): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                resources.getColor(R.color.gradient_start, null),
                resources.getColor(R.color.gradient_end, null)
            )
        ).apply {
            cornerRadius = 60f
        }
    }

    private fun expandMenu() {
        if (isMenuExpanded) return
        isMenuExpanded = true

        bubbleView?.let { windowManager.removeView(it) }

        menuView = createMenuView()
        val params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM or Gravity.END
            x = 20
            y = 20
        }

        windowManager.addView(menuView, params)
    }

    private fun collapseMenu() {
        if (!isMenuExpanded) return
        isMenuExpanded = false

        menuView?.let { windowManager.removeView(it) }
        createBubble()
    }

    private fun createMenuView(): LinearLayout {
        val menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(resources.getColor(R.color.color_primary, null))
            val padding = 16
            setPadding(padding, padding, padding, padding)
        }

        val title = android.widget.TextView(this).apply {
            text = "Recording Controls"
            textSize = 14f
            setTextColor(resources.getColor(R.color.color_text_primary, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        menuContainer.addView(title)

        menuContainer.addView(createMenuButton("Record", android.R.drawable.ic_media_play) {
            toggleRecording()
        })

        if (isRecording) {
            menuContainer.addView(createMenuButton("Pause", android.R.drawable.ic_media_pause) {
                pauseRecording()
            })
        }

        menuContainer.addView(createMenuButton("Stop", android.R.drawable.ic_media_next) {
            stopRecording()
        })

        menuContainer.addView(createMenuButton("Settings", android.R.drawable.ic_dialog_info) {
            openSettings()
        })

        menuContainer.addView(createMenuButton("Close", android.R.drawable.ic_menu_close_clear_cancel) {
            collapseMenu()
        })

        return menuContainer
    }

    private fun createMenuButton(label: String, iconRes: Int, onClick: () -> Unit): LinearLayout {
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 8)
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }

        val icon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(32, 32)
            setImageDrawable(ContextCompat.getDrawable(this@FloatingBubbleService, iconRes))
            setColorFilter(resources.getColor(R.color.color_text_primary, null))
        }

        val text = android.widget.TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(resources.getColor(R.color.color_text_primary, null))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(12, 0, 0, 0)
        }

        buttonLayout.addView(icon)
        buttonLayout.addView(text)

        return buttonLayout
    }

    private fun toggleRecording() {
        if (isRecording) pauseRecording() else startRecording()
    }

    private fun startRecording() {
        isRecording = true
        val intent = Intent(this, RecorderService::class.java)
        intent.action = "ACTION_START_RECORDING"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRecording() {
        isRecording = false
        val intent = Intent(this, RecorderService::class.java)
        intent.action = "ACTION_PAUSE_RECORDING"
        startService(intent)
        Toast.makeText(this, "Recording paused", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false
        val intent = Intent(this, RecorderService::class.java)
        intent.action = "ACTION_STOP_RECORDING"
        startService(intent)
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
    }

    private fun openSettings() {
        val intent = Intent(this, com.waterrec.app.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        bubbleView?.let { windowManager.removeView(it) }
        menuView?.let { windowManager.removeView(it) }
    }

    private inner class BubbleTouchListener : OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var clickTime = 0L

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val params = bubbleView?.layoutParams as? WindowManager.LayoutParams ?: return false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    clickTime = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY
                        windowManager.updateViewLayout(bubbleView, params)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val clickDuration = System.currentTimeMillis() - clickTime
                    if (clickDuration < 200) {
                        expandMenu()
                    }
                }
            }
            return true
        }
    }
}
