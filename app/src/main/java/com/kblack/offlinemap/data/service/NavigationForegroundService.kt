package com.kblack.offlinemap.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.kblack.offlinemap.R
import com.kblack.offlinemap.ui.MainActivity

class NavigationForegroundService : Service() {

    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY

        when (intent.action) {
            ACTION_START -> {
                createNotificationChannel()
                val notification = buildNotification(
                    instruction = intent.getStringExtra(EXTRA_INSTRUCTION) ?: "",
                    rotation    = intent.getFloatExtra(EXTRA_ROTATION, 0f),
                    progress    = intent.getIntExtra(EXTRA_PROGRESS, 0)
                )
                startForeground(NOTIFICATION_ID, notification)
            }

            ACTION_UPDATE -> {
                val notification = buildNotification(
                    instruction = intent.getStringExtra(EXTRA_INSTRUCTION) ?: "",
                    rotation    = intent.getFloatExtra(EXTRA_ROTATION, 0f),
                    progress    = intent.getIntExtra(EXTRA_PROGRESS, 0)
                )
                notificationManager.notify(NOTIFICATION_ID, notification)
            }

            ACTION_STOP -> {
                notificationManager.cancel(NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
                NavigationServiceHelper.notifyStop()
                stopSelf()
            }

            ACTION_HIDE -> {
                notificationManager.cancel(NOTIFICATION_ID)
            }

            ACTION_SHOW -> {
                val notification = buildNotification(
                    instruction = intent.getStringExtra(EXTRA_INSTRUCTION) ?: "",
                    rotation    = intent.getFloatExtra(EXTRA_ROTATION, 0f),
                    progress    = intent.getIntExtra(EXTRA_PROGRESS, 0)
                )
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
        return START_STICKY
    }

    private fun buildNotification(
        instruction: String,
        rotation: Float,
        progress: Int
    ): Notification {

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val exitIntent = PendingIntent.getService(
            this, 1,
            Intent(this, NavigationForegroundService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(packageName, R.layout.noti_navigation).apply {
            setTextViewText(R.id.tv_instruction, instruction)
            setFloat(R.id.iv_maneuver, "setRotation", rotation)
            setInt(R.id.iv_maneuver, "setColorFilter", Color.WHITE)
            setProgressBar(R.id.progress_distance, 100, progress, false)
//            setOnClickPendingIntent(R.id.btn_exit_navigation, exitIntent)
        }

        val exitAction = NotificationCompat.Action.Builder(
            IconCompat.createWithResource(this, R.drawable.d_off),
            "Exit navigation",
            exitIntent
        ).build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.d_on)
            .setContentTitle(instruction)
            .setContentText("Navigation in progress")
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .addAction(exitAction)
            .setContentIntent(openAppIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Navigation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID        = "navigation_channel"
        const val NOTIFICATION_ID   = 1001
        const val ACTION_START      = "NAV_START"
        const val ACTION_UPDATE     = "NAV_UPDATE"
        const val ACTION_STOP       = "NAV_STOP"
        const val ACTION_HIDE       = "NAV_HIDE"
        const val ACTION_SHOW       = "NAV_SHOW"
        const val EXTRA_INSTRUCTION = "extra_instruction"
        const val EXTRA_ROTATION    = "extra_rotation"
        const val EXTRA_PROGRESS    = "extra_progress"
    }
}