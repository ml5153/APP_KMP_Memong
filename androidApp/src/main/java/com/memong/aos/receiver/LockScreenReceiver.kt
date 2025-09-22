package com.memong.aos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_USE_LOCKSCREEN_MEMO
import com.memong.aos.ui.LockScreenActivity

internal class LockScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_SCREEN_ON == intent?.action) {
            if (PreferenceUtil.get(KEY_USE_LOCKSCREEN_MEMO, false)) {
                val i = Intent(context, LockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(i)
            }
        }
    }
}