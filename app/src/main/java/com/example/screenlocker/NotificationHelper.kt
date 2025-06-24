package com.timurg.screenlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    fun createNotification(context: Context): Notification {
        val channelId = "locker_channel"

        // Создаем канал для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                channelId,
                "Screen Locker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                description = "Фоновый сервис блокировки"
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(this)
            }
        }

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Экран заблокирован")
            .setContentText("Двойной тап для разблокировки")
            .setSmallIcon(R.drawable.ic_notification) // Иконка обязательна!
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
}