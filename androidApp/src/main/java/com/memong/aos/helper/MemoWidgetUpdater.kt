package com.memong.aos.helper

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.ui.widget.MemoWidgetMedium
import com.avatye.haru.log.LogTrack
import com.memong.aos.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class MemoWidgetUpdater {
    fun observeMemoChanges(context: Context) {
        val db = MemoDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            db.memoDao().observeAllMemos().collect {
                LogTrack.d("MemoObserver", { "Memo changed, refreshing widget" })
                val manager = AppWidgetManager.getInstance(context)
                val component = ComponentName(context, MemoWidgetMedium::class.java)
                val ids = manager.getAppWidgetIds(component) // 먼저 선언해야 함
                manager.notifyAppWidgetViewDataChanged(
                    manager.getAppWidgetIds(component),
                    R.id.widgetMemoList
                )

                // 🔁 강제 업데이트 호출
                val intent = Intent(context, MemoWidgetMedium::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}