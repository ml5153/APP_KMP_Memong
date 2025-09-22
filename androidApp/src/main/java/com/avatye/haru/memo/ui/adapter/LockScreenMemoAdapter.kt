package com.avatye.haru.memo.ui.adapter

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
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.dao.MemoDao
import com.avatye.haru.memo.data.entity.MemoEntity
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_TURN_OFF_TITLE
import com.avatye.haru.memo.data.utils.RowCodecUtil.HARU_MEMO_CHECKED
import com.avatye.haru.memo.data.utils.RowCodecUtil.HARU_MEMO_UNCHECKED
import com.avatye.haru.memo.data.utils.Util.Companion.formatDate
import com.avatye.haru.memo.databinding.ItemLockEmptyImportantMemoBinding
import com.avatye.haru.memo.databinding.ItemMemoGridBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LockScreenMemoAdapter(
    private var items: MutableList<MemoEntity>,
    private val memoDao: MemoDao,
    private val onItemClick: (MemoEntity?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    inner class MemoViewHolder(val binding: ItemMemoGridBinding) :
        RecyclerView.ViewHolder(binding.gridRootView) {

        fun bind(item: MemoEntity) = with(binding) {
            val lp = root.layoutParams
            if (lp != null) {
                lp.width =
                    root.context.resources.getDimensionPixelSize(R.dimen.lock_memo_item_width)
                lp.height =
                    root.context.resources.getDimensionPixelSize(R.dimen.lock_memo_item_height)
                root.layoutParams = lp
            }
            root.cardElevation = 0f

            binding.lyItemMemoGrid.setOnClickListener {
                onItemClick(item)
            }

            // 날짜
            gridTvDate.text = formatDate(item.created)


            // 이미지
            val imagePaths = item.imagePath.values.flatten()
            if (imagePaths.isNotEmpty()) {
                gridImgBody.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(imagePaths.first())
                    .into(gridImgBody)
                gridTvBody.visibility = View.GONE
            } else {
                gridImgBody.visibility = View.GONE
                gridTvBody.text = buildPreviewSpannable(itemView.context, item, gridTvBody)
                gridTvBody.maxLines = 6
                gridTvBody.isSelected = true
                gridTvBody.visibility = View.VISIBLE
            }

            // 잠금 상태
            if (item.isLocked) {
                gridLayoutBF.setBackgroundResource(R.drawable.ripple_lock_rectangle)
                gridTvBody.visibility = View.INVISIBLE
                gridImgLock.visibility = View.VISIBLE
                gridImgBody.visibility = View.GONE
            } else {
                gridLayoutBF.setBackgroundResource(R.drawable.ripple_rectangle)
                try {
                    val rawColor = item.bgColor?.takeIf { it.isNotBlank() } ?: "#FFFFFF"
                    val parsedColor = Color.parseColor(rawColor)
                    gridLayoutBF.backgroundTintList = ColorStateList.valueOf(parsedColor)
                } catch (_: IllegalArgumentException) {
                    gridLayoutBF.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                }

                gridImgLock.visibility = View.GONE

                // 이미지/텍스트 가시성만 정리
                if (imagePaths.isNotEmpty()) {
                    gridImgBody.visibility = View.VISIBLE
                    gridTvBody.visibility = View.GONE
                } else {
                    gridImgBody.visibility = View.GONE
                    gridTvBody.visibility = View.VISIBLE

                    // ⬇️ 아직 위에서 세팅 안 했다면(혹은 항상 보장하고 싶다면) 프리뷰 스팬 세팅
                    gridTvBody.text = buildPreviewSpannable(itemView.context, item, gridTvBody)
                    gridTvBody.maxLines = 6
                    gridTvBody.isSelected = true
                }
            }

            // 중요도
            gridImgImportant.visibility = View.VISIBLE
            gridImgImportant.isSelected = item.isImportant
            lyGridImgImportant.setOnClickListener {
                val updated = item.copy(isImportant = !item.isImportant)
                CoroutineScope(Dispatchers.IO).launch {
                    memoDao.updateMemo(updated)
                    withContext(Dispatchers.Main) {
                        val index = bindingAdapterPosition
                        if (index != RecyclerView.NO_POSITION) {
                            if (!updated.isImportant) {
                                items.removeAt(index)
                                notifyItemRemoved(index)

                                // 이벤트 전송 (즐겨찾기 해제)
                                EventUtil.sendEvent(
                                    context = it.context,
                                    category = EventUtil.CATEGORY_LOCKSCREEN,
                                    action = EventUtil.ACTION_LOCKSCREEN_IMPORTANT_OFF
                                )
                            } else {
                                items[index] = updated
                                notifyItemChanged(index)

                                // 이벤트 전송 (즐겨찾기 설정)
                                EventUtil.sendEvent(
                                    context = it.context,
                                    category = EventUtil.CATEGORY_LOCKSCREEN,
                                    action = EventUtil.ACTION_LOCKSCREEN_IMPORTANT_ON
                                )
                            }
                        }
                    }
                }
            }


            // 체크박스는 항상 gone
            gridCheckbox.visibility = View.GONE
        }
    }

    inner class EmptyViewHolder(private val binding: ItemLockEmptyImportantMemoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() = with(binding) {
            val lp = root.layoutParams
            if (lp != null) {
                lp.width =
                    root.context.resources.getDimensionPixelSize(R.dimen.lock_memo_empty_item_width)
                lp.height =
                    root.context.resources.getDimensionPixelSize(R.dimen.lock_memo_empty_item_height)
                root.layoutParams = lp
            }
            root.cardElevation = 0f
            binding.lyItemEmpty.setOnClickListener {
                onItemClick(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_TYPE_EMPTY) {
            val binding = ItemLockEmptyImportantMemoBinding.inflate(inflater, parent, false)
            EmptyViewHolder(binding)
        } else {
            val binding = ItemMemoGridBinding.inflate(inflater, parent, false)
            MemoViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MemoViewHolder) {
            holder.bind(items[position])
        } else if (holder is EmptyViewHolder) {
            holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.isEmpty() && position == 0) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return if (items.isEmpty()) 1 else items.size
    }

    fun submitList(newItems: List<MemoEntity>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun updateItem(updatedItem: MemoEntity) {
        val index = items.indexOfFirst { it._id == updatedItem._id }
        if (index != -1) {
            items[index] = updatedItem
            notifyItemChanged(index)
        }
    }

    fun updateFavorite(updatedItem: MemoEntity) {
        val newList = items.toMutableList()
        val index = newList.indexOfFirst { it._id == updatedItem._id }

        if (updatedItem.isImportant) {
            if (index == -1) {
                newList.add(updatedItem)
            } else {
                newList[index] = updatedItem
            }
        } else {
            if (index != -1) {
                newList.removeAt(index)
            }
        }

        // 정렬 적용 (작성 시간 기준으로 최신순)
        val sortedList = newList
            .filter { it.isImportant }
            .sortedByDescending { it.created }
            .toList()

        submitList(sortedList)
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
            } catch (_: Exception) {}

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
