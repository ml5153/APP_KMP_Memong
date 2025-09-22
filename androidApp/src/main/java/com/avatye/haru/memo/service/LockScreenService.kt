package com.avatye.haru.memo.service

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.data.utils.LockScreenNotificationUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_USE_LOCKSCREEN_MEMO
import com.avatye.haru.memo.receiver.LockScreenReceiver

@RequiresApi(Build.VERSION_CODES.O)
class LockScreenService : Service() {

    private var lockScreenReceiver: BroadcastReceiver? = null
    private val handler = Handler(Looper.getMainLooper())
    private val permissionCheckRunnable = object : Runnable {
        override fun run() {
            if (!hasAllRequiredPermissions()) {
                LogTrack.w("LockScreenService", { "권한이 사라져 서비스 종료" })
                PreferenceUtil.set(KEY_USE_LOCKSCREEN_MEMO, false)
                stopSelf()
            } else {
                handler.postDelayed(this, 60_000L) // 1분마다 다시 검사
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 리시버 등록
        lockScreenReceiver = LockScreenReceiver()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(lockScreenReceiver, filter)

        // 권한 주기 검사 시작
        handler.post(permissionCheckRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = LockScreenNotificationUtil.createNotification(this)
        startForeground(1001, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(permissionCheckRunnable)
        lockScreenReceiver?.let { unregisterReceiver(it) }
        LockScreenNotificationUtil.cancel(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun hasAllRequiredPermissions(): Boolean {
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasNotificationPermission && hasOverlayPermission && hasLocationPermission
    }
}