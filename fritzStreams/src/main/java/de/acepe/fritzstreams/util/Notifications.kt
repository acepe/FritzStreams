package de.acepe.fritzstreams.util

import android.app.Notification
import android.app.Notification.VISIBILITY_PUBLIC
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import de.acepe.fritzstreams.MainActivity
import de.acepe.fritzstreams.R
import de.acepe.fritzstreams.backend.DownloadInfo
import de.acepe.fritzstreams.backend.DownloadState
import java.util.Random

class Notifications {
    companion object {

        private val ONGOING_NOTIFICATION_ID = getRandomNumber()
        private const val SMALL_ICON = R.mipmap.ic_launcher
        private const val CHANNEL_ID = "FritzStreamsChannel"

        private fun getRandomNumber(): Int {
            return Random().nextInt(100000)
        }

        fun createNotification(context: Context, downloadInfo: DownloadInfo): Notification {
            setupNotificationChannel(context)
            // Create Pending Intents.

            val progressPercent = downloadInfo.progressPercent
            val showTaskIntent = Intent(context, MainActivity::class.java)
            showTaskIntent.action = Intent.ACTION_MAIN
            showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                showTaskIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_text_title))
                .setContentText(contentText(downloadInfo, context))
                .setContentIntent(contentIntent)
                .setVisibility(VISIBILITY_PUBLIC)
                .setSmallIcon(SMALL_ICON)
                .setStyle(NotificationCompat.BigTextStyle())
                .setAutoCancel(false)


            if (progressPercent < 100 && downloadInfo.state == DownloadState.DOWNLOADING) {
                builder.setProgress(100, progressPercent, false)
            }
            return builder.build()
        }

        private fun setupNotificationChannel(context: Context) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_ID,
                    NotificationManager.IMPORTANCE_LOW
                )
            channel.description = context.getString(R.string.NotificationChannelDescription)
            channel.enableLights(false)
            channel.enableVibration(false)
            val notificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        private fun contentText(downloadInfo: DownloadInfo, context: Context): String? {
            return when {
                downloadInfo.state == DownloadState.CANCELLED -> context.getString(R.string.notification_text_cancelled)
                downloadInfo.state == DownloadState.FAILED -> context.getString(R.string.notification_text_failed)
                downloadInfo.progressPercent == 100 -> context.getString(R.string.notification_text_done)
                else -> context.getString(R.string.notification_text_downloading)
            }
        }

        fun cancelNotification(context: Context) {
            NotificationManagerCompat.from(context).cancel(ONGOING_NOTIFICATION_ID)
        }

    }
}
