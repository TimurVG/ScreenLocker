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

        binding.lockSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(this)) {
                    requestOverlayPermission()
                    binding.lockSwitch.isChecked = false
                } else {
                    startService(Intent(this, ScreenLockerService::class.java).apply {
                        action = "START"
                    })
                }
            } else {
                stopService(Intent(this, ScreenLockerService::class.java))
            }
        }
    }

    private fun requestOverlayPermission() {
        startActivity(Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        ))
    }

    override fun onResume() {
        super.onResume()
        // Обновляем состояние переключателя при возвращении в приложение
        binding.lockSwitch.isChecked = isServiceRunning()
    }

    private fun isServiceRunning(): Boolean {
        // Простая проверка работы сервиса
        return try {
            val manager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
            manager.getRunningServices(Integer.MAX_VALUE)
                .any { it.service.className == ScreenLockerService::class.java.name }
        } catch (e: Exception) {
            false
        }
    }
}