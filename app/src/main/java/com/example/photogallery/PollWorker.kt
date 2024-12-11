package com.example.photogallery


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.photogallery.api.FlickrFetchr



private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return try {
            val query = QueryPreferences.getStoredQuery(context)
            val lastResultId = QueryPreferences.getLastResultId(context)

            // Выполнение сетевого запроса
            val items: List<GalleryItem> = if (query.isEmpty()) {
                FlickrFetchr().fetchPhotosRequest()
                    .execute()
                    .body()
                    ?.photos
                    ?.galleryItems ?: emptyList()
            } else {
                FlickrFetchr().searchPhotosRequest(query)
                    .execute()
                    .body()
                    ?.photos
                    ?.galleryItems ?: emptyList()
            }

            if (items.isEmpty()) {
                Log.i(TAG, "No new items found.")
                return Result.success()
            }

            val resultId = items.first().id
            if (resultId == lastResultId) {
                Log.i(TAG, "Got an old result: $resultId")
                return Result.success()
            } else {
                Log.i(TAG, "Got a new result: $resultId")
                QueryPreferences.setLastResultId(context, resultId)

                // Создание уведомления
                createNotification(resultId)
                return Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching photos", e)
            Result.failure() // Возврат ошибки в случае исключения
        }
    }

    @SuppressLint("MissingPermission")
    private fun createNotification(resultId: String) {
        val intent = PhotoGalleryActivity.newIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resources = context.resources
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setTicker(resources.getString(R.string.new_pictures_title))
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle(resources.getString(R.string.new_pictures_title))
            .setContentText(resources.getString(R.string.new_pictures_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(0, notification)
    }
}