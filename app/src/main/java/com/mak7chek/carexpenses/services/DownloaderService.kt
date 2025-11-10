package com.mak7chek.carexpenses.data // (або твій шлях)

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloaderService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val downloadManager = context.getSystemService<DownloadManager>()

    fun downloadTripsReport(token: String, filterQuery: String) {

        val baseUrl = "https://car-expenses-api.onrender.com"
        val url = "$baseUrl/api/trips/export$filterQuery"

        val filename = "trips_export_${LocalDate.now()}.csv"

        val request = DownloadManager.Request(android.net.Uri.parse(url))
            .setTitle(filename)
            .setDescription("Завантаження звіту про поїздки...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .setMimeType("text/csv")

            .addRequestHeader("Authorization", "Bearer $token")

        downloadManager?.enqueue(request)
    }
}