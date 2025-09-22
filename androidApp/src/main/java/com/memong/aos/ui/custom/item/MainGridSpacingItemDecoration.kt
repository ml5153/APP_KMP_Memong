package com.memong.aos.ui.custom.item

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.memong.aos.ui.adapter.MemoGridAdapter


internal class MainGridSpacingItemDecoration(
    private val layoutManager: GridLayoutManager,
    private val spacing: Int,
    private val isMemoItemType: (position: Int) -> Boolean,
    private val getSectionIndexForPosition: (position: Int) -> Int?,
    private val getFirstAndLastMemoIndexInSection: (sectionIndex: Int) -> Pair<Int, Int>?,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION || !isMemoItemType(position)) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val spanCount = layoutManager.spanCount
        val spanIndex = layoutManager.spanSizeLookup.getSpanIndex(position, spanCount)
        val groupIndex = layoutManager.spanSizeLookup.getSpanGroupIndex(position, spanCount)

        val sectionSpanRange = getFirstLastSpanGroupInSection(position)
        val isFirstGroup = sectionSpanRange?.first == groupIndex
        val isLastGroup = sectionSpanRange?.second == groupIndex

        val sectionIndex = getSectionIndexForPosition(position)
        val isDateSection = sectionIndex != null &&
                parent.adapter?.getItemViewType(sectionIndex) == MemoGridAdapter.VIEW_TYPE_DATE_SECTION

        val left = spacing * (spanCount - spanIndex) / spanCount
        val right = spacing * (spanIndex + 1) / spanCount

        val top = if (isFirstGroup && !isDateSection) spacing else spacing / 2
        val bottom = if (isLastGroup) spacing else spacing / 2
        outRect.set(left, top, right, bottom)
    }

    private fun getFirstLastSpanGroupInSection(position: Int): Pair<Int, Int>? {
        val spanCount = layoutManager.spanCount
        val sectionIndex = getSectionIndexForPosition(position) ?: return null

        val (startIndex, endIndex) = getFirstAndLastMemoIndexInSection(sectionIndex) ?: return null

        val firstGroup = layoutManager.spanSizeLookup.getSpanGroupIndex(startIndex, spanCount)
        val lastGroup = layoutManager.spanSizeLookup.getSpanGroupIndex(endIndex, spanCount)

        return firstGroup to lastGroup
    }
}
