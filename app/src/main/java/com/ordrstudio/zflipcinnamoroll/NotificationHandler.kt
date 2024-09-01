package com.ordrstudio.zflipcinnamoroll

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

const val NOTIFICATION_TIME_CUTOFF = 10000
const val INTENT_EXTRA_ABOUT = "reminder_about"
const val ALARM__ID = 1250
const val NOTIFICATION_ID = 1256

enum class ReminderAbout {
    Starving,
    Boredom,
    Depressed,
    Exhausted,
}

@Parcelize
data class NotificationData (
    val text: String = "Tap to check on your Cinnamoroll",
) : Parcelable

class NotificationHandler(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val CHANNEL_ID = "CINNAMOROLL_CHANNEL"
    private val CHANNEL_NAME = "CINNAMOROLL"
    private val NOTIFICATION_TITLE = "Cinnamoroll"
    private val CHANNEL_DESCRIPTION = "A channel used to send occasional notifications about the status of your Cinnamoroll"

    private val notificationDataMap = mapOf(
        ReminderAbout.Starving to NotificationData("Your Cinnamoroll is starving. Tap to check on your Cinnamoroll"),
        ReminderAbout.Boredom to NotificationData("Your Cinnamoroll is very bored. Tap to check on your Cinnamoroll"),
        ReminderAbout.Depressed to NotificationData("Your Cinnamoroll really miss you. Tap to check on your Cinnamoroll"),
        ReminderAbout.Exhausted to NotificationData("Your Cinnamoroll need to go to sleep. Tap to check on your Cinnamoroll")
    )

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(state: CinnamorollState) {
        // We only want to notify the user if it's more than a certain amount of time
        val data = state.estimateTimeToNextNotification()
        if (data.notifyTime > NOTIFICATION_TIME_CUTOFF) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM__ID,
                Intent(context, NotificationReceiver::class.java).apply {
                    addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                    putExtra(INTENT_EXTRA_ABOUT, data.about)
                },
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + data.notifyTime, pendingIntent)
        }
    }

    fun sendNotification(about: ReminderAbout) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val data = notificationDataMap[about]!!
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(data.text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Set the intent that fires when the user taps the notification.
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun clearAlarm() {
        alarmManager.cancelAll()
    }
}

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationService = context?.let { NotificationHandler(it) }
        if (intent?.hasExtra(INTENT_EXTRA_ABOUT) == true) {
            notificationService?.sendNotification(intent.getSerializableExtra(
                INTENT_EXTRA_ABOUT, ReminderAbout::class.java)!!)
        }
    }
}