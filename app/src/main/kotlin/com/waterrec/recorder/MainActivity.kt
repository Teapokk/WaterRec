package com.waterrec.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.waterrec.recorder.ui.FloatingBubbleService

class MainActivity : AppCompatActivity() {

    companion object {
        private const val OVERLAY_PERMISSION_REQ_CODE = 1234
        private const val PREFS_NAME = "WaterRecSettings"
    }

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setupPremiumProgrammaticUI()
    }

    private fun setupPremiumProgrammaticUI() {
        val screenGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#001220"),
                Color.parseColor("#020813")
            )
        )

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = screenGradient
            setPadding(48, 80, 48, 80)
        }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            addView(rootLayout)
        }

        val titleView = TextView(this).apply {
            text = "WaterRec"
            textSize = 36f
            setTextColor(Color.parseColor("#FFFFFF"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val subtitleView = TextView(this).apply {
            text = "Premium Screen Recorder"
            textSize = 14f
            setTextColor(Color.parseColor("#00E5FF"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, 64)
        }

        rootLayout.addView(titleView)
        rootLayout.addView(subtitleView)

        rootLayout.addView(createSettingsCard("Optional Watermark", "Support us by enabling our watermark on videos.", "pref_watermark", false))
        rootLayout.addView(createSettingsCard("Show Touches", "Natively record screen taps and swipe gestures.", "pref_taps", true))
        rootLayout.addView(createSettingsCard("Record Keyboard", "Capture typing and virtual keyboard inputs.", "pref_keyboard", false))
        rootLayout.addView(createSettingsCard("Hide App UI (Clean Record)", "Exclude WaterRec floating UI from final video.", "pref_hide_ui", true))

        val spacer = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        }
        rootLayout.addView(spacer)

        val cianoButtonBackground = GradientDrawable().apply {
            setColor(Color.parseColor("#00E5FF"))
            cornerRadius = 100f
        }

        val startButton = Button(this).apply {
            text = "LAUNCH OVERLAY"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            background = cianoButtonBackground
            setTextColor(Color.parseColor("#000000"))
            setPadding(0, 48, 0, 48)
            elevation = 12f
            
            setOnClickListener {
                checkOverlayPermissionAndStart()
            }
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 64
            bottomMargin = 32
        }
        rootLayout.addView(startButton, buttonParams)

        setContentView(scrollView)
    }

    private fun createSettingsCard(title: String, subtitle: String, prefKey: String, defaultValue: Boolean): LinearLayout {
        val cardBackground = GradientDrawable().apply {
            setColor(Color.parseColor("#0B192C"))
            cornerRadius = 32f
        }

        val cardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = cardBackground
            setPadding(48, 48, 48, 48)
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }

        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(0, 0, 32, 0)
        }

        val titleText = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.parseColor("#FFFFFF"))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subtitleText = TextView(this).apply {
            text = subtitle
            textSize = 13f
            setTextColor(Color.parseColor("#90E0EF"))
            setPadding(0, 8, 0, 0)
        }

        textColumn.addView(titleText)
        textColumn.addView(subtitleText)

        val switchWidget = Switch(this).apply {
            isChecked = sharedPrefs.getBoolean(prefKey, defaultValue)
            
            setOnCheckedChangeListener { _, isChecked ->
                sharedPrefs.edit().putBoolean(prefKey, isChecked).apply()
            }
        }

        cardContainer.addView(textColumn)
        cardContainer.addView(switchWidget)

        return cardContainer
    }

    private fun checkOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            Toast.makeText(this, "Please grant Overlay Permission", Toast.LENGTH_LONG).show()
        } else {
            startFloatingService()
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingBubbleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startFloatingService()
            } else {
                Toast.makeText(this, "WaterRec needs overlay permission to show the floating bubble.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

