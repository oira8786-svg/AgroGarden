package com.agrogarden.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.agrogarden.R
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Аграрная задача"
        NotificationManagerCompat.from(applicationContext).notify(
            System.currentTimeMillis().toInt(),
            NotificationCompat.Builder(applicationContext, "agro")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("AgroGarden")
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
        )

        val repeatDays = inputData.getInt("repeatDays", 0)
        val dateTime = inputData.getString("dateTime")
        val uniqueName = inputData.getString("uniqueName")
        if (repeatDays > 0 && !dateTime.isNullOrBlank() && !uniqueName.isNullOrBlank()) {
            return try {
                val next = LocalDateTime.parse(dateTime).plusDays(repeatDays.toLong())
                val delay = Duration.between(LocalDateTime.now(), next).toMillis().coerceAtLeast(0)
                val data = Data.Builder()
                    .putString("title", title)
                    .putInt("repeatDays", repeatDays)
                    .putString("dateTime", next.toString())
                    .putString("uniqueName", uniqueName)
                    .build()
                val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(applicationContext)
                    .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
                Result.success()
            } catch (_: Exception) {
                Result.failure()
            }
        }
        return Result.success()
    }
}
