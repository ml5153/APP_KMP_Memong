package com.avatye.haru.memo.ui.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import com.avatye.haru.memo.data.entity.BodyItem
import com.avatye.haru.memo.data.entity.MemoEntity
import com.bumptech.glide.Glide
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_TURN_OFF_TITLE
import com.avatye.haru.memo.data.utils.RowCodecUtil.HARU_MEMO_CHECKED
import com.avatye.haru.memo.data.utils.RowCodecUtil.HARU_MEMO_UNCHECKED
import com.avatye.haru.memo.databinding.ItemTrashGridBinding
import java.util.Calendar

internal class TrashAdapter(
    val items: MutableList<MemoEntity>
) : RecyclerView.Adapter<TrashAdapter.TrashViewHolder>() {

    private var isEditMode = false
    private val selectedIds = mutableSetOf<String>()

    var onItemClick: ((MemoEntity) -> Unit)? = null
    var onSelectChanged: ((Set<String>) -> Unit)? = null
    var onRequestEditMode: ((initialId: String) -> Unit)? = null

    fun setEditMode(editMode: Boolean, selected: Set<String>) {
        isEditMode = editMode
        selectedIds.clear()
        selectedIds.addAll(selected)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashViewHolder {
        val binding = ItemTrashGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrashViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrashViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TrashViewHolder(private val binding: ItemTrashGridBinding) :
        RecyclerView.ViewHolder(binding.gridRootView) {

        fun bind(item: MemoEntity) = with(binding) {
            bindContent(item)
            bindBackground(item)
            bindDeleteDate(item)
            bindSelectionMode(item)
        }

        private fun ItemTrashGridBinding.bindContent(item: MemoEntity) {
            gridImgImportant.isSelected = item.isImportant

            val imagePaths = item.imagePath.values.flatten()
            if (imagePaths.isNotEmpty()) {
                gridImgBody.visibility = View.VISIBLE
                gridTvBody.visibility = View.GONE
                Glide.with(itemView.context).load(imagePaths.first()).into(gridImgBody)
            } else {
                gridImgBody.visibility = View.GONE
                gridTvBody.visibility = View.VISIBLE
                gridTvBody.text = buildPreviewSpannable(itemView.context, item, gridTvBody) // 교체
                gridTvBody.maxLines = 6
                gridTvBody.isSelected = true
            }
        }

        private fun buildSpannable(title: String, body: List<BodyItem>): CharSequence {
            val spannable = SpannableStringBuilder()
            val hasTitle = title.isNotBlank()
            val bodyText = body.joinToString("\n") { it.text }
            val hasBody = bodyText.isNotBlank()

            if (hasTitle) {
                val start = spannable.length
                spannable.append(title)

                if (PreferenceUtil.get(KEY_TURN_OFF_TITLE, true)) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        spannable.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            if (hasTitle && hasBody) spannable.append("\n")
            if (hasBody) spannable.append(bodyText)

            return spannable
        }

        private fun ItemTrashGridBinding.bindBackground(item: MemoEntity) {
            if (item.isLocked) {
                gridLayoutBF.setBackgroundResource(R.drawable.ripple_lock_rectangle)
                gridLayoutBF.backgroundTintList = null
            } else {
                gridLayoutBF.setBackgroundResource(R.drawable.ripple_rectangle)
                val color = try {
                    Color.parseColor(item.bgColor?.takeIf { it.isNotBlank() } ?: "#FFFFFF")
                } catch (_: Exception) {
                    Color.WHITE
                }
                gridLayoutBF.backgroundTintList = ColorStateList.valueOf(color)
            }
        }

        private fun ItemTrashGridBinding.bindDeleteDate(item: MemoEntity) {
            val millisPerDay = 86_400_000L

            // 1. 삭제된 시점 기준으로 자정으로 내림
            val deletedStartOfDay = Calendar.getInstance().apply {
                timeInMillis = item.deleted
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // 2. 삭제 기준일: 삭제일 00시 + 14일
            val deletedDeadline = deletedStartOfDay + (14 * millisPerDay)

            // 3. 현재 날짜의 자정
            val todayStartOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // 4. 남은 날짜 계산
            val daysLeft = ((deletedDeadline - todayStartOfDay) / millisPerDay)

            // 5. 텍스트 표시
            gridTvDate.text = when {
                daysLeft > 0 -> "${daysLeft}일 후 삭제"
                daysLeft == 0L -> "오늘 삭제 예정"
                else -> "삭제 대상"
            }
        }

        private fun ItemTrashGridBinding.bindSelectionMode(item: MemoEntity) {
            if (isEditMode) {
                gridCheckboxContainer.visibility = View.VISIBLE
                lyGridImgImportant.visibility = View.GONE

                gridCheckbox.setOnCheckedChangeListener(null)
                gridCheckbox.isChecked = selectedIds.contains(item.uuid)
                gridCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    toggleSelection(item.uuid, isChecked)
                }

                lyItemMemoGrid.setOnClickListener {
                    val toggled = !selectedIds.contains(item.uuid)
                    toggleSelection(item.uuid, toggled)
                    gridCheckbox.setOnCheckedChangeListener(null)
                    gridCheckbox.isChecked = toggled
                    gridCheckbox.setOnCheckedChangeListener { _, isChecked ->
                        toggleSelection(item.uuid, isChecked)
                    }
                }

                lyItemMemoGrid.setOnLongClickListener(null)
            } else {
                gridCheckboxContainer.visibility = View.GONE
                lyGridImgImportant.visibility = View.VISIBLE

                lyItemMemoGrid.setOnClickListener { onItemClick?.invoke(item) }

                lyItemMemoGrid.setOnLongClickListener {
                    binding.root.context.vibrate()
                    onRequestEditMode?.invoke(item.uuid)
                    true
                }
            }
        }

        private fun toggleSelection(id: String, isSelected: Boolean) {
            if (isSelected) selectedIds.add(id) else selectedIds.remove(id)
            onSelectChanged?.invoke(selectedIds)
        }

        private fun Context.vibrate(duration: Long = 40L) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
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

            d.alpha = if (this@TrashAdapter.isEditMode) (0.3f * 255).toInt() else 255

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
