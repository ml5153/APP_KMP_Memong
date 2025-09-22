package com.memong.aos.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.memong.aos.R
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.ui.MemoDetailActivity
import com.memong.aos.ui.MemoListActivity

internal class MemoWidgetSmall : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        for (widgetId in widgetIds) {
            // 새 메모 클릭 인텐트
            val newMemoIntent = Intent(context, MemoWidgetSmall::class.java).apply {
                action = ACTION_NEW_MEMO_CLICKED
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

            val newMemoPendingIntent = PendingIntent.getBroadcast(
                context,
                widgetId, // 고유성 보장
                newMemoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 로고 클릭 인텐트
            val logoIntent = Intent(context, MemoWidgetSmall::class.java).apply {
                action = ACTION_LOGO_CLICKED
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

            val logoPendingIntent = PendingIntent.getBroadcast(
                context,
                widgetId + 1000, // 고유성 충돌 방지
                logoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_memo_small).apply {
                setOnClickPendingIntent(R.id.newMemoContainer, newMemoPendingIntent)
                setOnClickPendingIntent(R.id.widgetImgLogo, logoPendingIntent)
            }

            manager.updateAppWidget(widgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        // 위젯이 처음 추가될 때만 이벤트 전송
        EventUtil.sendEvent(
            context,
            EventUtil.CATEGORY_WIDGET,
            EventUtil.ACTION_SMALL_WIDGET
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_NEW_MEMO_CLICKED -> {
                MemoDetailActivity.startWidget(
                    context = context,
                    MemoMode.CREATE_MEMO
                )
            }

            ACTION_LOGO_CLICKED -> {
                MemoListActivity.startWidget(
                    context = context
                )
            }
        }
    }


    companion object {
        private const val ACTION_LOGO_CLICKED = "ACTION_LOGO_CLICKED"
        private const val ACTION_NEW_MEMO_CLICKED = "ACTION_NEW_MEMO_CLICKED"
    }
}
