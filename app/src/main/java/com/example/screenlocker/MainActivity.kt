package com.example.screenlocker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var lockSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lockSwitch = findViewById(R.id.lockSwitch)

        lockSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkOverlayPermission()) {
                    startService(Intent(this, ScreenLockerService::class.java))
                    moveTaskToBack(true)
                } else {
                    lockSwitch.isChecked = false
                }
            } else {
                stopService(Intent(this, ScreenLockerService::class.java))
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Включите разрешение 'Отображать поверх других приложений'", Toast.LENGTH_LONG).show()
            false
        } else {
            true
        }
    }
}