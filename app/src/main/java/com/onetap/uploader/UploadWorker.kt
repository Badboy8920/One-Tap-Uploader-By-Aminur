
package com.onetap.uploader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.json.JSONArray

class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val title = inputData.getString("title") ?: "Your video"
        val scheduleId = inputData.getString("scheduleId") ?: ""

        val message =
            "Upload API is not connected. Your video was not published."

        updateScheduleStatus(
            scheduleId,
            "Upload API Not Connected"
        )

        showNotification(title, message, scheduleId)

        return Result.failure()
    }

    private fun updateScheduleStatus(
        scheduleId: String,
        status: String
    ) {
        if (scheduleId.isBlank()) return

        val prefs = applicationContext.getSharedPreferences(
            "OneTapUploaderPrefs",
            Context.MODE_PRIVATE
        )

        try {
            val saved = prefs.getString("scheduled_videos", null)
                ?: return

            val array = JSONArray(saved)

            for (i in 0 until array.length()) {
                val item = JSONArray(array.getString(i))

                if (item.optString(6) == scheduleId) {
                    while (item.length() < 8) {
                        item.put("")
                    }

                    item.put(7, status)
                    array.put(i, item.toString())
                    break
                }
            }

            prefs.edit()
                .putString("scheduled_videos", array.toString())
                .apply()

        } catch (_: Exception) {
        }
    }

    private fun showNotification(
        title: String,
        message: String,
        scheduleId: String
    ) {
        val manager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val channelId = "onetap_upload_status"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Upload Status",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            manager.createNotificationChannel(channel)
        }

        val notification = Notification.Builder(
            applicationContext,
            channelId
        )
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("OneTap Uploader")
            .setContentText("$title: $message")
            .setStyle(
                Notification.BigTextStyle().bigText(message)
            )
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(
                scheduleId.hashCode(),
                notification
            )
        } catch (_: SecurityException) {
        }
    }
}
