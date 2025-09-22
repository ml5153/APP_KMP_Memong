package com.avatye.haru.memo.data.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.avatye.haru.memo.R
import com.avatye.haru.log.LogTrack

object AutoBackupNotificationUtil {

    private const val CHANNEL_ID = "auto_backup_channel"
    private const val NOTIFICATION_ID = 2025

    fun showNotification(context: Context, backupTime: String, backupState: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                LogTrack.w("AutoBackupNotification", { "알림 권한 없음 → 생략" })
                return
            }
        }
        createNotificationChannel(context)

        val isDarkMode = when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
        val failureColor = ContextCompat.getColor(context, R.color.haru_red)

        val remoteViews = RemoteViews(context.packageName, R.layout.notification_auto_backup).apply {
            if (backupState) {
                setTextViewText(R.id.textAutoBackupComplete, "자동 백업 완료")
                setTextViewText(R.id.autoBackupTime, backupTime)
                setTextColor(R.id.textAutoBackupComplete, textColor)
                setTextColor(R.id.autoBackupTime, textColor)
            } else {
                setTextViewText(R.id.textAutoBackupComplete, "자동 백업 실패")
                setTextViewText(R.id.autoBackupTime, backupTime)  // 실패 원인 전달됨
                setTextColor(R.id.textAutoBackupComplete, failureColor)
                setTextColor(R.id.autoBackupTime, textColor)
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.noti_memong_small_icon)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "자동 백업 알림"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }
}
