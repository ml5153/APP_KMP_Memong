package com.avatye.haru.memo.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.database.MemoDatabase
import com.avatye.haru.memo.data.enum.MemoMode
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.service.MemoListWidgetService
import com.avatye.haru.memo.ui.MemoDetailActivity
import com.avatye.haru.memo.ui.MemoListActivity
import com.avatye.haru.memo.ui.PasswordActivity
import com.avatye.haru.memo.ui.SettingBackupActivity
import kotlinx.coroutines.runBlocking

internal class MemoWidgetMedium : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_memo_medium)

            // RemoteViewsService 연결
            val serviceIntent = Intent(context, MemoListWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                data = "memog2://widget/$id".toUri() // 고유한 URI
            }
            views.setRemoteAdapter(R.id.widgetMemoList, serviceIntent)

            views.setEmptyView(R.id.widgetMemoList, R.id.widgetTextEmpty)

            // 로고 클릭
            views.setOnClickPendingIntent(
                R.id.widgetImgLogo,
                createBroadcastIntent(context, ACTION_OPEN_MAIN, id + 2000, id)
            )

            // 새 메모 작성
            views.setOnClickPendingIntent(
                R.id.widgetBtnWrite,
                createBroadcastIntent(context, ACTION_WRITE_MEMO, id, id)
            )

            // 백업 설정 화면
            val backupIntent = Intent(context, SettingBackupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val backupPendingIntent = PendingIntent.getActivity(
                context, id + 1000, backupIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetBtnBackup, backupPendingIntent)

            val clickIntentTemplate = Intent(context, MemoWidgetMedium::class.java).apply {
                action = ACTION_ITEM_CLICK
            }
            val clickPendingIntentTemplate = PendingIntent.getBroadcast(
                context, 0, clickIntentTemplate,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widgetMemoList, clickPendingIntentTemplate)

            // 위젯 업데이트 및 강제 새로고침
            manager.updateAppWidget(id, views)
            manager.notifyAppWidgetViewDataChanged(id, R.id.widgetMemoList)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        // 위젯이 처음 추가될 때만 이벤트 전송
        EventUtil.sendEvent(
            context,
            EventUtil.CATEGORY_WIDGET,
            EventUtil.ACTION_MEDIUM_WIDGET
        )
    }

    private fun createBroadcastIntent(
        context: Context,
        action: String,
        requestCode: Int,
        widgetId: Int
    ): PendingIntent {
        val intent = Intent(context, MemoWidgetMedium::class.java).apply {
            this.action = action
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            component = ComponentName(context, MemoWidgetMedium::class.java)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action

        when (action) {
            ACTION_WRITE_MEMO -> {
                MemoDetailActivity.startWidget(context, MemoMode.CREATE_MEMO, memoItem = null)
            }

            ACTION_OPEN_MAIN -> {
                MemoListActivity.startWidget(context)
            }

            ACTION_ITEM_CLICK -> {
                val uuid = intent.getStringExtra(EXTRA_MEMO_UUID)
                val isLocked = intent.getBooleanExtra(EXTRA_MEMO_IS_LOCKED, false)

                if (uuid != null) {
                    val mode = MemoMode.READ_MEMO

                    // 1. DB에서 MemoEntity 직접 조회
                    val db = MemoDatabase.getInstance(context)
                    val memo = runBlocking {
                        db.memoDao().getMemoByUUID(uuid)
                    }

                    // 2. PasswordActivity or MemoDetailActivity 실행
                    if (isLocked) {
                        PasswordActivity.startWidget(context, memoItem = memo)
                    } else {
                        MemoDetailActivity.startWidget(context, mode, memoItem = memo)
                    }
                }
            }


            else -> {
                LogTrack.d("MemoWidgetProvider", { "Unhandled action: $action" })
            }
        }
    }

    companion object {
        private const val ACTION_OPEN_MAIN = "ACTION_OPEN_MAIN"
        private const val ACTION_WRITE_MEMO = "ACTION_WRITE_MEMO"
        const val ACTION_ITEM_CLICK = "com.example.memog2.android.ACTION_ITEM_CLICK"

        const val EXTRA_MEMO_UUID = "EXTRA_MEMO_UUID"
        const val EXTRA_MEMO_IS_LOCKED = "EXTRA_MEMO_IS_LOCKED"
    }
}
