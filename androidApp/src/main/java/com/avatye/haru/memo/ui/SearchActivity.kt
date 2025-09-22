package com.avatye.haru.memo.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.MemoEventFlow
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.database.MemoDatabase
import com.avatye.haru.memo.data.entity.MemoEntity
import com.avatye.haru.memo.data.entity.MemoEvent
import com.avatye.haru.memo.data.entity.MemoSectionListItem
import com.avatye.haru.memo.data.enum.DynamicSectionType
import com.avatye.haru.memo.data.enum.MainLayoutMode
import com.avatye.haru.memo.data.enum.MemoMode
import com.avatye.haru.memo.data.extension.hideKeyboard
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_SIMPLIFY
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_MEMO_LAYOUT_MODE
import com.avatye.haru.memo.data.utils.RowCodecUtil.HARU_MEMO_CHECKED
import com.avatye.haru.memo.data.utils.RowCodecUtil.HARU_MEMO_UNCHECKED
import com.avatye.haru.memo.data.utils.Util
import com.avatye.haru.memo.data.utils.Util.Companion.toastShort
import com.avatye.haru.memo.databinding.ActivitySearchBinding
import com.avatye.haru.memo.ui.MemoListActivity
import com.avatye.haru.memo.ui.adapter.BaseSearchAdapter
import com.avatye.haru.memo.ui.adapter.MemoGridAdapter
import com.avatye.haru.memo.ui.adapter.MemoListAdapter
import com.avatye.haru.memo.ui.adapter.TagChipAdapter
import com.avatye.haru.memo.ui.custom.header.SettingHeaderView
import com.avatye.haru.memo.ui.custom.item.GridSpacingItemDecoration
import com.avatye.haru.memo.ui.custom.item.MainListSpacingItemDecoration
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

internal class SearchActivity : BaseActivity() {

    override val NAME: String
        get() = SearchActivity::class.java.simpleName

    private lateinit var binding: ActivitySearchBinding
    private lateinit var tagAdapter: TagChipAdapter
    private lateinit var gridAdapter: MemoGridAdapter
    private lateinit var listAdapter: MemoListAdapter
    private lateinit var searchAdapter: BaseSearchAdapter

    private val memoDao by lazy { MemoDatabase.getInstance(this).memoDao() }
    private val tagDao by lazy { MemoDatabase.getInstance(this).tagDao() }
    private val taggingDao by lazy { MemoDatabase.getInstance(this).taggingDao() }

    private enum class HeaderState { INITIAL, SELECTOR_ACTIVE, SEARCH_RESULT }

    private var currentHeaderState = HeaderState.INITIAL
    private var currentSearchTagId: Int? = null // null이면 텍스트 검색
    private var liveSearchJob: Job? = null

    // SearchActivity 내부
    private val memoDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    val updatedResult = if (currentSearchTagId != null) {
                        // 태그 기준 검색
                        val taggings = taggingDao.getMemosByTagId(currentSearchTagId!!)
                        val memos = taggings.mapNotNull { tagging -> memoDao.getMemoById(tagging.memoid) }
                            .filter { it.isDeleted.not() }
                            .sortedByDescending { it.created }
                        memos
                    } else {
                        // 텍스트 기준 검색
                        searchByText(binding.headerSearch.getInputText().trim())
                    }
                    searchAdapter.updateFlatItems(groupMemosForSearch(updatedResult))
                    updateEmptyStateForResults(updatedResult.isNotEmpty())
                }
            }
        }

    private val passwordCheckLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    PasswordActivity.EXTRA_MEMO_ITEM,
                    MemoEntity::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(PasswordActivity.EXTRA_MEMO_ITEM)
            }

            // 먼저 리스트 갱신
            lifecycleScope.launch {
                val updatedResult = if (currentSearchTagId != null) {
                    val taggings = taggingDao.getMemosByTagId(currentSearchTagId!!)
                    val memos = taggings.mapNotNull { tagging ->
                        memoDao.getMemoById(tagging.memoid)
                    }.filter { it.isDeleted.not() }
                        .sortedByDescending { it.created }
                    memos
                } else {
                    searchByText(binding.headerSearch.getInputText().trim())
                }

                searchAdapter.updateFlatItems(groupMemosForSearch(updatedResult))
                updateEmptyStateForResults(updatedResult.isNotEmpty())

                // 이후 상세 진입
                item?.let {
                    val intent = MemoDetailActivity.getIntent(
                        activity = this@SearchActivity,
                        mode = MemoMode.READ_MEMO,
                        memoItem = it
                    )
                    memoDetailLauncher.launch(intent) // ✨ 핵심: launcher 사용
                }
            }
        }
    }


    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, SearchActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.searchRootView, 0)

        observeEventFlow()
        setupSuggestedTagRecyclerView()
        setupSearchResultRecyclerView()
        setupHeader()

        if (savedInstanceState == null) {
            binding.headerSearch.post {
                binding.headerSearch.focusAndShowKeyboard()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackAction()
            }
        })

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )
    }

    override fun onResume() {
        super.onResume()
        setupSuggestedTagRecyclerView()
        setupSearchResultRecyclerView()
        setTags()
        binding.bottomBannerView.onResume()
    }


    override fun onPause() {
        super.onPause()
        binding.bottomBannerView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.bottomBannerView.onDestroy()
    }


    private fun observeEventFlow() {
        lifecycleScope.launch {
            MemoEventFlow.events.collect { event ->
                when (event) {
                    is MemoEvent.AllMemoUpdated,
                    is MemoEvent.MemoUpdated -> {
                        lifecycleScope.launch {
                            val updatedResult = if (currentSearchTagId != null) {
                                // 태그 기준 검색
                                val taggings = taggingDao.getMemosByTagId(currentSearchTagId!!)
                                val memos = taggings.mapNotNull { tagging ->
                                    memoDao.getMemoById(tagging.memoid)
                                }.filter { it.isDeleted.not() }
                                    .sortedByDescending { it.created }
                                memos
                            } else {
                                // 텍스트 기준 검색
                                searchByText(binding.headerSearch.getInputText().trim())
                            }

                            searchAdapter.updateFlatItems(groupMemosForSearch(updatedResult))
                            updateEmptyStateForResults(updatedResult.isNotEmpty())
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun setupSuggestedTagRecyclerView() {
        tagAdapter = TagChipAdapter(context = this@SearchActivity) { tagName ->
            lifecycleScope.launch {
                val tag = tagDao.getAllTags().find { it.tagName == tagName } ?: return@launch
                val taggings = taggingDao.getMemosByTagId(tag._id)
                val memos = taggings.mapNotNull { tagging -> memoDao.getMemoById(tagging.memoid) }
                    .filter { it.isDeleted.not() }
                    .sortedByDescending { it.created }
                if (memos.isEmpty()) {
                    toastShort(this@SearchActivity, "해당 태그가 등록된 메모가 없습니다")
                    return@launch
                }
                EventUtil.sendEvent(this@SearchActivity, EventUtil.CATEGORY_SEARCH, EventUtil.ACTION_TAG_CLICK)
                currentSearchTagId = tag._id
                currentHeaderState = HeaderState.INITIAL
                showSearchResult(true)
                searchAdapter.updateFlatItems(groupMemosForSearch(memos))
                updateEmptyStateForResults(memos.isNotEmpty())
            }
        }

        binding.recyclerSuggestedTags.apply {
            adapter = tagAdapter
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
        }

        loadRecentTags()
    }

    private fun loadRecentTags() {
        lifecycleScope.launch {
            val recentTags = tagDao.getAllActiveTags()
                .sortedByDescending { it.created }

            tagAdapter.submitList(recentTags)

            val hasTags = recentTags.isNotEmpty()
            binding.textNoTagData.visibility = if (hasTags) View.GONE else View.VISIBLE
            binding.recyclerSuggestedTags.visibility = if (hasTags) View.VISIBLE else View.GONE
        }
    }

    private fun setupSearchResultRecyclerView() {
        val modeString = PreferenceUtil.get(KEY_MEMO_LAYOUT_MODE, MainLayoutMode.GRID.name)
        val displayMode = runCatching { MainLayoutMode.valueOf(modeString) }
            .getOrElse { MainLayoutMode.GRID }

        // 공통 클릭 리스너
        val onMemoClick: (MemoEntity) -> Unit = { item ->
            val intent = MemoDetailActivity.getIntent(
                activity = this@SearchActivity,
                mode = MemoMode.READ_MEMO,
                memoItem = item
            )
            memoDetailLauncher.launch(intent)
        }

        val onRequestPassword: (MemoEntity) -> Unit = { item ->
            if (PreferenceUtil.get(KEY_SIMPLIFY, false)) {
                val intent = MemoDetailActivity.getIntent(
                    activity = this@SearchActivity,
                    mode = MemoMode.READ_MEMO,
                    memoItem = item
                )
                memoDetailLauncher.launch(intent)
            } else {
                val intent = PasswordActivity.createIntent(
                    activity = this@SearchActivity,
                    mode = SettingHeaderView.SetHeaderMode.PASSWORD_CHECK_SEARCH,
                    memoItem = item
                )
                passwordCheckLauncher.launch(intent)
            }
        }

        // 리사이클러뷰 공통 초기화
        val rv = binding.recyclerView
        // 기존 데코 제거
        if (rv.itemDecorationCount > 0) {
            for (i in rv.itemDecorationCount - 1 downTo 0) rv.removeItemDecorationAt(i)
        }

        when (displayMode) {
            MainLayoutMode.GRID -> {
                // 어댑터 준비
                val created = MemoGridAdapter(activity = this, isSearchMode = true)
                created.setOnMemoClickListener(onMemoClick)
                created.setOnRequestPasswordCheck(onRequestPassword)

                gridAdapter = created
                searchAdapter = object : BaseSearchAdapter {
                    override fun setOnMemoClickListener(listener: (MemoEntity) -> Unit) = created.setOnMemoClickListener(listener)
                    override fun setOnRequestPasswordCheck(listener: (MemoEntity) -> Unit) = created.setOnRequestPasswordCheck(listener)
                    override fun updateFlatItems(items: List<MemoSectionListItem>) = created.updateFlatItems(items)
                    override fun updateMemoTags(tags: Map<Int, List<String>>) = created.updateMemoTags(tags)
                }

                val spanCount = 3
                val itemSpacingPx = Util.dpToPx(this@SearchActivity, 16)

                rv.layoutManager = GridLayoutManager(this@SearchActivity, spanCount)
                rv.adapter = created
                rv.addItemDecoration(GridSpacingItemDecoration(spanCount, itemSpacingPx))
            }

            MainLayoutMode.LIST -> {
                val created = MemoListAdapter(activity = this, isSearchMode = true)
                created.setOnMemoClickListener(onMemoClick)
                created.setOnRequestPasswordCheck(onRequestPassword)

                listAdapter = created
                searchAdapter = object : BaseSearchAdapter {
                    override fun setOnMemoClickListener(listener: (MemoEntity) -> Unit) = created.setOnMemoClickListener(listener)
                    override fun setOnRequestPasswordCheck(listener: (MemoEntity) -> Unit) = created.setOnRequestPasswordCheck(listener)
                    override fun updateFlatItems(items: List<MemoSectionListItem>) = created.updateFlatItems(items)
                    override fun updateMemoTags(tags: Map<Int, List<String>>) = created.updateMemoTags(tags)
                }

                val listItemSpacingPx = Util.dpToPx(this, 10)

                rv.layoutManager = LinearLayoutManager(this@SearchActivity)
                rv.adapter = created
                rv.addItemDecoration(
                    MainListSpacingItemDecoration(
                        spacing = listItemSpacingPx,
                        isMemoItemType = { pos ->
                            listAdapter.getItemViewType(pos) == MemoListAdapter.VIEW_TYPE_MEMO
                        },
                        isFirstMemoAfterSection = { pos ->
                            if (pos == 0) {
                                true
                            } else {
                                val prevType = listAdapter.getItemViewType(pos - 1)
                                val currentType = listAdapter.getItemViewType(pos)
                                currentType == MemoListAdapter.VIEW_TYPE_MEMO &&
                                        prevType != MemoListAdapter.VIEW_TYPE_MEMO &&
                                        prevType != MemoListAdapter.VIEW_TYPE_DATE_SECTION
                            }
                        },
                        isLastMemoBeforeSection = { pos ->
                            val itemCount = listAdapter.itemCount
                            if (pos == itemCount - 1) {
                                true
                            } else {
                                val nextType = listAdapter.getItemViewType(pos + 1)
                                val currentType = listAdapter.getItemViewType(pos)
                                currentType == MemoListAdapter.VIEW_TYPE_MEMO &&
                                        nextType != MemoListAdapter.VIEW_TYPE_MEMO
                            }
                        }
                    )
                )
            }

            else -> {}
        }

        setTags() // 태그 바인딩은 공통
    }


    private fun setTags() {
        CoroutineScope(Dispatchers.IO).launch {
            val taggingList = taggingDao.getAllTaggings()
            val tagList = tagDao.getAllTags()
            val tagIdToName = tagList.associate { it._id to it.tagName }

            val memoIdToTagNames = taggingList.groupBy { it.memoid }
                .mapValues { it.value.mapNotNull { tagIdToName[it.tagid] } }

            withContext(Dispatchers.Main) {
                searchAdapter.updateMemoTags(memoIdToTagNames)
            }
        }
    }

    private fun setupHeader() = with(binding.headerSearch) {
        initHeaderState()

        setOnTextChangedListener { rawText ->
            val trimmed = rawText.trim()
            currentSearchTagId = null               // 실시간 검색은 태그 모드 해제
            liveSearchJob?.cancel()

            if (trimmed.isEmpty()) {
                // 입력이 비면 초기 상태로
                initHeaderState()
            } else {
                // 타이핑 중: 버튼(돋보기) 스타일은 '입력중'으로, 결과는 실시간 노출
                currentHeaderState = HeaderState.SELECTOR_ACTIVE
                updateClearButton(trimmed)
                showSearchResult(true)

                liveSearchJob = lifecycleScope.launch {
                    // 과도한 DB 호출 방지
                    delay(150)

                    // 최신 텍스트와 동일한지 확인 (중간에 더 입력했을 수 있음)
                    val latest = binding.headerSearch.getInputText().trim()
                    if (latest != trimmed) return@launch

                    val result = searchByText(trimmed)
                    // 결과 노출 (없으면 EmptyItem)
                    currentHeaderState = HeaderState.SEARCH_RESULT
                    updateClearButton() // 아이콘을 clear로 변경
                    showSearchResult(true)
                    searchAdapter.updateFlatItems(groupMemosForSearch(result))
                    updateEmptyStateForResults(result.isNotEmpty())
                }
            }
        }

        setOnEditTextFocusListener {
            if (currentHeaderState == HeaderState.SEARCH_RESULT) {
                // 포커스 재획득 시에도 입력중 상태 표현은 유지 (아이콘/색 등)
                currentHeaderState = HeaderState.SELECTOR_ACTIVE
                updateClearButton(getInputText().trim())
                // 결과는 계속 보이도록 유지
                showSearchResult(true)
                setEmptyStateVisible(false)
            }
        }

        setOnEditorActionListener {
            // 여전히 엔터(또는 키보드 검색 액션)로도 검색 가능
            val text = getInputText().trim()
            currentSearchTagId = null
            liveSearchJob?.cancel()

            lifecycleScope.launch {
                val result = searchByText(text)

                if (result.isEmpty()) {
                    toastShort(this@SearchActivity, "해당 내용이 입력된 메모가 없습니다")
                    setEmptyStateVisible(false)
                    return@launch
                }

                currentHeaderState = HeaderState.SEARCH_RESULT
                updateClearButton()
                showClearButton(true)
                showSearchResult(true)
                searchAdapter.updateFlatItems(groupMemosForSearch(result))
                updateEmptyStateForResults(result.isNotEmpty())
                binding.headerSearch.clearFocus()
                hideKeyboard()
            }
        }

        setOnBackClickListener {
            handleBackAction()
        }

        setOnClearClickListener {
            val text = getInputText().trim()
            currentSearchTagId = null

            when (currentHeaderState) {
                HeaderState.INITIAL -> {
                    toastShort(context, "검색어를 입력해주세요")
                }
                HeaderState.SELECTOR_ACTIVE -> {
                    // 돋보기 버튼 누르면 확정 검색 (기존 동작 유지)
                    liveSearchJob?.cancel()
                    lifecycleScope.launch {
                        val result = searchByText(text)

                        if (result.isEmpty()) {
                            toastShort(this@SearchActivity, "해당 내용이 입력된 메모가 없습니다")
                            setEmptyStateVisible(false)
                            return@launch
                        }

                        currentHeaderState = HeaderState.SEARCH_RESULT
                        updateClearButton()
                        showClearButton(true)
                        showSearchResult(true)
                        searchAdapter.updateFlatItems(groupMemosForSearch(result))
                        updateEmptyStateForResults(result.isNotEmpty())
                        hideKeyboard()
                        binding.headerSearch.clearFocus()
                    }
                }
                HeaderState.SEARCH_RESULT -> {
                    // X(클리어) 버튼 동작
                    binding.headerSearch.clearText()
                    resetToInitialState()
                }
            }
        }
    }

    private fun initHeaderState() = with(binding.headerSearch) {
        setEditText("")
        setHint("검색어를 입력해주세요")
        showClearButton(true)
        showCheckbox(false)
        showDeleteButton(false)

        findViewById<View>(R.id.lyBack).visibility = View.VISIBLE
        findViewById<View>(R.id.lyDelete).visibility = View.GONE
        findViewById<View>(R.id.cbMainHeaderCheckbox).visibility = View.GONE

        currentHeaderState = HeaderState.INITIAL
        updateClearButton("")
        showSearchResult(false)
        setEmptyStateVisible(false)
    }

    private fun resetToInitialState() {
        initHeaderState()
    }

    private fun updateClearButton(trimmedText: String) = with(binding.headerSearch) {
        val btnClear = findViewById<ImageView>(R.id.btnClear)

        currentHeaderState =
            if (currentHeaderState != HeaderState.SEARCH_RESULT && trimmedText.isNotEmpty()) {
                HeaderState.SELECTOR_ACTIVE
            } else if (currentHeaderState != HeaderState.SEARCH_RESULT && trimmedText.isEmpty()) {
                HeaderState.INITIAL
            } else {
                currentHeaderState
            }

//        setEditTextEditable(currentHeaderState != HeaderState.SEARCH_RESULT)

        when (currentHeaderState) {
            HeaderState.INITIAL -> {
                btnClear.setImageResource(R.drawable.ic_search_selector)
                btnClear.isSelected = false
                btnClear.visibility = View.GONE
            }

            HeaderState.SELECTOR_ACTIVE -> {
                btnClear.setImageResource(R.drawable.ic_search_clear)
                btnClear.isSelected = false
                btnClear.visibility = View.VISIBLE
            }

            HeaderState.SEARCH_RESULT -> {
                btnClear.setImageResource(R.drawable.ic_search_clear)
                btnClear.isSelected = false
                btnClear.visibility = View.VISIBLE
            }
        }
    }

    private fun updateClearButton() {
        updateClearButton(binding.headerSearch.getInputText().trim())
    }

    private fun showSearchResult(show: Boolean) {
        binding.recyclerView.visibility = if (show) View.VISIBLE else View.GONE
        binding.tagSuggestionLayer.visibility = if (show) View.GONE else View.VISIBLE
        if (!show) setEmptyStateVisible(false)
    }

    private fun hideAllResultViews() {
        binding.recyclerView.visibility = View.GONE
        binding.tagSuggestionLayer.visibility = View.GONE
        setEmptyStateVisible(false)
    }

    private suspend fun searchByText(query: String): List<MemoEntity> {
        val memos = memoDao.getAllNoConditionMemos()
        val tags = tagDao.getAllTags()
        val taggings = taggingDao.getAllTaggings()

        val tagNameMap = tags.associateBy { it._id }
        val memoTagsMap = taggings.groupBy { it.memoid }

        return memos
            .filter { it.isDeleted.not() }
            .filter { memo ->
                val inTitle = memo.title.contains(query, ignoreCase = true)

                // Body 텍스트에서 특수 토큰 제거 후 검색
                val inBody = memo.body.any { row ->
                    val clean = row.text
                        .replace(HARU_MEMO_CHECKED, "")
                        .replace(HARU_MEMO_UNCHECKED, "")
                    clean.contains(query, ignoreCase = true)
                }

                val inTags = memoTagsMap[memo._id]
                    ?.mapNotNull { tagNameMap[it.tagid]?.tagName }
                    ?.any { it.contains(query, ignoreCase = true) } ?: false

                inTitle || inBody || inTags
            }
            .sortedByDescending { it.created }
    }

    private fun handleBackAction() {
        if (currentHeaderState == HeaderState.SEARCH_RESULT || currentSearchTagId != null) {
            currentSearchTagId = null
            resetToInitialState()
        } else {
            finish()
        }
    }

    // SectionHeader에 한정하지 말고 MemoSectionListItem 전체를 반환하도록 수정
    private fun groupMemosForSearch(memos: List<MemoEntity>): List<MemoSectionListItem> {
        return if (memos.isEmpty()) {
            listOf(MemoSectionListItem.EmptyItem)
        } else {
            listOf(
                MemoSectionListItem.SectionHeader(
                    sectionType = DynamicSectionType.TODAY,
                    title = DynamicSectionType.TODAY.sectionName,
                    isExpanded = true,
                    memos = memos
                )
            )
        }
    }

    private fun setEmptyStateVisible(visible: Boolean) {
        binding.tvEmptyState.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateEmptyStateForResults(hasResults: Boolean) {
        val isResultContext = (currentHeaderState == HeaderState.SEARCH_RESULT) || (currentSearchTagId != null)
        if (isResultContext) {
            setEmptyStateVisible(!hasResults)
        } else {
            setEmptyStateVisible(false)
        }
    }
}