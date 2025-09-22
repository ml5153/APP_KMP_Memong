package com.avatye.haru.memo.ui.listener

import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class FabVisibilityScrollListener(
    private val fabView: View
) : RecyclerView.OnScrollListener() {

    private var scrolling = false

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            RecyclerView.SCROLL_STATE_DRAGGING,
            RecyclerView.SCROLL_STATE_SETTLING -> {
                if (!scrolling) {
                    scrolling = true
                    fabView.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            fabView.visibility = View.GONE
                        }
                        .start()
                }
            }
            RecyclerView.SCROLL_STATE_IDLE -> {
                if (scrolling) {
                    scrolling = false
                    fabView.visibility = View.VISIBLE
                    fabView.alpha = 0f
                    fabView.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
            }
        }
    }
}

