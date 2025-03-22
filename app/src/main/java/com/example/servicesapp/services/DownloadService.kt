package com.example.servicesapp.services

import android.app.*
import android.content.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.*
import java.io.IOException

class DownloadService : Service() {

    private lateinit var serviceLooper: Looper
    private lateinit var serviceHandler: ServiceHandler

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("DownloadServiceThread", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper)

        // Create the notification channel (for Android 8.0+)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val urls = intent?.getStringArrayListExtra("PDF_URLS") ?: emptyList()

        urls.forEach { url ->
            serviceHandler.post {
                downloadFile(url)
            }
        }

        return START_NOT_STICKY
    }

    private fun downloadFile(url: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveFileUsingMediaStore(url)
        } else {
            saveFileUsingDownloadManager(url)
        }
    }

    private fun saveFileUsingMediaStore(url: String) {
        val fileName = sanitizeFileName(url.substringAfterLast("/"))
        Log.d("DownloadService", "Downloading file: $fileName from URL: $url")

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val contentResolver = contentResolver
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        }

        uri?.let {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Log.e("DownloadService", "Failed to download file: ${response.message}")
                            Log.e("DownloadService", "Response code: ${response.code}")
                            Log.e("DownloadService", "Response body: ${response.body?.string()}")
                            showNotification("Download Failed", "Failed to download file: ${response.message}")
                            return@use
                        }

                        response.body?.byteStream()?.copyTo(outputStream)
                        Log.d("DownloadService", "File saved successfully: $fileName")
                    }
                }

                showNotification("Download Complete", "File saved to Downloads folder")
            } catch (e: IOException) {
                Log.e("DownloadService", "IOException: ${e.message}")
                e.printStackTrace()
                showNotification("Download Failed", "Could not download file")
            } catch (e: Exception) {
                Log.e("DownloadService", "Exception: ${e.message}")
                e.printStackTrace()
                showNotification("Download Failed", "Could not download file")
            }
        } ?: run {
            Log.e("DownloadService", "Failed to create URI for file: $fileName")
            showNotification("Download Failed", "Could not create file")
        }
    }

    private fun saveFileUsingDownloadManager(url: String) {
        val fileName = sanitizeFileName(url.substringAfterLast("/"))
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading PDF")
            .setDescription("Downloading PDF file from $url")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)

        showNotification("Download Started", "Check notifications for progress")
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "DownloadChannel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "DownloadChannel",
                "File Downloads",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for file downloads"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private class ServiceHandler(looper: Looper) : Handler(looper)
}