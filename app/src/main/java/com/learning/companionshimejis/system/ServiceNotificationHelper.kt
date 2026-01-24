package com.learning.companionshimejis.system

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import com.learning.companionshimejis.R
import com.learning.companionshimejis.service.MainService

/**
 * Helps in creating and starting the foreground service notification for main service.
 */
class ServiceNotificationHelper(private val context: Service) {

    companion object {
        const val CHANNEL_ID = "overlay_channel"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                            CHANNEL_ID,
                            "Overlay Service Channel",
                            NotificationManager.IMPORTANCE_HIGH
                    )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun startForeground() {
        val stopIntent =
                Intent(context, MainService::class.java).apply {
                    action = MainService.ACTION_STOP
                }

        val stopPendingIntent =
                PendingIntent.getService(
                        context,
                        0,
                        stopIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

        val notification =
                NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle("Floating Pets Active")
                        .setContentText("Your pets are floating around!")
                        .setSmallIcon(R.drawable.ic_pet_cat)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOngoing(true)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .addAction(
                                android.R.drawable.ic_menu_close_clear_cancel,
                                "Stop",
                                stopPendingIntent
                        )
                        .build()

        if (Build.VERSION.SDK_INT >= 34) {
            context.startForeground(
                    1,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            context.startForeground(1, notification)
        }
    }
}
