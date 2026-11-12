
package com.onetap.uploader

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object UploadScheduler {

    fun schedule(
        context: Context,
        id: String,
        title: String,
        videoUri: String,
        caption: String,
        platforms: String,
        delayMillis: Long
    ) {

        val input = Data.Builder()
            .putString("scheduleId", id)
            .putString("title", title)
            .putString("videoUri", videoUri)
            .putString("caption", caption)
            .putString("platforms", platforms)
            .build()

        val request =
            OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(input)
                .setInitialDelay(
                    delayMillis.coerceAtLeast(0L),
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "scheduled_upload_$id",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }
}
