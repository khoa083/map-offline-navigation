package com.kblack.offlinemap.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_DOWNLOAD_ERROR_MESSAGE
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_DOWNLOAD_FILE_NAME
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_DOWNLOAD_MAP_DIR
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_DOWNLOAD_RATE
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_DOWNLOAD_RECEIVED_BYTES
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_DOWNLOAD_REMAINING_MS
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_NAME
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_START_UNZIPPING
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_TOTAL_BYTES
import com.kblack.offlinemap.presentation.ui.Constant.KEY_MAP_URL
import com.kblack.offlinemap.presentation.ui.Constant.TMP_FILE_EXT
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "map_download_channel_foreground"
private var channelCreated = false

class MapDownloadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationId: Int = params.id.hashCode()

    init {
        if (!channelCreated) {
            val channel = NotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                "Map Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Notifications for map download progress" }
            notificationManager.createNotificationChannel(channel)
            channelCreated = true
        }
    }

    override suspend fun doWork(): Result {

        val fileUrl = inputData.getString(KEY_MAP_URL)
        val mapName = inputData.getString(KEY_MAP_NAME) ?: "Map_VietNam"
        val fileName = inputData.getString(KEY_MAP_DOWNLOAD_FILE_NAME)
        val mapDir = inputData.getString(KEY_MAP_DOWNLOAD_MAP_DIR)!!
        val totalBytes = inputData.getLong(KEY_MAP_TOTAL_BYTES, 0L)

        return withContext(IO) {
            if (fileUrl == null || fileName == null) {
                Result.failure()
            } else {
                return@withContext try {

                    setForeground(createForegroundInfo(0, mapName))

                    val outputDir = File(
                        applicationContext.getExternalFilesDir(null),
                        mapDir
                    )
                    val outputTmpFile = File(
                        applicationContext.getExternalFilesDir(null),
                        listOf(mapDir, "${fileName}.$TMP_FILE_EXT").joinToString(File.separator)
                    )

                    val originalFilePath = outputTmpFile.absolutePath.replace(".$TMP_FILE_EXT", "")
                    val originalFile = File(originalFilePath)

                    if (originalFile.exists() && fileName.endsWith(".tar.zst")) {
                        setProgress(
                            Data.Builder().putBoolean(KEY_MAP_START_UNZIPPING, true).build()
                        )
                        setForeground(
                            createForegroundInfo(
                                progress = 100,
                                mapName = mapName,
                                isUnzipping = true
                            )
                        )
                        extractTarZst(srcFile = originalFile, destDir = outputDir)
                        patchStyleJson(context = applicationContext, destDir = outputDir)
                        return@withContext Result.success()
                    }

                    var downloadedBytes = 0L
                    val bytesReadSizeBuffer: MutableList<Long> = mutableListOf()
                    val bytesReadLatencyBuffer: MutableList<Long> = mutableListOf()

                    val url = URL(fileUrl)

                    val connection = url.openConnection() as HttpURLConnection

                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }

                    val outputFileBytes = outputTmpFile.length()

                    if (outputFileBytes > 0) {
                        connection.setRequestProperty("Range", "bytes=${outputFileBytes}-")
                        connection.setRequestProperty("Accept-Encoding", "identity")
                    }
                    connection.connect()

                    if (
                        connection.responseCode == HttpURLConnection.HTTP_OK ||
                        connection.responseCode == HttpURLConnection.HTTP_PARTIAL
                    ) {
                        val contentRange = connection.getHeaderField("Content-Range")

                        if (contentRange != null) {
                            // Parse the Content-Range header
                            val rangeParts = contentRange.substringAfter("bytes ").split("/")
                            val byteRange = rangeParts[0].split("-")
                            val startByte = byteRange[0].toLong()

                            downloadedBytes += startByte
                        }
                    } else {
                        throw IOException("HTTP error code: ${connection.responseCode}")
                    }

                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(outputTmpFile, true /* append */)

                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int
                    var lastSetProgressTs: Long = 0
                    var deltaBytes = 0L
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        deltaBytes += bytesRead

                        val curTs = System.currentTimeMillis()
                        if (curTs - lastSetProgressTs > 200) {

                            var bytesPerMs = 0f
                            if (lastSetProgressTs != 0L) {
                                if (bytesReadSizeBuffer.size == 5) {
                                    bytesReadSizeBuffer.removeAt(0)
                                }
                                bytesReadSizeBuffer.add(deltaBytes)
                                if (bytesReadLatencyBuffer.size == 5) {
                                    bytesReadLatencyBuffer.removeAt(0)
                                }
                                bytesReadLatencyBuffer.add(curTs - lastSetProgressTs)
                                deltaBytes = 0L
                                bytesPerMs = bytesReadSizeBuffer.sum()
                                    .toFloat() / bytesReadLatencyBuffer.sum()
                            }

                            // Calculate remaining seconds
                            var remainingMs = 0f
                            if (bytesPerMs > 0f && totalBytes > 0L) {
                                remainingMs = (totalBytes - downloadedBytes) / bytesPerMs
                            }

                            setProgress(
                                Data.Builder()
                                    .putLong(KEY_MAP_DOWNLOAD_RECEIVED_BYTES, downloadedBytes)
                                    .putLong(KEY_MAP_DOWNLOAD_RATE, (bytesPerMs * 1000).toLong())
                                    .putLong(KEY_MAP_DOWNLOAD_REMAINING_MS, remainingMs.toLong())
                                    .build()
                            )
                            setForeground(
                                createForegroundInfo(
                                    progress = (downloadedBytes * 100 / totalBytes).toInt(),
                                    mapName = mapName,
                                )
                            )
                            lastSetProgressTs = curTs
                        }
                    }

                    outputStream.close()
                    inputStream.close()


                    if (originalFile.exists()) {
                        originalFile.delete()
                    }
                    outputTmpFile.renameTo(originalFile)

                    if (fileName.endsWith(".tar.zst")) {
                        setProgress(
                            Data.Builder().putBoolean(KEY_MAP_START_UNZIPPING, true).build()
                        )
                        setForeground(
                            createForegroundInfo(
                                progress = 100,
                                mapName = mapName,
                                isUnzipping = true
                            )
                        )
                        extractTarZst(srcFile = originalFile, destDir = outputDir)
                        patchStyleJson(context = applicationContext, destDir = outputDir)
                    }

                    Result.success()
                } catch (e: IOException) {
                    Result.failure(
                        Data.Builder().putString(KEY_MAP_DOWNLOAD_ERROR_MESSAGE, e.message).build()
                    )
                }
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(0)
    }

    private fun createForegroundInfo(
        progress: Int,
        mapName: String? = null,
        isUnzipping: Boolean = false
    ): ForegroundInfo {
        var title = "Downloading map"
        if (mapName != null) {
            title = "Downloading \"$mapName\""
        }
        val content = if (isUnzipping) "Unzipping..." else "Downloading in progress: $progress%"

        val intent =
            Intent(
                applicationContext,
                Class.forName("com.kblack.offlinemap.presentation.MainActivity")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val notification =
            NotificationCompat.Builder(
                applicationContext, FOREGROUND_NOTIFICATION_CHANNEL_ID
            )
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true) // Makes the notification non-dismissable
                .setProgress(100, progress, isUnzipping)
                .setContentIntent(pendingIntent)
                .build()

        return ForegroundInfo(
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    private fun extractTarZst(srcFile: File, destDir: File) {
        if (!destDir.exists()) destDir.mkdirs()

        ZstdCompressorInputStream(
            BufferedInputStream(FileInputStream(srcFile))
        ).use { zstdIn ->
            TarArchiveInputStream(zstdIn).use { tarIn ->
                var entry = tarIn.nextEntry
                while (entry != null) {
                    val outFile = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            tarIn.copyTo(out)
                        }
                    }
                    entry = tarIn.nextEntry
                }
            }
        }

        srcFile.delete()
    }

    private fun patchStyleJson(context: Context, destDir: File) {
        val pmtilesFile = destDir.listFiles { file ->
            file.extension == "pmtiles"
        }?.firstOrNull()
        if (pmtilesFile == null) return

        val pmtilesPath = pmtilesFile.absolutePath
        val fontDirPath = File(destDir, "font").absolutePath

        val styleJson = context.assets
            .open("style.json")
            .bufferedReader()
            .readText()
            .replace("MAP_PATH_PLACEHOLDER", "file://$pmtilesPath")
            .replace("FONT_PATH_PLACEHOLDER", "file://$fontDirPath/{fontstack}/{range}.pbf")

        File(destDir, "style_runtime.json").writeText(styleJson)
    }


}