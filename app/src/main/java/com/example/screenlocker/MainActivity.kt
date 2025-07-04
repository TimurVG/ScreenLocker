package com.example.screenlocker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.screenlocker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startLockService()
            } else {
                requestOverlayPermission()
            }
        }
    }

    private fun startLockService() {
        startService(Intent(this, LockService::class.java))
        finish()
    }

    private fun requestOverlayPermission() {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:$packageName")
            startActivity(this)
        }
    }
}