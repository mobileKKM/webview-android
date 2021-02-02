package de.codebucket.mkkm.activity

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import de.codebucket.mkkm.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

}