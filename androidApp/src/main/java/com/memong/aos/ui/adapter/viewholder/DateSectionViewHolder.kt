package com.memong.aos.ui.adapter.viewholder

import android.app.Activity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.memong.aos.R
import com.memong.aos.data.entity.MemoSectionListItem
import com.memong.aos.data.enum.MainGroupMode
import com.memong.aos.data.enum.MemoSortType
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_MEMO_GROUP_MODE
import com.memong.aos.data.utils.PreferenceUtil.getDynamicSectionKey
import com.memong.aos.databinding.ItemMemoSectionHeaderBinding
import com.memong.aos.ui.adapter.MemoGridAdapter
import com.memong.aos.ui.adapter.MemoListAdapter


internal class DateSectionViewHolder(
    private val activity: Activity,
    private val binding: ItemMemoSectionHeaderBinding,
    private val isSelected: () -> Boolean,
    private val currentSortType: () -> MemoSortType,
) : RecyclerView.ViewHolder(binding.lvSectionHeaderContainer) {

    private fun getSavedGroupMode(): MainGroupMode {
        val saved = PreferenceUtil.get(KEY_MEMO_GROUP_MODE, MainGroupMode.DATE.name)
        return runCatching { MainGroupMode.valueOf(saved) }.getOrDefault(MainGroupMode.DATE)
    }

    fun bind(header: MemoSectionListItem.SectionHeader, position: Int) {
        val savedGroupMode = getSavedGroupMode()
        val hasAll = header.memos.any { !it.isImportant && !it.isLocked }
        when {
            hasAll -> {
                binding.lySectionPin.isVisible = false
                binding.lySectionDate.isVisible = currentSortType() != MemoSortType.CUSTOM
                binding.tvSectionDateCount.isVisible = savedGroupMode == MainGroupMode.TAG

                binding.lvSectionHeaderContainer.setBackgroundResource(R.color.recyclerview_color)
                binding.tvSectionDateTitle.text = header.title
                binding.tvSectionDateCount.text = "(${header.memos.size})"
                binding.tvSectionDateArrow.isVisible = header.needExpandable
                binding.lySectionDate.setBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.view_background_color
                    )
                )

                //  추가된 부분: 화살표 표시 + 초기 회전값 적용
                setArrowRotation(header.isExpanded)
                if (header.needExpandable) {
                    binding.lvSectionHeaderContainer.setOnClickListener {
                        if (isSelected() && header.isExpanded) {
                            return@setOnClickListener
                        }


                        val rv = itemView.parent as? RecyclerView ?: return@setOnClickListener

                        //  부분 갱신: 어댑터에게 맡기기 (header.isExpanded 내부에서 토글됨)
                        (bindingAdapter as? MemoGridAdapter)?.toggleSection(header, rv)
                        (bindingAdapter as? MemoListAdapter)?.toggleSection(header, rv)

                        //  토글 이후 header.isExpanded 는 최신 상태 → 그에 맞춰 애니메이션
                        animateArrow(header.isExpanded)

                        //  펼침 상태 영구 저장(동적 섹션 포함 모든 섹션 공통 키)
                        PreferenceUtil.set(getDynamicSectionKey(header.sectionType.key), header.isExpanded)
                    }
                }
            }

            else -> {
                binding.lvSectionHeaderContainer.isVisible = false
            }
        }

    }


    private fun setArrowRotation(isExpanded: Boolean) {
        binding.tvSectionDateArrow.rotation = if (isExpanded) 0f else -90f
    }

    // 클릭 시 애니메이션 회전
    private fun animateArrow(isExpanded: Boolean) {
        binding.tvSectionDateArrow.animate()
            .rotation(if (isExpanded) 0f else -90f)
            .setDuration(200)
            .start()
    }
}
