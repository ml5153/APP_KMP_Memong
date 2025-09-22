package com.memong.aos.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.memong.aos.BuildConfig
import com.memong.aos.MemoEventFlow
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_MEMO_PERMANENT_DELETE
import com.memong.aos.data.utils.PreferenceUtil.KEY_MEMO_RESTORE
import com.memong.aos.data.utils.Util
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.databinding.ActivityTrashBinding
import com.memong.aos.helper.MemoWidgetUpdater
import com.memong.aos.ui.adapter.TrashAdapter
import com.memong.aos.ui.custom.dialog.MemoCustomDialog
import com.memong.aos.ui.custom.header.TrashHeaderView.TrashHeaderMode
import com.memong.aos.ui.custom.item.GridSpacingItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

internal class TrashActivity : BaseActivity() {

    override val NAME: String
        get() = TrashActivity::class.java.simpleName

    private lateinit var binding: ActivityTrashBinding
    private lateinit var adapter: TrashAdapter

    private val selectedIds = mutableSetOf<String>()
    private var isEditMode = false

    private val memoDao by lazy {
        MemoDatabase.getInstance(this).memoDao()
    }

    private val tagDao by lazy {
        MemoDatabase.getInstance(this).tagDao()
    }

    private val taggingDao by lazy {
        MemoDatabase.getInstance(this).taggingDao()
    }

    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, TrashActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.trashRootView, paddingDp = 0)

        setupRecyclerView()
        setupHeader()
        setupBackHandler()
        cleanExpiredTrash()
        loadDeletedMemos()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )
    }

    override fun onPause() {
        super.onPause()
        binding.bottomBannerView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.bottomBannerView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (PreferenceUtil.get(KEY_MEMO_RESTORE, false)) {
            loadDeletedMemos()
        }
        if (PreferenceUtil.get(KEY_MEMO_PERMANENT_DELETE, false)) {
            loadDeletedMemos()
            PreferenceUtil.set(KEY_MEMO_PERMANENT_DELETE, false)
        }
        binding.bottomBannerView.onResume()
    }

    private fun setupRecyclerView() {
        val spanCount = 3
        val itemSpacingPx = Util.dpToPx(this, 16)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@TrashActivity, spanCount)
            addItemDecoration(GridSpacingItemDecoration(spanCount, itemSpacingPx))
        }
    }

    private fun loadDeletedMemos() {
        lifecycleScope.launch {
            val memos = withContext(Dispatchers.IO) {
                memoDao.getDeleteMemos()
            }

            binding.recyclerView.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
            binding.headerTrash.setButtonsVisibility(true)

            if (!::adapter.isInitialized) {
                adapter = TrashAdapter(mutableListOf()) // 빈 리스트로 초기화
                binding.recyclerView.adapter = adapter

                // 리스너 설정은 한 번만!
                adapter.onSelectChanged = {
                    selectedIds.clear()
                    selectedIds.addAll(it)
                    binding.headerTrash.setTitleCount(selectedIds.size)
                    val isAllSelected = selectedIds.size == adapter.items.size
                    binding.headerTrash.setCheckAllState(isAllSelected)
                }

                adapter.onRequestEditMode = { initialId ->
                    if (!isEditMode) {
                        enterEditMode()
                        selectedIds.add(initialId)
                        adapter.setEditMode(true, selectedIds)
                        binding.headerTrash.setTitleCount(selectedIds.size)
                        binding.headerTrash.setCheckAllState(selectedIds.size == adapter.items.size)
                    }
                }

                adapter.onItemClick = { memo ->
                    if (!isEditMode) {
                        TrashDetailActivity.start(this@TrashActivity, memo)
                    }
                }
            }

            adapter.items.clear()
            adapter.items.addAll(memos)
            adapter.notifyDataSetChanged()
            exitEditMode()

            if (memos.isNullOrEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.headerTrash.setButtonsVisibility(false)
            }
        }
    }

    private fun cleanExpiredTrash() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 오늘 자정을 한국 시간대로 정확히 계산
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -14)  // 14일 전 자정
            }

            val cutoff = calendar.timeInMillis  // 자정 기준 14일 전

            memoDao.deleteExpiredTrash(cutoff)

            withContext(Dispatchers.Main) {
                loadDeletedMemos()
            }
        }
    }

    private fun setupHeader() {
        binding.headerTrash.apply {
            setMode(TrashHeaderMode.NORMAL)

            setOnBackClickListener {
                if (isEditMode) exitEditMode() else finish()
            }

            setOnSelectClickListener {
                if (isEditMode) {
                    // 선택 복원 실행
                    if (selectedIds.isEmpty()) {
                        toastShort(
                            this@TrashActivity,
                            context.getString(R.string.haru_empty_select_memo)
                        )
                        return@setOnSelectClickListener
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        val now = System.currentTimeMillis()
                        memoDao.restoreMemos(selectedIds.toList(), now)
                        withContext(Dispatchers.Main) {
                            exitEditMode()
                            PreferenceUtil.set(KEY_MEMO_RESTORE, true)
                            MemoWidgetUpdater().observeMemoChanges(this@TrashActivity)
                            toastShort(this@TrashActivity, "선택하신 메모가 복원되었습니다.")
                            EventUtil.sendEvent(this@TrashActivity, EventUtil.CATEGORY_TRASH, EventUtil.ACTION_TRASH_RESTORE)
                            MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                            loadDeletedMemos()
                        }
                    }
                } else {
                    enterEditMode()
                }
            }

            setOnClearAllClickListener {
                if (isEditMode) {
                    // 선택 영구삭제 실행
                    if (selectedIds.isEmpty()) {
                        toastShort(
                            this@TrashActivity,
                            context.getString(R.string.haru_empty_select_memo)
                        )
                        return@setOnClearAllClickListener
                    }
                    showDeleteMemosDialog(selectedIds.toList())
                } else {
                    showDeleteAllMemosDialog()
                }
            }

            setOnCheckAllChangedListener { checked ->
                toggleSelectAll(checked)
            }
        }
    }


    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEditMode) {
                    exitEditMode()
                } else {
                    finish()
                }
            }
        })
    }

    private fun enterEditMode() {
        isEditMode = true
        selectedIds.clear()
        adapter.setEditMode(true, selectedIds)
        binding.headerTrash.setMode(TrashHeaderMode.EDIT)
        binding.headerTrash.setTitleCount(0)
    }

    private fun exitEditMode() {
        isEditMode = false
        selectedIds.clear()
        adapter.setEditMode(false, selectedIds)
        binding.headerTrash.setMode(TrashHeaderMode.NORMAL)
        binding.headerTrash.setCheckAllState(false)
    }

    private fun toggleSelectAll(checked: Boolean) {
        selectedIds.clear()
        if (checked) {
            selectedIds.addAll(adapter.items.map { it.uuid })
        }
        adapter.setEditMode(true, selectedIds)
        binding.headerTrash.setTitleCount(selectedIds.size)
        binding.headerTrash.setCheckAllState(selectedIds.size == adapter.items.size)
    }

    private fun showDeleteMemosDialog(selectedIds: List<String>) {
        if (isFinishing) return
        val dialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_trash_delete_dialog_delete_title))
            setMessage(getString(R.string.haru_trash_delete_dialog_delete_message))
            setSubMessage(getString(R.string.haru_trash_delete_dialog_delete_description))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_trash_delete_dialog_delete_confirm),
                onCancel = { dismiss() },
                onConfirm = {
                    lifecycleScope.launch(Dispatchers.IO) {
                        deleteMemosWithCleanup(uuidList = selectedIds)
                        withContext(Dispatchers.Main) {
                            exitEditMode()
                            EventUtil.sendEvent(this@TrashActivity, EventUtil.CATEGORY_TRASH, EventUtil.ACTION_TRASH_DELETE)
                            toastShort(this@TrashActivity, "선택하신 메모가 영구삭제되었습니다.")
                            loadDeletedMemos()
                        }
                    }
                    dismiss()
                }
            )
        }
        dialog.show()
    }

    private fun showDeleteAllMemosDialog() {
        if (isFinishing) return
        val dialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_trash_delete_dialog_delete_title))
            setMessage(getString(R.string.haru_trash_delete_dialog_delete_message))
            setSubMessage(getString(R.string.haru_trash_delete_dialog_delete_description))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_trash_delete_dialog_delete_confirm),
                onCancel = { dismiss() },
                onConfirm = {
                    lifecycleScope.launch(Dispatchers.IO) {
                        deleteMemosWithCleanup(purgeAllDeletedMemos = true)
                        withContext(Dispatchers.Main) {
                            exitEditMode()
                            EventUtil.sendEvent(this@TrashActivity, EventUtil.CATEGORY_TRASH, EventUtil.ACTION_TRASH_ALL_DELETE)
                            toastShort(this@TrashActivity, "휴지통이 비워졌습니다.")
                            loadDeletedMemos()
                        }
                    }
                    dismiss()
                }
            )
        }
        dialog.show()
    }

    private suspend fun deleteMemosWithCleanup(
        uuidList: List<String>? = null, // null이면 전체 삭제
        purgeAllDeletedMemos: Boolean = false
    ) {
        val tagIdsToCheck = mutableSetOf<Int>()
        val memoIds = mutableListOf<Int>()
        val selectedUuids = mutableListOf<String>()

        // UUID로 대상 메모 확인
        if (uuidList != null) {
            selectedUuids.addAll(uuidList)
            LogTrack.d { "삭제 대상 UUID: $selectedUuids" }

            for (uuid in selectedUuids) {
                val memoId = memoDao.getIdByUUID(uuid)
                if (memoId == null) {
                    LogTrack.d { "⚠memoId not found for UUID: $uuid" }
                    continue
                }
                memoIds.add(memoId)
                LogTrack.d { "매핑된 memoId: $memoId (from UUID: $uuid)" }

                val tags = tagDao.getTagsForMemo(memoId)
                val tagIds = tags.map { it._id }
                tagIdsToCheck.addAll(tagIds)
                LogTrack.d { "memoId=$memoId → tagIds=$tagIds" }

                taggingDao.deleteAllTagsForMemo(memoId)
                LogTrack.d { "태깅 삭제 완료: memoId=$memoId" }
            }

            memoDao.deleteMemosPermanently(selectedUuids)
            LogTrack.d { "메모 삭제 완료: uuids=$selectedUuids" }
        }

        // 전체 삭제 (purgeDeletedMemos)
        if (purgeAllDeletedMemos) {
            val deletedMemos = memoDao.getDeleteMemos()
            val deletedUuids = deletedMemos.map { it.uuid }
            val deletedIds = deletedMemos.map { it._id }

            LogTrack.d { "휴지통 전체 삭제 시작: ${deletedUuids.size}건" }

            for (memoId in deletedIds) {
                val tags = tagDao.getTagsForMemo(memoId)
                val tagIds = tags.map { it._id }
                tagIdsToCheck.addAll(tagIds)
                taggingDao.deleteAllTagsForMemo(memoId)
            }

            memoDao.purgeDeletedMemos()
            LogTrack.d { "휴지통 비우기 완료" }
        }

        // 태그 정리
        for (tagId in tagIdsToCheck) {
            val stillTagged = taggingDao.countTaggingsForTag(tagId) > 0
            if (!stillTagged) {
                tagDao.deleteQueryTag(tagId)
                LogTrack.d { "태그 삭제 완료: tagId=$tagId" }
            } else {
                LogTrack.d { "태그 유지됨 (다른 태깅 존재): tagId=$tagId" }
            }
        }

        // dangling tagging 정리
        taggingDao.purgeDanglingTaggings()
        LogTrack.d { "dangling tagging 정리 완료" }

        // dangling tag 정리
        tagDao.purgeDanglingTags()
        LogTrack.d { "dangling tag 정리 완료" }
    }

}
