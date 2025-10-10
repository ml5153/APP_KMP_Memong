package com.memong.aos.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.google.firebase.ai.type.content
import com.memong.aos.BuildConfig
import com.memong.aos.MemongApplication
import com.memong.aos.MemoEventFlow
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.entity.BodyItem
import com.memong.aos.data.entity.BodyRow
import com.memong.aos.data.entity.MemoBlock
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.data.enum.MemoSortType
import com.memong.aos.data.extension.getParcelableCompat
import com.memong.aos.data.extension.hideKeyboard
import com.memong.aos.data.extension.isAlive
import com.memong.aos.data.extension.showIme
import com.memong.aos.data.extension.start
import com.memong.aos.data.extension.toHex
import com.memong.aos.data.utils.AutoBackupUtil
import com.memong.aos.data.utils.BaseUtil
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.FileUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_PASSWORD_SWITCH
import com.memong.aos.data.utils.RowCodecUtil
import com.memong.aos.data.utils.Util
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.databinding.ActivityMemoDetailBinding
import com.memong.aos.helper.MemoWidgetUpdater
import com.memong.aos.ui.adapter.MemoBlockAdapter
import com.memong.aos.ui.adapter.itemDecoration.HorizontalSpaceItemDecoration
import com.memong.aos.ui.custom.bottomsheet.MemoInfoBottomSheetCustomDialog
import com.memong.aos.ui.custom.dialog.DialogMode
import com.memong.aos.ui.custom.dialog.MemoColorCustomDialog
import com.memong.aos.ui.custom.dialog.MemoCustomAiAnswerDialog
import com.memong.aos.ui.custom.dialog.MemoCustomDialog
import com.memong.aos.ui.custom.header.SettingHeaderView
import com.memong.aos.ui.custom.menu.MemoMenuView
import com.memong.aos.ui.viewmodel.MemoDetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


internal class MemoDetailActivity : BaseActivity() {

    override val NAME: String
        get() = MemoDetailActivity::class.java.simpleName
    private lateinit var binding: ActivityMemoDetailBinding

    companion object {
        private const val EXTRA_MODE = "EXTRA:MEMO-MODE"
        private const val EXTRA_MEMO_ITEM = "EXTRA:MEMO-ITEM"

        const val EXTRA_UPDATED_MEMO_ID = "EXTRA:UPDATED-MEMO-ID"

        const val TITLE_END_POS = 0
        const val IMAGE_END_POS = 1


        fun start(
            activity: Activity,
            mode: MemoMode,
            memoItem: MemoEntity? = null,
            close: Boolean = false
        ) {
            activity.start(
                intent = Intent(activity, MemoDetailActivity::class.java).apply {
                    putExtra(EXTRA_MODE, mode as Parcelable)
                    putExtra(EXTRA_MEMO_ITEM, memoItem)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }

        fun startWidget(
            context: Context,
            mode: MemoMode,
            memoItem: MemoEntity? = null
        ) {
            val intent = Intent(context, MemoDetailActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode as Parcelable)
                putExtra(EXTRA_MEMO_ITEM, memoItem)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }

        fun getIntent(
            activity: Activity,
            mode: MemoMode,
            memoItem: MemoEntity? = null
        ): Intent {
            return Intent(activity, MemoDetailActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode as Parcelable)
                putExtra(EXTRA_MEMO_ITEM, memoItem)
            }
        }
    }

    private val deleteDialog by lazy {
        MemoCustomDialog(this@MemoDetailActivity)
    }

    private var isImportant = false
    private var isBullet = false
    private var isInsertingBullet = false
    private var isImageSelectionMode = false

    private var regiPasswordDialog: MemoCustomDialog? = null
    private var memoColorCustomDialog: MemoColorCustomDialog? = null

    private lateinit var cameraImageUri: Uri
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var tagLauncher: ActivityResultLauncher<Intent>
    private lateinit var drawLauncher: ActivityResultLauncher<Intent>

    var memoSortType: MemoSortType
        set(value) {
            PreferenceUtil.set(
                key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE,
                value = value.name
            )
        }
        get() {
            return when (PreferenceUtil.get(
                key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE,
                defaultValue = MemoSortType.CUSTOM.name
            )) {
                MemoSortType.LATEST_CREATE.name -> MemoSortType.LATEST_CREATE
                MemoSortType.OLDEST_CREATE.name -> MemoSortType.OLDEST_CREATE
                MemoSortType.LATEST_UPDATE.name -> MemoSortType.LATEST_UPDATE
                MemoSortType.OLDEST_UPDATE.name -> MemoSortType.OLDEST_UPDATE
                else -> MemoSortType.CUSTOM
            }
        }

    val memoDao by lazy {
        MemoDatabase.getInstance(this@MemoDetailActivity).memoDao()
    }
    val tagDao by lazy {
        MemoDatabase.getInstance(this@MemoDetailActivity).tagDao()
    }
    val taggingDao by lazy {
        MemoDatabase.getInstance(this@MemoDetailActivity).taggingDao()
    }

    private var etTitle: EditText? = null
    private var etBody: EditText? = null
    private val memoBlockAdapter: MemoBlockAdapter by lazy {
        MemoBlockAdapter(this@MemoDetailActivity)
    }

    private val viewModel: MemoDetailViewModel by viewModels()
    val memoItem: MemoEntity
        get() = viewModel.memoItem.value ?: MemoEntity.empty()

    val memoMode: MemoMode
        get() = viewModel.memoMode.value


    private val matchRanges = mutableListOf<Pair<Int, Int>>()
    private var currentMatchIndex = 0
    private var currentBgHexColor = "#FFFFFF"


    private val generativeModel by lazy {
        (application as MemongApplication).generativeModel
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteDialog.onDestroy()
        binding.headerDetail.onDestroy()
        binding.headerSearch.onDestroy()
        regiPasswordDialog?.onDestroy()
        binding.editorToolBar.onDestroy()
        memoColorCustomDialog?.onDestroy()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.memoDetailRootView, paddingDp = 0)

        val memoMode = intent?.getParcelableCompat<MemoMode>(EXTRA_MODE) ?: MemoMode.READ_MEMO
        val extraMemoItem =
            intent?.getParcelableCompat<MemoEntity>(EXTRA_MEMO_ITEM) ?: MemoEntity.empty()
        viewModel.setMemoItem(extraMemoItem)

        // setMode
        setModeView(mode = memoMode)

        // Memo Background Color
        setBgColor()

        // recyclerView
        setRecyclerViews()

        // Soft-BackKer
        setBackKeyListener()

        // toolbar
        setEditorToolbar()

        // Memo-Search
        setMemoSearchClickListener()

        // Camera/Gallery/TAG/Draw Launcher
        setCameraLauncher()
        setGalleryLauncher()
        setTagLauncher()
        setDrawLauncher()

        // ViewModel
        observeViewModel()

        // EventFlow
        observeEventFlow()

        // Keypad-Scroll
        setAdjustKeyboard()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )

    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.memoBlocks.collect { blocks ->
                LogTrack.d {
                    blocks.withIndex()
                        .joinToString("\n") { "[${it.index}] ${it.value::class.simpleName}" }
                }
                memoBlockAdapter.submitBlockList(blocks.toList())
                updateImageSelectBtnVisibility()
            }
        }

        lifecycleScope.launch {
            viewModel.memoItem.collect { memo ->
                memo?.bgColor?.let { color ->
                    try {
                        val colorInt = color.toColorInt()


                        if (memo.isLocked) {
                            binding.memoDetailRootView.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@MemoDetailActivity,
                                    R.color.haru_lock_detail_color
                                )
                            )
                            binding.headerDetail.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@MemoDetailActivity,
                                    R.color.haru_lock_header_color
                                )
                            )
                        } else {
                            binding.memoDetailRootView.setBackgroundColor(colorInt)
                            binding.headerDetail.setBackgroundColor(colorInt)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }

    private fun observeEventFlow() {
        CoroutineScope(Dispatchers.IO).launch {
            MemoEventFlow.events.collect { event ->
                LogTrack.i(NAME) { "observeEventFlow -> event: $event" }
                when (event) {
                    is MemoEvent.PasswordEvent -> {
                        viewModel.updateMemoLockState(
                            memoDao = memoDao,
                            memoId = event.memoId,
                            isLocked = event.isLocked
                        ) {
                            LogTrack.i(NAME) { "observeEventFlow -> MemoEvent.PasswordEvent { event.memoId: ${event.memoId}, event.isLocked: ${event.isLocked} }" }

                            if (event.isLocked) {
                                Util.toastShort(
                                    this@MemoDetailActivity,
                                    getString(R.string.haru_password_locked_toast)
                                )
                                EventUtil.sendEvent(
                                    context = this@MemoDetailActivity,
                                    category = EventUtil.CATEGORY_DETAIL,
                                    action = EventUtil.ACTION_LOCK_ON
                                )
                            } else {
                                Util.toastShort(
                                    this@MemoDetailActivity,
                                    getString(R.string.haru_password_unlocked_toast)
                                )
                                EventUtil.sendEvent(
                                    context = this@MemoDetailActivity,
                                    category = EventUtil.CATEGORY_DETAIL,
                                    action = EventUtil.ACTION_LOCK_OFF
                                )
                            }
                            setModeView(mode = MemoMode.READ_MEMO)
                            MemoEventFlow.emit(event = MemoEvent.AllMemoUpdated)
                        }

                    }

                    is MemoEvent.PasswordMemoDeleteEvent -> {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

                    else -> {

                    }
                }
            }
        }
    }


    private fun updateImageSelectBtnVisibility() {
        binding.headerDetail.setImageSelectBtnVisible(
            memoBlockAdapter.hasAnyImageBlocks() && (memoMode == MemoMode.CREATE_MEMO || memoMode == MemoMode.MODIFY_MEMO)
        )
    }


    private fun setAdjustKeyboard() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.memoDetailRootView) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, imeInsets.bottom) // 키보드 or 내비게이션 바 중 더 큰 값을 적용
            )
            insets
        }
    }


    private fun setBgColor() {
        val bgColor = runCatching {
            (memoItem.bgColor ?: "#FFFFFF").toColorInt()
        }.getOrDefault(Color.WHITE)



        if (memoItem.isLocked) {
            binding.memoDetailRootView.setBackgroundColor(
                ContextCompat.getColor(
                    this@MemoDetailActivity,
                    R.color.haru_lock_detail_color
                )
            )
            binding.headerDetail.setBackgroundColor(
                ContextCompat.getColor(
                    this@MemoDetailActivity,
                    R.color.haru_lock_header_color
                )
            )
        } else {
            binding.memoDetailRootView.setBackgroundColor(bgColor)
            binding.headerDetail.setBackgroundColor(bgColor)
        }
    }


    private fun setModeView(mode: MemoMode) {
        viewModel.setMemoMode(mode = mode)
        memoBlockAdapter.setMemoMode(mode = mode)
        binding.headerDetail.setLockState(isLocked = memoItem.isLocked)
        binding.headerDetail.setHeaderMode(mode)
        updateImageSelectBtnVisibility()
        LogTrack.i(NAME) { "setModeView -> { mode : $mode }" }

        when (mode) {
            MemoMode.CREATE_MEMO -> {
                setCreateModeView()
                binding.rvBodyImage.post {
                    memoBlockAdapter.focusFirstEditableRow()
                }
            }

            MemoMode.MODIFY_MEMO -> setModifyModeView()
            MemoMode.READ_MEMO -> setReadModeView()
            MemoMode.NONE -> {
                LogTrack.e(NAME) { "setModeView -> { Mode is NONE  }" }
            }
        }
    }

    private fun setRecyclerViews() {
        setupRecyclerViewLayout()
        setupMemoBlockAdapter()
        setupRecyclerViewTouchListener()
    }

    private fun setupRecyclerViewLayout() {
        with(binding.rvBodyImage) {
            layoutManager = LinearLayoutManager(context)
            adapter = memoBlockAdapter
            itemAnimator = null
            setHasFixedSize(true)
            addItemDecoration(HorizontalSpaceItemDecoration(space = 10))
        }
    }

    private fun setupMemoBlockAdapter() {
        memoBlockAdapter.setAttachedRecyclerView(binding.rvBodyImage)

        memoBlockAdapter.onRequestEdit = { adapterPos, rowIndex ->
            if (memoMode == MemoMode.READ_MEMO) {
                binding.headerSearch.isVisible = false
                clearSearch()
                setModeView(MemoMode.MODIFY_MEMO)
                focusRowSafely(adapterPos, rowIndex, toEnd = true)
            }
        }

        memoBlockAdapter.onImagePresenceChanged = { hasImages ->
            binding.headerDetail.setImageSelectBtnVisible(
                hasImages && (memoMode == MemoMode.CREATE_MEMO || memoMode == MemoMode.MODIFY_MEMO)
            )

            if (!hasImages) {
                binding.headerDetail.onExitImageSelectMode?.invoke()
            }
        }
    }

    private fun handleEditTextBind(editText: EditText, mode: MemoMode, isTitle: Boolean) {
        if (isTitle) {
            etTitle = editText
        } else {
            etBody = editText
        }

        if (mode == MemoMode.READ_MEMO || mode == MemoMode.CREATE_MEMO) {
            setModeView(mode = MemoMode.MODIFY_MEMO.takeIf { mode == MemoMode.READ_MEMO }
                ?: MemoMode.CREATE_MEMO)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerViewTouchListener() {
        binding.rvBodyImage.setOnTouchListener { view, event ->
            if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener false

            val recyclerView = view as RecyclerView
            val touchedView = recyclerView.findChildViewUnder(event.x, event.y)

            val isBlankAreaClicked = touchedView == null
            val isReadMode = memoMode == MemoMode.READ_MEMO

            if (isBlankAreaClicked && isReadMode) {
                handleEmptyAreaClick()
                return@setOnTouchListener true
            }

            false
        }
    }

    private fun handleEmptyAreaClick() {
        if (memoMode == MemoMode.READ_MEMO) {
            setModeView(MemoMode.MODIFY_MEMO) // 1) 모드 전환
        }

        // 2) 레이아웃/리스트가 갱신될 시간을 한 프레임 줌
        binding.rvBodyImage.post {
            val pos = memoBlockAdapter.getLastBodyBlockPosition() ?: return@post
            val block =
                memoBlockAdapter.currentList.getOrNull(pos) as? MemoBlock.BodyBlock ?: return@post
            val lastRow = (block.bodyRows.lastIndex).coerceAtLeast(0)
            focusRowSafely(pos, lastRow, toEnd = true)
        }
    }


    private fun focusRowSafely(adapterPos: Int, rowIndex: Int, toEnd: Boolean) {
        val rv = binding.rvBodyImage
        rv.stopScroll()

        // 살짝 오프셋을 두고 스크롤(있으면)
        val lm = rv.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
        val offset = Util.dpToPx(this, 16)
        if (lm != null) {
            lm.scrollToPositionWithOffset(adapterPos, offset)
        } else {
            rv.scrollToPosition(adapterPos)
        }

        var tries = 10  // 최대 재시도 횟수
        fun attempt() {
            rv.post {
                // 리스트가 변했을 수도 있어 최신 BodyBlock 포지션 재확인
                val pos = memoBlockAdapter.getLastBodyBlockPosition() ?: adapterPos

                val vh = rv.findViewHolderForAdapterPosition(pos)
                        as? MemoBlockAdapter.BodyBlockViewHolder
                if (vh == null) {
                    if (tries-- > 0) {
                        rv.postDelayed({ attempt() }, 16L)
                    }
                    return@post
                }

                val block = memoBlockAdapter.currentList.getOrNull(pos) as? MemoBlock.BodyBlock
                if (block == null) {
                    if (tries-- > 0) {
                        rv.postDelayed({ attempt() }, 16L)
                    }
                    return@post
                }

                val lastIdx = block.bodyRows.lastIndex.coerceAtLeast(0)
                val safeIndex = rowIndex.coerceIn(0, lastIdx)

                val et = vh.getRowEditText(safeIndex)
                if (et == null) {
                    if (tries-- > 0) {
                        rv.postDelayed({ attempt() }, 16L)
                    }
                    return@post
                }

                // 포커스 부여 (requestFocusFromTouch() 쓰지 말 것)
                et.isFocusableInTouchMode = true
                et.isFocusable = true
                et.requestFocus()

                et.post {
                    val end = et.text?.length ?: 0
                    et.setSelection(if (toEnd) end else 0)
                    et.showIme()
                    et.bringPointIntoView(et.selectionEnd)
                    // 필요 시 입력툴바 높이만큼 여유
                    ensureCaretVisibleAfterIme(rv, vh, et, extraBottomReserveDp = 56)
                }
            }
        }

        // 첫 시도
        rv.post { attempt() }
    }


    private fun ensureCaretVisibleAfterIme(
        rv: RecyclerView,
        holder: RecyclerView.ViewHolder,
        et: EditText,
        extraBottomReserveDp: Int = 0 // 툴바 등 추가 여유
    ) {
        val root = rv.rootView
        var done = false

        val cb = object : WindowInsetsAnimationCompat.Callback(
            WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
        ) {
            override fun onProgress(
                insets: WindowInsetsCompat,
                runningAnimations: List<WindowInsetsAnimationCompat>
            ): WindowInsetsCompat = insets

            override fun onEnd(animation: WindowInsetsAnimationCompat) {
                if (done) return
                val currentInsets = ViewCompat.getRootWindowInsets(rv) ?: return
                if (!currentInsets.isVisible(WindowInsetsCompat.Type.ime())) return

                done = true
                ViewCompat.setWindowInsetsAnimationCallback(root, null)

                et.post {
                    et.bringPointIntoView(et.selectionEnd)
                    postScrollIfCursorObscured(rv, holder, et, extraBottomReserveDp)
                }
            }
        }

        ViewCompat.setWindowInsetsAnimationCallback(root, cb)

        // onEnd 못 받는 단말 대비
        rv.postDelayed({
            if (done) return@postDelayed
            val currentInsets = ViewCompat.getRootWindowInsets(rv)
            if (currentInsets?.isVisible(WindowInsetsCompat.Type.ime()) == true) {
                done = true
                ViewCompat.setWindowInsetsAnimationCallback(root, null)
                et.bringPointIntoView(et.selectionEnd)
                postScrollIfCursorObscured(rv, holder, et, extraBottomReserveDp)
            }
        }, 120L)

        // 누수 방지
        rv.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit
            override fun onViewDetachedFromWindow(v: View) {
                ViewCompat.setWindowInsetsAnimationCallback(root, null)
                rv.removeOnAttachStateChangeListener(this)
            }
        })
    }

    private fun postScrollIfCursorObscured(
        rv: RecyclerView,
        holder: RecyclerView.ViewHolder,
        et: EditText,
        extraBottomReserveDp: Int = 0 // 추가 여유(툴바 56dp 등)
    ) {
        val layout = et.layout ?: return
        val sel = et.selectionEnd.coerceAtLeast(0)
        val line = layout.getLineForOffset(sel)
        val caretBottomInEt = et.totalPaddingTop + layout.getLineBottom(line)

        val etLoc = IntArray(2).also { et.getLocationInWindow(it) }
        val rvLoc = IntArray(2).also { rv.getLocationInWindow(it) }

        val caretYInRv = (etLoc[1] - rvLoc[1]) + caretBottomInEt

        val insets = ViewCompat.getRootWindowInsets(rv)
        val imeBottom = insets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
        val extraReservePx = Util.dpToPx(rv.context, extraBottomReserveDp)

        // IME + 추가 여유(툴바 등)를 제외한 RV의 가시 하단
        val visibleBottomInRv = rv.height - imeBottom - extraReservePx

        val pad = (rv.resources.displayMetrics.density * 8).toInt()

        val overflow = caretYInRv - (visibleBottomInRv - pad)
        if (overflow > 0) {
            rv.smoothScrollBy(0, overflow)
        } else {
            val etTopInRv = (etLoc[1] - rvLoc[1])
            val visibleTopInRv =
                0 + (insets?.getInsets(WindowInsetsCompat.Type.systemBars())?.top ?: 0)
            val underflow = (visibleTopInRv + pad) - etTopInRv
            if (underflow > 0) {
                rv.smoothScrollBy(0, -underflow)
            }
        }
    }

    private fun setBackKeyListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogTrack.i(NAME) { "setBackKeyListener::handleOnBackPressed { mode : $memoMode, isImageSelectionMode=$isImageSelectionMode }" }

                // 🔹 시스템 백에서도 선택 모드 우선 해제
                if (isImageSelectionMode) {
                    isImageSelectionMode = false
                    memoBlockAdapter.setImageSelectionMode(false)
                    binding.headerDetail.setHeaderMode(MemoMode.MODIFY_MEMO)
                    updateImageSelectBtnVisibility()
                    return
                }

                when (memoMode) {
                    MemoMode.CREATE_MEMO,
                    MemoMode.MODIFY_MEMO -> {
                        saveMemo(isForceClose = true)
                    }

                    MemoMode.READ_MEMO -> {
                        if (binding.headerSearch.isVisible) {
                            binding.headerSearch.isVisible = false
                            clearSearch()
                        } else {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }

                    MemoMode.NONE -> {
                        // do nothing
                    }
                }
            }
        })
    }

    private fun setMemoSearchClickListener() {
        // back
        binding.headerSearch.onBackClick = {
            binding.headerSearch.isVisible = false
            binding.headerSearch.clearFocusAndHideKeyboard()
            clearSearch()
        }
        // 검색필드 입력
        binding.headerSearch.onSearchClick = { keyword ->
            val matchCount = memoBlockAdapter.applySearchKeyword(keyword)
            val currentIndex = memoBlockAdapter.currentMatchIndex + 1
            LogTrack.i(NAME) { "setMemoSearchClickListener -> { keyword: $keyword,   $currentIndex / $matchCount }" }

            if (matchCount > 0) {
                binding.headerSearch.setHasResults(true)
                binding.headerSearch.setIndicator("$currentIndex / $matchCount")
            } else {
                binding.headerSearch.setHasResults(false)
                binding.headerSearch.setIndicator("0 / 0")

                memoBlockAdapter.clearSearch()
                memoBlockAdapter.clearAllHighlights()
            }
            EventUtil.sendEvent(
                this@MemoDetailActivity,
                EventUtil.CATEGORY_DETAIL,
                EventUtil.ACTION_MEMO_DETAIL_SEARCH
            )
        }

        // clear
        binding.headerSearch.onClearClick = {
            clearSearch()
        }

        // 위로이동
        binding.headerSearch.onMoveUpClick = {
            memoBlockAdapter.moveToPreviousMatch { currentIndex, total ->
                binding.headerSearch.setIndicator("$currentIndex / $total")
            }
        }

        // 아래로이동
        binding.headerSearch.onMoveDownClick = {
            memoBlockAdapter.moveToNextMatch { currentIndex, total ->
                binding.headerSearch.setIndicator("$currentIndex / $total")
            }
        }

    }


    private fun setCreateModeView() {
        // Bottom toolbar
        binding.lyBottomToolbarContainer.isVisible = true

        binding.headerDetail.onBackClick = {
            saveMemo(isForceClose = true)
        }

        binding.headerDetail.onSaveClick = {
            saveMemo()
        }

        binding.headerDetail.onImageSelectedClick = {
            isImageSelectionMode = !isImageSelectionMode
            memoBlockAdapter.setImageSelectionMode(isImageSelectionMode)
            // (선택모드 UI 전환 필요 시) binding.headerDetail.setImageSelectionMode(isImageSelectionMode)
        }
        binding.headerDetail.onImageSelectedDeleteClick = {
            memoBlockAdapter.deleteSelectedImages()
            isImageSelectionMode = false
            // binding.headerDetail.setImageSelectionMode(false)
        }
    }

    private fun setModifyModeView() {
        // Bottom toolbar
        binding.lyBottomToolbarContainer.isVisible = true

        binding.headerDetail.onBackClick = {
            if (isImageSelectionMode) {
                isImageSelectionMode = false
                updateImageSelectBtnVisibility()
            } else {
                // 일반 수정 모드일 경우 저장 후 닫기
                saveMemo(isForceClose = true)
            }
        }

        binding.headerDetail.onSaveClick = {
            saveMemo()
        }

        binding.headerDetail.onImageSelectedClick = {
            isImageSelectionMode = !isImageSelectionMode
            memoBlockAdapter.setImageSelectionMode(isImageSelectionMode)
        }

        binding.headerDetail.onImageSelectedDeleteClick = {
            memoBlockAdapter.deleteSelectedImages()
            isImageSelectionMode = false
            updateImageSelectBtnVisibility()
        }

        binding.headerDetail.onExitImageSelectMode = {
            // Back 버튼을 누른 경우 HeaderView 내부에서 호출됨
            // 여기서 adapter selectionMode 해제까지 책임지도록 변경
            isImageSelectionMode = false
            memoBlockAdapter.setImageSelectionMode(false)
            updateImageSelectBtnVisibility()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setReadModeView() {
        val blocks = mutableListOf<MemoBlock>()
        // 날짜
        val displayDate = when (memoSortType) {
            MemoSortType.LATEST_CREATE,
            MemoSortType.OLDEST_CREATE -> memoItem.created

            MemoSortType.LATEST_UPDATE,
            MemoSortType.OLDEST_UPDATE -> memoItem.modified

            MemoSortType.CUSTOM -> memoItem.created
        }
        blocks += MemoBlock.DateBlock(date = Util.formatDate(displayDate))


        // 2) 텍스트/이미지 인덱스 맵
        val textGroups = memoItem.body.groupBy { it.index }.toSortedMap()
        val imageGroups = memoItem.imagePath.toSortedMap()
        val orderedKeys = (textGroups.keys + imageGroups.keys).toSortedSet()

        if (orderedKeys.isEmpty()) {
            // 케이스: 텍스트만 있고 BodyItem이 없는 "타이틀만" 메모 (또는 이미지/바디 모두 없음)
            val rows = mutableListOf<BodyRow>()
            if (!memoItem.title.isNullOrBlank()) {
                rows += RowCodecUtil.parse(memoItem.title)
            }
            memoItem.body.forEach { rows += RowCodecUtil.parse(it.text) }
            if (rows.isNotEmpty()) {
                blocks += MemoBlock.BodyBlock(bodyRows = rows)
            }
        } else {
            //  일반 케이스: 인덱스 순서대로 교차 배치 (첫 키에 타이틀 주입)
            val firstKey = orderedKeys.first()
            var titleInjected = false

            for (key in orderedKeys) {
                val bodyRows = mutableListOf<BodyRow>()

                // 첫 키에 타이틀을 먼저 주입 (해당 키가 이미지 전용이어도 타이틀만 담은 BodyBlock을 하나 만든다)
                if (!titleInjected && !memoItem.title.isNullOrBlank() && key == firstKey) {
                    bodyRows += RowCodecUtil.parse(memoItem.title)
                    titleInjected = true
                }

                // 같은 인덱스의 본문행 추가
                textGroups[key]?.forEach { item ->
                    bodyRows += RowCodecUtil.parse(item.text)
                }

                if (bodyRows.isNotEmpty()) {
                    blocks += MemoBlock.BodyBlock(bodyRows = bodyRows)
                }

                // 이미지 블록 추가 (Uri 문자열 리스트로 저장)
                imageGroups[key]?.let { paths ->
                    val uris = paths.map { it }   //  paths 자체가 String 경로라면 그대로 사용
                    if (uris.isNotEmpty()) {
                        blocks += MemoBlock.ImageUriBlock(uris = uris.toMutableList())
                    }
                }
            }
        }



        viewModel.setMemoBlocks(blocks)

        LogTrack.i(NAME) { "setReadModeView -> { memoSortType: $memoSortType }" }


        // Bottom toolbar
        binding.lyBottomToolbarContainer.isVisible = false

        // back 키
        binding.headerDetail.onBackClick = {
            setResult(Activity.RESULT_OK)
            finish()
        }


        // 즐겨찾기
        binding.headerDetail.setImportantState(memoItem.isImportant)
        binding.headerDetail.onAction1Click = {
            try {
                viewModel.updateImportant()
                binding.headerDetail.setImportantState(memoItem.isImportant)

                val action = if (memoItem.isImportant) {
                    EventUtil.ACTION_IMPORTANT_ON
                } else {
                    EventUtil.ACTION_IMPORTANT_OFF
                }
                EventUtil.sendEvent(
                    context = this@MemoDetailActivity,
                    category = EventUtil.CATEGORY_DETAIL,
                    action = action
                )

                CoroutineScope(Dispatchers.IO).launch {
                    memoDao.updateMemo(memoItem)
                    MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                }
                MemoWidgetUpdater().observeMemoChanges(this@MemoDetailActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 메모 내 검색 ( setMemoSearchClickListener() )
        binding.headerDetail.onAction2Click = {
            binding.headerSearch.isVisible = true
            binding.headerSearch.focusAndShowKeyboard(selectAll = true)
        }
        // 메모 내 설정
        binding.headerDetail.onAction3Click = {
            MemoMenuView(this@MemoDetailActivity).apply {
                setMemoItem(memoItem = memoItem)
                show(
                    anchor = binding.headerDetail.findViewById(R.id.btnAction3),
                    mode = MemoMenuView.Mode.DETAIL
                )
                setOnItemClickListener { position, label ->
                    when (label) {
                        // 공유하기
                        getString(R.string.haru_menu_share) -> {
                            try {
                                val bgColorInt = try {
                                    (memoItem.bgColor ?: "#FFFFFF").toColorInt()
                                } catch (e: Exception) {
                                    Color.WHITE
                                }

                                // 여백(px) 지정
                                val paddingPx = (16 * resources.displayMetrics.density).toInt()

                                // RecyclerView 전체 캡처 (배경 + 여백 + hint 제거)
                                val bitmap = captureRecyclerViewWithBackground(
                                    binding.rvBodyImage,
                                    bgColorInt,
                                    paddingPx
                                )

                                // PDF 파일 저장
                                val pdfDir = File(cacheDir, "pdf").apply { if (!exists()) mkdirs() }
                                val pdfFile =
                                    File(pdfDir, "haru_memo_${System.currentTimeMillis()}.pdf")
                                bitmapToPdf(bitmap, pdfFile, bgColorInt)

                                // 공유
                                val pdfUri = FileProvider.getUriForFile(
                                    this@MemoDetailActivity,
                                    "${packageName}.fileprovider",
                                    pdfFile
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                EventUtil.sendEvent(
                                    this@MemoDetailActivity,
                                    EventUtil.CATEGORY_DETAIL,
                                    EventUtil.ACTION_SHARE_MEMO
                                )
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        getString(R.string.haru_menu_share)
                                    )
                                )

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Util.toastShort(this@MemoDetailActivity, "PDF 변환 중 오류가 발생했습니다.")
                            }
                        }

                        // 비밀메모로 변경
                        getString(R.string.haru_menu_secret) -> {
                            if (PreferenceUtil.get(KEY_PASSWORD_SWITCH, false)) {
                                if (memoItem.isLocked) {
                                    Util.toastShort(
                                        this@MemoDetailActivity,
                                        getString(R.string.haru_item_lock_memo_exception1)
                                    )
                                } else {
                                    PasswordActivity.start(
                                        activity = this@MemoDetailActivity,
                                        mode = SettingHeaderView.SetHeaderMode.PASSWORD_LOCK,
                                        memoItem = memoItem
                                    )
                                }
                            } else {
                                showRegiPasswordDialog()
                            }
                        }

                        // 비밀메모 해제
                        getString(R.string.haru_menu_secret_unlock) -> {
                            if (PreferenceUtil.get(KEY_PASSWORD_SWITCH, false)) {
                                if (memoItem.isLocked) {
                                    PasswordActivity.start(
                                        activity = this@MemoDetailActivity,
                                        mode = SettingHeaderView.SetHeaderMode.PASSWORD_UNLOCK,
                                        memoItem = memoItem
                                    )
                                } else {
                                    Util.toastShort(
                                        this@MemoDetailActivity,
                                        getString(R.string.haru_item_lock_memo_exception2)
                                    )
                                }
                            } else {
                                showRegiPasswordDialog()
                            }
                        }

                        // 삭제하기
                        getString(R.string.haru_menu_delete) -> {
                            with(deleteDialog) {
                                setTitle(getString(R.string.haru_delete_dialog_delete_title))
                                setMessage(getString(R.string.haru_delete_dialog_delete_message))

                                setButton(
                                    cancelText = getString(R.string.haru_delete_dialog_delete_cancel),
                                    confirmText = getString(R.string.haru_delete_dialog_delete_confirm),
                                    onCancel = {
                                        dismiss()
                                    },
                                    onConfirm = {
                                        if (memoItem.isLocked) {
                                            // 잠금 메모일 경우: 비밀번호 확인 후 삭제로 이동
                                            PasswordActivity.startDelete(
                                                activity = this@MemoDetailActivity, // ← 액티비티 이름으로 수정
                                                memoItems = listOf(memoItem)
                                            )
                                            EventUtil.sendEvent(
                                                this@MemoDetailActivity,
                                                EventUtil.CATEGORY_DETAIL,
                                                EventUtil.ACTION_DELETE_MEMO
                                            )
                                            dismiss()
                                        } else {
                                            // 잠금되지 않은 경우: 기존 삭제 로직
                                            try {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    viewModel.updateDelete()
                                                    memoDao.updateMemo(memoItem)
                                                    EventUtil.sendEvent(
                                                        this@MemoDetailActivity,
                                                        EventUtil.CATEGORY_DETAIL,
                                                        EventUtil.ACTION_DELETE_MEMO
                                                    )
                                                    withContext(Dispatchers.Main) {
                                                        MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                                                        toastShort(
                                                            this@MemoDetailActivity,
                                                            getString(R.string.haru_memo_delete_move_to_trash)
                                                        )
                                                        dismiss()
                                                        setResult(Activity.RESULT_OK)
                                                        finish()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                dismiss()
                                                setResult(Activity.RESULT_OK)
                                                finish()
                                            }
                                        }
                                    }
                                )
                            }

                            if (this@MemoDetailActivity.isAlive) {
                                deleteDialog.show()
                            }
                        }

                        // 메모정보
                        getString(R.string.haru_menu_information) -> {
                            val dialog = MemoInfoBottomSheetCustomDialog.newInstance(memoItem)
                            dialog.show(
                                supportFragmentManager,
                                MemoInfoBottomSheetCustomDialog.NAME
                            )
                        }

                        // AI
                        getString(R.string.haru_menu_ai_summation) -> {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                Util.toastShort(
                                    this@MemoDetailActivity,
                                    "Android 6.0 (API 23) 이상 기기에서만 지원됩니다."
                                )
                                return@setOnItemClickListener
                            }
                            EventUtil.sendEvent(
                                this@MemoDetailActivity,
                                EventUtil.CATEGORY_DETAIL,
                                EventUtil.ACTION_USE_AI_SUMMATION
                            )
                            summarizeMemo(memoItem)
                        }
                    }
                }
            }
        }
        MemoEventFlow.emit(MemoEvent.AllMemoUpdated)

    }

    private fun summarizeMemo(memoItem: MemoEntity) {
        lifecycleScope.launch {
            showProgressView()
            val (summary, hasImage) = try {
                val result = askAiSummation(memoItem.title, memoItem.body, memoItem.imagePath)
                result to memoItem.imagePath.isNotEmpty()
            } finally {
                hideProgressView()
            }
            showSummationDialog(summary, hasImage)
        }
    }

    private suspend fun askAiSummation(
        title: String,
        body: List<BodyItem>,
        imagePath: Map<Int, List<String>>
    ): String {
        return try {
            val response = generativeModel.generateContent(
                content {
                    text(
                        """
                    아래 메모의 제목과 내용을 읽고 반드시 한국어로 요약해 주세요.
                    - 5줄 이내의 텍스트로 작성하세요.
                    - {[H4RU_CHECK3D]}, {[H4RU_U2CHECK3D]} 이 두 텍스트가 타이틀 또는 바디에 있을 경우에는 답변에서 체크박스라고 표현해주세요. 
                    - 여러 줄로 나누지 말고 하나의 문단으로 자연스럽게 이어지도록 작성하세요.
                    - 마지막에는 마침표 하나로 끝내세요.
                    
                    제목: $title
                    """.trimIndent()
                    )

                    // 본문 텍스트 추가
                    body.forEach { item ->
                        if (!item.text.isNullOrBlank()) {
                            text(item.text)
                        }
                    }

                    // 이미지가 있다면, 첫 번째 이미지만 전달
                    val firstImageUri = imagePath.values.firstOrNull()?.firstOrNull()
                    if (firstImageUri != null) {
                        val uri = firstImageUri.toUri()
                        val bitmap = this@MemoDetailActivity.uriToBitmap(uri)
                        if (bitmap != null) {
                            image(bitmap) // Firebase GenAI SDK는 Bitmap을 받음
                        }
                    }
                }
            )

            val rawText = response.text ?: "요약을 가져오지 못했어요 😢"

            // 불필요한 개행 제거 → 한 문단으로 합치기
            val singleParagraph = rawText
                .replace("\n", " ") // 줄바꿈을 공백으로
                .replace(Regex("\\s+"), " ") // 여러 공백을 하나로
                .trim()

            // **텍스트** → <b>텍스트</b>
            singleParagraph.replace(Regex("\\*\\*(.+?)\\*\\*"), "<b>$1</b>")

        } catch (e: Exception) {
            val msg = e.message ?: ""
            when {
                msg.contains("PERMISSION_DENIED", true) ||
                        msg.contains("quota", true) ||
                        msg.contains("exceed", true) -> {
                    "무료 사용 한도가 모두 소진되었습니다. 잠시 후 다시 이용해 주세요."
                }

                else -> {
                    "AI 호출 중 오류가 발생했어요: ${e.message}"
                }
            }
        }
    }

    /** 요약 다이얼로그 */
    private fun showSummationDialog(summation: String, hasImage: Boolean) {
        MemoCustomAiAnswerDialog(this, DialogMode.SUMMARY).apply {
            setTitle(getString(R.string.haru_ai_summary))
            setMessage(
                HtmlCompat.fromHtml(summation, HtmlCompat.FROM_HTML_MODE_LEGACY)
            )

            // 이미지가 있을 때만 경고 문구 노출
            setBottomSubMessage(hasImage)

            // 요약 모드 → cancel 버튼 하나만 전체 영역 차지
            setButton(
                cancelText = getString(R.string.haru_ai_close_summary),
                confirmText = "", // 무시됨
                onCancel = {
                    onDestroy()
                }
            )
        }.show()
    }

    // content:// URI → Bitmap 변환 유틸
    private fun Context.uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToPdf(bitmap: Bitmap, pdfFile: File, bgColor: Int) {
        val document = PdfDocument()

        // 최소 높이 (예: 화면 높이, 혹은 A4 비율 기준)
        val minHeight = (resources.displayMetrics.heightPixels * 0.8f).toInt()
        val pageHeight = maxOf(bitmap.height, minHeight)

        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, pageHeight, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawColor(bgColor)

        // 상단 정렬 (필요하다면 중앙 정렬도 가능)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        document.finishPage(page)
        pdfFile.outputStream().use { document.writeTo(it) }
        document.close()
    }

    private fun captureRecyclerViewWithBackground(
        rv: RecyclerView,
        bgColor: Int,
        padding: Int
    ): Bitmap {
        val adapter = rv.adapter ?: return createBitmap(1, 1)

        // 1) 각 아이템 높이 계산
        val totalHeight = (0 until adapter.itemCount).sumOf { position ->
            val viewType = adapter.getItemViewType(position)
            val holder = adapter.createViewHolder(rv, viewType)
            adapter.onBindViewHolder(holder, position)

            // hint 제거
            holder.itemView.findViewsByType(EditText::class.java).forEach { et ->
                if (et.text.isNullOrEmpty()) et.hint = ""
            }

            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(rv.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            holder.itemView.measuredHeight
        }

        // 마지막에 항상 20px 추가
        val finalHeight = totalHeight + 200

        // 2) RecyclerView 크기 늘려서 모든 아이템 attach
        rv.measure(
            View.MeasureSpec.makeMeasureSpec(rv.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(finalHeight, View.MeasureSpec.EXACTLY)
        )
        rv.layout(rv.left, rv.top, rv.right, rv.top + finalHeight)

        // 3) 캔버스에 전체 RecyclerView 그리기
        val bigBitmap =
            createBitmap(rv.measuredWidth + padding * 2, rv.measuredHeight + padding * 2)
        val canvas = Canvas(bigBitmap)
        canvas.drawColor(bgColor) // 전체 배경색
        canvas.translate(padding.toFloat(), padding.toFloat()) // 여백 적용
        rv.draw(canvas)

        return bigBitmap
    }

    // ViewGroup 안에서 특정 타입의 View 찾기
    private fun <T : View> View.findViewsByType(clazz: Class<T>): List<T> {
        val result = mutableListOf<T>()
        if (clazz.isInstance(this)) {
            result.add(this as T)
        }
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                result.addAll(getChildAt(i).findViewsByType(clazz))
            }
        }
        return result
    }

    private fun saveBitmapsToCache(bitmaps: List<Bitmap>): List<String> {
        return FileUtil.getUriForCompressedBitmaps(
            context = this@MemoDetailActivity,
            bitmaps = bitmaps
        ).map { it.toString() }
    }

    private fun showRegiPasswordDialog() {
        if (!this.isAlive) return

        regiPasswordDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_dialog_set_password__title)) // 원하는 대로 수정 가능
            setMessage(getString(R.string.haru_dialog_set_password_message))
            setSubMessage(getString(R.string.haru_dialog_set_password_sub))
            setBottomSubMessage(getString(R.string.haru_dialog_set_password_bottom_sub))

            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_dialog_set_password_confirm),
                onCancel = { dismiss() },
                onConfirm = {
                    dismiss()
                    PasswordActivity.start(
                        activity = this@MemoDetailActivity,
                        SettingHeaderView.SetHeaderMode.PASSWORD_REGI,
                        memoItem = memoItem
                    )
                }
            )
        }
        regiPasswordDialog?.show()
    }


    private fun saveMemo(isForceClose: Boolean = false) {
        when (memoMode) {
            /** 저장하기 */
            MemoMode.CREATE_MEMO -> {
                val latestBlocks = memoBlockAdapter.getCurrentBlocks()
                viewModel.setMemoBlocks(latestBlocks)
                latestBlocks.withIndex().forEach {
                    LogTrack.i { "setCreateModeView -> [$it.index] ${it.value::class.simpleName} - ${it.value}" }
                }

                val userID = BaseUtil.getAndroidId(this@MemoDetailActivity)
                val uuID = BaseUtil.getUUID()
                //                val bodyItems: MutableList<BodyItem> = mutableListOf()
                val (title, bodyItems) = viewModel.getTitleAndBodyItems()
                val imagePathMap = mutableMapOf<Int, List<String>>()
                val now = System.currentTimeMillis()
                var contentIndex = 0

                viewModel.memoBlocks.value.forEachIndexed { index, block ->
                    LogTrack.i { "setCreateModeView -> [$index] ${block::class.simpleName} - ${block}" }
                    when (block) {

                        // body-index (Date 제외)
                        is MemoBlock.BodyBlock -> {
//                            bodyItems.add(
//                                BodyItem(
//                                    index = contentIndex,
//                                    text = block.body
//                                )
//                            )
                            contentIndex++
                        }

                        // image-index (Date 제외)
                        is MemoBlock.ImageUriBlock -> {
                            imagePathMap[contentIndex] = block.uris.toList()   // 그대로 사용
                            contentIndex++
                        }

                        else -> Unit
                    }
                }


                val hasTitle = title.isNotBlank()
                val hasBody = bodyItems.any { it.text.isNotBlank() }
                val hasImage = imagePathMap.values.any { it.isNotEmpty() }
                // 빈메모
                if (!hasTitle && !hasBody && !hasImage) {
                    LogTrack.e(NAME) { "setCreateModeView -> the empty memo!!" }
                    finish()
                    return
                }

                LogTrack.i(NAME) {
                    "setCreateModeView -> { currentBlocks.size: ${memoBlockAdapter.itemCount} \n" +
                            " userid: ${userID}, uuID: ${uuID}, title: ${title}, bodyItems: ${bodyItems.size}, imagePathMap: ${imagePathMap.size}, " +
                            "currentTime(created/modified/read/custom): ${now}, bgColor: ${currentBgHexColor} \n" +
                            "}"
                }


                viewModel.insertNewMemo(
                    memoDao = memoDao,
                    tagDao = tagDao,
                    taggingDao = taggingDao,
                    userId = userID,
                    uuid = uuID,
                    title = title,
                    body = bodyItems,
                    imagePath = imagePathMap,
                    bgColor = currentBgHexColor,
                    now = now,
                    onInserted = {
                        MemoWidgetUpdater().observeMemoChanges(this)
                        //                        etTitle?.exitCursor()
                        //                        etBody?.exitCursor()
                        hideKeyboard()
                        Util.toastShort(
                            this,
                            getString(R.string.haru_create_write_memo_mode_save)
                        )
                        // 자동백업
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                AutoBackupUtil.performAutoBackupIfNeeded(this@MemoDetailActivity)
                            }
                        }

                        if (isForceClose) {
                            finish()
                        } else {
                            setModeView(mode = MemoMode.READ_MEMO)
                        }
                        EventUtil.sendEvent(
                            this@MemoDetailActivity,
                            EventUtil.CATEGORY_DETAIL,
                            EventUtil.ACTION_MEMO_CREATE
                        )
                        MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                    }
                )
            }


            /** 수정하기 */
            MemoMode.MODIFY_MEMO -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val latestBlocks = memoBlockAdapter.getCurrentBlocks()
                    viewModel.setMemoBlocks(latestBlocks)
                    latestBlocks.withIndex().forEach {
                        LogTrack.i { "setModifyModeView -> [$it.index] ${it.value::class.simpleName} - ${it.value}" }
                    }

                    val (title, bodyItems) = viewModel.getTitleAndBodyItems()
                    val imagePathMap = mutableMapOf<Int, List<String>>()
                    val now = System.currentTimeMillis()
                    var contentIndex = 0

                    viewModel.memoBlocks.value.forEachIndexed { index, block ->
                        LogTrack.i { "setModifyModeView -> [$index] ${block::class.simpleName} - ${block}" }
                        when (block) {
                            is MemoBlock.BodyBlock -> {
//                            bodyItems.add(
//                                BodyItem(
//                                    index = contentIndex,
//                                    text = block.body
//                                )
//                            )
                                contentIndex++
                            }

                            is MemoBlock.ImageUriBlock -> {
                                imagePathMap[contentIndex] = block.uris.toList()  // 비트맵 저장 X, 그대로 Uri 리스트 사용
                                contentIndex++
                            }


                            else -> Unit
                        }
                    }

                    val hasTitle = title.isNotBlank()
                    val hasBody = bodyItems.any { it.text.isNotBlank() }
                    val hasImage = imagePathMap.values.any { it.isNotEmpty() }

                    if (!hasTitle && !hasBody && !hasImage) {
                        withContext(Dispatchers.Main) {
                            LogTrack.e(NAME) { "setModifyModeView -> the empty memo!!" }
                            finish()
                        }
                        return@launch
                    }

                    viewModel.updateMemo(
                        memoDao = memoDao,
                        tagDao = tagDao,
                        taggingDao = taggingDao,
                        title = title,
                        body = bodyItems,
                        imagePath = imagePathMap,
                        now = now,
                        onUpdated = {
                            MemoWidgetUpdater().observeMemoChanges(this@MemoDetailActivity)
                            Util.toastShort(
                                this@MemoDetailActivity,
                                getString(R.string.haru_modify_memo_mode_update)
                            )
//                        etTitle?.exitCursor()
//                        etBody?.exitCursor()
                            hideKeyboard()

                            // 자동백업
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    AutoBackupUtil.performAutoBackupIfNeeded(this@MemoDetailActivity)
                                }
                            }

                            if (isForceClose) {
                                finish()
                            } else {
                                setModeView(mode = MemoMode.READ_MEMO)
                            }
                            EventUtil.sendEvent(
                                this@MemoDetailActivity,
                                EventUtil.CATEGORY_DETAIL,
                                EventUtil.ACTION_MEMO_MODIFY
                            )
                            MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                        }
                    )

                }
            }

            else -> {}
        }
    }


    private fun showModifyDialog() {
        if (!this.isAlive) return

        val dialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_dialog_modify_close_title))
            setMessage(getString(R.string.haru_dialog_modify_close_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_dialog_common_close),
                onCancel = {
                    setModeView(mode = MemoMode.MODIFY_MEMO)
//                    etTitle?.enterCursor()
//                    etBody?.enterCursor()
                    dismiss()
                },
                onConfirm = {
                    setModeView(mode = MemoMode.READ_MEMO)
//                    etTitle?.exitCursor()
//                    etBody?.exitCursor()
                    this@MemoDetailActivity.hideKeyboard()
                    dismiss()
                }
            )
        }
        dialog.show()

    }


    private fun showCreateWriteDialog() {
        if (!this.isAlive) return

        val dialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_dialog_create_write_close_title))
            setMessage(getString(R.string.haru_dialog_create_write_close_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_dialog_common_close),
                onCancel = {
                    setModeView(mode = MemoMode.CREATE_MEMO)
//                    etTitle?.enterCursor()
//                    etBody?.enterCursor()
                    dismiss()
                },
                onConfirm = {
//                    etTitle?.exitCursor()
//                    etBody?.exitCursor()
                    this@MemoDetailActivity.hideKeyboard()
                    dismiss()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            )
        }
        dialog.show()

    }


    private fun setEditorToolbar() {
        if (!this.isAlive) return

        with(binding.editorToolBar) {
            setLocked(isLocked = memoItem.isLocked)

            // 손글씨
            setOnDrawingClickListener {
                drawLauncher.launch(
                    DrawActivity.createIntent(this@MemoDetailActivity)
                )
            }

            // 체크박스
            binding.editorToolBar.setOnCheckBoxClickListener {
                EventUtil.sendEvent(
                    this@MemoDetailActivity,
                    EventUtil.CATEGORY_DETAIL,
                    EventUtil.ACTION_TOOL_CHECKBOX
                )
                memoBlockAdapter.toggleCheckboxAtCaret()
            }

            // 불릿
            setOnBulletClickListener {
                EventUtil.sendEvent(
                    this@MemoDetailActivity,
                    EventUtil.CATEGORY_DETAIL,
                    EventUtil.ACTION_TOOL_BULLET
                )
                memoBlockAdapter.toggleBulletAtCaret()
            }

            // 갤러리
            setOnGalleryClickListener {
                EventUtil.sendEvent(
                    this@MemoDetailActivity,
                    EventUtil.CATEGORY_DETAIL,
                    EventUtil.ACTION_TOOL_GALLERY
                )
                galleryLauncher.launch("image/*")
            }

            // 카메라
            setOnCameraClickListener {
                EventUtil.sendEvent(
                    this@MemoDetailActivity,
                    EventUtil.CATEGORY_DETAIL,
                    EventUtil.ACTION_TOOL_CAMERA
                )
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            // 메모색상
            setOnMemoColorClickListener {
                if (!this@MemoDetailActivity.isAlive) return@setOnMemoColorClickListener
                memoColorCustomDialog = MemoColorCustomDialog(
                    context = this@MemoDetailActivity,
                    selectedColor = (memoItem.bgColor?.takeIf { it.isNotBlank() }
                        ?: "#FFFFFF").toColorInt()
                ).apply {
                    setButton(
                        onPreview = { color ->
                            if (memoItem.isLocked) {
                                binding.memoDetailRootView.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@MemoDetailActivity,
                                        R.color.haru_lock_detail_color
                                    )
                                )
                                binding.headerDetail.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@MemoDetailActivity,
                                        R.color.haru_lock_header_color
                                    )
                                )
                            } else {
                                binding.memoDetailRootView.setBackgroundColor(color)
                                binding.headerDetail.setBackgroundColor(color)
                            }

                        },
                        onConfirm = { color ->
                            viewModel.updateMemoBackgroundColor(
                                colorHex = color.toHex(),
                                memoDao = memoDao,
                                onUpdated = {
                                    currentBgHexColor = color.toHex()

                                    if (memoItem.isLocked) {
                                        binding.memoDetailRootView.setBackgroundColor(
                                            ContextCompat.getColor(
                                                this@MemoDetailActivity,
                                                R.color.haru_lock_detail_color
                                            )
                                        )
                                        binding.headerDetail.setBackgroundColor(
                                            ContextCompat.getColor(
                                                this@MemoDetailActivity,
                                                R.color.haru_lock_header_color
                                            )
                                        )
                                    } else {
                                        binding.memoDetailRootView.setBackgroundColor(color)
                                        binding.headerDetail.setBackgroundColor(color)
                                    }
                                    EventUtil.sendEvent(
                                        this@MemoDetailActivity,
                                        EventUtil.CATEGORY_DETAIL,
                                        EventUtil.ACTION_TOOL_BACKGROUND
                                    )
                                    MemoEventFlow.emit(event = MemoEvent.AllMemoUpdated)
                                    dismiss()
                                }
                            )
                        },
                        onCancel = { originColor ->
                            if (memoItem.isLocked) {
                                binding.memoDetailRootView.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@MemoDetailActivity,
                                        R.color.haru_lock_detail_color
                                    )
                                )
                                binding.headerDetail.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@MemoDetailActivity,
                                        R.color.haru_lock_header_color
                                    )
                                )
                            } else {
                                binding.memoDetailRootView.setBackgroundColor(originColor)
                                binding.headerDetail.setBackgroundColor(originColor)
                            }

                            dismiss()
                        },
                    )
                }
                memoColorCustomDialog?.show()

            }
        }
    }


    private fun getFontSizeFromLabel(label: String): Float = when (label) {
        getString(R.string.haru_menu_font_size_xs) -> 12f
        getString(R.string.haru_menu_font_size_s) -> 14f
        getString(R.string.haru_menu_font_size_m) -> 16f
        getString(R.string.haru_menu_font_size_l) -> 18f
        getString(R.string.haru_menu_font_size_xl) -> 20f
        else -> 15f
    }


    private fun clearSearch() {
        memoBlockAdapter.clearSearch()
        binding.headerSearch.clearSearch()  // ← 검색어 필드 비우고 UI 초기화
    }


    fun getRotatedBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.use { stream ->
                ExifInterface(stream)
            } ?: return null

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rawBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return null

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                // 나머지는 회전 없음
            }

            return Bitmap.createBitmap(
                rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun setCameraLauncher() {
        if (isAlive) {
            permissionLauncher =
                this@MemoDetailActivity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        val imageFile =
                            File.createTempFile("photo_", ".jpg", this@MemoDetailActivity.cacheDir)
                        cameraImageUri = FileProvider.getUriForFile(
                            this@MemoDetailActivity,
                            "${this@MemoDetailActivity.packageName}.fileprovider",
                            imageFile
                        )
                        cameraLauncher.launch(cameraImageUri)
                    } else {
                        if (this@MemoDetailActivity.isAlive) {
                            val dialog = MemoCustomDialog(this).apply {
                                setTitle(getString(R.string.haru_dialog_permission_camera_title))
                                setMessage(getString(R.string.haru_dialog_permission_camera_message))
                                setButton(
                                    cancelText = getString(R.string.haru_dialog_common_cancel),
                                    confirmText = getString(R.string.haru_dialog_common_system_setting),
                                    onCancel = {
                                        dismiss()
                                    },
                                    onConfirm = {
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = "package:$packageName".toUri()
                                            }
                                        startActivity(intent)
                                        dismiss()
                                    }
                                )
                            }
                            dialog.show()
                        }
                    }
                }

            cameraLauncher =
                registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                    LogTrack.i(NAME) { "setCameraLauncher -> $success" }

                    if (!::cameraImageUri.isInitialized) {
                        LogTrack.e(NAME) { "cameraImageUri not initialized! Skipping camera result" }
                        Util.toastShort(
                            this,
                            getString(R.string.haru_put_in_photo_take_picture_toast)
                        )
                        return@registerForActivityResult
                    }

                    if (success) {
                        try {
                            cameraImageUri?.let { uri ->
                                val focusedPos: Int = memoBlockAdapter.currentFocusedPosition
                                val insertPos = if (focusedPos <= TITLE_END_POS) {
                                    TITLE_END_POS
                                } else {
                                    focusedPos + IMAGE_END_POS
                                }

                                LogTrack.i(NAME) { "setCameraLauncher -> { focusedPos: $focusedPos, insertPos: $insertPos, uri: $uri }" }
                                // 이제는 Bitmap 대신 Uri를 넘겨야 함
                                viewModel.setImageBlock(position = insertPos, uris = listOf(uri.toString()))

                                // 포커스 이동
                                binding.rvBodyImage.post {
                                    focusRowSafely(
                                        adapterPos = insertPos + 1,
                                        rowIndex = 0,
                                        toEnd = false
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            LogTrack.e(NAME) { "setCameraLauncher -> Failed to handle image Uri: $e" }
                        }
                    }

                }

        }
    }

    private fun setGalleryLauncher() {
        if (!isAlive) return

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
                if (uris.isEmpty()) return@registerForActivityResult

                try {
                    val focusedPos: Int = memoBlockAdapter.currentFocusedPosition
                    val insertPos = maxOf(focusedPos + IMAGE_END_POS, TITLE_END_POS + IMAGE_END_POS)

                    LogTrack.i(NAME) {
                        "setGalleryLauncher -> { focusedPos: $focusedPos, insertPos: $insertPos, count: ${uris.size} }"
                    }

                    // 바로 String 변환
                    val uriList = uris.map(Uri::toString)

                    if (uriList.isNotEmpty()) {
                        viewModel.setImageListBlock(position = insertPos, uris = uriList)

                        // 포커스 이동
                        binding.rvBodyImage.post {
                            focusRowSafely(
                                adapterPos = insertPos + 1,
                                rowIndex = 0,
                                toEnd = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    LogTrack.e(NAME) { "setGalleryLauncher -> Failed: $e" }
                }
            }
    }


    private fun setTagLauncher() {
        if (isAlive) {
            tagLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    LogTrack.i(NAME) { "setTagLauncher -> RESULT_OK " }
                    setModeView(mode = MemoMode.READ_MEMO)

                }
            }
        }
    }

    private fun setDrawLauncher() {
        if (isAlive) {
            drawLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    LogTrack.i(NAME) { "setDrawLauncher -> RESULT_OK " }
                    result.data?.getParcelableCompat<Uri>("draw_uri")?.let { uri ->
                        try {
                            LogTrack.i(NAME) { "setDrawLauncher -> RESULT_OK { uri: $uri }" }

                            val focusedPos = memoBlockAdapter.currentFocusedPosition
                            val insertPos = if (focusedPos <= TITLE_END_POS) {
                                TITLE_END_POS
                            } else {
                                focusedPos + IMAGE_END_POS
                            }

                            // Bitmap 대신 Uri String 저장
                            viewModel.setImageBlock(position = insertPos, uris = listOf(uri.toString()))

                            // 포커스 이동
                            binding.rvBodyImage.post {
                                focusRowSafely(
                                    adapterPos = insertPos + 1,
                                    rowIndex = 0,
                                    toEnd = false
                                )
                            }

                        } catch (e: Exception) {
                            LogTrack.e(NAME) { "setDrawLauncher -> Failed: $e" }
                        }
                    }
                }
            }
        }
    }

}
