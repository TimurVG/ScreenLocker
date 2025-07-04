package com.timurvg.screenlocker.utils  // Замените на ваш пакет!

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

object PermissionHelper {
    const val OVERLAY_PERMISSION_CODE = 1001

    fun checkOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(context)) {
            requestOverlayPermission(context)
            false
        } else {
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission(context: Context) {
        context.startActivity(Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ))
    }
}