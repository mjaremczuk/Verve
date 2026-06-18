package com.mjaremczuk.motiv.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mjaremczuk.motiv.MainActivity
import com.mjaremczuk.motiv.domain.usecase.GetQuoteOfTheDayUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val getQuoteOfTheDayUseCase: GetQuoteOfTheDayUseCase by inject()

    override suspend fun doWork(): Result {
        val (quote, _) = getQuoteOfTheDayUseCase()

        if (quote != null) {
            showNotification(quote.text, quote.author)
        }

        // Reschedule for the next day's set time
        scheduleNextDailyNotification(applicationContext)

        return Result.success()
    }

    private fun showNotification(text: String, author: String) {
        val channelId = "verve_daily_motivation"
        val notificationId = 1001

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Motivation"
            val descriptionText = "Daily motivational quotes to start your day"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val appIcon = com.mjaremczuk.motiv.R.mipmap.ic_launcher

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(appIcon)
            .setContentTitle("Daily Motivation")
            .setContentText("\"$text\" — $author")
            .setStyle(NotificationCompat.BigTextStyle().bigText("\"$text\"\n\n— $author"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val WORK_NAME = "verve_daily_notification"

        fun scheduleNextDailyNotification(context: Context) {
            val sharedPreferences = context.getSharedPreferences("verve_prefs", Context.MODE_PRIVATE)
            val isEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
            if (!isEnabled) return

            val hour = sharedPreferences.getInt("notification_hour", 8)
            val minute = sharedPreferences.getInt("notification_minute", 0)

            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If the scheduled time has already passed today, set it for tomorrow
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.DAY_OF_YEAR, 1)
            }

            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

            val dailyWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag("verve_daily_notification_tag")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                dailyWorkRequest
            )
        }

        fun cancelDailyNotification(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
