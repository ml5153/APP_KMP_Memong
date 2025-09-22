package com.memong.aos.service

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import com.avatye.haru.log.LogTrack
import com.memong.aos.data.factory.MemoListWidgetFactory

class MemoListWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        LogTrack.d("MemoWidgetService", { "Intent received: ${intent.toUri(0)}" })
        LogTrack.d("MemoWidgetService", { "Factory created for widgetId: $widgetId" })

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            LogTrack.w("MemoWidgetService", { "Invalid widget ID received in intent: $intent" })
        }

        return MemoListWidgetFactory(
            context = applicationContext,
            appWidgetId = widgetId
        )
    }
}

