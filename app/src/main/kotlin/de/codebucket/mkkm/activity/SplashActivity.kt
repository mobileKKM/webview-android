package de.codebucket.mkkm.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

import androidx.appcompat.app.AppCompatActivity

import de.codebucket.mkkm.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            launch(intent);
        }, 1500L)
    }

    private fun launch(intent: Intent) {
        runOnUiThread {
            startActivity(intent)
            overridePendingTransition(0, android.R.anim.fade_out)
            finish()
        }
    }
}
