package com.avatye.haru.memo.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.Patterns
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.entity.BitmapImage
import com.avatye.haru.memo.data.entity.BodyRow
import com.avatye.haru.memo.data.entity.BodyType
import com.avatye.haru.memo.data.entity.MatchPosition
import com.avatye.haru.memo.data.entity.MemoBlock
import com.avatye.haru.memo.data.enum.MemoMode
import com.avatye.haru.memo.data.extension.enterCursor
import com.avatye.haru.memo.data.extension.exitCursor
import com.avatye.haru.memo.data.extension.hideKeyboard
import com.avatye.haru.memo.data.extension.isAlive
import com.avatye.haru.memo.data.extension.showIme
import com.avatye.haru.memo.data.utils.AnimationUtil
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_CHECKED_MEMO_AUTO_SORT
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_QUICK_PAGE
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_TURN_OFF_TITLE
import com.avatye.haru.memo.data.utils.Util
import com.avatye.haru.memo.data.utils.VibrationUtil
import com.avatye.haru.memo.databinding.ItemMemoBodyBlockBinding
import com.avatye.haru.memo.databinding.ItemMemoDateBlockBinding
import com.avatye.haru.memo.databinding.ItemMemoImageBlockBinding
import com.avatye.haru.memo.ui.custom.dialog.MemoCustomDialog
import com.avatye.haru.memo.ui.custom.item.GridSpacingItemDecoration
import com.avatye.haru.memo.ui.custom.view.CursorAwareEditText
import com.avatye.haru.memo.ui.custom.view.setOnSelectionChangedListener
import com.bumptech.glide.Glide
import java.util.regex.Pattern
import kotlin.math.max

class MemoBlockAdapter(
    private val activity: AppCompatActivity,
) : ListAdapter<MemoBlock, RecyclerView.ViewHolder>(MemoBlockDiffCallback()) {

    companion object {
        private const val NAME = "MemoBlockAdapter"

        private const val VIEW_TYPE_DATE = 0
        private const val VIEW_TYPE_BODY = 1
        private const val VIEW_TYPE_IMAGE = 2

        private const val BULLET = "• "

    }

    private var currentMode: MemoMode = MemoMode.NONE

    private var onTitleBind: ((EditText, MemoMode) -> Unit)? = null
    private var onBodyBind: ((EditText, MemoMode) -> Unit)? = null
    var onRequestEdit: ((adapterPosition: Int, rowIndex: Int) -> Unit)? = null

    var currentFocusedPosition: Int = -1

    var matchPositions = mutableListOf<MatchPosition>()  // 매치된 block의 position 리스트
    var currentMatchIndex = 0                  // 현재 몇 번째 매치인지
    private var currentKeyword = ""                    // 현재 검색 중인 키워드
    private var attachedRecyclerView: RecyclerView? = null
    private var isExporting: Boolean = false

    private var imageSelectionMode: Boolean = false     // 이미지 선택모드 상태
    private val selectionCountByPos = mutableMapOf<Int, Int>()      // 블록별(어댑터 포지션) 선택 개수 집계

    var onImagePresenceChanged: ((Boolean) -> Unit)? = null
    private var lastHasImages: Boolean = false

    private fun String.hasBulletPrefix() = startsWith(BULLET)
    private fun String.addBulletPrefix() = if (hasBulletPrefix()) this else BULLET + this
    private fun String.stripBulletPrefix() = if (hasBulletPrefix()) removePrefix(BULLET) else this


    fun setExporting(value: Boolean) {
        isExporting = value
    }

    fun setMemoMode(mode: MemoMode) {
        if (this.currentMode == mode) return
        this.currentMode = mode
        if (itemCount > 0) {
            // 리스트는 그대로 두고, 모드만 반영 (payload로 부분 갱신)
            notifyItemRangeChanged(0, itemCount, "payload_mode")
        }
    }

    fun setOnEditTextBindCallback(
        onTitleBind: ((EditText, MemoMode) -> Unit)?,
        onBodyBind: ((EditText, MemoMode) -> Unit)?
    ) {
        this.onTitleBind = onTitleBind
        this.onBodyBind = onBodyBind
    }

    fun setAttachedRecyclerView(rv: RecyclerView) {
        this.attachedRecyclerView = rv
    }

    fun getCurrentBlocks(): List<MemoBlock> {
        return currentList.toList()
    }

    fun getAllImageUrisInMemo(): List<String> {
        return currentList
            .filterIsInstance<MemoBlock.ImageUriBlock>()
            .flatMap { it.uris }
    }

    fun focusFirstEditableRow() {
        val rv = attachedRecyclerView ?: return
        val firstBodyPos = currentList.indexOfFirst { it is MemoBlock.BodyBlock }
        if (firstBodyPos == -1) return

        rv.post {
            rv.scrollToPosition(firstBodyPos)
            rv.post {
                val holder =
                    rv.findViewHolderForAdapterPosition(firstBodyPos) as? BodyBlockViewHolder
                        ?: return@post
                val et = holder.getRowEditText(0) ?: return@post
                et.requestFocus()
                et.setSelection(et.text?.length ?: 0)

                // 키보드 표시
                rv.showIme()
            }
        }
    }


    fun isImageSelectionMode(): Boolean = imageSelectionMode
    fun setImageSelectionMode(enabled: Boolean) {
        imageSelectionMode = enabled
        if (!enabled) {
            selectionCountByPos.clear()
            attachedRecyclerView?.let { rv ->
                for (i in 0 until rv.childCount) {
                    val holder = rv.getChildViewHolder(rv.getChildAt(i))
                    if (holder is ImageUriViewHolder) {
                        holder.setSelectionModeForImages(false)
                        holder.clearSelections()
                    }
                }
            }
        }
        // payload 대신 전체 notify로 안전하게
        notifyItemRangeChanged(0, itemCount)
    }


    internal fun updateSelectionCount(blockPos: Int, count: Int) {
        selectionCountByPos[blockPos] = count
    }

    fun deleteSelectedImages() {
        val rv = attachedRecyclerView ?: return
        // 화면에 붙어있는 이미지블록 홀더들에 지우기 요청
        for (i in 0 until rv.childCount) {
            val holder = rv.getChildViewHolder(rv.getChildAt(i))
            if (holder is ImageUriViewHolder) {
                holder.deleteSelectedFromThisBlock()
                selectionCountByPos[holder.bindingAdapterPosition] = 0
            }
        }
        // 비어진 이미지블록 제거
        val newBlocks = currentList.toMutableList()
        var changed = false
        for (i in newBlocks.size - 1 downTo 0) {
            val block = newBlocks[i]
            if (block is MemoBlock.ImageUriBlock && block.uris.isEmpty()) {
                newBlocks.removeAt(i)
                changed = true
            }
        }
        if (changed) submitBlockList(newBlocks.toList())

        // 전체 선택모드 종료 & 카운트 초기화
        setImageSelectionMode(false)
        notifyImagePresenceIfChanged()
    }

    fun submitBlockList(blocks: List<MemoBlock>) {
        super.submitList(blocks) {
            notifyImagePresenceIfChanged()
        }
    }


    fun hasAnyImageBlocks(): Boolean =
        currentList.any { it is MemoBlock.ImageUriBlock && it.uris.isNotEmpty() }

    private fun notifyImagePresenceIfChanged() {
        val has = hasAnyImageBlocks()
        if (has != lastHasImages) {
            lastHasImages = has
            onImagePresenceChanged?.invoke(has)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is MemoBlock.DateBlock -> VIEW_TYPE_DATE
        is MemoBlock.BodyBlock -> VIEW_TYPE_BODY
        is MemoBlock.ImageUriBlock -> VIEW_TYPE_IMAGE   // 변경
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DATE -> {
                val binding = ItemMemoDateBlockBinding.inflate(inflater, parent, false)
                DateBlockViewHolder(binding)
            }

            VIEW_TYPE_BODY -> {
                val binding = ItemMemoBodyBlockBinding.inflate(inflater, parent, false)
                BodyBlockViewHolder(binding)
            }

            VIEW_TYPE_IMAGE -> {
                val binding = ItemMemoImageBlockBinding.inflate(inflater, parent, false)
                ImageUriViewHolder(binding, this@MemoBlockAdapter)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (item) {
            is MemoBlock.DateBlock -> {
                if (position == 0) {
                    (holder as DateBlockViewHolder).bind(block = item, mode = currentMode)
                } else {
                    holder.itemView.isVisible = false
                }
            }

            is MemoBlock.BodyBlock -> {
                holder.itemView.isVisible = true
                (holder as BodyBlockViewHolder).bind(block = item, mode = currentMode)
            }

            is MemoBlock.ImageUriBlock -> {
                holder.itemView.isVisible = true
                (holder as ImageUriViewHolder).bind(item, currentMode)
                holder.setSelectionModeForImages(imageSelectionMode)
            }

            else -> {}
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains("payload_image_select_mode") && holder is ImageUriViewHolder) {
            // 선택 모드만 갱신 (전체 bind 불필요)
            holder.setSelectionModeForImages(imageSelectionMode)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = currentList.size


    inner class DateBlockViewHolder(val binding: ItemMemoDateBlockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(block: MemoBlock.DateBlock, mode: MemoMode) {
            with(binding.tvDate) {
                text = block.date
                when (mode) {
                    MemoMode.CREATE_MEMO -> {
                        binding.tvDate.isVisible = false
                    }


                    MemoMode.MODIFY_MEMO,
                    MemoMode.READ_MEMO -> {
                        binding.tvDate.isVisible = true
                    }

                    MemoMode.NONE -> {

                    }
                }
            }
        }
    }

    inner class BodyBlockViewHolder(val binding: ItemMemoBodyBlockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentTextWatcher: TextWatcher? = null

        private val container = binding.bodyRowContainer
        private var focusedRowIndex: Int = -1
        private var caretOffsetInRow: Int = -1
        private var handlingEnter = false
        private var handlingBackspace = false
        private var swappingRow = false  // 가드 플래그
        private var applyingTitleBold = false


        fun getRowEditText(rowIndex: Int): EditText? {
            val container = binding.bodyRowContainer
            val rowView = container.getChildAt(rowIndex) ?: return null
            return rowView.findViewById(R.id.etRowBody) as? EditText
        }

        fun focusRow(rowIndex: Int, toEnd: Boolean = true) {
            val et = getRowEditText(rowIndex) ?: return
            et.requestFocus()
            val len = et.text?.length ?: 0
            et.setSelection(if (toEnd) len else 0)
        }


        /** fromIndex가 속한 '연속된 CHECKBOX 구간'의 시작~끝 인덱스를 반환 */
        private fun findCheckboxRun(block: MemoBlock.BodyBlock, fromIndex: Int): Pair<Int, Int> {
            var s = fromIndex
            while (s > 0 && block.bodyRows[s - 1].type == BodyType.CHECKBOX) s--
            var e = fromIndex
            while (e < block.bodyRows.size - 1 && block.bodyRows[e + 1].type == BodyType.CHECKBOX) e++
            return s to e
        }

        /** 체크 상태에 따라 같은 구간 내에서 행을 재배치하고, 새 인덱스를 반환 */
        private fun resortCheckboxRowInRun(
            block: MemoBlock.BodyBlock,
            fromIndex: Int
        ): Int {
            if (fromIndex !in block.bodyRows.indices) return fromIndex
            val row = block.bodyRows[fromIndex]
            if (row.type != BodyType.CHECKBOX) return fromIndex

            val (runStart, runEnd) = findCheckboxRun(block, fromIndex)

            // 꺼낸다
            val removed = block.bodyRows.removeAt(fromIndex)

            // 제거 후 구간 끝 재계산
            val pivot =
                (fromIndex - 1).coerceAtLeast(runStart).coerceAtMost(block.bodyRows.lastIndex)
            val (ns, ne) = if (block.bodyRows.isEmpty()) (0 to -1) else findCheckboxRun(
                block,
                pivot
            )

            val insertIndex = if (removed.isChecked) {
                // ✓ 체크됨 → 구간의 맨 아래(= 마지막 체크박스 뒤)에 둔다
                ne + 1
            } else {
                // ✗ 체크 해제 → 구간에서 '첫 체크됨' 앞(= 미체크 영역의 맨 뒤)에 둔다
                val firstChecked = (ns..ne).firstOrNull { idx ->
                    block.bodyRows[idx].type == BodyType.CHECKBOX && block.bodyRows[idx].isChecked
                } ?: (ne + 1)
                firstChecked
            }

            val target = insertIndex.coerceIn(ns, (ne + 1).coerceAtMost(block.bodyRows.size))
            block.bodyRows.add(target, removed)
            return target
        }


        // MemoBlockAdapter 내부, 필드들 아래 아무 곳
        /** fromPos 기준, 그 이전에서 가장 가까운 BodyBlock 어댑터 포지션 찾기 (이미지/날짜는 건너뜀) */
        private fun nearestPrevBodyPos(fromPos: Int): Int? {
            for (i in fromPos - 1 downTo 0) if (getItem(i) is MemoBlock.BodyBlock) return i
            return null
        }

        /** targetPos의 BodyBlock 마지막 줄로 포커스 이동 */
        private fun focusLastRowOfBody(targetPos: Int) {
            val rv = attachedRecyclerView ?: return
            rv.post {
                rv.scrollToPosition(targetPos)
                rv.post {
                    val holder = rv.findViewHolderForAdapterPosition(targetPos) as? BodyBlockViewHolder ?: return@post
                    val block = getItem(targetPos) as? MemoBlock.BodyBlock ?: return@post
                    val lastRow = block.bodyRows.lastIndex.coerceAtLeast(0)
                    val et = holder.getRowEditText(lastRow) ?: return@post
                    et.requestFocus()
                    et.setSelection(et.text?.length ?: 0)
                    rv.showIme()
                }
            }
        }

        /** pos의 BodyBlock을 제거하고, (직전 블록이 Body일 때만 호출) 직전 Body로 포커스 */
        private fun deleteBodyAtAndFocusPrev(pos: Int) {
            val rv = attachedRecyclerView ?: return
            val prevPos = pos - 1
            val prevIsBody = prevPos >= 0 && getItem(prevPos) is MemoBlock.BodyBlock
            if (!prevIsBody) return

            val blocks = currentList.toMutableList()
            blocks.removeAt(pos)
            submitBlockList(blocks.toList())

            rv.post { focusLastRowOfBody(prevPos) }
        }


        @SuppressLint("ClickableViewAccessibility")
        fun bind(block: MemoBlock.BodyBlock, mode: MemoMode) {
            container.removeAllViews()

            val isFirstBodyBlock: Boolean =
                currentList.filterIsInstance<MemoBlock.BodyBlock>().indexOf(block) == 0

            block.bodyRows.forEachIndexed { index, row ->
                val isTitleLine = isFirstBodyBlock && index == 0
                val rowView = when (row.type) {
                    BodyType.TEXT -> createTextRow(index, row, mode, isTitleLine)
                    BodyType.CHECKBOX -> createCheckboxRow(index, row, mode, isTitleLine)
                }
                container.addView(rowView)
            }

        }


        /** 포커스된 행/커서 위치 알려주기 */
        fun getFocusedRowInfo(): Pair<Int, Int>? {
            return if (focusedRowIndex >= 0 && caretOffsetInRow >= 0) {
                focusedRowIndex to caretOffsetInRow
            } else null
        }

        /** 해당 행만 교체 (토글 시 깜빡임/포커스 유실 방지) */
        fun rebindRow(
            block: MemoBlock.BodyBlock,
            rowIndex: Int,
            keepFocus: Boolean,
            cursorOffset: Int
        ) {
            if (rowIndex < 0) return
            val row = block.bodyRows.getOrNull(rowIndex) ?: return

            val newView = when (row.type) {
                BodyType.TEXT -> createTextRow(
                    rowIndex = rowIndex,
                    row = row,
                    mode = currentMode,
                    isTitleLine = rowIndex == 0 && isFirstBody(block)
                )

                BodyType.CHECKBOX -> createCheckboxRow(
                    rowIndex = rowIndex,
                    row = row,
                    mode = currentMode,
                    isTitleLine = rowIndex == 0 && isFirstBody(block)
                )
            }

            val count = container.childCount
            when {
                rowIndex < count -> {
                    // 기존 뷰 교체
                    val old = container.getChildAt(rowIndex)
                    if (old != null) container.removeView(old)
                    container.addView(newView, rowIndex)
                }

                rowIndex == count -> {
                    // 새 행 추가 (맨 뒤에)
                    container.addView(newView)
                }

                else -> {
                    // 인덱스가 너무 크면 그냥 맨 뒤에 추가 (로그만 남김)
                    LogTrack.w(NAME) { "rebindRow: rowIndex=$rowIndex > childCount=$count, append" }
                    container.addView(newView)
                }
            }

            if (keepFocus) {
                val et = newView.findViewById<EditText>(R.id.etRowBody) ?: return
                // attach 후 포커스/커서 설정 (post로 안정화)
                newView.post {
                    et.requestFocus()
                    val sel = cursorOffset.coerceIn(0, et.text?.length ?: 0)
                    et.setSelection(sel)
                }
            }
        }


        private fun isFirstBody(block: MemoBlock.BodyBlock): Boolean {
            val firstIndex = currentList.indexOfFirst { it is MemoBlock.BodyBlock }
            return bindingAdapterPosition == firstIndex
        }

        private fun prevIndexByPreference(block: MemoBlock.BodyBlock, from: Int): Int? {
            if (from <= 0) return null
            // 1) 이전 텍스트 우선
            for (i in from - 1 downTo 0) {
                if (block.bodyRows[i].type == BodyType.TEXT) return i
            }
            // 2) 없으면 이전 체크박스
            for (i in from - 1 downTo 0) {
                if (block.bodyRows[i].type == BodyType.CHECKBOX) return i
            }
            return null
        }

        private fun createTextRow(
            rowIndex: Int,
            row: BodyRow,
            mode: MemoMode,
            isTitleLine: Boolean
        ): View {
            val et = CursorAwareEditText(itemView.context).apply {
                id = R.id.etRowBody
                // 스타일
                textSize = getMemoFontSize(activity)
                background = null
                includeFontPadding = false
                setLineSpacing(0f, 1.0f)
                setPadding(0, Util.dpToPx(itemView.context, 2), 0, Util.dpToPx(itemView.context, 2))
                minHeight = 0
                gravity = Gravity.TOP or Gravity.START

                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                isSingleLine = false
                isFocusableInTouchMode = true
                applyStrikeSpan(this, false)

                // 링크 + 태그 하이라이트(+첫 줄 Bold) 적용
                val displayed = composeDisplaySpannable(row.text, isTitleLine, mode)
                setText(displayed, TextView.BufferType.SPANNABLE)

                if ((mode == MemoMode.CREATE_MEMO || mode == MemoMode.MODIFY_MEMO)
                    && isTitleLine
                    && PreferenceUtil.get(KEY_TURN_OFF_TITLE, true)
                ) {
                    post { ensureTitleBold(this) }
                }

                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        currentFocusedPosition = bindingAdapterPosition
                        focusedRowIndex = rowIndex
                    }
                }
                setOnSelectionChangedListener { start, _ ->
                    if (hasFocus()) {
                        caretOffsetInRow = start
                        focusedRowIndex = rowIndex
                    }
                }

                addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                        row.text = s?.toString() ?: ""
                    }

                    override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        if (s.isNullOrEmpty()) return

                        // (1) 제목 Bold 보정
                        if ((currentMode == MemoMode.CREATE_MEMO || currentMode == MemoMode.MODIFY_MEMO)
                            && isTitleLine
                            && PreferenceUtil.get(KEY_TURN_OFF_TITLE, true)
                        ) {
                            val selStart = selectionStart
                            val selEnd = selectionEnd
                            ensureTitleBold(this@apply)
                            // 커서 복원
                            val len = text?.length ?: 0
                            setSelection(selStart.coerceIn(0, len), selEnd.coerceIn(0, len))
                        }

                        // (2) 태그 하이라이트 보정
                        s.getSpans(0, s.length, ForegroundColorSpan::class.java).forEach { span ->
                            s.removeSpan(span)
                        }
                        val tagPattern = Regex("#[\\w가-힣]+")
                        val tagColor = ContextCompat.getColor(context, R.color.haru_primary_orange)
                        tagPattern.findAll(s).forEach { match ->
                            s.setSpan(
                                ForegroundColorSpan(tagColor),
                                match.range.first,
                                match.range.last + 1,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                            )
                        }
                    }
                })

                setOnKeyListener { _, keyCode, event ->
                    // 1) 불릿 자동 동작
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                        val caret = selectionStart.coerceAtLeast(0)
                        val full = text?.toString().orEmpty()

                        // 현재 줄 범위
                        val lineStart = full.lastIndexOf('\n', (caret - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
                        val lineEnd = full.indexOf('\n', caret).let { if (it == -1) full.length else it }
                        val currentLine = full.substring(lineStart, lineEnd)

                        if (currentLine.startsWith(BULLET)) {
                            val afterBullet = currentLine.substring(BULLET.length)

                            if (afterBullet.isBlank()) {
                                // (엔터 2번) 비어있는 불릿 줄에서 엔터 → 불릿 제거 + 일반 빈 줄로 전환
                                val e = this@apply.text ?: return@setOnKeyListener true
                                // 불릿 접두사 삭제
                                e.delete(lineStart, lineStart + BULLET.length)
                                // 커서 위치는 BULLET 삭제만큼 앞으로 당겨짐
                                val caretNow = (caret - BULLET.length).coerceAtLeast(lineStart)
                                // 일반 개행 삽입 (불릿 없이)
                                e.insert(caretNow, "\n")
                                setSelection(caretNow + 1)
                                return@setOnKeyListener true
                            } else {
                                // (엔터 1번) 내용 있는 불릿 줄에서 엔터 → 다음 줄에 새 불릿
                                val insert = "\n$BULLET"
                                this@apply.text?.insert(caret, insert)
                                setSelection(caret + insert.length)
                                return@setOnKeyListener true
                            }
                        }
                        // 불릿이 아니면 기본 개행 동작으로 넘김
                    }

                    if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                        if (handlingBackspace) return@setOnKeyListener true
                        handlingBackspace = true
                        try {
                            val caret = selectionStart.coerceAtLeast(0)
                            val curText = text?.toString().orEmpty()

                            if (caret == 0 && curText.isEmpty()) {
                                val pos = bindingAdapterPosition
                                if (pos == RecyclerView.NO_POSITION) return@setOnKeyListener true
                                val parentBlock = (currentList.getOrNull(pos) as? MemoBlock.BodyBlock) ?: return@setOnKeyListener true

                                // 이 바디블록이 '빈 한 줄'뿐일 때만 블록 단위 처리
                                if (parentBlock.bodyRows.size == 1) {
                                    val hasPrev = pos - 1 >= 0
                                    if (!hasPrev) {
                                        // 이전 블록이 없음 → 유지
                                        return@setOnKeyListener true
                                    }
                                    val prevItem = getItem(pos - 1)
                                    when (prevItem) {
                                        is MemoBlock.BodyBlock -> {
                                            // 규칙 1) 직전이 Body → 현재 블록 삭제 + 직전 Body로 포커스
                                            deleteBodyAtAndFocusPrev(pos)
                                        }

                                        is MemoBlock.ImageUriBlock -> {
                                            // 규칙 2) 직전이 Image → 현재 블록은 유지, 이미지보다 더 이전의 Body로 포커스만
                                            val target = nearestPrevBodyPos(pos - 1)
                                            if (target != null) {
                                                focusLastRowOfBody(target)
                                            } else {
                                                // 규칙 3) 더 이전에도 Body 없음 → 유지
                                            }
                                        }

                                        else -> {
                                            // Date 등 → 유지
                                        }
                                    }
                                    return@setOnKeyListener true
                                }

                                // 블록에 여러 줄이면: 기존 "행 삭제 후 이전 행 끝으로" 로직 유지
                                val prevIndex = (rowIndex - 1).coerceAtLeast(0)
                                parentBlock.bodyRows.removeAt(rowIndex)
                                val caretTo = parentBlock.bodyRows.getOrNull(prevIndex)?.text?.length ?: 0
                                rebuildAllRows(parentBlock, focusRow = prevIndex, caretOffset = caretTo)
                                return@setOnKeyListener true
                            }
                        } finally {
                            handlingBackspace = false
                        }
                    }
                    false
                }

                setMemoTouchListener(editText = this@apply, mode = currentMode) {
                    onRequestEdit?.invoke(bindingAdapterPosition, rowIndex)
                }
            }
            return et
        }

        private fun applyCheckIcon(iv: ImageView, checked: Boolean) {
            iv.setImageResource(
                if (checked) R.drawable.ic_checkbox_item_p
                else R.drawable.ic_checkbox_item_n
            )
        }

        private fun applyStrikeSpan(et: EditText, checked: Boolean) {
            val e = et.text ?: return
            // 기존 취소선 제거
            e.getSpans(0, e.length, StrikethroughSpan::class.java).forEach { e.removeSpan(it) }

            if (checked) {
                if (e.isEmpty()) {
                    // 비어있어도 0..0에 INCLUSIVE_INCLUSIVE로 깔아두면
                    // 이후 입력이 들어올 때 자동으로 스팬이 확장되어 취소선 유지됨
                    e.setSpan(
                        StrikethroughSpan(),
                        0, 0,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                } else {
                    e.setSpan(
                        StrikethroughSpan(),
                        0, e.length,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            }
        }

        private fun createCheckboxRow(
            rowIndex: Int,
            row: BodyRow,
            mode: MemoMode,
            isTitleLine: Boolean
        ): View {
            val container = LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
                isBaselineAligned = false
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = Util.dpToPx(context, 2)
                    bottomMargin = Util.dpToPx(context, 2)
                }
                setPadding(0, 0, 0, 0)
            }

            val et = CursorAwareEditText(itemView.context).apply {
                id = R.id.etRowBody

                setTextSize(TypedValue.COMPLEX_UNIT_SP, getMemoFontSize(context))
                background = null
                includeFontPadding = false
                setLineSpacing(0f, 1.0f)
                setPadding(0, Util.dpToPx(context, 2), 0, Util.dpToPx(context, 2))
                minHeight = 0
                gravity = Gravity.TOP or Gravity.START

                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                isSingleLine = false
                isFocusableInTouchMode = true

                // 체크박스 행도 태그 색상(READ에서 링크 포함) 적용
                val displayed =
                    composeDisplaySpannable(text = row.text, isTitleLine = isTitleLine, mode = mode)
                setText(displayed, TextView.BufferType.SPANNABLE)
                if ((mode == MemoMode.CREATE_MEMO || mode == MemoMode.MODIFY_MEMO)
                    && isTitleLine
                    && PreferenceUtil.get(KEY_TURN_OFF_TITLE, true)
                ) {
                    post { ensureTitleBold(this) }
                }

                applyStrikeSpan(this, row.isChecked)

                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        currentFocusedPosition = bindingAdapterPosition
                        focusedRowIndex = rowIndex
                    }
                }
                setOnSelectionChangedListener { start, _ ->
                    if (hasFocus()) {
                        caretOffsetInRow = start
                        focusedRowIndex = rowIndex
                    }
                }

                addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                        row.text = s?.toString() ?: ""
                    }

                    override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        if (s.isNullOrEmpty()) return

                        // (1) 제목 Bold 보정
                        if ((currentMode == MemoMode.CREATE_MEMO || currentMode == MemoMode.MODIFY_MEMO)
                            && isTitleLine
                            && PreferenceUtil.get(KEY_TURN_OFF_TITLE, true)
                        ) {
                            val selStart = selectionStart
                            val selEnd = selectionEnd
                            ensureTitleBold(this@apply)
                            val len = text?.length ?: 0
                            setSelection(selStart.coerceIn(0, len), selEnd.coerceIn(0, len))
                        }

                        // (2) 체크박스 취소선 보정
                        if (row.isChecked) {
                            val e = this@apply.text ?: return
                            e.getSpans(0, e.length, StrikethroughSpan::class.java).forEach { e.removeSpan(it) }
                            e.setSpan(
                                StrikethroughSpan(),
                                0, e.length,
                                Spannable.SPAN_INCLUSIVE_INCLUSIVE
                            )
                        }

                        // (3) 태그 하이라이트 보정
                        s.getSpans(0, s.length, ForegroundColorSpan::class.java).forEach { span ->
                            s.removeSpan(span)
                        }
                        val tagPattern = Regex("#[\\w가-힣]+")
                        val tagColor = ContextCompat.getColor(context, R.color.haru_primary_orange)
                        tagPattern.findAll(s).forEach { match ->
                            s.setSpan(
                                ForegroundColorSpan(tagColor),
                                match.range.first,
                                match.range.last + 1,
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                            )
                        }
                    }
                })

                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                        val pos = bindingAdapterPosition
                        if (pos == RecyclerView.NO_POSITION) return@setOnKeyListener true
                        val parentBlock = (currentList.getOrNull(pos) as? MemoBlock.BodyBlock)
                            ?: return@setOnKeyListener true

                        val cursor = selectionStart.coerceAtLeast(0)
                        val full = text?.toString().orEmpty()

                        if (row.type == BodyType.CHECKBOX && full.isBlank()) {
                            row.type = BodyType.TEXT
                            row.isChecked = false
                            // 같은 줄에 그대로 포커스 유지
                            rebindRow(parentBlock, rowIndex, keepFocus = true, cursorOffset = 0)
                            return@setOnKeyListener true
                        }

                        // (체크박스 + 내용 있음) Enter → 현재 줄을 분할하고,
                        // 새 줄은 "체크박스"로 만들어서 아래에 추가
                        val before = full.substring(0, cursor)
                        val after = full.substring(cursor)

                        row.text = before
                        val insertIndex = rowIndex + 1
                        val nextType = if (row.text.isBlank()) BodyType.TEXT else BodyType.CHECKBOX

                        parentBlock.bodyRows.add(
                            insertIndex,
                            BodyRow(text = after, type = nextType, isChecked = false)
                        )

                        // 부분 교체(rebindRow) 대신 전체 재구성으로 뷰/데이터 동기화 보장
                        rebuildAllRows(parentBlock, focusRow = insertIndex, caretOffset = 0)
                        return@setOnKeyListener true
                    }

                    if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                        if (handlingBackspace) return@setOnKeyListener true
                        handlingBackspace = true
                        try {
                            val caret = selectionStart.coerceAtLeast(0)
                            if (caret == 0 && text?.isEmpty() == true) {
                                val pos = bindingAdapterPosition
                                if (pos == RecyclerView.NO_POSITION) return@setOnKeyListener true
                                val parentBlock = (currentList.getOrNull(pos) as? MemoBlock.BodyBlock)
                                    ?: return@setOnKeyListener true

                                // ──  블록이 '빈 한 줄'뿐일 때: 블록 단위 규칙 우선 적용 ──
                                if (parentBlock.bodyRows.size == 1) {
                                    val hasPrev = pos - 1 >= 0
                                    if (!hasPrev) {
                                        // (규칙3) 이전 블록 없음 → 유지. 필요 시 체크박스를 TEXT로만 전환
                                        row.type = BodyType.TEXT
                                        row.isChecked = false
                                        rebindRow(parentBlock, rowIndex, keepFocus = true, cursorOffset = 0)
                                        return@setOnKeyListener true
                                    }

                                    val prevItem = getItem(pos - 1)
                                    when (prevItem) {
                                        is MemoBlock.BodyBlock -> {
                                            // (규칙1) 직전이 Body → 현재 바디블록 삭제 + 직전 Body 마지막 줄로 포커스
                                            deleteBodyAtAndFocusPrev(pos)
                                        }

                                        is MemoBlock.ImageUriBlock -> {
                                            // (규칙2) 직전이 Image → 현재 바디블록은 유지, 이미지보다 더 이전 Body로 포커스만
                                            val target = nearestPrevBodyPos(pos - 1)
                                            if (target != null) {
                                                focusLastRowOfBody(target)
                                            } else {
                                                // 더 이전 Body 없음 → 유지. 필요 시 체크박스를 TEXT로만 전환
                                                row.type = BodyType.TEXT
                                                row.isChecked = false
                                                rebindRow(parentBlock, rowIndex, keepFocus = true, cursorOffset = 0)
                                            }
                                        }

                                        else -> {
                                            // Date 등 → 유지. 필요 시 체크박스를 TEXT로만 전환
                                            row.type = BodyType.TEXT
                                            row.isChecked = false
                                            rebindRow(parentBlock, rowIndex, keepFocus = true, cursorOffset = 0)
                                        }
                                    }
                                    return@setOnKeyListener true
                                }

                                // ── 블록에 여러 줄 이상일 때: 기존 행 수준 처리 유지 ──
                                // 1) 빈 체크박스 → 체크박스만 TEXT로 변환 (블록은 유지)
                                if (row.type == BodyType.CHECKBOX) {
                                    row.type = BodyType.TEXT
                                    row.isChecked = false
                                    rebindRow(parentBlock, rowIndex, keepFocus = true, cursorOffset = 0)
                                    return@setOnKeyListener true
                                }

                                // 2) 빈 TEXT 줄 → 행 삭제 + 이전 행 끝으로 포커스
                                val prevIndex = rowIndex - 1
                                if (prevIndex >= 0) {
                                    parentBlock.bodyRows.removeAt(rowIndex)
                                    val caretTo = parentBlock.bodyRows.getOrNull(prevIndex)?.text?.length ?: 0
                                    rebuildAllRows(parentBlock, focusRow = prevIndex, caretOffset = caretTo)
                                } else {
                                    parentBlock.bodyRows.removeAt(rowIndex)
                                    if (parentBlock.bodyRows.isEmpty()) {
                                        parentBlock.bodyRows.add(
                                            BodyRow(text = "", type = BodyType.TEXT, isChecked = false)
                                        )
                                    }
                                    rebuildAllRows(parentBlock, focusRow = 0, caretOffset = 0)
                                }
                                return@setOnKeyListener true
                            }
                        } finally {
                            handlingBackspace = false
                        }
                    }
                    false
                }

                setMemoTouchListener(editText = this@apply, mode = currentMode) {
                    onRequestEdit?.invoke(bindingAdapterPosition, rowIndex)
                }
            }

            val iv = AppCompatImageView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    Util.dpToPx(context, 18),
                    Util.dpToPx(context, 18)
                ).apply {
                    rightMargin = Util.dpToPx(context, 6)
                }
                isClickable = true
                isFocusable = false
                applyCheckIcon(this, row.isChecked)
                setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                    if (currentMode == MemoMode.READ_MEMO) {
                        onRequestEdit?.invoke(pos, rowIndex)
                        return@setOnClickListener
                    }

                    // 현재 커서 위치(있다면) 보존
                    val hadFocus = et.hasFocus()
                    val caret = et.selectionStart.coerceAtLeast(0)

                    //  토글 직전/직후 상태 저장
                    val wasChecked = row.isChecked
                    row.isChecked = !wasChecked
                    applyCheckIcon(this, row.isChecked)
                    applyStrikeSpan(et = et, row.isChecked)


                    if (PreferenceUtil.get(KEY_CHECKED_MEMO_AUTO_SORT, false)) {
                        val block = (currentList.getOrNull(pos) as? MemoBlock.BodyBlock) ?: return@setOnClickListener

                        // 정렬 전, 현재 체크박스 행들의 '옛 top' 좌표 캡처
                        val oldTopMap = captureOldTopMapForCheckboxRows(block)

                        val clickedRef = row
                        val movedToChecked = (!wasChecked && row.isChecked)

                        // 전체 run 정렬(너가 적용해 둔 함수)
                        ensureAllCheckboxRunsSorted(block, clickedRef, movedToChecked)

                        // 새 위치로 뷰를 만들고(=rebuild), 포커스 복원
                        if (hadFocus) {
                            val newIndex = block.bodyRows.indexOfFirst { it === clickedRef }
                                .takeIf { it >= 0 } ?: rowIndex
                            rebuildAllRows(block, focusRow = newIndex, caretOffset = caret)
                        } else {
                            rebuildAllRows(block, focusRow = null)
                        }

                        //  정렬/리빌드 후, 이동 애니메이션 적용
                        animateCheckboxReorder(block, oldTopMap)
                    }
                }


                ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
                    override fun onInitializeAccessibilityNodeInfo(
                        host: View,
                        info: AccessibilityNodeInfoCompat
                    ) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = "android.widget.CheckBox"
                        info.isCheckable = true
                        info.isChecked = row.isChecked
                    }
                })
            }

            container.addView(iv)
            container.addView(
                et,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
            alignCheckboxCenterByBaseline(et, iv, iconDp = 18)

            return container
        }

        // MemoBlockAdapter.BodyBlockViewHolder 내부

        /** 멀티라인 row를 커서 기준으로 분할해 커서 줄만 단독 BodyRow로 만든다.
         *  return: (새로운 rowIndex, 새로운 caretOffset)
         */
        fun isolateCaretLine(
            block: MemoBlock.BodyBlock,
            rowIndex: Int,
            et: EditText
        ): Pair<Int, Int> {
            val full = et.text?.toString().orEmpty()
            if (!full.contains('\n')) return rowIndex to et.selectionStart.coerceAtLeast(0)

            val caret = et.selectionStart.coerceAtLeast(0)

            val lineStart = full.lastIndexOf('\n', (caret - 1).coerceAtLeast(0))
                .let { if (it == -1) 0 else it + 1 }
            val lineEnd = full.indexOf('\n', caret).let { if (it == -1) full.length else it }

            // 🔧 엔터 손실 방지: 그대로 보존
            val beforePart = full.substring(0, lineStart)   // 앞쪽 포함(\n까지)
            val currentLine = full.substring(lineStart, lineEnd)
            val afterPart = full.substring(lineEnd)         // 뒤쪽(\n 포함 가능)

            // 현재 row를 현재 라인으로 교체
            block.bodyRows[rowIndex].apply {
                text = currentLine
            }

            // 앞부분을 위로 (빈 라인도 TEXT로 보존)
            var insertAt = rowIndex
            if (beforePart.isNotEmpty()) {
                val beforeLines = beforePart.split('\n', ignoreCase = false, limit = 0)
                // 마지막 조각은 항상 "" 이므로, 그 직전까지 위로 삽입
                beforeLines.dropLast(1).forEachIndexed { idx, s ->
                    block.bodyRows.add(rowIndex + idx, BodyRow(text = s, type = BodyType.TEXT))
                }
                insertAt = rowIndex + (beforeLines.size - 1).coerceAtLeast(0)
            }

            // 뒷부분을 아래로 (빈 라인도 보존)
            if (afterPart.isNotEmpty()) {
                val afterLines = afterPart.split('\n', ignoreCase = false, limit = 0)
                afterLines.forEachIndexed { idx, s ->
                    block.bodyRows.add(insertAt + 1 + idx, BodyRow(text = s, type = BodyType.TEXT))
                }
            }

            rebuildAllRows(block, focusRow = insertAt, caretOffset = (caret - lineStart))
            return insertAt to (caret - lineStart)
        }


        /** 같은 BodyBlock 내의 모든 체크박스 run(연속 구간)을 '미체크 → 체크' 순으로 정렬한다.
         *  단, 이번 클릭으로 체크로 바뀐 줄(clickedRef)은 그 run의 '체크 영역' 맨 끝으로 보낸다.
         */
        private fun ensureAllCheckboxRunsSorted(
            block: MemoBlock.BodyBlock,
            clickedRef: BodyRow?,                // 이번에 탭한 행(객체 참조)
            clickedMovedToChecked: Boolean       // 이번 탭으로 '체크됨' 상태가 되었는지
        ) {
            val rows = block.bodyRows
            var i = 0
            while (i < rows.size) {
                if (rows[i].type == BodyType.CHECKBOX) {
                    val (s, e) = findCheckboxRun(block, i) // 연속 체크박스 구간
                    val run = rows.subList(s, e + 1).toMutableList()

                    val unchecked = run.filter { it.type == BodyType.CHECKBOX && !it.isChecked }
                    var checked = run.filter { it.type == BodyType.CHECKBOX && it.isChecked }

                    //  이번에 '체크됨'으로 바뀐 줄은 체크영역의 맨 뒤로 보내기
                    if (clickedMovedToChecked && clickedRef != null && run.any { it === clickedRef }) {
                        checked = checked.filter { it !== clickedRef } + clickedRef
                    }
                    // (체크 해제의 경우는 기존 순서 유지)

                    val newOrder = unchecked + checked
                    for (k in newOrder.indices) {
                        rows[s + k] = newOrder[k]
                    }
                    i = e + 1
                } else {
                    i++
                }
            }
        }


        /** 정렬 직전, 현재 컨테이너에서 '체크박스 행들'의 top 좌표를 BodyRow 참조로 저장 */
        private fun captureOldTopMapForCheckboxRows(
            block: MemoBlock.BodyBlock
        ): Map<BodyRow, Int> {
            val map = HashMap<BodyRow, Int>()
            val rows = block.bodyRows
            val childCount = container.childCount
            val n = minOf(rows.size, childCount)
            for (i in 0 until n) {
                val r = rows[i]
                if (r.type == BodyType.CHECKBOX) {
                    val child = container.getChildAt(i) ?: continue
                    map[r] = child.top // 컨테이너 기준 상대좌표
                }
            }
            return map
        }

        /** 정렬/리빌드 직후, 체크박스 행들에 '이동 애니메이션' 적용 */
        private fun animateCheckboxReorder(
            block: MemoBlock.BodyBlock,
            oldTopMap: Map<BodyRow, Int>,
            durationMs: Long = 220L
        ) {
            // container는 새 순서로 이미 rebind/rebuild된 상태여야 함
            container.post {
                val decel = android.view.animation.DecelerateInterpolator()
                val rows = block.bodyRows
                val childCount = container.childCount
                val n = minOf(rows.size, childCount)

                for (i in 0 until n) {
                    val r = rows[i]
                    if (r.type != BodyType.CHECKBOX) continue
                    val v = container.getChildAt(i) ?: continue

                    val oldTop = oldTopMap[r] ?: continue
                    val newTop = v.top
                    val dy = (oldTop - newTop).toFloat()
                    if (dy == 0f) continue

                    // 새 뷰를 '예전 위치'에서 시작하도록 설정
                    v.translationY = dy
                    v.alpha = 0f
                    v.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(durationMs)
                        .setInterpolator(decel)
                        .start()
                }
            }
        }


        /** 해당 BodyBlock의 모든 행을 다시 그림 (필요 시 포커스 복원) */
        fun rebuildAllRows(
            block: MemoBlock.BodyBlock,
            focusRow: Int? = null,
            caretOffset: Int = 0
        ) {
            val isFirst = isFirstBody(block)
            val rows = block.bodyRows

            container.removeAllViews()
            rows.forEachIndexed { idx, r ->
                val v = when (r.type) {
                    BodyType.TEXT -> createTextRow(
                        rowIndex = idx,
                        row = r,
                        mode = currentMode,
                        isTitleLine = (idx == 0 && isFirst)
                    )

                    BodyType.CHECKBOX -> createCheckboxRow(
                        rowIndex = idx,
                        row = r,
                        mode = currentMode,
                        isTitleLine = (idx == 0 && isFirst)
                    )
                }
                container.addView(v)
            }

            if (focusRow != null) {
                val et = getRowEditText(focusRow)
                et?.post {
                    et.requestFocus()
                    val sel = caretOffset.coerceIn(0, et.text?.length ?: 0)
                    et.setSelection(sel)
                }
            }
        }

        /** READ 모드에서만, 그리고 QUICK_PAGE 설정이 true일 때만 링크 하이라이트 적용.
         *  태그 주황색과 첫 줄 Bold(설정 on)는 그대로 유지.
         */
        private fun composeDisplaySpannable(
            text: String,
            isTitleLine: Boolean,
            mode: MemoMode
        ): SpannableStringBuilder {
            // 링크 하이라이트 여부: READ 모드 + 설정값
            val enableLinkHighlight =
                (mode == MemoMode.READ_MEMO) && PreferenceUtil.get(KEY_QUICK_PAGE, true)

            // 링크 스팬 적용 여부에 따라 베이스 만들기
            val base = if (enableLinkHighlight) {
                SpannableStringBuilder(makeHyperlinkedSpannable(text))
            } else {
                SpannableStringBuilder(text)
            }

            // 첫 BodyBlock의 첫 줄 제목 Bold (설정 on일 때)
            if (isTitleLine && PreferenceUtil.get(KEY_TURN_OFF_TITLE, true)) {
                val firstEnd = text.indexOf('\n').let { if (it == -1) text.length else it }
                base.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    firstEnd,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }

            // 태그(#...) 하이라이트(항상 적용)
            val tagPattern = Regex("#[\\w가-힣]+")
            val tagColor = ContextCompat.getColor(itemView.context, R.color.haru_primary_orange)
            tagPattern.findAll(text).forEach { match ->
                base.setSpan(
                    ForegroundColorSpan(tagColor),
                    match.range.first,
                    match.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }

            return base
        }

        private fun ensureTitleBold(et: EditText) {
            if (applyingTitleBold) return
            applyingTitleBold = true
            try {
                val e = et.text ?: return
                // 기존 제목용 Bold(시작이 0인 Bold만) 제거
                e.getSpans(0, e.length, StyleSpan::class.java)
                    .filter { it.style == Typeface.BOLD && e.getSpanStart(it) == 0 }
                    .forEach { e.removeSpan(it) }

                // 첫 줄 끝 계산
                val s = e.toString()
                val firstEnd = s.indexOf('\n').let { if (it == -1) e.length else it }

                if (firstEnd > 0) {
                    e.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        firstEnd,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            } finally {
                applyingTitleBold = false
            }
        }


    }

    inner class ImageUriViewHolder(
        val binding: ItemMemoImageBlockBinding,
        private val memoBlockAdapter: MemoBlockAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        private val imageAdapter by lazy {
            BlockItemImageGridAdapter(
                activity = activity,
                parentAdapter = memoBlockAdapter
            )
        }
        private var currentUris: MutableList<String> = mutableListOf()

        // export 전용 컨테이너
        private val EXPORT_TAG = "export_container"

        init {
            val spanCount = 2
            val itemSpacingPx = Util.dpToPx(activity, 6)

            with(binding.recyclerViewImages) {
                layoutManager = GridLayoutManager(context, spanCount)
                adapter = imageAdapter
                addItemDecoration(
                    GridSpacingItemDecoration(
                        spanCount = spanCount,
                        spacing = itemSpacingPx
                    )
                )
            }
        }

        fun bind(block: MemoBlock.ImageUriBlock, mode: MemoMode) {
            currentUris = block.uris.toMutableList()

            // Export 모드
            if (isExporting) {
                renderExportGrid(block.uris)
                binding.recyclerViewImages.isVisible = false
                return
            }

            // 일반 모드
            binding.recyclerViewImages.isVisible = true
            removeExportContainerIfAny()

            // Drag & Drop 활성화 여부
            if (mode == MemoMode.CREATE_MEMO || mode == MemoMode.MODIFY_MEMO) {
                val touchHelper = createImageTouchHelper(block) // 매번 새로 생성
                touchHelper.attachToRecyclerView(binding.recyclerViewImages)
            } else {
                // 읽기 모드에서는 드래그 불가
                binding.recyclerViewImages.itemAnimator = null
            }

            // Uri 리스트 반영
            imageAdapter.submitList(currentUris.map { uri ->
                BitmapImage(uri = uri, mode = currentMode)
            })

            // 콜백 연결
            imageAdapter.setOnImageListChanged { newUris ->
                block.uris = newUris.toMutableList()
                notifyImagePresenceIfChanged()
            }

            imageAdapter.setOnSelectionChanged { count ->
                updateSelectionCount(bindingAdapterPosition, count)
            }

            // 선택 모드 상태를 항상 반영
            imageAdapter.setSelectionMode(imageSelectionMode)
        }

        fun setSelectionModeForImages(enabled: Boolean) {
            LogTrack.i { "ImageUriViewHolder -> setSelectionModeForImages: $enabled" }
            imageAdapter.setSelectionMode(enabled)
        }

        fun deleteSelectedFromThisBlock() {
            imageAdapter.deleteSelected()
        }

        fun clearSelections() {
            imageAdapter.clearSelections()
        }

        private fun removeExportContainerIfAny() {
            val root = binding.root as ViewGroup
            val old = root.findViewWithTag<View>(EXPORT_TAG)
            if (old != null) root.removeView(old)
        }

        private fun renderExportGrid(uris: List<String>) {
            val context = binding.root.context
            val root = binding.root as ViewGroup

            removeExportContainerIfAny()

            val grid = GridLayout(context).apply {
                tag = EXPORT_TAG
                columnCount = 2
                val pad = Util.dpToPx(context, 6)
                setPadding(0, pad, 0, pad)
            }

            val spacing = Util.dpToPx(context, 6)
            val targetHeightPx = Util.dpToPx(context, 150)

            uris.forEach { uri ->
                val imageView = AppCompatImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = targetHeightPx
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        setMargins(spacing, spacing, spacing, spacing)
                    }
                }
                Glide.with(context)
                    .load(uri)
                    .override(ViewGroup.LayoutParams.MATCH_PARENT, targetHeightPx)
                    .centerCrop()
                    .into(imageView)

                grid.addView(imageView)
            }

            if (uris.size == 1) {
                val dummyView = View(context).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = targetHeightPx
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    }
                    setBackgroundColor(Color.TRANSPARENT)
                }
                grid.addView(dummyView)
            }

            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            root.addView(grid, params)
        }

        private fun createImageTouchHelper(block: MemoBlock.ImageUriBlock): ItemTouchHelper {
            return ItemTouchHelper(object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int {
                    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    return makeMovementFlags(dragFlags, 0)
                }

                override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    val from = vh.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    if (from != RecyclerView.NO_POSITION && to != RecyclerView.NO_POSITION) {
                        val moved = currentUris.removeAt(from)
                        currentUris.add(to, moved)
                        imageAdapter.submitList(currentUris.map { uri ->
                            BitmapImage(uri = uri, mode = currentMode)
                        })
                        block.uris = currentUris.toMutableList()
                    }
                    VibrationUtil.vibrate(context = activity)
                    return true
                }

                override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {}
                override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                    super.clearView(rv, vh)
                    AnimationUtil.scaleReset(vh.itemView.findViewById(R.id.ivGridImage))
                }

                override fun isLongPressDragEnabled(): Boolean = true
                override fun onSelectedChanged(vh: RecyclerView.ViewHolder?, actionState: Int) {
                    super.onSelectedChanged(vh, actionState)
                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && vh != null) {
                        AnimationUtil.scaleDown(vh.itemView.findViewById(R.id.ivGridImage))
                        VibrationUtil.vibrate(context = activity)
                    }
                }
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setMemoTouchListener(
        editText: EditText?,
        mode: MemoMode,
        onTouch: () -> Unit // ← 읽기모드에서 "수정 요청"을 던지는 콜백 (액티비티가 setModeView + 포커스 처리)
    ) {
        if (!activity.isAlive || editText == null) return

        val context = editText.context
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        var downY = 0f

        val isQuickPageEnabled = PreferenceUtil.get(KEY_QUICK_PAGE, true)

        // READ 모드에서는 선택/포커스/롱클릭을 막아 텍스트 선택이 뜨지 않도록
        if (mode == MemoMode.READ_MEMO) {
            editText.isFocusable = false
            editText.isFocusableInTouchMode = false
            editText.isCursorVisible = false
            editText.isLongClickable = false
            editText.setTextIsSelectable(false)
        } else {
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
            editText.isCursorVisible = true
            editText.isLongClickable = true
            editText.setTextIsSelectable(true)
        }

        editText.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.y
                    false // 스크롤 판단 위해 통과
                }

                MotionEvent.ACTION_UP -> {
                    val et = v as? EditText ?: return@setOnTouchListener false
                    val click = kotlin.math.abs(event.y - downY) < touchSlop

                    // 링크 퀵페이지 처리 (READ 모드에서만)
                    if (mode == MemoMode.READ_MEMO && isQuickPageEnabled && click) {
                        val layout = et.layout ?: return@setOnTouchListener false
                        val x = event.x.toInt().coerceIn(0, et.width)
                        val y = event.y.toInt().coerceIn(0, et.height)
                        val line = layout.getLineForVertical(y)
                        val offset = layout.getOffsetForHorizontal(line, x.toFloat())

                        val text = et.text
                        val spans = text.getSpans(offset, offset, URLSpan::class.java)
                        for (span in spans) {
                            val start = text.getSpanStart(span)
                            val end = text.getSpanEnd(span)
                            if (offset in start..end) {
                                val link = span.url
                                when {
                                    link.startsWith("tel:") -> showPhoneDialog(
                                        context,
                                        link.removePrefix("tel:")
                                    )

                                    link.startsWith("mailto:") -> showEmailDialog(
                                        context,
                                        link.removePrefix("mailto:")
                                    )

                                    Patterns.WEB_URL.matcher(link).matches() -> {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                link.toUri()
                                            )
                                        )
                                    }
                                }
                                return@setOnTouchListener true // 링크를 처리했으니 소비
                            }
                        }
                    }

                    // 로그 (선택)
                    LogTrack.i(NAME) {
                        "setMemoTouchListener::UP { mode=$mode, quick=$isQuickPageEnabled, click=$click }"
                    }

                    if (mode == MemoMode.READ_MEMO) {
                        // 🔸 READ: 짧은 탭이면 "수정 요청" 콜백만 날림. 실제 모드전환/포커스는 액티비티가 담당.
                        if (click) {
                            onTouch()
                            return@setOnTouchListener true // 선택/하이라이트 방지
                        }
                        // 드래그 등은 스크롤로 넘김
                        return@setOnTouchListener false
                    } else {
                        // 🔸 MODIFY/CREATE: 기존 동작 유지
                        if (click) {
                            if (!et.hasFocus()) {
                                et.enterCursor() // 네가 쓰던 확장함수
                            }
                            onTouch() // 필요 시 커서 이동/포커스 처리용
                        } else {
                            et.exitCursor()
                            activity.hideKeyboard()
                        }
                        return@setOnTouchListener false
                    }
                }

                else -> false
            }
        }
    }


    fun makeHyperlinkedSpannable(
        originalText: String,
        enablePhone: Boolean = true,
        enableEmail: Boolean = true,
        enableWeb: Boolean = true
    ): SpannableString {
        val spannable = SpannableString(originalText)

        if (originalText.isBlank()) return spannable

        val phonePattern = Pattern.compile("\\b\\d{3}-?\\d{3,4}-?\\d{4}\\b")
        val phoneMatchFilter = Linkify.MatchFilter { s, start, end ->
            val segment = s.subSequence(start, end).toString()
            segment.matches(Regex("\\d{3}-?\\d{3,4}-?\\d{4}"))
        }

        if (enableWeb || enableEmail) {
            var mask = 0
            if (enableWeb) mask = mask or Linkify.WEB_URLS
            if (enableEmail) mask = mask or Linkify.EMAIL_ADDRESSES
            Linkify.addLinks(spannable, mask)
        }

        if (enablePhone) {
            Linkify.addLinks(spannable, phonePattern, "tel:", phoneMatchFilter, null)
        }

        // 링크 색상 변경 (#F7A208)
        val linkColor = "#B5A8FF".toColorInt()
        val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
        spans.forEach { span ->
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)

            // 기존 URLSpan은 그대로 두되 색상만 덧씌움
            spannable.setSpan(
                ForegroundColorSpan(linkColor),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }


    // region { 메모 내 검색 }
    fun applySearchKeyword(keyword: String): Int {
        // 1) 기존 하이라이트 제거 (키워드/포지션은 초기화하지 않음)
        clearHighlightSpansOnly()

        matchPositions.clear()
        currentMatchIndex = 0
        currentKeyword = keyword

        if (keyword.isBlank()) return 0

        // 2) 검색 매치 계산
        for (i in currentList.indices) {
            val block = currentList[i]
            if (block is MemoBlock.BodyBlock) {
                block.bodyRows.forEachIndexed { rowIndex, row ->
                    val ranges = findMatchRanges(row.text, keyword)
                    ranges.forEachIndexed { occurrence, _ ->
                        matchPositions += MatchPosition(
                            position = i,
                            rowIndex = rowIndex,
                            indexInRow = occurrence
                        )
                    }
                }
            }
        }

        // 3) 새 검색어로 하이라이트 적용
        highlightVisibleMatches()
        return matchPositions.size
    }

    private fun clearHighlightSpansOnly() {
        val rv = attachedRecyclerView ?: return
        for (i in currentList.indices) {
            val block = getItem(i)
            if (block is MemoBlock.BodyBlock) {
                val vh = rv.findViewHolderForAdapterPosition(i) as? BodyBlockViewHolder ?: continue
                block.bodyRows.indices.forEach { rowIndex ->
                    val et = vh.getRowEditText(rowIndex) ?: return@forEach
                    val spans = et.text?.getSpans(0, et.text!!.length, BackgroundColorSpan::class.java) ?: return@forEach
                    spans.forEach { et.text!!.removeSpan(it) }
                }
            }
        }
    }

    private fun countMatches(text: String, keyword: String): Int {
        if (keyword.isBlank()) return 0
        var count = 0
        var index = text.indexOf(keyword, 0, ignoreCase = true)

        while (index >= 0) {
            count++
            index = text.indexOf(keyword, index + keyword.length, ignoreCase = true)
        }

        return count
    }


    fun highlightVisibleMatches() {
        val rv = attachedRecyclerView ?: return
        if (currentKeyword.isBlank()) return

        // 현재 포커스된 매치(블록/행/행내 인덱스)
        val current = matchPositions.getOrNull(currentMatchIndex)

        // 블록 단위로 모아 처리
        val byBlock = matchPositions.groupBy { it.position }
        for ((blockPos, matchesInBlock) in byBlock) {
            val vh =
                rv.findViewHolderForAdapterPosition(blockPos) as? BodyBlockViewHolder ?: continue
            val block = getItem(blockPos) as? MemoBlock.BodyBlock ?: continue

            // 행 단위로 묶어서 처리
            val byRow = matchesInBlock.groupBy { it.rowIndex }
            for ((rowIndex, _) in byRow) {
                val et = vh.getRowEditText(rowIndex) ?: continue
                val editable = et.text

                // 이 행에서 현재 선택된 occurrence (없으면 null)
                val currentOccurrence =
                    if (current?.position == blockPos && current.rowIndex == rowIndex) {
                        current.indexInRow
                    } else null

                applyHighlightToEditable(
                    editable = editable,
                    keyword = currentKeyword,
                    currentOccurrence = currentOccurrence
                )
            }
        }
    }


    private fun findMatchRanges(text: String, keyword: String): List<IntRange> {
        if (keyword.isBlank()) return emptyList()
        val ranges = mutableListOf<IntRange>()
        var idx = text.indexOf(keyword, 0, ignoreCase = true)
        while (idx >= 0) {
            ranges += (idx until idx + keyword.length)
            idx = text.indexOf(keyword, idx + keyword.length, ignoreCase = true)
        }
        return ranges
    }

    private fun applyHighlightToEditable(
        editable: Editable,
        keyword: String,
        currentOccurrence: Int?
    ) {
        // 기존 스팬 제거
        editable.getSpans(0, editable.length, BackgroundColorSpan::class.java)
            .forEach { editable.removeSpan(it) }

        val ranges = findMatchRanges(editable.toString(), keyword)
        ranges.forEachIndexed { idx, range ->
            val color =
                if (idx == currentOccurrence) "#6FDBFF".toColorInt() else "#CDCDCD".toColorInt()
            editable.setSpan(
                BackgroundColorSpan(color),
                range.first,
                range.last + 1, // inclusive 보정
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun moveToNextMatch(onIndexChanged: ((Int, Int) -> Unit)? = null) {
        if (matchPositions.isEmpty()) return
        currentMatchIndex = (currentMatchIndex + 1) % matchPositions.size
        `scrollToCurrentMatch`()
        onIndexChanged?.invoke(currentMatchIndex + 1, matchPositions.size)
    }


    fun moveToPreviousMatch(onIndexChanged: ((Int, Int) -> Unit)? = null) {
        if (matchPositions.isEmpty()) return
        currentMatchIndex = (currentMatchIndex - 1 + matchPositions.size) % matchPositions.size
        scrollToCurrentMatch()
        onIndexChanged?.invoke(currentMatchIndex + 1, matchPositions.size)
    }

    private fun scrollToCurrentMatch() {
        val rv = attachedRecyclerView ?: return
        val match = matchPositions.getOrNull(currentMatchIndex) ?: return

        // 화면에 안 보일 때만 점프
        if (rv.findViewHolderForAdapterPosition(match.position) == null) {
            rv.scrollToPosition(match.position)
        }

        rv.post {
            val vh = rv.findViewHolderForAdapterPosition(match.position) as? BodyBlockViewHolder ?: return@post
            val et = vh.getRowEditText(match.rowIndex) ?: return@post

            et.post {
                val layout = et.layout ?: return@post
                val ranges = findMatchRanges(et.text.toString(), currentKeyword)
                val targetRange = ranges.getOrNull(match.indexInRow) ?: return@post

                val line = layout.getLineForOffset(targetRange.first)
                val y = layout.getLineTop(line)

                // 패딩 있는 rect 계산
                val extra = et.lineHeight * 2
                val rect = Rect(0, (y - extra).coerceAtLeast(0), et.width, y + et.lineHeight + extra)

                et.requestRectangleOnScreen(rect, true)

                // 하이라이트도 뷰 렌더 직후 적용
                highlightVisibleMatches()
            }
        }
    }

    fun clearSearch() {
        currentKeyword = ""
        matchPositions.clear()
        currentMatchIndex = 0
        highlightVisibleMatches() // => span도 지움
    }


    fun clearSpans(editable: Editable?) {
        if (editable == null) return
        val spans = editable.getSpans(0, editable.length, BackgroundColorSpan::class.java)
        spans.forEach { editable.removeSpan(it) }
    }

    fun clearAllHighlights() {
        val rv = attachedRecyclerView ?: return

        for (i in currentList.indices) {
            val block = getItem(i)
            if (block is MemoBlock.BodyBlock) {
                val vh = rv.findViewHolderForAdapterPosition(i) as? BodyBlockViewHolder ?: continue
                block.bodyRows.indices.forEach { rowIndex ->
                    val et = vh.getRowEditText(rowIndex) ?: return@forEach
                    clearSpans(et.text)
                }
            }
        }

        matchPositions.clear()
        currentMatchIndex = 0
        currentKeyword = ""
    }

    private fun alignCheckboxCenterByBaseline(
        et: TextView,
        iv: ImageView,
        iconDp: Int,
        opticalDpOffset: Float = 0f
    ) {
        fun realign() {
            val layout = et.layout ?: return
            if (layout.lineCount <= 0) return

            val ctx = et.context
            val iconPx = Util.dpToPx(ctx, iconDp) // Int → px
            val optical = Util.dpToPx(ctx, opticalDpOffset.toInt()) // Float → Int → px

            val baseline = layout.getLineBaseline(0)
            val fmi = et.paint.fontMetricsInt
            val glyphCenterInTextBox = baseline + (fmi.ascent + fmi.descent) / 2f
            val glyphCenterY = et.totalPaddingTop + glyphCenterInTextBox

            val topMargin = (glyphCenterY - iconPx / 2f + optical).toInt().coerceAtLeast(0)

            (iv.layoutParams as LinearLayout.LayoutParams).apply {
                width = iconPx
                height = iconPx
                this.topMargin = topMargin
            }
            iv.requestLayout()
        }

        et.post { realign() }
        et.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                    realign()
                }
            }
        })
    }

    private fun getMemoFontSize(context: Context): Float {
        val label = PreferenceUtil.get(PreferenceUtil.KEY_MEMO_FONT_SIZE, "")
        return when (label) {
            context.getString(R.string.haru_menu_font_size_xs) -> 12f
            context.getString(R.string.haru_menu_font_size_s) -> 14f
            context.getString(R.string.haru_menu_font_size_m) -> 16f
            context.getString(R.string.haru_menu_font_size_l) -> 18f
            context.getString(R.string.haru_menu_font_size_xl) -> 20f
            else -> 15f
        }
    }

    private fun showPhoneDialog(context: Context, number: String) {
        val formattedNumber = number.replace(
            Regex("^0(\\d{1,2})(\\d{3,4})(\\d{4})$"),
            "0$1-$2-$3"
        )

        val dialog = MemoCustomDialog(context).apply {
            setTitle("전화 연결")
            setMessage(formattedNumber)
            setSubMessage("연결 하시겠습니까?")
            setButton(
                "취소", "전화걸기",
                onCancel = { dismiss() },
                onConfirm = {
                    EventUtil.sendEvent(
                        context,
                        EventUtil.CATEGORY_DETAIL,
                        EventUtil.ACTION_PAGE_CALL
                    )
                    val intent = Intent(Intent.ACTION_DIAL, "tel:$number".toUri())
                    context.startActivity(intent)
                    dismiss()
                }
            )
        }
        dialog.show()
    }

    private fun showEmailDialog(context: Context, email: String) {
        val dialog = MemoCustomDialog(context).apply {
            setTitle("메일 발송")
            setMessage(email)
            setSubMessage("발송 하시겠습니까?")
            setButton(
                "취소", "발송하기",
                onCancel = { dismiss() },
                onConfirm = {
                    EventUtil.sendEvent(
                        context,
                        EventUtil.CATEGORY_DETAIL,
                        EventUtil.ACTION_PAGE_EMAIL
                    )
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:$email".toUri()
                    }
                    context.startActivity(Intent.createChooser(intent, "메일 앱 선택"))
                    dismiss()
                }
            )
        }
        dialog.show()
    }

    fun getLastBodyBlockPosition(): Int? {
        val lastIndex = currentList
            .withIndex().lastOrNull { it.value is MemoBlock.BodyBlock }
            ?.index

        return lastIndex
    }


    /** 툴바 체크박스 버튼이 누르면 호출 */
    fun toggleCheckboxAtCaret() {
        val rv = attachedRecyclerView ?: return
        val blockPos = currentFocusedPosition
        if (blockPos == RecyclerView.NO_POSITION) return

        val block = getItem(blockPos) as? MemoBlock.BodyBlock ?: return
        val holder = rv.findViewHolderForAdapterPosition(blockPos) as? BodyBlockViewHolder ?: return
        val focused = holder.getFocusedRowInfo() ?: return
        var (rowIndex, caretOffset) = focused

        val et = holder.getRowEditText(rowIndex) ?: return

        // 멀티라인이면 커서 줄만 단독 row로 분리
        if (et.text?.contains('\n') == true) {
            val (newIndex, newCaret) = holder.isolateCaretLine(block, rowIndex, et)
            rowIndex = newIndex
            caretOffset = newCaret
        }

        val row = block.bodyRows.getOrNull(rowIndex) ?: return
        var newCaret = caretOffset

        if (row.type == BodyType.TEXT) {
            // TEXT → CHECKBOX: 불릿이 붙어 있으면 제거하고 커서 보정
            if (row.text.hasBulletPrefix()) {
                row.text = row.text.stripBulletPrefix()
                newCaret = (newCaret - BULLET.length).coerceAtLeast(0)
            }
            row.type = BodyType.CHECKBOX
            row.isChecked = false
        } else {
            // CHECKBOX → TEXT
            row.type = BodyType.TEXT
            row.isChecked = false
        }

        holder.rebindRow(block, rowIndex, keepFocus = true, cursorOffset = newCaret)
    }


    fun toggleBulletAtCaret() {
        val rv = attachedRecyclerView ?: return
        val blockPos = currentFocusedPosition
        if (blockPos == RecyclerView.NO_POSITION) return

        val block = getItem(blockPos) as? MemoBlock.BodyBlock ?: return
        val holder = rv.findViewHolderForAdapterPosition(blockPos) as? BodyBlockViewHolder ?: return
        val focused = holder.getFocusedRowInfo() ?: return
        var (rowIndex, caretOffset) = focused

        val et = holder.getRowEditText(rowIndex) ?: return

        // 멀티라인이면 커서 줄만 단독 row로 분리
        if (et.text?.contains('\n') == true) {
            val (newIndex, newCaret) = holder.isolateCaretLine(block, rowIndex, et)
            rowIndex = newIndex
            caretOffset = newCaret
        }

        val row = block.bodyRows.getOrNull(rowIndex) ?: return
        var newCaret = caretOffset

        // CHECKBOX였다면 먼저 TEXT로 전환(체크박스 제거) → 상호 배타 보장
        if (row.type == BodyType.CHECKBOX) {
            row.type = BodyType.TEXT
            row.isChecked = false
        }

        // 불릿 토글
        if (row.text.hasBulletPrefix()) {
            row.text = row.text.stripBulletPrefix()
            newCaret = (newCaret - BULLET.length).coerceAtLeast(0)
        } else {
            row.text = row.text.addBulletPrefix()
            newCaret += BULLET.length
        }

        holder.rebindRow(block, rowIndex, keepFocus = true, cursorOffset = newCaret)
    }


    class MemoBlockDiffCallback : DiffUtil.ItemCallback<MemoBlock>() {
        override fun areItemsTheSame(old: MemoBlock, new: MemoBlock): Boolean = when {
            old is MemoBlock.BodyBlock && new is MemoBlock.BodyBlock -> old.id == new.id
            old is MemoBlock.ImageUriBlock && new is MemoBlock.ImageUriBlock -> old.id == new.id
            old is MemoBlock.DateBlock && new is MemoBlock.DateBlock -> true // 맨 위 1개
            else -> false
        }

        override fun areContentsTheSame(old: MemoBlock, new: MemoBlock): Boolean = when {
            old is MemoBlock.BodyBlock && new is MemoBlock.BodyBlock -> {
                if (old.bodyRows.size != new.bodyRows.size) false
                for (i in old.bodyRows.indices) {
                    val a = old.bodyRows[i];
                    val b = new.bodyRows[i]
                    if (a.text != b.text || a.type != b.type || a.isChecked != b.isChecked) false
                }
                true
            }

            old is MemoBlock.ImageUriBlock && new is MemoBlock.ImageUriBlock -> {
                // 필요 시 더 엄밀히 비교
                old.uris.size == new.uris.size
            }

            old is MemoBlock.DateBlock && new is MemoBlock.DateBlock -> old.date == new.date
            else -> true
        }

        override fun getChangePayload(old: MemoBlock, new: MemoBlock): Any? = when {
            old is MemoBlock.DateBlock && new is MemoBlock.DateBlock && old.date != new.date -> "payload_date"
            else -> null
        }
    }

}