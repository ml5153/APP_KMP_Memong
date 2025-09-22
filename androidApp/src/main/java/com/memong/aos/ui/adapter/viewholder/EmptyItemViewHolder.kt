package com.memong.aos.ui.adapter.viewholder

import android.app.Activity
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.databinding.ItemMemoEmptyBinding
import com.memong.aos.ui.MemoDetailActivity


internal class EmptyItemViewHolder(
    val activity: Activity,
    val isGridMode: Boolean,
    val binding: ItemMemoEmptyBinding
) : RecyclerView.ViewHolder(binding.lyItemEmptyContainer) {
    fun bind() {


        val layoutParams = binding.lyItemEmptyContainer.layoutParams
        layoutParams.width = when (isGridMode) {
            true -> ViewGroup.LayoutParams.WRAP_CONTENT
            false -> ViewGroup.LayoutParams.MATCH_PARENT
        }
        binding.lyItemEmptyContainer.layoutParams = layoutParams


        if (isGridMode) {
            binding.lyGridItemEmpty.isVisible = true
            binding.lyListItemEmpty.isVisible = false
            binding.lyGridItemEmpty.setOnClickListener {
                MemoDetailActivity.start(
                    activity = activity,
                    mode = MemoMode.CREATE_MEMO
                )
            }
        } else {
            binding.lyGridItemEmpty.isVisible = false
            binding.lyListItemEmpty.isVisible = true
            binding.lyListItemEmpty.setOnClickListener {
                MemoDetailActivity.start(
                    activity = activity,
                    mode = MemoMode.CREATE_MEMO
                )
            }
        }
    }
}