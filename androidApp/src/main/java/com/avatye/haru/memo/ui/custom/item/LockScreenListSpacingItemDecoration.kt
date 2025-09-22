package com.avatye.haru.memo.ui.custom.item

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class LockScreenListSpacingItemDecoration(
    private val horizontal: Int,
    private val vertical: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = horizontal
        outRect.right = horizontal

        // 첫 번째 아이템은 위에도 간격 추가
        val position = parent.getChildAdapterPosition(view)
        outRect.top = if (position == 0) vertical else vertical / 2
        outRect.bottom = vertical / 2
    }
}

