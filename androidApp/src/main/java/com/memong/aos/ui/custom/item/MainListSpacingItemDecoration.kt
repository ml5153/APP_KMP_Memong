package com.memong.aos.ui.custom.item

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class MainListSpacingItemDecoration(
    private val spacing: Int,
    private val isMemoItemType: (position: Int) -> Boolean,
    private val isFirstMemoAfterSection: (position: Int) -> Boolean,
    private val isLastMemoBeforeSection: (position: Int) -> Boolean,
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

        val standard = (spacing * 1.7).toInt()
        val top = if (isFirstMemoAfterSection(position)) standard else spacing / 2
        val bottom = if (isLastMemoBeforeSection(position)) standard else spacing / 2


        outRect.set(standard, top, standard, bottom)
    }
}
