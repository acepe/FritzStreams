package de.acepe.fritzstreams.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import de.acepe.fritzstreams.MainActivity
import de.acepe.fritzstreams.R
import java.util.*

class Notifications {
    companion object {

        private val ONGOING_NOTIFICATION_ID = getRandomNumber()
        private const val SMALL_ICON = R.mipmap.ic_launcher
        private const val CHANNEL_ID = 1001

        private fun getRandomNumber(): Int {
            return Random().nextInt(100000)
        }

        fun createNotification(context: Context, progressPercent: Int): Notification {
            createNotificationChannel(context)
            // Create Pending Intents.
            val piLaunchMainActivity = getLaunchActivityPI(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID.toString())
                    .setContentTitle(context.getString(R.string.notification_text_title))
                    .setContentText(getContentText(progressPercent, context))
                    .setSmallIcon(SMALL_ICON)
                    .setContentIntent(piLaunchMainActivity)
                    .setStyle(NotificationCompat.BigTextStyle())
            if (progressPercent < 100) {
                builder.setProgress(100, progressPercent, false)
            }
            return builder.build()
        }

        private fun getContentText(progressPercent: Int, context: Context) =
                if (progressPercent == 100) context.getString(R.string.notification_text_done) else context.getString(R.string.notification_text_downloading)

        fun cancelNotification(context: Context) {
            NotificationManagerCompat.from(context).cancel(ONGOING_NOTIFICATION_ID)
        }

        private fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "channel name"
                val description = "channel descr"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID.toString(), name, importance)
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }

        private fun getLaunchActivityPI(context: Context): PendingIntent {
            val launchMainActivity = Intent(context, MainActivity::class.java)
            launchMainActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return PendingIntent.getActivity(context, getRandomNumber(), launchMainActivity, 0)
        }
    }
}
