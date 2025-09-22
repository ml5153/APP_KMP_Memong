package com.memong.aos.ui.adapter.viewholder

import android.app.Activity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.memong.aos.R
import com.memong.aos.data.entity.MemoSectionListItem
import com.memong.aos.data.enum.FixedSectionType
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED
import com.memong.aos.data.utils.PreferenceUtil.KEY_SECTION_SECRET_MEMO_IS_EXPANDED
import com.memong.aos.databinding.ItemMemoSectionHeaderBinding
import com.memong.aos.ui.adapter.MemoGridAdapter
import com.memong.aos.ui.adapter.MemoListAdapter

internal class LockSectionViewHolder(
    private val activity: Activity,
    private val binding: ItemMemoSectionHeaderBinding,
    private val isSelected: () -> Boolean,
    private val updateFlatList: () -> Unit
) : RecyclerView.ViewHolder(binding.lvSectionHeaderContainer) {

    fun bind(header: MemoSectionListItem.SectionHeader, position: Int) {
        val hasLock = header.memos.any { it.isLocked }

        if (hasLock) {
            binding.lySectionPin.isVisible = true
            binding.lySectionDate.isVisible = false
            binding.lvSectionHeaderContainer.setBackgroundResource(R.color.white)
            binding.ivSectionHeaderIcon.setImageResource(R.drawable.ic_item_lock2)
            binding.tvSectionTitle.text = header.title
            binding.tvSectionMemoCount.text = "(${header.memos.size})"
            binding.tvSectionMemoArrow.isVisible = header.needExpandable
            binding.lySectionPin.setBackgroundColor(
                ContextCompat.getColor(activity, R.color.bg_white)
            )

            // 초기 바인딩에서는 애니메이션 없이 바로 세팅
            setArrowRotation(header.isExpanded)

            if (header.needExpandable) {
                binding.lvSectionHeaderContainer.setOnClickListener {
                    if (isSelected() && header.isExpanded) return@setOnClickListener

                    val recyclerView = itemView.parent as? RecyclerView ?: return@setOnClickListener

                    (bindingAdapter as? MemoGridAdapter)?.toggleSection(header, recyclerView)
                    (bindingAdapter as? MemoListAdapter)?.toggleSection(header, recyclerView)

                    // 클릭으로 토글 시에는 애니메이션
                    animateArrow(header.isExpanded)

                    // 상태 저장
                    if (header.sectionType == FixedSectionType.SECRET) {
                        PreferenceUtil.set(KEY_SECTION_SECRET_MEMO_IS_EXPANDED, header.isExpanded)
                    } else if (header.sectionType == FixedSectionType.IMPORTANT) {
                        PreferenceUtil.set(KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED, header.isExpanded)
                    }
                }
            }
        } else {
            binding.lvSectionHeaderContainer.isVisible = false
        }
    }

    // 애니메이션 없이 즉시 세팅
    private fun setArrowRotation(isExpanded: Boolean) {
        binding.tvSectionMemoArrow.rotation = if (isExpanded) 0f else -90f
    }

    // 클릭 시 호출 → 애니메이션 적용
    private fun animateArrow(isExpanded: Boolean) {
        val targetRotation = if (isExpanded) 0f else -90f
        binding.tvSectionMemoArrow.animate()
            .rotation(targetRotation)
            .setDuration(200)
            .start()
    }
}