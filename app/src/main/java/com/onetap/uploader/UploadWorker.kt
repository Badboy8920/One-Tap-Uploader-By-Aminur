
package com.onetap.uploader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data

class UploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val title = inputData.getString("title")
        val videoUri = inputData.getString("videoUri")
        val caption = inputData.getString("caption")
        val platforms = inputData.getString("platforms")

        if (title.isNullOrBlank() ||
            videoUri.isNullOrBlank() ||
            platforms.isNullOrBlank()
        ) {
            return Result.failure(
                Data.Builder()
                    .putString("message", "Required upload details are missing")
                    .build()
            )
        }

        // Actual platform upload APIs are not connected yet.
        return Result.failure(
            Data.Builder()
                .putString(
                    "message",
                    "Upload API is not configured yet. Connect platform accounts first."
                )
                .build()
        )
    }
}
