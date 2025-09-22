package com.avatye.haru.memo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.databinding.ActivityTagManagementBinding
import com.avatye.haru.memo.data.database.MemoDatabase
import com.avatye.haru.memo.data.entity.MemoEntity
import com.avatye.haru.memo.data.entity.TagEntity
import com.avatye.haru.memo.data.entity.TagItem
import com.avatye.haru.memo.data.entity.TaggingEntity
import com.avatye.haru.memo.data.extension.getParcelableCompat
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.data.utils.Util.Companion.toastShort
import com.avatye.haru.memo.ui.adapter.TagAdapter
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class TagManagementActivity : BaseActivity() {

    override val NAME: String
        get() = TagManagementActivity::class.java.simpleName

    private lateinit var binding: ActivityTagManagementBinding
    private lateinit var tagAdapter: TagAdapter
    private lateinit var memoItem: MemoEntity

    val taggingDao by lazy {
        MemoDatabase.getInstance(this@TagManagementActivity).taggingDao()
    }

    enum class TagManageMode { ADD, DELETE, DETAIL }

    companion object {
        private const val EXTRA_MANAGE_MODE = "EXTRA:MANAGE-MODE"
        private const val EXTRA_MEMO_ITEM = "EXTRA:MEMO-ITEM"

        fun start(activity: Activity, mode: TagManageMode, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, TagManagementActivity::class.java).apply {
                    putExtra(EXTRA_MANAGE_MODE, mode.name)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }


        fun createIntent(
            activity: Activity,
            mode: TagManageMode,
            memoItem: MemoEntity?
        ): Intent {
            return Intent(activity, TagManagementActivity::class.java).apply {
                putExtra(EXTRA_MANAGE_MODE, mode.name)
                putExtra(EXTRA_MEMO_ITEM, memoItem)
            }
        }
    }

    private var mode: TagManageMode = TagManageMode.ADD

    val tagDao by lazy {
        MemoDatabase.getInstance(this@TagManagementActivity).tagDao()
    }

    private val tagList = mutableListOf<TagItem.SelectableTag>()

    private fun setBackKeyListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogTrack.i(NAME) { "setBackKeyListener::handleOnBackPressed" }

                setResult(Activity.RESULT_OK)
                finish()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTagManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.tagManagementRootView, paddingDp = 0)

        mode = intent.getStringExtra(EXTRA_MANAGE_MODE)?.let {
            TagManageMode.valueOf(it)
        } ?: TagManageMode.ADD

        memoItem = intent?.getParcelableCompat<MemoEntity>(EXTRA_MEMO_ITEM) ?: MemoEntity.empty()

        setBackKeyListener()
        setupHeader()
        setupRecyclerView()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )

        lifecycleScope.launch {
            tagList.clear()
            tagList.addAll(loadTagsFromDb())
            renderTags()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.bottomBannerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomBannerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.bottomBannerView.onDestroy()
    }


    private suspend fun loadTagsFromDb(): List<TagItem.SelectableTag> {
        val allTags = tagDao.getAllTags()

        // 🔍 Tagging 정보는 DETAIL 모드일 때만 조회
        val taggingList = if (mode == TagManageMode.DETAIL) {
            taggingDao.getTagsByMemoId(memoItem._id)
        } else {
            emptyList() // DELETE, ADD 모드에서는 선택된 태그 없음
        }

        val selectedTagIds = taggingList.map { it.tagid }.toSet()

        return allTags.map {
            TagItem.SelectableTag(
                id = it._id,
                tag = it.tagName,
                isChecked = selectedTagIds.contains(it._id), // DETAIL 모드일 때만 true
                createdAt = it.created
            )
        }
    }


    private fun setupHeader() = with(binding.headerSearch) {

        setOnBackClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        when (mode) {
            TagManageMode.ADD -> {
                setHint("새로운 태그 첨부")
                showCheckbox(false)
                showDeleteButton(false)
                setEditText("")
                setEditTextEnabled(true)
                showClearButton(false)

                setOnClearClickListener {
                    setEditText("")
                    renderTags()
                }

                setOnTextChangedListener { text ->
                    val trimmed = text.trim()
                    if (trimmed.isEmpty()) {
                        renderTags()
                        return@setOnTextChangedListener
                    }

                    val inputTags = trimmed.split(",")
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                        .distinct()

                    val existingTags = tagList
                        .map { it.tag.removePrefix("#").lowercase() }

                    val newTags = inputTags.filter { it !in existingTags }

                    if (newTags.isEmpty()) {
                        renderTags()
                    } else {
                        val displayText = newTags.joinToString(" ") { "#$it" }
                        val addable = TagItem.AddableTag(tag = displayText)
                        val sorted = tagList.sortedByDescending { it.createdAt }
                        tagAdapter.submitList(listOf(addable) + sorted)
                    }
                }
            }

            TagManageMode.DELETE -> {
                setDeleteMode(enabled = true, selectedCount = 0)

                setOnTextChangedListener {}
                setOnClearClickListener {}

                setOnHeaderCheckboxChanged { isChecked ->
                    tagAdapter.toggleAllCheckboxes(isChecked)
                    updateHeaderCheckedCount()
                }

                setOnDeleteClickListener {
                    val selectedTags = tagList.filter { it.isChecked }

                    if (selectedTags.isEmpty()) {
                        toastShort(context, "삭제할 태그를 선택해주세요.")
                        return@setOnDeleteClickListener
                    }

                    lifecycleScope.launch {
                        val toDeleteEntities = selectedTags.map {
                            TagEntity(
                                _id = it.id,
                                tagName = it.tag,
                                created = it.createdAt
                            )
                        }
                        tagDao.deleteTags(toDeleteEntities)
                        tagList.removeAll { it.isChecked }
                        toastShort(context, "선택하신 태그가 삭제되었습니다.")
                        renderTags()
                        setHeaderCheckbox(false)
                        updateSelectedCount(getSelectedCount())
                    }
                }
            }

            TagManageMode.DETAIL -> {
                setHint("새로운 태그 첨부")
                showCheckbox(false)
                showDeleteButton(false)
                setEditText("")
                setEditTextEnabled(true)
                showClearButton(false)

                setOnClearClickListener {
                    setEditText("")
                    renderTags()
                }

                setOnTextChangedListener { text ->
                    val trimmed = text.trim()
                    if (trimmed.isEmpty()) {
                        renderTags()
                        return@setOnTextChangedListener
                    }

                    val inputTags = trimmed.split(",")
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                        .distinct()

                    val existingTags = tagList
                        .map { it.tag.removePrefix("#").lowercase() }

                    val newTags = inputTags.filter { it !in existingTags }

                    if (newTags.isEmpty()) {
                        renderTags()
                    } else {
                        val displayText = newTags.joinToString(" ") { "#$it" }
                        val addable = TagItem.AddableTag(tag = displayText)
                        val sorted = tagList.sortedByDescending { it.createdAt }
                        tagAdapter.submitList(listOf(addable) + sorted)
                    }
                }
            }

            else -> {}
        }
    }

    private fun setupRecyclerView() {
        LogTrack.i(NAME) { "setupRecyclerView -> { mode : $mode }" }
        tagAdapter = TagAdapter(
            onAddClick = { inputText ->
                val rawTags = inputText.split(",")
                    .map { it.trim().removePrefix("#") }
                    .filter { it.isNotBlank() }
                    .distinct()

                val existingTagNames = tagList.map { it.tag.removePrefix("#").lowercase() }
                val newTagNames = rawTags.filter { it.lowercase() !in existingTagNames }

                if (newTagNames.isNotEmpty()) {
                    val baseTime = System.currentTimeMillis()
                    val newTagEntities = newTagNames.mapIndexed { index, tag ->
                        TagEntity(
                            tagName = "#$tag",
                            created = baseTime + (index * 100)
                        )
                    }

                    lifecycleScope.launch(Dispatchers.IO) {
                        val insertedIds = tagDao.insertTags(newTagEntities)
                        val newSelectableTags = newTagEntities.mapIndexed { index, tag ->
                            TagItem.SelectableTag(
                                id = insertedIds[index].toInt(),
                                tag = tag.tagName,
                                isChecked = true,
                                createdAt = tag.created
                            )
                        }
                        tagList.addAll(0, newSelectableTags)

                        // 태깅 추가
                        newSelectableTags.forEach { tag ->
                            val tagging = TaggingEntity(
                                memoid = memoItem._id,
                                tagid = tag.id
                            )
                            taggingDao.insertTagging(tagging)
                        }



                        withContext(Dispatchers.Main) {
                            binding.headerSearch.setEditText("")
                            renderTags()
                        }

                    }
                }
            },
            onTagCheckChanged = { _, _ ->
                updateHeaderCheckedCount()
            },
            showCheckbox = mode == TagManageMode.DELETE || mode == TagManageMode.DETAIL
        )
        binding.recyclerView.adapter = tagAdapter
        renderTags()
    }

    private fun renderTags() {
        tagAdapter.updateShowCheckbox(mode == TagManageMode.DELETE || mode == TagManageMode.DETAIL)
        val sorted = tagList.sortedByDescending { it.createdAt }
        tagAdapter.submitList(sorted)
    }

    private fun updateHeaderCheckedCount() {
        when (mode) {
            TagManageMode.ADD, TagManageMode.DELETE -> {
                val count = tagAdapter.getSelectedTags().size
                binding.headerSearch.setEditText("${count}개")
                binding.headerSearch.setHeaderCheckbox(tagAdapter.getSelectedTags().size == tagList.size)
            }

            TagManageMode.DETAIL -> {

            }
        }

        // 🔒 모드가 DETAIL일 때만 DB에 저장
        if (mode == TagManageMode.DETAIL) {
            lifecycleScope.launch(Dispatchers.IO) {
                taggingDao.deleteAllTagsForMemo(memoItem._id)
                tagAdapter.getSelectedTags().forEach { tag ->
                    val tagging = TaggingEntity(
                        memoid = memoItem._id,
                        tagid = tag.id
                    )
                    taggingDao.insertTagging(tagging)
                }
            }
        }
    }

    private fun getSelectedCount(): Int {
        return tagList.count { it.isChecked }
    }
}