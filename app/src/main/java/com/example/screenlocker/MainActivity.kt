package com.example.screenlocker

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.screenlocker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LockOverlayView.UnlockListener {
    private lateinit var binding: ActivityMainBinding
    private val OVERLAY_PERMISSION_REQ_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lockSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkOverlayPermission()
            } else {
                stopService(Intent(this, ScreenLockerService::class.java))
            }
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        } else {
            ScreenLockerService.start(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Settings.canDrawOverlays(this)) {
                ScreenLockerService.start(this)
            } else {
                binding.lockSwitch.isChecked = false
            }
        }
    }

    override fun onUnlock() {
        stopService(Intent(this, ScreenLockerService::class.java))
        binding.lockSwitch.isChecked = false
    }
}