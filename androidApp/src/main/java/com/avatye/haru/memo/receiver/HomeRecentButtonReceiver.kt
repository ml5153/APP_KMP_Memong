package com.avatye.haru.memo.receiver

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.avatye.haru.log.LogTrack

internal class HomeRecentButtonReceiver(
    private val activity: Activity,
    private val onHomePressed: (() -> Unit)? = null,
    private val onRecentPressed: (() -> Unit)? = null
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            val reason = intent.getStringExtra("reason")
            LogTrack.d("LockScreen", { "Received CLOSE_SYSTEM_DIALOGS, reason: $reason" })
            when (reason) {
                "homekey" -> {
                    LogTrack.d("LockScreen", { "Home button pressed" })
                    onHomePressed?.invoke()
                }
                "recentapps" -> {
                    LogTrack.d("LockScreen", { "Recent button pressed" })
                    onRecentPressed?.invoke()
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun register() {
        unregister()
        val filter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(this, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            activity.registerReceiver(this, filter)
        }
    }

    fun unregister() {
        try {
            activity.unregisterReceiver(this)
        } catch (e: IllegalArgumentException) {
            // Already unregistered
        }
    }
}
