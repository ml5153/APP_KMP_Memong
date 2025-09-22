package com.memong.aos.ui.adapter.itemDecoration

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ChipSpacingDecoration(private val spacePx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        // 마지막 아이템은 제외하고 오른쪽 간격만 주기
        if (position != RecyclerView.NO_POSITION && position < itemCount - 1) {
            outRect.right = spacePx
        }
    }
}
