package com.avatye.haru.memo.ui.custom.item

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView

internal class MainSectionBackgroundItemDecoration(
    private val sectionResolver: (position: Int) -> String?,
    private val sectionColorProvider: (sectionKey: String) -> Int
) : RecyclerView.ItemDecoration() {

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val sectionBounds = mutableMapOf<String, Rect>()

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION) continue

            val sectionKey = sectionResolver(position) ?: continue
            val bounds = Rect()
            parent.getDecoratedBoundsWithMargins(child, bounds)

            val existing = sectionBounds[sectionKey]
            if (existing == null) {
                sectionBounds[sectionKey] = Rect(bounds)
            } else {
                existing.union(bounds)
            }
        }

        for ((key, rect) in sectionBounds) {
            val paint = Paint().apply {
                color = sectionColorProvider(key)
                style = Paint.Style.FILL
            }
            c.drawRect(rect, paint)
        }
    }
}