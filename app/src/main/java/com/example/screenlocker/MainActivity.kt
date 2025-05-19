package com.example.screenlocker

import android.content.Intent
import android.net.Uri
import android.os.Build
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

        checkOverlayPermission()

        binding.switchLock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startLockService()
            } else {
                stopLockService()
            }
        }
    }

    private fun startLockService() {
        val serviceIntent = Intent(this, ScreenLockerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        startActivity(Intent(this, LockScreenActivity::class.java))
    }

    private fun stopLockService() {
        stopService(Intent(this, ScreenLockerService::class.java))
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLockService()
    }
}