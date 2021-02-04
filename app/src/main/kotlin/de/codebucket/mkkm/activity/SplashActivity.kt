package de.codebucket.mkkm.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import de.codebucket.mkkm.R
import de.codebucket.mkkm.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Wait for the UI to be drawn (in theory)
        binding.root.post {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.color_primary)
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                launch(intent);
            }, 800L)
        }
    }

    private fun launch(intent: Intent) {
        runOnUiThread {
            startActivity(intent)
            overridePendingTransition(0, android.R.anim.fade_out)
            finish()
        }
    }
}
