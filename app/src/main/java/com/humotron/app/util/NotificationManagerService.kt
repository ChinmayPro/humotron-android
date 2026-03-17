package com.humotron.app.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.humotron.app.R
import com.humotron.app.ui.MainActivity
import org.jspecify.annotations.NonNull

object NotificationManagerService {

    private const val CHANNEL_ID = "humotron_service"

    fun createNotification(
        context: Context,
    ): @NonNull Notification {

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.resources.getString(R.string.app_name))
            .setContentText(context.resources.getString(R.string.connecting_to_device))
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    MainActivity.newIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOngoing(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Una Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
            notificationBuilder.setChannelId(CHANNEL_ID)
        }
        return notificationBuilder.build()
    }
}