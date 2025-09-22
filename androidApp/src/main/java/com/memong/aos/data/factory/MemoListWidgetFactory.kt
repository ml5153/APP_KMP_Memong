package com.memong.aos.data.factory

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.graphics.scale
import com.avatye.haru.log.LogTrack
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.RowCodecUtil.HARU_MEMO_CHECKED
import com.memong.aos.data.utils.RowCodecUtil.HARU_MEMO_UNCHECKED
import com.memong.aos.data.utils.Util.Companion.dpToPx
import com.memong.aos.data.utils.Util.Companion.formatDate
import com.memong.aos.ui.widget.MemoWidgetMedium.Companion.EXTRA_MEMO_IS_LOCKED
import com.memong.aos.ui.widget.MemoWidgetMedium.Companion.EXTRA_MEMO_UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

internal class MemoListWidgetFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<MemoEntity> = emptyList()
    private val TAG = "MemoWidgetFactory"

    override fun onCreate() {
        LogTrack.d(TAG, { "onCreate() for widgetId=$appWidgetId" })
        loadItems()
    }

    override fun onDataSetChanged() {
        LogTrack.d(TAG, { "onDataSetChanged() for widgetId=$appWidgetId" })
        loadItems()
    }

    private fun loadItems() = runBlocking(Dispatchers.IO) {
        try {
            val freshDao = MemoDatabase.getInstance(context).memoDao()
            items = freshDao.getAllUnLockMemos()
            LogTrack.d(TAG, { "Items loaded: ${items.size}" })
        } catch (e: Exception) {
            LogTrack.e(TAG) { "Failed to load items from DB, $e" }
            items = emptyList()
        }
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position !in items.indices) {
            LogTrack.w(TAG, { "Invalid position: $position" })
            return RemoteViews(context.packageName, R.layout.item_widget_memo_list)
        }

        val item = items[position]
        LogTrack.d(TAG, { "getViewAt($position) uuid=${item.uuid}" })

        val views = RemoteViews(context.packageName, R.layout.item_widget_memo_list)

        bindTextViews(views, item)
        bindImageStateViews(views, item)
        bindImportanceIcon(views, item)
        bindBackgroundStateViews(views, item)


        // 로그로 상태 확인
        LogTrack.d(
            TAG,
            { "Binding item: $position uuid=${item.uuid}, isLocked=${item.isLocked}" }
        )

        val fillInIntent = Intent().apply {
            putExtra(EXTRA_MEMO_UUID, item.uuid)
            putExtra(EXTRA_MEMO_IS_LOCKED, item.isLocked)
        }

        LogTrack.d(TAG, { "Setting fillInIntent extras: ${fillInIntent.extras}" })

        views.setOnClickFillInIntent(R.id.widget_content_container, fillInIntent)


        return views
    }

    private fun bindTextViews(views: RemoteViews, item: MemoEntity) {
        val contentText = buildWidgetStyledText(item)

        views.setTextViewText(R.id.list_widget_tvContent, contentText)
        views.setTextViewText(R.id.list_widget_tvDate, formatDate(item.created))

        views.setViewVisibility(R.id.list_widget_tvContent, View.VISIBLE)
        views.setViewVisibility(R.id.list_widget_tvDate, View.VISIBLE)
    }

    private fun buildStyledText(item: MemoEntity): CharSequence {
        return SpannableStringBuilder().apply {
            val hasTitle = !item.title.isNullOrBlank()
            val bodyText = item.body.joinToString("\n") { it.text }
            val hasBody = bodyText.isNotBlank()

            if (hasTitle) {
                val start = length
                append(item.title)
                val end = length
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            if (hasTitle && hasBody) {
                append("\n")
            }

            if (hasBody) {
                append(bodyText)
            }
        }
    }

    private fun bindImageStateViews(views: RemoteViews, item: MemoEntity) {

        val firstImageUri = item.imagePath
            .toSortedMap()
            .values
            .firstOrNull { it.isNotEmpty() }
            ?.firstOrNull()

        if (!firstImageUri.isNullOrEmpty()) {
            val bitmap = loadBitmapFromUri(firstImageUri)
            if (bitmap != null) {
                val roundedBitmap = getRoundedBitmap(bitmap)
                views.setImageViewBitmap(R.id.list_widget_imgBody, roundedBitmap)
                views.setViewVisibility(R.id.list_widget_imgBody, View.VISIBLE)
                return
            } else {
            }
        } else {
        }

        views.setViewVisibility(R.id.list_widget_imgBody, View.GONE)
    }

    private fun loadBitmapFromUri(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val fileName = uri.lastPathSegment ?: run {
                return null
            }

            // 폴더 경로는 URI 내 path를 보고 분기
            val filePath = when {
                uri.path?.contains("/images/") == true -> File(context.filesDir, "images/$fileName")
                uri.path?.contains("/files/") == true -> File(context.filesDir, fileName)
                else -> File(context.filesDir, fileName) // fallback
            }

            if (!filePath.exists()) {
                return null
            }

            val original = BitmapFactory.decodeFile(filePath.absolutePath) ?: return null
            val scaled = original.scale(dpToPx(context, 45).toInt(), dpToPx(context, 35).toInt())
            return getRoundedBitmap(scaled)

        } catch (e: Exception) {
            null
        }
    }

    private fun getRoundedBitmap(src: Bitmap): Bitmap {
        val cornerRadius = dpToPx(context, 6).coerceAtMost(minOf(src.width, src.height) / 2)

        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val rect = RectF(0f, 0f, src.width.toFloat(), src.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius.toFloat(), cornerRadius.toFloat(), paint)

        return output
    }

    private fun bindBackgroundStateViews(views: RemoteViews, item: MemoEntity) {
        val headerBackgroundRes = when (item.bgColor.uppercase()) {
            "#FFFFFF" -> R.drawable.bg_widget_header
            "#FDFEBC" -> R.drawable.bg_widget_header_yellow
            "#E0FFDD" -> R.drawable.bg_widget_header_green
            "#D7FDFE" -> R.drawable.bg_widget_header_blue
            "#FCE6BF" -> R.drawable.bg_widget_header_orange
            "#FFE3E0" -> R.drawable.bg_widget_header_pink
            else -> R.drawable.bg_widget_header // fallback
        }

        views.setInt(R.id.widget_item_root, "setBackgroundResource", headerBackgroundRes)
    }

    private fun buildWidgetStyledText(item: MemoEntity): CharSequence {
        val ssb = SpannableStringBuilder()

        // 공통 파서: 토큰 → (체크박스 여부, 체크 여부, 순수 텍스트)
        fun parseRow(raw: String): Triple<Boolean, Boolean, String> = when {
            raw.startsWith(HARU_MEMO_CHECKED) ->
                Triple(true, true, raw.removePrefix(HARU_MEMO_CHECKED).trimStart())

            raw.startsWith(HARU_MEMO_UNCHECKED) ->
                Triple(true, false, raw.removePrefix(HARU_MEMO_UNCHECKED).trimStart())

            else -> Triple(false, false, raw)
        }

        fun appendRow(raw: String, isTitle: Boolean) {
            val (isCheckbox, isChecked, pure) = parseRow(raw)

            // 체크박스 기호 (ImageSpan 대신 유니코드)
            if (isCheckbox) ssb.append(if (isChecked) "☑ " else "☐ ")

            val textStart = ssb.length
            ssb.append(pure)
            val textEnd = ssb.length

            // 제목이면 Bold
            if (isTitle && PreferenceUtil.get(PreferenceUtil.KEY_TURN_OFF_TITLE, true) && pure.isNotBlank()) {
                ssb.setSpan(
                    StyleSpan(Typeface.BOLD),
                    textStart, textEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // 체크된 항목은 취소선 (제목/본문 동일)
            if (isCheckbox && isChecked && pure.isNotBlank()) {
                ssb.setSpan(android.text.style.StrikethroughSpan(), textStart, textEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            ssb.append('\n')
        }

        // 제목 처리: 이제 체크박스 토큰도 반영됨
        if (item.title.isNotBlank()) {
            appendRow(item.title, isTitle = true)
        }

        // 본문 처리
        item.body.forEach { bodyItem ->
            bodyItem.text.split('\n').forEach { appendRow(it, isTitle = false) }
        }

        // 마지막 개행 제거
        if (ssb.isNotEmpty() && ssb.last() == '\n') ssb.delete(ssb.length - 1, ssb.length)

        return ssb
    }


    private fun bindImportanceIcon(views: RemoteViews, item: MemoEntity) {
        val iconRes = if (item.isImportant) R.drawable.ic_important_p else R.drawable.ic_important_n
        views.setImageViewResource(R.id.list_widget_imgImportant, iconRes)
        views.setViewVisibility(R.id.list_widget_imgImportant, View.VISIBLE)
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        if (position in items.indices) items[position]._id.toLong() else position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {
        items = emptyList()
    }

}

