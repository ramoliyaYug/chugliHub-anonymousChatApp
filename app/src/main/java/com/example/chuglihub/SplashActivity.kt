package com.example.chuglihub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImage: ImageView
    private lateinit var appName: TextView
    private lateinit var appTagline: TextView
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize views
        logoImage = findViewById(R.id.logoImage)
        appName = findViewById(R.id.appName)
        appTagline = findViewById(R.id.appTagline)
        loadingSpinner = findViewById(R.id.loadingSpinner)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)

        // Start animations with delays
        logoImage.startAnimation(bounce)

        Handler(Looper.getMainLooper()).postDelayed({
            appName.visibility = View.VISIBLE
            appName.startAnimation(fadeIn)
        }, 800)

        Handler(Looper.getMainLooper()).postDelayed({
            appTagline.visibility = View.VISIBLE
            appTagline.startAnimation(slideUp)
        }, 1200)

        Handler(Looper.getMainLooper()).postDelayed({
            loadingSpinner.visibility = View.VISIBLE
            loadingSpinner.startAnimation(fadeIn)
        }, 1500)

        // Navigate to main activity after splash duration
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 3000) // 3 seconds splash time
    }
}