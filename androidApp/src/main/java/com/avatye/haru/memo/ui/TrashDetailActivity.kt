package com.avatye.haru.memo.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.MemoEventFlow
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.database.MemoDatabase
import com.avatye.haru.memo.data.entity.BodyRow
import com.avatye.haru.memo.data.entity.MemoBlock
import com.avatye.haru.memo.data.entity.MemoEntity
import com.avatye.haru.memo.data.entity.MemoEvent
import com.avatye.haru.memo.data.enum.MemoMode
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_MEMO_PERMANENT_DELETE
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_MEMO_RESTORE
import com.avatye.haru.memo.data.utils.RowCodecUtil
import com.avatye.haru.memo.data.utils.Util
import com.avatye.haru.memo.data.utils.Util.Companion.toastShort
import com.avatye.haru.memo.databinding.ActivityTrashDetailBinding
import com.avatye.haru.memo.helper.MemoWidgetUpdater
import com.avatye.haru.memo.ui.adapter.MemoBlockAdapter
import com.avatye.haru.memo.ui.custom.dialog.MemoCustomDialog
import com.avatye.haru.memo.ui.custom.header.TrashHeaderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import androidx.core.net.toUri

internal class TrashDetailActivity : BaseActivity() {

    override val NAME: String
        get() = TrashDetailActivity::class.java.simpleName

    private var binding: ActivityTrashDetailBinding? = null
    private var memoAdapter: MemoBlockAdapter? = null
    private lateinit var memoItem: MemoEntity
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
        private const val EXTRA_MEMO_ITEM = "EXTRA:MEMO-ITEM"

        fun start(activity: Activity, memoItem: MemoEntity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, TrashDetailActivity::class.java).apply {
                    putExtra(EXTRA_MEMO_ITEM, memoItem)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityTrashDetailBinding.inflate(layoutInflater)
        binding = viewBinding
        setContentView(viewBinding.root)
        configureWindowInsets(viewBinding.trashDetailRoot, paddingDp = 0)

        viewBinding.headerTrashDetail.setMode(TrashHeaderView.TrashHeaderMode.DETAIL)

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = viewBinding.bottomBannerView
        )

        val memo = intent?.getParcelableExtra<MemoEntity>(EXTRA_MEMO_ITEM)
        if (memo != null) {
            memoItem = memo
            setupBackHandler()
            setupRecyclerView(memoItem)
            setupHeaderListeners()
            updateDeleteNotice(memoItem)
            setBackgroundColor()
        } else {
            finish()
        }
    }


    override fun onPause() {
        super.onPause()
        binding?.bottomBannerView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding?.bottomBannerView?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.headerTrashDetail?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
        binding = null
    }

    private fun setBackgroundColor() {
        val resolvedColor = try {
            val rawColor = memoItem.bgColor
            if (rawColor.isNullOrBlank()) {
                Color.WHITE
            } else {
                Color.parseColor(rawColor)
            }
        } catch (e: Exception) {
            LogTrack.e { "배경색 파싱 실패 : $e" }
            Color.WHITE
        }

        if (memoItem.isLocked) {
            val context = binding?.root?.context ?: return
            val resolvedColor = ContextCompat.getColor(context, R.color.haru_lock_header_color)

            binding?.headerTrashDetail?.let { header ->
                header.findViewById<View>(R.id.trashHeaderRootView)?.setBackgroundColor(resolvedColor)
                header.findViewById<View>(R.id.trashHeaderContentView)?.setBackgroundColor(resolvedColor)
            }

            binding?.trashDetailRoot?.setBackgroundColor(resolvedColor)
        } else {
            binding?.headerTrashDetail?.let { header ->
                header.findViewById<View>(R.id.trashHeaderRootView)?.setBackgroundColor(resolvedColor)
                header.findViewById<View>(R.id.trashHeaderContentView)
                    ?.setBackgroundColor(resolvedColor)
            }

            binding?.trashDetailRoot?.setBackgroundColor(resolvedColor)
        }
    }


    private fun setupHeaderListeners() {
        binding?.headerTrashDetail?.apply {
            setOnBackClickListener {
                finish()
            }

            setOnSelectClickListener {
                // 복원: 다이얼로그 없이 즉시 처리
                lifecycleScope.launch(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    MemoDatabase.getInstance(this@TrashDetailActivity)
                        .memoDao().restoreMemos(listOf(memoItem.uuid), now)
                    withContext(Dispatchers.Main) {
                        PreferenceUtil.set(KEY_MEMO_RESTORE, true)
                        MemoWidgetUpdater().observeMemoChanges(this@TrashDetailActivity)
                        EventUtil.sendEvent(this@TrashDetailActivity, EventUtil.CATEGORY_TRASH, EventUtil.ACTION_TRASH_RESTORE)
                        toastShort(this@TrashDetailActivity, getString(R.string.haru_trash_toast_memo_restoration))
                        MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                        finish()
                    }
                }
            }

            setOnClearAllClickListener {
                if (isFinishing) return@setOnClearAllClickListener

                val dialog = MemoCustomDialog(this@TrashDetailActivity).apply {
                    setTitle(getString(R.string.haru_trash_delete_dialog_delete_title))
                    setMessage(getString(R.string.haru_trash_delete_dialog_delete_message))
                    setSubMessage(getString(R.string.haru_trash_delete_dialog_delete_description))
                    setButton(
                        cancelText = getString(R.string.haru_dialog_common_cancel),
                        confirmText = getString(R.string.haru_trash_delete_dialog_delete_confirm),
                        onCancel = { dismiss() },
                        onConfirm = {
                            lifecycleScope.launch(Dispatchers.IO) {
                                deleteMemosWithCleanup(uuidList = listOf(memoItem.uuid))
                                withContext(Dispatchers.Main) {
                                    PreferenceUtil.set(KEY_MEMO_PERMANENT_DELETE, true)
                                    EventUtil.sendEvent(this@TrashDetailActivity, EventUtil.CATEGORY_TRASH, EventUtil.ACTION_TRASH_DELETE)
                                    toastShort(
                                        this@TrashDetailActivity,
                                        getString(R.string.haru_trash_toast_memo_delete_permanently)
                                    )
                                    finish()
                                }
                            }
                            dismiss()
                        }
                    )
                }
                dialog.show()
            }
        }
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

    private fun updateDeleteNotice(memo: MemoEntity) {
        val millisPerDay = 24 * 60 * 60 * 1000L

        // 1. 삭제일을 자정으로 내림
        val deletedMidnight = Calendar.getInstance().apply {
            timeInMillis = memo.deleted
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 2. 삭제 마감일 = 삭제일 자정 + 14일
        val deletedDeadline = deletedMidnight + (14 * millisPerDay)

        // 3. 오늘 자정
        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 4. 남은 일 수 계산
        val daysLeft = (deletedDeadline - todayMidnight) / millisPerDay

        // 5. 텍스트 표시
        binding?.textTrashNotice?.text = when {
            daysLeft > 0 -> getString(R.string.haru_trash_toast_notice_days_left, daysLeft)
            daysLeft == 0L -> getString(R.string.haru_trash_toast_notice_today)
            else -> getString(R.string.haru_trash_toast_notice_expired)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerView(memo: MemoEntity) {
        memoAdapter = MemoBlockAdapter(this)
        memoAdapter?.setMemoMode(MemoMode.READ_MEMO)

        // 아이템 터치 차단 / 스크롤허용
        binding?.viewTouchBlocker?.apply {
            visibility = View.VISIBLE
            isClickable = true
            isFocusable = true
            setOnTouchListener { _, event ->
                // 스크롤(드래그) 제스처만 아래로 전달하고 나머지는 차단
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        // RecyclerView에 스크롤 이벤트 강제로 전달
                        binding?.recyclerContent?.dispatchTouchEvent(event)
                        true // 이벤트는 우리가 소비해서 아이템 클릭은 차단됨
                    }

                    else -> true // 클릭 등은 차단
                }
            }
        }

        binding?.recyclerContent?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = memoAdapter

            if (memo.isLocked) {
                val context = binding?.root?.context ?: return
                val resolvedColor = ContextCompat.getColor(context, R.color.haru_lock_detail_color)
                setBackgroundColor(resolvedColor)
            } else {
                try {
                    val bgColor = memo.bgColor
                    val parsedColor = if (bgColor.isNullOrBlank()) {
                        Color.WHITE // 기본 색상
                    } else {
                        Color.parseColor(bgColor)
                    }
                    setBackgroundColor(parsedColor)
                } catch (e: IllegalArgumentException) {
                    LogTrack.e(NAME) { "색상 파싱 실패: ${memo.bgColor}\", $e" }
                    setBackgroundColor(Color.WHITE) // fallback
                }
            }
        }

        val blocks = mutableListOf<MemoBlock>()

        // 날짜
        blocks += MemoBlock.DateBlock(Util.formatDate(memo.created))

        // 제목
        if (!memo.title.isNullOrEmpty()) {
//            blocks += MemoBlock.TitleBlock(memo.title)
        }

        // 내용
        blocks += MemoBlock.DateBlock(Util.formatDate(memo.created))

        val rows = mutableListOf<BodyRow>()
        memo.title.takeIf { !it.isNullOrBlank() }?.let { title ->
            rows += RowCodecUtil.parse(title)
        }
        memo.body.forEach { body ->
            rows += RowCodecUtil.parse(body.text)
        }
        if (rows.isNotEmpty()) {
            blocks += MemoBlock.BodyBlock(bodyRows = rows)
        }

        // 이미지
        if (memo.imagePath.isNotEmpty()) {
            val imageIndexList = memo.imagePath.toSortedMap()
            imageIndexList.forEach { (index, pathList) ->
                val uriList = pathList.mapNotNull { path ->
                    try {
                        val uri = path.toUri()
                        uri.toString()   // String 으로 변환해서 저장
                    } catch (e: Exception) {
                        LogTrack.e(NAME) { "이미지 Uri 파싱 실패: $path, $e" }
                        null
                    }
                }

                if (uriList.isNotEmpty()) {
                    val insertPos = (index + 3).coerceIn(0, blocks.size)
                    blocks.add(insertPos, MemoBlock.ImageUriBlock(uris = uriList.toMutableList())) // Uri 기반 블록
                }
            }
        }

        memoAdapter?.submitBlockList(blocks)
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()

            }
        })
    }
}
