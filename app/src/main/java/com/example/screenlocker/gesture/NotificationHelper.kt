package com.timurvg.screenlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "locker_channel"

    fun createNotification(context: Context): Notification {
        createChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Screen Locker")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                "Screen Locker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Screen lock service"
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(this)
            }
        }
    }
}