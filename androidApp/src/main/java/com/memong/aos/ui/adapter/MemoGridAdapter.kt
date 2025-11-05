package com.memong.aos.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.avatye.haru.log.LogTrack
import com.bumptech.glide.Glide
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.entity.MemoSectionListItem
import com.memong.aos.data.enum.DynamicSectionType
import com.memong.aos.data.enum.FixedSectionType
import com.memong.aos.data.enum.MainGroupMode
import com.memong.aos.data.enum.MemoSortType
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_MEMO_GROUP_MODE
import com.memong.aos.data.utils.PreferenceUtil.KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED
import com.memong.aos.data.utils.PreferenceUtil.KEY_SECTION_SECRET_MEMO_IS_EXPANDED
import com.memong.aos.data.utils.PreferenceUtil.KEY_SIMPLIFY
import com.memong.aos.data.utils.PreferenceUtil.getDynamicSectionKey
import com.memong.aos.data.utils.RowCodecUtil.HARU_MEMO_CHECKED
import com.memong.aos.data.utils.RowCodecUtil.HARU_MEMO_UNCHECKED
import com.memong.aos.data.utils.Util
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.data.utils.VibrationUtil
import com.memong.aos.databinding.ItemMemoEmptyBinding
import com.memong.aos.databinding.ItemMemoGridBinding
import com.memong.aos.databinding.ItemMemoSectionHeaderBinding
import com.memong.aos.ui.SearchActivity
import com.memong.aos.ui.adapter.viewholder.DateSectionViewHolder
import com.memong.aos.ui.adapter.viewholder.EmptyItemViewHolder
import com.memong.aos.ui.adapter.viewholder.ImportantSectionViewHolder
import com.memong.aos.ui.adapter.viewholder.LockSectionViewHolder
import com.memong.aos.ui.listener.ItemSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MemoGridAdapter(
    private val activity: Activity,
    private val isSearchMode: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var isSelected = false
    private val selectedMemoIds = mutableSetOf<Int>()

    private var listener: ItemSelectedListener? = null
    private var requestPasswordCheck: ((MemoEntity) -> Unit)? = null
    private var memoClick: ((MemoEntity) -> Unit)? = null

    private var memoTags: Map<Int, List<String>> = emptyMap()

    private val sectionHeaderItems: MutableList<MemoSectionListItem.SectionHeader> = mutableListOf()
    private val flatItems: MutableList<MemoSectionListItem> = mutableListOf()

    // ★ 스페이서 구분 플래그: 전체가 빈 상태로 보여줄 때만 true
    private var isWholeListEmptyState: Boolean = false

    // ★ spanCount 관리 (기본 3; GridLayoutManager에 붙으면 자동 갱신)
    private var spanCount: Int = 3

    companion object {
        private const val NAME = "MemoGridAdapter"

        const val VIEW_TYPE_EMPTY = 0
        const val VIEW_TYPE_LOCK_SECTION = 1
        const val VIEW_TYPE_IMPORTANT_SECTION = 2
        const val VIEW_TYPE_DATE_SECTION = 3
        const val VIEW_TYPE_MEMO = 4
        const val VIEW_TYPE_SPACER = 99
    }


    val memoDao by lazy {
        MemoDatabase.getInstance(activity).memoDao()
    }

    private var currentSortType: MemoSortType = MemoSortType.LATEST_CREATE

    private var dragItemTouchHelper: ItemTouchHelper? = null

    private var onMemoUpdated: (() -> Unit)? = null

    val isSecretExpanded: Boolean
        get() {
            return PreferenceUtil.get(KEY_SECTION_SECRET_MEMO_IS_EXPANDED, false)
        }

    val isImportantExpanded: Boolean
        get() {
            return PreferenceUtil.get(KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED, false)
        }

    fun setOnMemoUpdatedListener(callback: () -> Unit) {
        this.onMemoUpdated = callback
    }

    // ★ GridLayoutManager spanCount 자동 감지
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (recyclerView.layoutManager as? GridLayoutManager)?.let {
            spanCount = it.spanCount
        }
    }

    fun updateItems(headers: List<MemoSectionListItem.SectionHeader>) {
        sectionHeaderItems.clear()
        sectionHeaderItems.addAll(headers)
        updateFlatList()
    }

    // 검색 전용
    fun updateFlatItems(items: List<MemoSectionListItem>) {
        if (!isSearchMode) error("updateFlatItems는 검색 모드에서만 호출해야 합니다.")

        flatItems.clear()

        // SectionHeader -> MemoItem으로 변환
        items.forEach { item ->
            when (item) {
                is MemoSectionListItem.MemoItem -> flatItems.add(item)
                is MemoSectionListItem.SectionHeader -> {
                    flatItems.addAll(item.memos.map { MemoSectionListItem.MemoItem(it) })
                }

                else -> Unit
            }
        }

        // 검색 모드는 스페이서 불필요 (섹션 구분 없음)
        isWholeListEmptyState = flatItems.isEmpty()
        notifyDataSetChanged()
    }

    private fun updateFlatList() {
        if (isSearchMode) {
            notifyDataSetChanged()
            return
        }

        flatItems.clear()

        val hasMemo = sectionHeaderItems.any { it.memos.isNotEmpty() }
        isWholeListEmptyState = !hasMemo // ★ 스페이서/빈 리스트 구분에 사용

        if (hasMemo) {
            sectionHeaderItems.forEach { header ->
                flatItems.add(header)

                val shouldShowMemos = !header.needExpandable || header.isExpanded
                if (shouldShowMemos) {
                    val memoItems = header.memos.map { MemoSectionListItem.MemoItem(it) }
                    flatItems.addAll(memoItems)

                    // ★ 마지막 줄 스페이서 채우기 (spanCount 기준)
                    val remainder = memoItems.size % spanCount
                    if (remainder != 0) {
                        val need = spanCount - remainder
                        repeat(need) {
                            // 스페이서는 EmptyItem을 재활용하되, isWholeListEmptyState=false 이므로 SPACER로 렌더링됨
                            flatItems.add(MemoSectionListItem.EmptyItem)
                        }
                    }
                }
            }
        } else {
            // 전체 빈 상태일 때만 EmptyItem 1개 추가 -> VIEW_TYPE_EMPTY로 렌더링
            flatItems.add(MemoSectionListItem.EmptyItem)
        }

        notifyDataSetChanged()
    }

    fun toggleSection(header: MemoSectionListItem.SectionHeader, recyclerView: RecyclerView) {
        val headerIndex = flatItems.indexOf(header)
        if (headerIndex == -1) return

        val startPos = headerIndex + 1
        val memoCount = header.memos.size

        if (header.isExpanded) {
            header.isExpanded = false
            // 접기: 메모 + 스페이서까지 제거
            // 메모 개수 + 스페이서 개수 계산
            val toRemove = run {
                val remainder = memoCount % spanCount
                memoCount + if (remainder == 0) 0 else (spanCount - remainder)
            }
            flatItems.subList(startPos, (startPos + toRemove).coerceAtMost(flatItems.size)).clear()
            notifyItemRangeRemoved(startPos, toRemove)
            recyclerView.invalidateItemDecorations()
        } else {
            header.isExpanded = true
            // 펼치기: 메모 + 스페이서 삽입
            val memoItems = header.memos.map { MemoSectionListItem.MemoItem(it) }
            flatItems.addAll(startPos, memoItems)

            val remainder = memoCount % spanCount
            val spacerCount = if (remainder == 0) 0 else (spanCount - remainder)
            repeat(spacerCount) {
                flatItems.add(startPos + memoItems.size, MemoSectionListItem.EmptyItem)
            }

            notifyItemRangeInserted(startPos, memoItems.size + spacerCount)
        }

        notifyItemChanged(headerIndex) // 헤더 UI 갱신
    }

    fun updateSingleMemo(updated: MemoEntity?) {
        if (updated == null) return
        var updatedSection: MemoSectionListItem.SectionHeader? = null

        sectionHeaderItems.forEachIndexed { index, header ->
            val idx = header.memos.indexOfFirst { it._id == updated._id }
            if (idx != -1) {
                val newMemoList = header.memos.toMutableList().apply { set(idx, updated) }
                sectionHeaderItems[index] = header.copy(memos = newMemoList)
                updatedSection = sectionHeaderItems[index]
                return@forEachIndexed
            }
        }

        if (updatedSection != null) updateFlatList()
    }

    fun updateMemoTags(map: Map<Int, List<String>>) {
        memoTags = map
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val item = flatItems.getOrNull(position) ?: return VIEW_TYPE_EMPTY

        return if (isSearchMode) {
            if (item is MemoSectionListItem.MemoItem) VIEW_TYPE_MEMO else VIEW_TYPE_EMPTY
        } else {
            when (item) {
                is MemoSectionListItem.EmptyItem -> {
                    // ★ 전체 빈 리스트일 때만 Empty 화면, 그 외에는 스페이서로 처리
                    if (isWholeListEmptyState) VIEW_TYPE_EMPTY else VIEW_TYPE_SPACER
                }

                is MemoSectionListItem.SectionHeader -> when (item.sectionType) {
                    FixedSectionType.SECRET -> VIEW_TYPE_LOCK_SECTION
                    FixedSectionType.IMPORTANT -> VIEW_TYPE_IMPORTANT_SECTION
                    else -> VIEW_TYPE_DATE_SECTION
                }

                is MemoSectionListItem.MemoItem -> VIEW_TYPE_MEMO
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        LogTrack.i(NAME) { "=> onCreateViewHolder viewType: $viewType" }
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                if (isSearchMode) {
                    error("SearchMode에서 EmptyItem은 생성될 수 없습니다")
                } else {
                    EmptyItemViewHolder(
                        activity = activity,
                        isGridMode = true,
                        binding = ItemMemoEmptyBinding.inflate(inflater, parent, false)
                    )
                }
            }

            VIEW_TYPE_LOCK_SECTION -> {
                if (isSearchMode) error("SearchMode에서 LockSectionViewHolder는 생성될 수 없습니다")
                LockSectionViewHolder(
                    activity = activity,
                    binding = ItemMemoSectionHeaderBinding.inflate(inflater, parent, false),
                    isSelected = { isSelected },
                    updateFlatList = { updateFlatList() }
                )
            }

            VIEW_TYPE_IMPORTANT_SECTION -> {
                if (isSearchMode) error("SearchMode에서 ImportantSectionViewHolder는 생성될 수 없습니다")
                ImportantSectionViewHolder(
                    activity = activity,
                    binding = ItemMemoSectionHeaderBinding.inflate(inflater, parent, false),
                    isSelected = { isSelected },
                    updateFlatList = { updateFlatList() }
                )
            }

            VIEW_TYPE_DATE_SECTION -> {
                if (isSearchMode) error("SearchMode에서 DateSectionViewHolder는 생성될 수 없습니다")
                DateSectionViewHolder(
                    activity = activity,
                    binding = ItemMemoSectionHeaderBinding.inflate(inflater, parent, false),
                    isSelected = { isSelected },
                    currentSortType = { currentSortType }
                )
            }

            VIEW_TYPE_MEMO -> MemoViewHolder(ItemMemoGridBinding.inflate(inflater, parent, false))

            // ★ 스페이서: 간단한 FrameLayout (섹션 배경색을 bind에서 입힘)
            VIEW_TYPE_SPACER -> SpacerViewHolder(
                FrameLayout(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    isClickable = false
                    isFocusable = false
                }
            )

            else -> {
                LogTrack.e(NAME) { "Unknown viewType: $viewType, fallback to Empty" }
                EmptyItemViewHolder(
                    activity = activity,
                    isGridMode = true,
                    binding = ItemMemoEmptyBinding.inflate(inflater, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = flatItems.getOrNull(position)) {
            is MemoSectionListItem.EmptyItem -> {
                // ★ isWholeListEmptyState=true에서만 Empty 바인딩; 스페이서는 아래에서 처리
                if (getItemViewType(position) == VIEW_TYPE_EMPTY && holder is EmptyItemViewHolder) {
                    holder.bind()
                } else if (getItemViewType(position) == VIEW_TYPE_SPACER && holder is SpacerViewHolder) {
                    holder.bind(getSectionTypeForPosition(position))
                }
            }

            is MemoSectionListItem.SectionHeader -> {
                when (holder) {
                    is LockSectionViewHolder -> holder.bind(item, position)
                    is ImportantSectionViewHolder -> holder.bind(item, position)
                    is DateSectionViewHolder -> holder.bind(item, position)
                }
            }

            is MemoSectionListItem.MemoItem -> {
                if (holder is MemoViewHolder) holder.bind(item.memo, position)
            }

            else -> {
                LogTrack.d(NAME) { "검색 결과에 대한 빈 뷰 처리" }
            }
        }
    }

    // ★ 스페이서 전용 뷰홀더
    private inner class SpacerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(sectionTypeKey: String?) {
            val ctx = itemView.context
            val bgColor = resolveSectionRowBgColor(ctx, sectionTypeKey)
            itemView.setBackgroundColor(bgColor)
        }
    }

    private fun resolveSectionRowBgColor(context: Context, sectionTypeKey: String?): Int {
        return when (sectionTypeKey) {
            FixedSectionType.SECRET.key ->
                ContextCompat.getColor(context, R.color.recyclerview_color)

            FixedSectionType.IMPORTANT.key ->
                ContextCompat.getColor(context, R.color.recyclerview_color)

            else ->
                Color.TRANSPARENT // 일반/날짜 섹션은 필요 없으면 투명
        }
    }

    inner class MemoViewHolder(val binding: ItemMemoGridBinding) : RecyclerView.ViewHolder(binding.gridRootView) {

        private fun getSavedGroupMode(): MainGroupMode {
            val saved = PreferenceUtil.get(KEY_MEMO_GROUP_MODE, MainGroupMode.DATE.name)
            return runCatching { MainGroupMode.valueOf(saved) }.getOrDefault(MainGroupMode.DATE)
        }


        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: MemoEntity, position: Int) {
            with(binding) {
                val sectionType = getSectionTypeForPosition(position)
                val hasImages = item.imagePath.values.any { it.isNotEmpty() }
                val isFromSearchActivity = activity is SearchActivity

                gridLayoutBF.background = null
                if (item.isLocked) {
                    if (PreferenceUtil.get(KEY_SIMPLIFY, false)) {
                        gridImgLock.setImageResource(R.drawable.ic_item_unlock)
                    } else {
                        gridImgLock.setImageResource(R.drawable.ic_item_lock)
                    }
                    gridLayoutBF.setBackgroundResource(R.drawable.ripple_lock_rectangle)
                    gridLayoutBF.backgroundTintList = null

                    gridImgLock.isVisible = true
                    gridImgBody.isVisible = false
                } else {
                    gridLayoutBF.setBackgroundResource(R.drawable.ripple_rectangle)
                    gridLayoutBF.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.bgColor))
                    gridImgLock.isVisible = false
                    gridImgBody.isVisible = hasImages
                }

                // body (순서 중요: 먼저 maxLines/ellipsize -> setText -> post 강제 말줄임)
                val maxLinesForBody = if (item.isLocked) 1 else 6
                gridTvBody.maxLines = maxLinesForBody
                gridTvBody.ellipsize = TextUtils.TruncateAt.END

                val preview = buildPreviewSpannable(gridTvBody.context, item, gridTvBody)
                gridTvBody.setText(preview, TextView.BufferType.SPANNABLE)

                val typeface = ResourcesCompat.getFont(root.context, R.font.pretendard_regular)
                gridTvBody.setTypeface(typeface)

                // 레이아웃 이후 실제 줄수 기준으로 강제 말줄임(스팬/개행 대응)
                gridTvBody.post {
                    val tv = gridTvBody
                    val layout = tv.layout ?: return@post
                    if (tv.lineCount > maxLinesForBody) {
                        val end = layout.getLineEnd(maxLinesForBody - 1).coerceAtMost(tv.text.length)
                        val src = tv.text
                        val cut: CharSequence = if (src is Spanned) src.subSequence(0, end) else src.substring(0, end)
                        val trimmed = cut.trimEnd { it == '\n' || it == ' ' }
                        val out = SpannableStringBuilder(trimmed).apply { append('\u2026') } // …
                        tv.setText(out, TextView.BufferType.SPANNABLE)
                    }
                }

                // date
                val displayDate = when (currentSortType) {
                    MemoSortType.LATEST_CREATE, MemoSortType.OLDEST_CREATE -> item.created
                    MemoSortType.LATEST_UPDATE, MemoSortType.OLDEST_UPDATE -> item.modified
                    MemoSortType.CUSTOM -> item.created
                }
                gridTvDate.text = Util.formatDate(displayDate)

                // image
                if (hasImages) {
                    val firstUri = item.imagePath.values.firstOrNull { it.isNotEmpty() }?.firstOrNull()
                    firstUri?.let { uriStr ->
                        Glide.with(gridImgBody).load(uriStr).into(gridImgBody)
                    }
                } else {
                    Glide.with(gridImgBody).clear(gridImgBody)
                    gridImgBody.setImageDrawable(null)
                }

                // selection
                gridCheckboxContainer.isVisible = isSelected
                gridCheckbox.isChecked = selectedMemoIds.contains(item._id)
                gridCheckbox.apply {
                    isClickable = false
                    isFocusable = false
                    isEnabled = false
                }

                // important
                lyGridImgImportant.isVisible = !item.isLocked && !isSelected
                gridImgImportant.isSelected = item.isImportant
                if (!isFromSearchActivity) {
                    lyGridImgImportant.setOnClickListener {
                        try {
                            val updatedItem = item.copy(isImportant = !item.isImportant)
                            CoroutineScope(Dispatchers.IO).launch {
                                memoDao.updateMemo(updatedItem)
                                withContext(Dispatchers.Main) {
                                    gridImgImportant.isSelected = updatedItem.isImportant
                                    onMemoUpdated?.invoke()

                                    // 중요 상태 변경 이벤트 전송
                                    val action = if (updatedItem.isImportant) {
                                        EventUtil.ACTION_IMPORTANT_ON
                                    } else {
                                        EventUtil.ACTION_IMPORTANT_OFF
                                    }
                                    EventUtil.sendEvent(
                                        context = lyGridImgImportant.context,
                                        category = EventUtil.CATEGORY_MAIN,
                                        action = action
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // drag
                gridDragContainer.isVisible =
                    currentSortType == MemoSortType.CUSTOM &&
                            !isSelected &&
                            !item.isLocked &&
                            !item.isImportant &&
                            sectionType != FixedSectionType.SECRET.key &&
                            sectionType != FixedSectionType.IMPORTANT.key

                gridDragContainer.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN && currentSortType == MemoSortType.CUSTOM) {
                        dragItemTouchHelper?.startDrag(this@MemoViewHolder)
                    }
                    true
                }

                // click/long-click
                lyItemMemoGrid.setOnClickListener {
                    val memoId = item._id
                    if (isSelected) {
                        if (selectedMemoIds.contains(memoId)) selectedMemoIds.remove(memoId) else selectedMemoIds.add(memoId)
                        notifyItemChanged(bindingAdapterPosition)
                        listener?.onSelectedItem(
                            selectCount = selectedMemoIds.size,
                            itemCount = getDeduplicationMemoItemCount()
                        )
                    } else {
                        if (item.isLocked) requestPasswordCheck?.invoke(item) else memoClick?.invoke(item)
                    }
                }


                val savedGroupMode = getSavedGroupMode()
                val allowLongClick = !isFromSearchActivity && savedGroupMode == MainGroupMode.DATE
                lyItemMemoGrid.isLongClickable = allowLongClick
                lyItemMemoGrid.setOnLongClickListener {
                    // 실행 시점에 다시 확인(모드가 바뀌었을 수 있음)
                    val stillAllow = !isFromSearchActivity && savedGroupMode == MainGroupMode.DATE
                    if (!stillAllow) {
                        toastShort(activity, "날짜별로 정리된 메모(\uD83D\uDDD3\uFE0F)에서만 관리가 가능합니다.")
                        return@setOnLongClickListener false
                    }

                    val currentPos = bindingAdapterPosition
                    if (currentPos == RecyclerView.NO_POSITION) return@setOnLongClickListener false
                    enterSelectMode(position = currentPos)
                    VibrationUtil.vibrate(activity)
                    true
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

                d.alpha = if (this@MemoGridAdapter.isSelected) (0.3f * 255).toInt() else 255

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

    override fun getItemCount() = flatItems.size

    fun getSectionTypeForPosition(position: Int): String? {
        for (i in position downTo 0) {
            val item = flatItems.getOrNull(i)
            if (item is MemoSectionListItem.SectionHeader) {
                return item.sectionType.key
            }
        }
        return null
    }


    private fun getDeduplicationMemoItemCount(): Int {
        return flatItems
            .filterIsInstance<MemoSectionListItem.MemoItem>()
            .map { it.memo._id }
            .distinct()
            .count()
    }


    fun getSectionIndexForPosition(position: Int): Int? {
        for (i in position downTo 0) {
            val item = flatItems.getOrNull(i)
            if (item is MemoSectionListItem.SectionHeader) {
                return i
            }
        }
        return null
    }

    fun getFirstAndLastMemoIndexInSection(sectionIndex: Int): Pair<Int, Int>? {
        val sectionType = (flatItems.getOrNull(sectionIndex) as? MemoSectionListItem.SectionHeader)?.sectionType ?: return null

        val start = flatItems.indexOfFirst {
            it is MemoSectionListItem.MemoItem && getSectionIndexForPosition(flatItems.indexOf(it)) == sectionIndex
        }
        val end = flatItems.indexOfLast {
            it is MemoSectionListItem.MemoItem && getSectionIndexForPosition(flatItems.indexOf(it)) == sectionIndex
        }
        return if (start != -1 && end != -1) Pair(start, end) else null
    }


    fun getSelectedItems(): List<MemoEntity> {
        return flatItems
            .filterIsInstance<MemoSectionListItem.MemoItem>()
            .map { it.memo }
            .filter { selectedMemoIds.contains(it._id) }
    }


    fun setItemSelectedListener(listener: ItemSelectedListener) {
        this.listener = listener
    }

    fun setItemTouchHelper(helper: ItemTouchHelper) {
        this.dragItemTouchHelper = helper
    }

    fun setOnRequestPasswordCheck(callback: (MemoEntity) -> Unit) {
        this.requestPasswordCheck = callback
    }

    fun setSortType(sortType: MemoSortType) {
        this.currentSortType = sortType
    }

    fun getExpandStateSection(headers: List<MemoSectionListItem.SectionHeader>): List<MemoSectionListItem.SectionHeader> {
        return headers.map {
            when (it.sectionType) {
                FixedSectionType.SECRET -> if (it.needExpandable) it.copy(isExpanded = isSecretExpanded) else it
                FixedSectionType.IMPORTANT -> if (it.needExpandable) it.copy(isExpanded = isImportantExpanded) else it
                else -> {
                    if (it.needExpandable) {
                        it.copy(isExpanded = PreferenceUtil.get(getDynamicSectionKey(it.sectionType.key), it.isExpanded))
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun selectAll() {
        if (!isSelected) {
            isSelected = true
        }

        flatItems.forEach {
            if (it is MemoSectionListItem.MemoItem) {
                selectedMemoIds.add(it.memo._id)
            }
        }

        notifyDataSetChanged()
        listener?.onSelectedItem(
            selectCount = selectedMemoIds.size,
            itemCount = getDeduplicationMemoItemCount()
        )
    }

    fun unSelectAll() {
        selectedMemoIds.clear()
        notifyDataSetChanged()

        listener?.onSelectedItem(
            selectCount = 0,
            itemCount = getDeduplicationMemoItemCount()
        )
    }

    fun isInSelectMode(): Boolean = isSelected
    fun enterSelectMode(position: Int = -1) {
        if (!isSelected) {
            isSelected = true
            selectedMemoIds.clear()

            // 선택모드 진입 시 모든 섹션 확장
            if (position >= 0) {
                (flatItems.getOrNull(position) as? MemoSectionListItem.MemoItem)?.let {
                    selectedMemoIds.add(it.memo._id)
                }
            }

            // 모든 섹션 펼침
            val updatedSections = sectionHeaderItems.map {
                if (it.needExpandable && !it.isExpanded) it.copy(isExpanded = true) else it
            }
            sectionHeaderItems.clear()
            sectionHeaderItems.addAll(updatedSections)

            updateFlatList()

            listener?.onSelectedItem(
                selectCount = selectedMemoIds.size,
                itemCount = getDeduplicationMemoItemCount()
            )
        }
    }

    fun exitSelectMode() {
        if (isSelected) {
            isSelected = false
            selectedMemoIds.clear()

            // 선택모드 종료 시 섹션 접기
            val updatedSections = sectionHeaderItems.map { header ->
                when (header.sectionType) {
                    FixedSectionType.SECRET -> if (header.needExpandable) {
                        header.copy(
                            isExpanded = PreferenceUtil.get(
                                KEY_SECTION_SECRET_MEMO_IS_EXPANDED,
                                false
                            )
                        )
                    } else header

                    FixedSectionType.IMPORTANT -> if (header.needExpandable) {
                        header.copy(
                            isExpanded = PreferenceUtil.get(
                                KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED,
                                false
                            )
                        )
                    } else header

                    else -> header // 동적 섹션은 기존 상태 유지
                }
            }
            sectionHeaderItems.clear()
            sectionHeaderItems.addAll(updatedSections)

            updateFlatList()
        }
    }

    fun setOnMemoClickListener(callback: (MemoEntity) -> Unit) {
        this.memoClick = callback
    }

    class MemoItemTouchHelperCallback(
        private val adapter: MemoGridAdapter,
        private val otherAdapter: MemoListAdapter
    ) : ItemTouchHelper.Callback() {

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return if (adapter.currentSortType == MemoSortType.CUSTOM) {
                makeMovementFlags(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                    0 // swipe disabled
                )
            } else {
                0
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition

            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false

            val fromItem =
                adapter.flatItems.getOrNull(from) as? MemoSectionListItem.MemoItem ?: return false
            val toItem =
                adapter.flatItems.getOrNull(to) as? MemoSectionListItem.MemoItem ?: return false

            // 잠금/중요 메모 제외
            if (fromItem.memo.isLocked || fromItem.memo.isImportant) return false
            if (toItem.memo.isLocked || toItem.memo.isImportant) return false

            //  flatItems 내부 순서 변경
            adapter.flatItems.removeAt(from)
            adapter.flatItems.add(to, fromItem)

            adapter.notifyItemMoved(from, to)
            return true
        }


        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            val temp = adapter.flatItems
                .filterIsInstance<MemoSectionListItem.MemoItem>()
                .filter { !it.memo.isLocked && !it.memo.isImportant }
                .map { it.memo }

            val now = System.currentTimeMillis()
            val finalList = temp.mapIndexed { index, memo ->
                memo.copy(custom = now - index)
            }

            CoroutineScope(Dispatchers.IO).launch {
                finalList.forEach { adapter.memoDao.updateMemo(it) }

                // 섹션 다시 구성
                val secretList = adapter.memoDao.getLockMemosByCustomDesc()
                val importantList = adapter.memoDao.getImportantMemosByCustomDesc()
                val regularList = adapter.memoDao.getAllMemosByCustomDesc()

                val grouped = buildList {
                    if (secretList.isNotEmpty()) add(
                        MemoSectionListItem.SectionHeader(
                            sectionType = FixedSectionType.SECRET,
                            title = FixedSectionType.SECRET.sectionName,
                            isExpanded = adapter.isSecretExpanded,
                            needExpandable = true,
                            memos = secretList
                        )
                    )
                    if (importantList.isNotEmpty()) add(
                        MemoSectionListItem.SectionHeader(
                            sectionType = FixedSectionType.IMPORTANT,
                            title = FixedSectionType.IMPORTANT.sectionName,
                            isExpanded = adapter.isImportantExpanded,
                            needExpandable = true,
                            memos = importantList
                        )
                    )
                    if (regularList.isNotEmpty()) add(
                        MemoSectionListItem.SectionHeader(
                            sectionType = DynamicSectionType.ALL,
                            title = DynamicSectionType.ALL.sectionName,
                            isExpanded = false,
                            needExpandable = false,
                            memos = regularList
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    adapter.updateItems(grouped)
                    otherAdapter.updateItems(grouped)
                }
            }
        }
    }

}
