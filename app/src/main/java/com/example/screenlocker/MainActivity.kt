package com.timurvg.screenlocker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.timurvg.screenlocker.databinding.ActivityMainBinding
import com.timurvg.screenlocker.service.LockService
import com.timurvg.screenlocker.utils.PermissionHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (PermissionHelper.checkOverlayPermission(this)) {
            startService(Intent(this, LockService::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionHelper.OVERLAY_PERMISSION_CODE &&
            PermissionHelper.checkOverlayPermission(this)) {
            startService(Intent(this, LockService::class.java))
        }
    }
}