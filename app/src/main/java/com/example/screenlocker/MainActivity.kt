package com.example.screenlocker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var switchLock: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchLock = findViewById(R.id.switchLock)

        switchLock.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                if (hasOverlayPermission()) {
                    startService(Intent(this, ScreenLockerService::class.java))
                    Toast.makeText(this, "Сервис блокировки запущен", Toast.LENGTH_SHORT).show()
                } else {
                    switchLock.isChecked = false
                    requestOverlayPermission()
                }
            } else {
                stopService(Intent(this, ScreenLockerService::class.java))
                Toast.makeText(this, "Сервис блокировки остановлен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Включите разрешение 'Отображать поверх других окон'", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasOverlayPermission() && switchLock.isChecked) {
            startService(Intent(this, ScreenLockerService::class.java))
        }
    }
}