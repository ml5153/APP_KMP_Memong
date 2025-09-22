package com.memong.aos.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.memong.aos.R
import com.memong.aos.data.dao.MemoDao
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.RowCodecUtil.HARU_MEMO_CHECKED
import com.memong.aos.data.utils.RowCodecUtil.HARU_MEMO_UNCHECKED
import com.memong.aos.data.utils.Util.Companion.formatDate
import com.memong.aos.databinding.ItemMemoListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LockScreenMemoListAdapter(
    private val context: Context,
    private val memoDao: MemoDao,
    private val onItemClick: (MemoEntity) -> Unit,
    private val onImportantChanged: (MemoEntity) -> Unit
) : ListAdapter<MemoEntity, LockScreenMemoListAdapter.MemoViewHolder>(MemoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMemoListBinding.inflate(inflater, parent, false)
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemoViewHolder(private val binding: ItemMemoListBinding) :
        RecyclerView.ViewHolder(binding.listRootView) {

        fun bind(item: MemoEntity) = with(binding) {

            root.setOnClickListener {
                onItemClick(item)
            }

            // 날짜
            listTvDate.text = formatDate(item.created)

            // 이미지
            val imagePaths = item.imagePath.values.flatten()
            if (imagePaths.isNotEmpty()) {
                listImgBody.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(imagePaths.first())
                    .into(listImgBody)
            } else {
                listImgBody.visibility = View.GONE
            }

            // 잠금 여부
            if (item.isLocked) {
                listLayoutContent.setBackgroundResource(R.drawable.ripple_lock_rectangle)
                listTvBody.visibility = View.INVISIBLE
                listImgLock.visibility = View.VISIBLE
                listImgBody.visibility = View.GONE
            } else {
                listLayoutContent.setBackgroundResource(R.drawable.ripple_rectangle)
                try {
                    val rawColor = item.bgColor?.takeIf { it.isNotBlank() } ?: "#FFFFFF"
                    val parsedColor = Color.parseColor(rawColor)
                    listLayoutContent.backgroundTintList = ColorStateList.valueOf(parsedColor)
                } catch (_: IllegalArgumentException) {
                    listLayoutContent.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                }

                // 프리뷰 스팬 적용 (제목 Bold + 체크박스 아이콘 + 취소선)
                listTvBody.text = buildPreviewSpannable(itemView.context, item, listTvBody)
                listTvBody.maxLines = 2
                listTvBody.isSelected = true
                listTvBody.visibility = View.VISIBLE

                listImgLock.visibility = View.GONE
            }

            // 중요 여부
            listImgImportant.visibility = View.VISIBLE
            listImgImportant.isSelected = item.isImportant
            lyListImgImportant.setOnClickListener {
                val updated = item.copy(isImportant = !item.isImportant)

                CoroutineScope(Dispatchers.IO).launch {
                    memoDao.updateMemo(updated)

                    withContext(Dispatchers.Main) {
                        updateItem(updated) // UI 갱신
                        onImportantChanged(updated)

                        // 이벤트 전송
                        if (updated.isImportant) {
                            EventUtil.sendEvent(
                                context = it.context,
                                category = EventUtil.CATEGORY_LOCKSCREEN,
                                action = EventUtil.ACTION_LOCKSCREEN_IMPORTANT_ON
                            )
                        } else {
                            EventUtil.sendEvent(
                                context = it.context,
                                category = EventUtil.CATEGORY_LOCKSCREEN,
                                action = EventUtil.ACTION_LOCKSCREEN_IMPORTANT_OFF
                            )
                        }
                    }
                }
            }


            // 체크박스는 항상 gone
            listCheckbox.visibility = View.GONE
        }
    }

    class MemoDiffCallback : DiffUtil.ItemCallback<MemoEntity>() {
        override fun areItemsTheSame(oldItem: MemoEntity, newItem: MemoEntity): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: MemoEntity, newItem: MemoEntity): Boolean {
            return oldItem == newItem
        }
    }

    fun updateItem(updatedItem: MemoEntity) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it._id == updatedItem._id }
        if (index != -1) {
            currentList[index] = updatedItem
            submitList(currentList)  // 리스트 갱신으로 DiffUtil이 작동
        }
    }

    private fun buildPreviewSpannable(
        context: Context,
        item: MemoEntity,
        textView: TextView
    ): CharSequence {
        val ssb = SpannableStringBuilder()

        // 1) 공통: 한 줄 파싱 (토큰 → 체크박스 여부/상태/순수 텍스트)
        fun parseRow(raw: String): Triple<Boolean, Boolean, String> {
            return when {
                raw.startsWith(HARU_MEMO_CHECKED) ->
                    Triple(true, true, raw.removePrefix(HARU_MEMO_CHECKED).trimStart())

                raw.startsWith(HARU_MEMO_UNCHECKED) ->
                    Triple(true, false, raw.removePrefix(HARU_MEMO_UNCHECKED).trimStart())

                else -> Triple(false, false, raw)
            }
        }

        // 2) 공통: 체크박스 아이콘 추가
        fun appendCheckboxIcon(isChecked: Boolean) {
            val d = AppCompatResources.getDrawable(
                context,
                if (isChecked) R.drawable.ic_checkbox_item_p else R.drawable.ic_checkbox_item_n
            )?.mutate() ?: return

            try {
                DrawableCompat.setTintList(DrawableCompat.wrap(d), null)
            } catch (_: Exception) {
            }

            val sizePx = textView.textSize.toInt() // px
            d.setBounds(0, 0, sizePx, sizePx)

            val start = ssb.length
            ssb.append('\uFFFC') // 아이콘 자리
            ssb.setSpan(
                ImageSpan(d, DynamicDrawableSpan.ALIGN_BOTTOM),
                start, start + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.append(' ')
        }

        // 3) 제목 전용: 체크박스/볼드/취소선까지 처리
        fun appendTitleLine(raw: String) {
            val (isCheckbox, isChecked, pure) = parseRow(raw)

            if (isCheckbox) appendCheckboxIcon(isChecked)

            val textStart = ssb.length
            ssb.append(pure)
            val textEnd = ssb.length

            // 제목 볼드(설정값 on일 때), 텍스트가 있을 때만
            if (PreferenceUtil.get(PreferenceUtil.KEY_TURN_OFF_TITLE, true) && pure.isNotBlank()) {
                ssb.setSpan(
                    StyleSpan(Typeface.BOLD),
                    textStart, textEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            // 체크된 체크박스 제목이면 취소선
            if (isCheckbox && isChecked && pure.isNotBlank()) {
                ssb.setSpan(
                    StrikethroughSpan(),
                    textStart, textEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            ssb.append('\n')
        }

        // 4) 바디 전용: 기존 로직(아이콘/취소선) 유지
        fun appendBodyLine(body: String) {
            val (isCheckbox, isChecked, pure) = parseRow(body)

            if (isCheckbox) appendCheckboxIcon(isChecked)

            val textStart = ssb.length
            ssb.append(pure)
            if (isCheckbox && isChecked && pure.isNotBlank()) {
                ssb.setSpan(
                    StrikethroughSpan(),
                    textStart, ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            ssb.append('\n')
        }

        // 제목 처리: 항상 첫 줄만 볼드
        val rawTitle = item.title.orEmpty()
        if (rawTitle.isNotBlank()) {
            val nl = rawTitle.indexOf('\n')
            val titleLine = if (nl >= 0) rawTitle.substring(0, nl) else rawTitle
            appendTitleLine(titleLine)

            // 레거시 방어: 타이틀에 남은 줄이 있으면 본문처럼 붙이기
            if (nl >= 0 && nl + 1 < rawTitle.length) {
                val extra = rawTitle.substring(nl + 1)
                extra.split('\n').forEach { appendBodyLine(it) }
            }
        }

        // 본문 처리: 기존처럼 줄 단위
        item.body.forEach { bodyItem ->
            bodyItem.text.split('\n').forEach { appendBodyLine(it) }
        }

        // 마지막 개행 제거
        if (ssb.isNotEmpty() && ssb.last() == '\n') {
            ssb.delete(ssb.length - 1, ssb.length)
        }
        return ssb
    }

}
