package com.kblack.offlinemap.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kblack.offlinemap.data.worker.MapDownloadWorker
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.repository.AppLifecycleProvider
import com.kblack.offlinemap.domain.repository.MapDownloadRepository
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_DOWNLOAD_ERROR_MESSAGE
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_DOWNLOAD_FILE_NAME
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_DOWNLOAD_MAP_DIR
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_DOWNLOAD_RATE
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_DOWNLOAD_RECEIVED_BYTES
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_DOWNLOAD_REMAINING_MS
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_NAME
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_START_UNZIPPING
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_TOTAL_BYTES
import com.kblack.offlinemap.data.utils.Constant.KEY_MAP_URL
import com.kblack.offlinemap.data.utils.Constant.MAP_NAME_TAG
import com.kblack.offlinemap.data.utils.Constant.TMP_FILE_EXT
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class MapDownloadRepositoryImpl(
    private val context: Context,
    private val lifecycleProvider: AppLifecycleProvider,
    private val workManager: WorkManager,
) : MapDownloadRepository {

    private val downloadStartTimeSharedPreferences =
        context.getSharedPreferences("download_start_time_ms", Context.MODE_PRIVATE)

    private val externalFilesDir = context.getExternalFilesDir(null)

    override fun downloadMap(
        map: MapModel,
        onStatusUpdated: (map: MapModel, status: MapDownloadStatus) -> Unit
    ) {

        val builder = Data.Builder()
        val totalBytes = map.totalBytes
        val inputDataBuilder =
            builder
                .putString(KEY_MAP_NAME, map.name)
                .putString(KEY_MAP_URL, map.url)
                .putString(KEY_MAP_DOWNLOAD_MAP_DIR, map.normalizedName)
                .putString(KEY_MAP_DOWNLOAD_FILE_NAME, map.downloadFileName)
                .putLong(KEY_MAP_TOTAL_BYTES, totalBytes)

        val inputData = inputDataBuilder.build()

        val downloadWorkRequest =
            OneTimeWorkRequestBuilder<MapDownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(inputData)
                .addTag("$MAP_NAME_TAG:${map.name}")
                .build()

        val workerId = downloadWorkRequest.id

        workManager.enqueueUniqueWork(map.name, ExistingWorkPolicy.REPLACE, downloadWorkRequest)

        observerWorkerProgress(
            workerId = workerId,
            map = map,
            onStatusUpdated = onStatusUpdated,
        )
    }

    override fun cancelDownloadMap(map: MapModel) {
        workManager.cancelAllWorkByTag("$MAP_NAME_TAG:${map.name}")
    }

    override fun cancelAll(onComplete: () -> Unit) {
        workManager
            .cancelAllWork()
            .result
            .addListener({ onComplete() }, Executors.newSingleThreadExecutor())
    }

    override fun getLocalMapStatus(map: MapModel): MapDownloadStatus {
        var status = MapDownloadStatusType.NOT_DOWNLOADED
        var receivedBytes = 0L
        var totalBytes = 0L

        val tarZstFile = File(externalFilesDir, "${map.normalizedName}/${map.downloadFileName}")

        if (isMapPartiallyDownloaded(map)) {
            status = MapDownloadStatusType.PARTIALLY_DOWNLOADED
            val tmpFile = File(
                externalFilesDir,
                "${map.normalizedName}/${map.downloadFileName}.$TMP_FILE_EXT"
            )
            receivedBytes = tmpFile.length()
            totalBytes = map.totalBytes
        } else if (tarZstFile.exists() && map.downloadFileName.endsWith(".tar.zst")) {
            status = MapDownloadStatusType.PARTIALLY_DOWNLOADED
            receivedBytes = map.totalBytes
            totalBytes = map.totalBytes
        } else if (isMapDownloaded(map)) {
            status = MapDownloadStatusType.SUCCEEDED
        }

        return MapDownloadStatus(
            status = status,
            receivedBytes = receivedBytes,
            totalBytes = totalBytes,
        )
    }

    override fun deleteMap(map: MapModel) {
        val mapDir = File(externalFilesDir, map.normalizedName)
        if (mapDir.exists()) {
            mapDir.deleteRecursively()
        }
    }

    private fun observerWorkerProgress(
        workerId: UUID,
        map: MapModel,
        onStatusUpdated: (map: MapModel, status: MapDownloadStatus) -> Unit
    ) {
        workManager.getWorkInfoByIdLiveData(workerId).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        downloadStartTimeSharedPreferences.edit {
                            putLong(map.name, System.currentTimeMillis())
                        }
                    }

                    WorkInfo.State.RUNNING -> {
                        val receivedBytes =
                            workInfo.progress.getLong(KEY_MAP_DOWNLOAD_RECEIVED_BYTES, 0L)
                        val downloadRate = workInfo.progress.getLong(KEY_MAP_DOWNLOAD_RATE, 0L)
                        val remainingSeconds =
                            workInfo.progress.getLong(KEY_MAP_DOWNLOAD_REMAINING_MS, 0L)
                        val startUnzipping =
                            workInfo.progress.getBoolean(KEY_MAP_START_UNZIPPING, false)

                        if (!startUnzipping) {
                            if (receivedBytes != 0L) {
                                onStatusUpdated(
                                    map,
                                    MapDownloadStatus(
                                        status = MapDownloadStatusType.IN_PROGRESS,
                                        totalBytes = map.totalBytes,
                                        receivedBytes = receivedBytes,
                                        bytesPerSecond = downloadRate,
                                        remainingMs = remainingSeconds,
                                    ),
                                )
                            }
                        } else {
                            onStatusUpdated(
                                map,
                                MapDownloadStatus(status = MapDownloadStatusType.UNZIPPING),
                            )
                        }
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        onStatusUpdated(
                            map,
                            MapDownloadStatus(status = MapDownloadStatusType.SUCCEEDED)
                        )
                        sendNotification(
                            title = "Map Downloaded",
                            text = "",
                            mapName = map.name,
                        )

                        downloadStartTimeSharedPreferences.edit { remove(map.name) }
                    }

                    WorkInfo.State.FAILED,
                    WorkInfo.State.CANCELLED -> {
                        var status = MapDownloadStatusType.FAILED
                        val errorMessage =
                            workInfo.outputData.getString(KEY_MAP_DOWNLOAD_ERROR_MESSAGE) ?: ""

                        if (workInfo.state == WorkInfo.State.CANCELLED) {
                            status = MapDownloadStatusType.NOT_DOWNLOADED
                        } else {
                            sendNotification(
                                title = "Map Download Failed",
                                text = "",
                                mapName = "",
                            )
                        }
                        onStatusUpdated(
                            map,
                            MapDownloadStatus(status = status, errorMessage = errorMessage),
                        )

                        downloadStartTimeSharedPreferences.edit { remove(map.name) }
                    }

                    else -> {
                        null
                    }
                }
            }
        }
    }

    //todo: FIXME - hardcode
    override fun getStyleJsonPath(map: MapModel): String? {
        val file = File(externalFilesDir, "${map.normalizedName}/style_runtime.json")
        return if (file.exists()) file.absolutePath else null
    }

    override fun getGraphPath(map: MapModel): String? {
        val file = File(externalFilesDir, "${map.normalizedName}/graph-cache")
        return if (file.exists()) file.absolutePath else null
    }

    private fun sendNotification(title: String, text: String, mapName: String) {
        if (lifecycleProvider.isAppInForeground) {
            return
        }

        val channelId = "download_notification"
        val channelName = "Download notification"

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, Class.forName("com.kblack.offlinemap.presentation.MainActivity"))

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, builder.build())
        }
    }

    private fun isMapDownloaded(map: MapModel): Boolean {
        val pmtilesFile = File(externalFilesDir, "${map.normalizedName}/${map.pmtilesName}")
        return pmtilesFile.exists()
    }

    private fun isMapPartiallyDownloaded(map: MapModel): Boolean {
        val tmpFile = File(
            externalFilesDir,
            "${map.normalizedName}/${map.downloadFileName}.$TMP_FILE_EXT"
        )
        return tmpFile.exists()
    }

}