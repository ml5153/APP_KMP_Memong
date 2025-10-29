package com.memong.aos.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.memong.aos.BuildConfig
import com.memong.aos.MemoEventFlow
import com.memong.aos.MemongApplication
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.entity.BodyRow
import com.memong.aos.data.entity.MemoBlock
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.entity.MemoSectionListItem
import com.memong.aos.data.entity.TutorialStep
import com.memong.aos.data.enum.DynamicRetroSectionType
import com.memong.aos.data.enum.DynamicSectionType
import com.memong.aos.data.enum.FixedSectionType
import com.memong.aos.data.enum.MainGroupMode
import com.memong.aos.data.enum.MainGroupMode.DATE
import com.memong.aos.data.enum.MainGroupMode.TAG
import com.memong.aos.data.enum.MainLayoutMode
import com.memong.aos.data.enum.MainSectionType
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.data.enum.MemoSortType
import com.memong.aos.data.enum.TutorialFocusType
import com.memong.aos.data.extension.isAlive
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.AnimationUtil
import com.memong.aos.data.utils.BaseUtil
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.GoogleDriveUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_DYNAMIC_SECTIONS_IS_INIT
import com.memong.aos.data.utils.PreferenceUtil.KEY_MEMO_GROUP_MODE
import com.memong.aos.data.utils.PreferenceUtil.KEY_MEMO_LAYOUT_MODE
import com.memong.aos.data.utils.PreferenceUtil.KEY_MEMO_RESTORE
import com.memong.aos.data.utils.PreferenceUtil.KEY_PASSWORD_SWITCH
import com.memong.aos.data.utils.PreferenceUtil.KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED
import com.memong.aos.data.utils.PreferenceUtil.KEY_SECTION_SECRET_MEMO_IS_EXPANDED
import com.memong.aos.data.utils.PreferenceUtil.KEY_SHOW_TUTORIAL_MEMO_MAIN
import com.memong.aos.data.utils.PreferenceUtil.KEY_SIMPLIFY
import com.memong.aos.data.utils.PreferenceUtil.KEY_USE_LOCKSCREEN_MEMO
import com.memong.aos.data.utils.PreferenceUtil.getDynamicSectionKey
import com.memong.aos.data.utils.RemoteConfigUtil
import com.memong.aos.data.utils.RowCodecUtil
import com.memong.aos.data.utils.StatisticsUtil
import com.memong.aos.data.utils.Util
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.databinding.ActivityMemoListBinding
import com.memong.aos.helper.MemoWidgetUpdater
import com.memong.aos.service.LockScreenService
import com.memong.aos.ui.adapter.MemoBlockAdapter
import com.memong.aos.ui.adapter.MemoGridAdapter
import com.memong.aos.ui.adapter.MemoListAdapter
import com.memong.aos.ui.adapter.itemDecoration.HorizontalSpaceItemDecoration
import com.memong.aos.ui.custom.dialog.DialogMode
import com.memong.aos.ui.custom.dialog.MemoAICustomDialog
import com.memong.aos.ui.custom.dialog.MemoCustomAdDialog
import com.memong.aos.ui.custom.dialog.MemoCustomAiAnswerDialog
import com.memong.aos.ui.custom.dialog.MemoCustomDialog
import com.memong.aos.ui.custom.dialog.MemoCustomPopupDialog
import com.memong.aos.ui.custom.dialog.MemoSortingCustomDialog
import com.memong.aos.ui.custom.header.SettingHeaderView
import com.memong.aos.ui.custom.item.MainGridSpacingItemDecoration
import com.memong.aos.ui.custom.item.MainListSpacingItemDecoration
import com.memong.aos.ui.custom.item.MainSectionBackgroundItemDecoration
import com.memong.aos.ui.custom.menu.MemoMenuView
import com.memong.aos.ui.listener.FabVisibilityScrollListener
import com.memong.aos.ui.listener.ItemSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


internal class MemoListActivity : BaseActivity(), View.OnClickListener, ItemSelectedListener {
    override val NAME: String
        get() = MemoListActivity::class.java.simpleName

    private lateinit var binding: ActivityMemoListBinding
    private var currentLayoutMode: MainLayoutMode = MainLayoutMode.GRID
    private var currentGroupMode: MainGroupMode = DATE
    private var passwordGuideDialog: MemoCustomDialog? = null
    private var memoSortingCustomDialog: MemoSortingCustomDialog? = null
    private var dateTimeDialog: MemoCustomDialog? = null

    private lateinit var gridAdapter: MemoGridAdapter
    private lateinit var listAdapter: MemoListAdapter

    private var isDynamicSectionInit: Boolean = false

    private var tutorialStep = 0

    // Chip → 질문 매핑 정의
    private val chipToQuestionMap by lazy {
        mapOf(
            getString(R.string.haru_chip_today_weather_title) to getString(R.string.haru_chip_today_weather),
            getString(R.string.haru_chip_today_news_title) to getString(R.string.haru_chip_today_news),
            getString(R.string.haru_chip_today_lunch_title) to getString(R.string.haru_chip_today_lunch),
            getString(R.string.haru_chip_today_dinner_title) to getString(R.string.haru_chip_today_dinner),
            getString(R.string.haru_chip_today_stock_title) to getString(R.string.haru_chip_today_stock),
            getString(R.string.haru_chip_today_exchange_rate_title) to getString(R.string.haru_chip_today_exchange_rate),
            getString(R.string.haru_chip_today_sports_title) to getString(R.string.haru_chip_today_sports),
            getString(R.string.haru_chip_today_issue_title) to getString(R.string.haru_chip_today_issue),
            getString(R.string.haru_chip_today_schedule_title) to getString(R.string.haru_chip_today_schedule),
            getString(R.string.haru_chip_today_movie_title) to getString(R.string.haru_chip_today_movie),
            getString(R.string.haru_chip_today_drama_title) to getString(R.string.haru_chip_today_drama),
            getString(R.string.haru_chip_today_book_title) to getString(R.string.haru_chip_today_book),
            getString(R.string.haru_chip_today_music_title) to getString(R.string.haru_chip_today_music),
            getString(R.string.haru_chip_today_health_tip_title) to getString(R.string.haru_chip_today_health_tip),
            getString(R.string.haru_chip_today_diet_tip_title) to getString(R.string.haru_chip_today_diet_tip),
            getString(R.string.haru_chip_today_workout_title) to getString(R.string.haru_chip_today_workout),
            getString(R.string.haru_chip_today_trip_title) to getString(R.string.haru_chip_today_trip),
            getString(R.string.haru_chip_today_recipe_title) to getString(R.string.haru_chip_today_recipe),
            getString(R.string.haru_chip_today_tip_title) to getString(R.string.haru_chip_today_tip),
            getString(R.string.haru_chip_today_english_title) to getString(R.string.haru_chip_today_english),
            getString(R.string.haru_chip_today_japanese_title) to getString(R.string.haru_chip_today_japanese),
            getString(R.string.haru_chip_today_chinese_title) to getString(R.string.haru_chip_today_chinese),
            getString(R.string.haru_chip_today_history_title) to getString(R.string.haru_chip_today_history),
            getString(R.string.haru_chip_today_quote_title) to getString(R.string.haru_chip_today_quote),
            getString(R.string.haru_chip_today_proverb_title) to getString(R.string.haru_chip_today_proverb),
            getString(R.string.haru_chip_today_politics_title) to getString(R.string.haru_chip_today_politics),
            getString(R.string.haru_chip_today_economy_title) to getString(R.string.haru_chip_today_economy),
            getString(R.string.haru_chip_today_society_title) to getString(R.string.haru_chip_today_society),
            getString(R.string.haru_chip_today_international_title) to getString(R.string.haru_chip_today_international),
            getString(R.string.haru_chip_today_science_title) to getString(R.string.haru_chip_today_science),
            getString(R.string.haru_chip_today_it_title) to getString(R.string.haru_chip_today_it),
            getString(R.string.haru_chip_today_environment_title) to getString(R.string.haru_chip_today_environment),
            getString(R.string.haru_chip_today_indicator_title) to getString(R.string.haru_chip_today_indicator),
            getString(R.string.haru_chip_today_real_estate_title) to getString(R.string.haru_chip_today_real_estate),
            getString(R.string.haru_chip_today_oil_title) to getString(R.string.haru_chip_today_oil),
            getString(R.string.haru_chip_today_gold_title) to getString(R.string.haru_chip_today_gold),
            getString(R.string.haru_chip_today_coin_title) to getString(R.string.haru_chip_today_coin),
            getString(R.string.haru_chip_today_fashion_title) to getString(R.string.haru_chip_today_fashion),
            getString(R.string.haru_chip_today_shopping_title) to getString(R.string.haru_chip_today_shopping),
            getString(R.string.haru_chip_today_traffic_title) to getString(R.string.haru_chip_today_traffic),
            getString(R.string.haru_chip_today_fine_dust_title) to getString(R.string.haru_chip_today_fine_dust)
        )
    }


    private val generativeModel by lazy {
        (application as MemongApplication).generativeModel
    }


    val memoDao by lazy {
        MemoDatabase.getInstance(this@MemoListActivity).memoDao()
    }
    val tagDao by lazy {
        MemoDatabase.getInstance(this@MemoListActivity).tagDao()
    }
    val taggingDao by lazy {
        MemoDatabase.getInstance(this@MemoListActivity).taggingDao()
    }

    private var memoSortType: MemoSortType
        set(value) {
            PreferenceUtil.set(
                key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE,
                value = value.name
            )
        }
        get() {
            return when (PreferenceUtil.get(
                key = PreferenceUtil.KEY_MEMO_SORTING_CURRENT_TYPE,
                defaultValue = MemoSortType.LATEST_CREATE.name
            )) {
                MemoSortType.LATEST_CREATE.name -> MemoSortType.LATEST_CREATE
                MemoSortType.OLDEST_CREATE.name -> MemoSortType.OLDEST_CREATE
                MemoSortType.LATEST_UPDATE.name -> MemoSortType.LATEST_UPDATE
                MemoSortType.OLDEST_UPDATE.name -> MemoSortType.OLDEST_UPDATE
                else -> MemoSortType.CUSTOM
            }
        }


    private val passwordCheckLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            LogTrack.i(NAME) { "passwordCheckLauncher::RESULT_OK" }
            val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    PasswordActivity.EXTRA_MEMO_ITEM,
                    MemoEntity::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(PasswordActivity.EXTRA_MEMO_ITEM)
            }
            item?.let {
                EventUtil.sendEvent(
                    this@MemoListActivity,
                    EventUtil.CATEGORY_DETAIL,
                    EventUtil.ACTION_MEMO_READ
                )
                MemoDetailActivity.start(
                    activity = this,
                    mode = MemoMode.READ_MEMO,
                    memoItem = it
                )
            }
        }
    }


    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, MemoListActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }

        fun startRestore(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, MemoListActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK,
                close = close
            )
        }

        fun startWidget(
            context: Context,
        ) {
            val intent = Intent(context, MemoListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.headerView.onDestroy()
        passwordGuideDialog?.onDestroy()
        memoSortingCustomDialog?.onDestroy()
        binding.newMemoContainer.clearAnimation()
        binding.bottomBannerView.onDestroy()
        AnimationUtil.stopPulse(view = binding.newMemoContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LogTrack.i(tag = NAME) { " onCreate()" }

        StatisticsUtil.trackStatistics(this)

        // 루트 뷰에 인셋 적용
        configureWindowInsets(binding.memoListRootView, paddingDp = 0)

        // 메인화면 초기값
        PreferenceUtil.set(KEY_SIMPLIFY, false)
        PreferenceUtil.set(KEY_SECTION_SECRET_MEMO_IS_EXPANDED, false)
        PreferenceUtil.set(KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED, true)
        PreferenceUtil.set(KEY_MEMO_GROUP_MODE, DATE.name)
        PreferenceUtil.set(KEY_DYNAMIC_SECTIONS_IS_INIT, false)


        // 뷰 초기화
        setView()
        observeEventFlow()

        // 메인 팝업
        if (!showDateTimeWarningDialogIfNeeded()) {
            showMainPopup()
        }

        // 백업 가이드
        maybeShowBackupGuideDialog()

        // 리사이클러뷰 초기화
        initAdapters()
        initRecyclerViews()

        // 태그 설정
        setTags()
        // 드래그 설정
        setDragItemListener()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )

        // 헤더 설정
//        val mode = getSavedViewMode()
//        setHeaderModeView(mode)

        // listener
        setBackKeyListener()
        binding.newMemoContainer.setOnClickListener(this@MemoListActivity)
        setupBackHandler()
    }

    override fun onPause() {
        super.onPause()
        binding.bottomBannerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        showDateTimeWarningDialogIfNeeded()
        maybeStartLockScreenService()
        binding.bottomBannerView.onResume()
        if (PreferenceUtil.get(KEY_MEMO_RESTORE, false)) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    MemoWidgetUpdater().observeMemoChanges(this@MemoListActivity)
                    PreferenceUtil.set(KEY_MEMO_RESTORE, false)
                }
            }
        }
    }

    /**
     * @return true = 경고 다이얼로그가 떠서 메인 팝업을 띄우면 안 됨
     *         false = 정상 상태라서 메인 팝업 띄워도 됨
     */
    private fun showDateTimeWarningDialogIfNeeded(): Boolean {
        return try {
            val autoTime = Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME, 0)
            val autoTimeZone =
                Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME_ZONE, 0)

            if (autoTime == 0 || autoTimeZone == 0) {
                if (dateTimeDialog?.isShowing == true) return true

                dateTimeDialog = MemoCustomDialog(this).apply {
                    setTitle("⚠️ 시스템 날짜/시간 설정 오류")
                    setMessage(
                        "현재 기기의 날짜 및 시간이 자동으로 설정되지 않았습니다.\n" +
                                "앱의 정상 이용을 위해 자동 설정을 켜주세요."
                    )
                    setBottomSubMessage("자동 시간/자동 시간대 설정을 활성화해야 앱이 정상 동작합니다.")
                    setButton(
                        cancelText = "닫기",
                        confirmText = "설정으로 이동",
                        onCancel = {
                            onDestroy()
                            finishAffinity()
                        },
                        onConfirm = {
                            try {
                                val intent = Intent(Settings.ACTION_DATE_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(intent)
                                dismiss()
                                dateTimeDialog = null
                            } catch (e: Exception) {
                                toastShort(this@MemoListActivity, "설정 화면을 열 수 없습니다.")
                            }
                        }
                    )
                }
                dateTimeDialog?.show()
                true // 다이얼로그 뜸
            } else {
                dateTimeDialog?.dismiss()
                dateTimeDialog = null
                false // 정상 상태
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showMainPopup() {
        val setting = RemoteConfigUtil.getMainPopupSetting()

        // 최종 노출 가능 여부 (사용 여부 + 버전 허용 + 기간 허용)
        if (!RemoteConfigUtil.canShowMainPopup()) return

        // 사용자가 이미 닫은 경우는 다시 띄우지 않음
        val isClosed = PreferenceUtil.get(PreferenceUtil.KEY_MAIN_POPUP_CLOSED, false)
        if (isClosed) return

        val dialog = MemoCustomPopupDialog(this)

        val rawUrl = setting.main_popup_load_image
        val directUrl = GoogleDriveUtil.toDirectUrl(rawUrl)
        val landingUrl = setting.main_popup_landing_url

        dialog.setImage(directUrl) {
            when (setting.main_popup_landing_page) {
                0 -> if (landingUrl.isNotBlank()) {
                    runCatching {
                        startActivity(Intent(Intent.ACTION_VIEW, landingUrl.toUri()))
                    }.onFailure {
                        toastShort(this@MemoListActivity, "URL을 로드할 수 없습니다.")
                    }
                }

                1 -> SettingActivity.start(this)
                2 -> SettingServiceInfoActivity.start(this)
                3 -> SettingWebViewActivity.start(
                    this,
                    getString(R.string.haru_webview_mode_notice)
                )

                4 -> SettingWebViewActivity.start(
                    this,
                    getString(R.string.haru_webview_mode_backup)
                )

                5 -> SettingWebViewActivity.start(this, getString(R.string.haru_webview_mode_faq))
            }
            dialog.onDestroy()
        }

        dialog.setButton(
            confirmText = getString(R.string.haru_main_popup_close),
            onConfirm = {
                PreferenceUtil.set(PreferenceUtil.KEY_MAIN_POPUP_CLOSED, true)
                dialog.onDestroy()
            }
        )

        dialog.show()
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentLayoutMode == MainLayoutMode.EDIT) {
                    val savedLayoutMode = getSavedLayoutMode()
                    val savedGroupMode = getSavedGroupMode()

                    when (savedLayoutMode) {
                        MainLayoutMode.GRID -> gridAdapter.exitSelectMode()
                        MainLayoutMode.LIST -> listAdapter.exitSelectMode()
                        MainLayoutMode.EDIT -> {
                            // nothing
                        }
                    }
                    LogTrack.i(NAME) { "setEditModeView -> { savedLayoutMode: $savedLayoutMode, savedGroupMode: $savedGroupMode }" }
                    setHeaderModeView(layoutMode = savedLayoutMode, groupMode = savedGroupMode)
                } else {
                    showExitDialog()
                }
            }
        })
    }

    private fun showExitDialog() {
        if (isFinishing) return
        val dialog = MemoCustomAdDialog(
            context = this@MemoListActivity,
            placementId = BuildConfig.ADCASH_FINISH_POPUP_PID,
            bannerAdSize = BannerAdSize.DYNAMIC
        ).apply {
            setMessage(getString(R.string.haru_exit_dialog_message))
            setMessageTopMarginDp(0)

            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_dialog_exit_confirm),
                onCancel = { dismiss() },
                onConfirm = {
                    dismiss()
                    finishAffinity() // 전체 액티비티 종료 → 앱 종료
                }
            )
        }
        dialog.show()
    }

    private fun observeEventFlow() {
        lifecycleScope.launch {
            MemoEventFlow.events.collect { event ->
                LogTrack.i(NAME) { "observeEventFlow -> event: ${event.toString()}" }
                when (event) {
                    is MemoEvent.AllMemoUpdated -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                // 어댑터
                                refreshMemoList()
                                // 태그
                                setTags()
                            }
                        }
                    }

                    is MemoEvent.MemoUpdated -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            val updated = memoDao.getMemoById(event.memoId)
                            withContext(Dispatchers.Main) {
                                // 어댑터
                                gridAdapter.updateSingleMemo(updated)
                                listAdapter.updateSingleMemo(updated)
                                // 태그
                                setTags()
                            }
                        }
                    }

                    is MemoEvent.PasswordMemoDeleteEvent -> {
                        toastShort(
                            this@MemoListActivity,
                            getString(R.string.haru_lock_memo_delete_move_to_trash)
                        )
                        refreshMemoList()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    private fun setView() {
        binding.memoListRootView.post {
            // 새메모하기 버튼
            AnimationUtil.startPulse(view = binding.newMemoContainer)

            // 튜토리얼 뷰
            showTutorialStep()
        }
    }


    private fun showTutorialStep() {

        val isShowingTutorial = PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)
        LogTrack.i { "$NAME -> showTutorialStep { isShowingTutorialMain: $isShowingTutorial }" }
        if (isShowingTutorial) {
            return
        }

        val steps = listOf(
            TutorialStep(
                target = binding.newMemoContainer,
                title = getString(R.string.haru_tutorial_main_title_step_1),
                desc = getString(R.string.haru_tutorial_main_desc_step_1) + "               ",
                focus = TutorialFocusType.MAIN_FOCUS
            ),
            TutorialStep(
                target = binding.headerView.getAction1Button(),
                title = getString(R.string.haru_tutorial_main_title_step_2),
                desc = getString(R.string.haru_tutorial_main_desc_step_2),
                focus = TutorialFocusType.SUB_FOCUS
            ),
            TutorialStep(
                target = binding.headerView.getAction2Button(),
                title = getString(R.string.haru_tutorial_main_title_step_3),
                desc = getString(R.string.haru_tutorial_main_desc_step_3),
                focus = TutorialFocusType.SUB_FOCUS
            ),
            TutorialStep(
                target = binding.headerView.getAction3Button(),
                title = getString(R.string.haru_tutorial_main_title_step_4),
                desc = getString(R.string.haru_tutorial_main_desc_step_4),
                focus = TutorialFocusType.SUB_FOCUS
            )
        )

        if (tutorialStep >= steps.size) return
        steps[tutorialStep].let { step ->
            MaterialTapTargetPrompt.Builder(this)
                .setTarget(step.target)
                .setPrimaryText(step.title)
                .setSecondaryText(step.desc)
                .setPrimaryTextSize(Util.spToPx(this, 20f))
                .setSecondaryTextSize(Util.spToPx(this, 15f))
                .setPrimaryTextTypeface(
                    ResourcesCompat.getFont(
                        this@MemoListActivity,
                        R.font.lineseed_kr_bold
                    )
                )
                .setSecondaryTextTypeface(
                    ResourcesCompat.getFont(
                        this@MemoListActivity,
                        R.font.lineseed_kr_regular
                    )
                )
                .setBackgroundColour(ContextCompat.getColor(this, R.color.haru_primary_orange))
                .setPrimaryTextColour(Color.WHITE)
                .setSecondaryTextColour(Color.WHITE)
                .setFocalRadius(if (step.focus == TutorialFocusType.MAIN_FOCUS) 120f else 60f)
//                .setPromptBackground(RectanglePromptBackground())
                .setAutoDismiss(false) // 배경 클릭해도 닫히지 않음
                .setAutoFinish(false)  // 포커스 클릭 시 자동 종료 방지
                .setPromptStateChangeListener { prompt, state ->
                    when (state) {
                        MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                            LogTrack.i { "$NAME -> showTutorialStep -> MaterialTapTargetPrompt::STATE_FOCAL_PRESSED{ tutorialStep: $tutorialStep }" }
                            tutorialStep++
                            if (tutorialStep >= steps.size) {
                                PreferenceUtil.set(KEY_SHOW_TUTORIAL_MEMO_MAIN, true)
                                prompt.dismiss()
                            } else {
                                prompt.dismiss()
                                showTutorialStep()
                            }
                        }
                    }
                }
                .show()
        }
    }


    private fun initAdapters() {
        // gridAdapter
        gridAdapter = MemoGridAdapter(
            activity = this@MemoListActivity,
        ).apply {
            setSortType(sortType = memoSortType)
            setItemSelectedListener(this@MemoListActivity)
            setOnMemoClickListener(callback = { item ->
                EventUtil.sendEvent(
                    this@MemoListActivity,
                    EventUtil.CATEGORY_DETAIL,
                    EventUtil.ACTION_MEMO_READ
                )
                MemoDetailActivity.start(
                    activity = this@MemoListActivity,
                    mode = MemoMode.READ_MEMO,
                    memoItem = item
                )
            })
            setOnMemoUpdatedListener { refreshMemoList() }
            setOnRequestPasswordCheck { item ->
                if (PreferenceUtil.get(KEY_SIMPLIFY, false)) {
                    EventUtil.sendEvent(
                        this@MemoListActivity,
                        EventUtil.CATEGORY_DETAIL,
                        EventUtil.ACTION_MEMO_READ
                    )
                    MemoDetailActivity.start(
                        activity = this@MemoListActivity,
                        mode = MemoMode.READ_MEMO,
                        memoItem = item
                    )
                } else {
                    val intent = PasswordActivity.createIntent(
                        activity = this@MemoListActivity,
                        mode = SettingHeaderView.SetHeaderMode.PASSWORD_CHECK,
                        memoItem = item
                    )
                    passwordCheckLauncher.launch(intent)
                }
            }
        }

        // listAdapter
        listAdapter = MemoListAdapter(
            this@MemoListActivity
        ).apply {
            setSortType(sortType = memoSortType)
            setItemSelectedListener(this@MemoListActivity)
            setOnMemoClickListener(callback = { item ->
                EventUtil.sendEvent(
                    this@MemoListActivity,
                    EventUtil.CATEGORY_DETAIL,
                    EventUtil.ACTION_MEMO_READ
                )
                MemoDetailActivity.start(
                    activity = this@MemoListActivity,
                    mode = MemoMode.READ_MEMO,
                    memoItem = item
                )
            })
            setOnMemoUpdatedListener { refreshMemoList() }
            setOnRequestPasswordCheck { item ->
                if (PreferenceUtil.get(KEY_SIMPLIFY, false)) {
                    EventUtil.sendEvent(
                        this@MemoListActivity,
                        EventUtil.CATEGORY_DETAIL,
                        EventUtil.ACTION_MEMO_READ
                    )
                    MemoDetailActivity.start(
                        activity = this@MemoListActivity,
                        mode = MemoMode.READ_MEMO,
                        memoItem = item
                    )
                } else {
                    val intent = PasswordActivity.createIntent(
                        activity = this@MemoListActivity,
                        mode = SettingHeaderView.SetHeaderMode.PASSWORD_CHECK,
                        memoItem = item
                    )
                    passwordCheckLauncher.launch(intent)
                }
            }
        }
    }


    private fun initRecyclerViews() {
        val gridItemSectionSpanCount = 3
        val gridItemSpanCount = 1
        val gridItemSpacingPx = Util.dpToPx(this, 16)
        val listItemSpacingPx = Util.dpToPx(this, 10)
        val scrollListener = FabVisibilityScrollListener(binding.newMemoContainer)

        // 그리드뷰
        binding.rvMemoGrid.apply {
            layoutManager = GridLayoutManager(context, gridItemSectionSpanCount).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (gridAdapter.getItemViewType(position)) {
                            MemoGridAdapter.VIEW_TYPE_LOCK_SECTION,
                            MemoGridAdapter.VIEW_TYPE_IMPORTANT_SECTION,
                            MemoGridAdapter.VIEW_TYPE_DATE_SECTION,
                            MemoGridAdapter.VIEW_TYPE_EMPTY -> gridItemSectionSpanCount

                            else -> gridItemSpanCount
                        }
                    }
                }
            }
            adapter = gridAdapter

            // 배경색
            addItemDecoration(
                MainSectionBackgroundItemDecoration(
                    sectionResolver = { position -> gridAdapter.getSectionTypeForPosition(position) },
                    sectionColorProvider = { sectionKey ->
                        when (sectionKey) {
                            FixedSectionType.SECRET.key, FixedSectionType.IMPORTANT.key -> ContextCompat.getColor(
                                this@MemoListActivity,
                                R.color.recyclerview_color
                            )

                            else -> ContextCompat.getColor(
                                this@MemoListActivity,
                                R.color.view_background_color
                            )
                        }
                    }
                )
            )

            // 아이템 간격
            addItemDecoration(
                MainGridSpacingItemDecoration(
                    layoutManager = layoutManager as GridLayoutManager,
                    spacing = gridItemSpacingPx,
                    isMemoItemType = { pos -> gridAdapter.getItemViewType(pos) == MemoGridAdapter.VIEW_TYPE_MEMO },
                    getSectionIndexForPosition = { pos -> gridAdapter.getSectionIndexForPosition(pos) },
                    getFirstAndLastMemoIndexInSection = { sectionIndex ->
                        gridAdapter.getFirstAndLastMemoIndexInSection(
                            sectionIndex
                        )
                    }
                )
            )

            addOnScrollListener(scrollListener)
        }

        // 리스트뷰
        binding.rvMemoList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter

            // 배경색
            addItemDecoration(
                MainSectionBackgroundItemDecoration(
                    sectionResolver = { position -> listAdapter.getSectionTypeForPosition(position) },
                    sectionColorProvider = { sectionKey ->
                        when (sectionKey) {
                            FixedSectionType.SECRET.key, FixedSectionType.IMPORTANT.key -> ContextCompat.getColor(
                                this@MemoListActivity,
                                R.color.recyclerview_color
                            )

                            else -> ContextCompat.getColor(
                                this@MemoListActivity,
                                R.color.view_background_color
                            )
                        }
                    }
                )
            )

            // 아이템 간격
            addItemDecoration(
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
            addOnScrollListener(scrollListener)
        }


        // 리스트 초기화
        refreshMemoList(isBeginning = true)

    }


    private fun setHeaderModeView(
        layoutMode: MainLayoutMode,
        groupMode: MainGroupMode = getSavedGroupMode(),
        value: Map<String, Any>? = null
    ) {
        currentLayoutMode = layoutMode
        currentGroupMode = groupMode
        binding.headerView.setHeaderMode(
            memoSortType = this@MemoListActivity.memoSortType,
            layoutMode = layoutMode,
            groupMode = groupMode,
            value = value
        )

        when (layoutMode) {
            MainLayoutMode.GRID -> setGridModeView()
            MainLayoutMode.LIST -> setListModeView()
            MainLayoutMode.EDIT -> setEditModeView()
        }
    }


    private fun setTags() {
        CoroutineScope(Dispatchers.IO).launch {
            val taggingList = taggingDao.getAllTaggings()  // TaggingEntity(memoid, tagid)
            val tagList = tagDao.getAllTags()              // TagEntity(_id, tagName)

            withContext(Dispatchers.Main) {
                val tagIdToName: Map<Int, String> = tagList.associate {
                    it._id to it.tagName
                }

                // memoid → tagName 리스트 맵 생성
                val memoIdToTagNames: Map<Int, List<String>> = taggingList
                    .groupBy { it.memoid }
                    .mapValues { (_, taggings) ->
                        taggings.mapNotNull { tagIdToName[it.tagid] }
                    }

                gridAdapter.updateMemoTags(map = memoIdToTagNames)
                listAdapter.updateMemoTags(map = memoIdToTagNames)
            }
        }


    }

    private fun setGridModeView() {
        binding.rvMemoGrid.isVisible = true
        binding.rvMemoList.isVisible = false

        binding.headerView.apply {

            // 검색
            setOnAction1ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction1ClickListener
                }
                SearchActivity.start(this@MemoListActivity)
            }
            // Group 토글(Date, Tag)
            setOnAction2ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction2ClickListener
                }
                if (memoSortType == MemoSortType.CUSTOM) {
                    when (currentGroupMode) {
                        DATE -> {
                            toastShort(
                                this@MemoListActivity,
                                getString(R.string.haru_header_tag_mode_unavailable_when_custom_sort)
                            )
                        }

                        TAG -> {
                            toastShort(
                                this@MemoListActivity,
                                getString(R.string.haru_header_date_mode_unavailable_when_custom_sort)
                            )
                        }

                        else -> {}
                    }
                    return@setOnAction2ClickListener

                } else {
                    toggleGroupMode()
                }
            }
            // Layout 토글(Grid, List)
            setOnAction3ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction3ClickListener
                }
                toggleLayoutMode()
            }
            // 메뉴
            setOnAction4ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction4ClickListener
                }
                openMenuOption()
            }
        }
    }

    private fun setListModeView() {

        binding.rvMemoGrid.isVisible = false
        binding.rvMemoList.isVisible = true

        binding.headerView.apply {
            // 검색
            setOnAction1ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction1ClickListener
                }
                SearchActivity.start(this@MemoListActivity)
            }
            // Group 토글(Date, Tag)
            setOnAction2ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction2ClickListener
                }

                if (memoSortType == MemoSortType.CUSTOM) {
                    when (currentGroupMode) {
                        DATE -> {
                            toastShort(
                                this@MemoListActivity,
                                getString(R.string.haru_header_tag_mode_unavailable_when_custom_sort)
                            )
                        }

                        TAG -> {
                            toastShort(
                                this@MemoListActivity,
                                getString(R.string.haru_header_date_mode_unavailable_when_custom_sort)
                            )
                        }

                        else -> {}
                    }
                    return@setOnAction2ClickListener

                } else {
                    toggleGroupMode()
                }
            }
            // Layout 토글(Grid, List)
            setOnAction3ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction3ClickListener
                }
                toggleLayoutMode()
            }
            // 메뉴
            setOnAction4ClickListener {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return@setOnAction4ClickListener
                }
                openMenuOption()
            }
        }
    }

    private fun setEditModeView() {
        val savedLayoutMode = getSavedLayoutMode()
        val savedGroupMode = getSavedGroupMode()

        val selectedItems = when (savedLayoutMode) {
            MainLayoutMode.GRID -> gridAdapter.getSelectedItems()
            MainLayoutMode.LIST -> listAdapter.getSelectedItems()
            else -> emptyList()
        }


        binding.headerView.apply {
            // back
            setOnBackClickListener {
                when (savedLayoutMode) {
                    MainLayoutMode.GRID -> gridAdapter.exitSelectMode()
                    MainLayoutMode.LIST -> listAdapter.exitSelectMode()
                    MainLayoutMode.EDIT -> {
                        // nothing
                    }
                }
                LogTrack.i(NAME) { "setEditModeView -> savedLayoutMode: $savedLayoutMode" }
                setHeaderModeView(layoutMode = savedLayoutMode, groupMode = savedGroupMode)
            }

            // 공유
            setOnAction1ClickListener {
                if (selectedItems.any { it.isLocked }) {
                    if (PreferenceUtil.get(KEY_SIMPLIFY, false)) {
                        shareMemos()
                    } else {
                        val intent = PasswordActivity.createIntent(
                            activity = this@MemoListActivity,
                            mode = SettingHeaderView.SetHeaderMode.PASSWORD_CHECK,
                        )
                        passwordCheckLauncher.launch(intent)
                    }
                } else {
                    shareMemos()
                }
                EventUtil.sendEvent(
                    this@MemoListActivity,
                    EventUtil.CATEGORY_MAIN,
                    EventUtil.ACTION_SHARE_MEMO
                )
            }


            // 복제
            setOnAction2ClickListener {
                val selectCount = getCurrentSelectCount()
                if (selectCount == 1) {
                    val item = selectedItems.first()
                    if (item.isLocked) {
                        if (PreferenceUtil.get(KEY_SIMPLIFY, false)) {
                            copyMemo()
                        } else {
                            val intent = PasswordActivity.createIntent(
                                activity = this@MemoListActivity,
                                mode = SettingHeaderView.SetHeaderMode.PASSWORD_CHECK,
                            )
                            passwordCheckLauncher.launch(intent)
                        }
                    } else {
                        copyMemo()
                    }
                    EventUtil.sendEvent(
                        this@MemoListActivity,
                        EventUtil.CATEGORY_MAIN,
                        EventUtil.ACTION_COPY_MEMO
                    )
                } else {
                    toastShort(
                        this@MemoListActivity,
                        getString(R.string.haru_header_selected_mode_copy_memo_just_one_selected)
                    )
                }
            }


            // 삭제
            setOnAction3ClickListener {
                showDeleteDialog()
            }

            // 메모 일괄 클릭
            setOnCheckBoxClickListener { isChecked ->
                onMemoAllSelected(isChecked)
            }

        }
    }


    private fun getCurrentSelectCount(): Int {
        return when (getSavedLayoutMode()) {
            MainLayoutMode.GRID -> gridAdapter.getSelectedItems().size
            MainLayoutMode.LIST -> listAdapter.getSelectedItems().size
            else -> 0
        }
    }


    private fun getSavedLayoutMode(): MainLayoutMode {
        val saved = PreferenceUtil.get(KEY_MEMO_LAYOUT_MODE, MainLayoutMode.GRID.name)
        return runCatching { MainLayoutMode.valueOf(saved) }.getOrDefault(MainLayoutMode.GRID)
    }


    private fun getSavedGroupMode(): MainGroupMode {
        val saved = PreferenceUtil.get(KEY_MEMO_GROUP_MODE, DATE.name)
        return runCatching { MainGroupMode.valueOf(saved) }.getOrDefault(DATE)
    }


    private fun toggleLayoutMode() {
        val mode: MainLayoutMode =
            if (currentLayoutMode == MainLayoutMode.GRID) MainLayoutMode.LIST else MainLayoutMode.GRID
        LogTrack.i(NAME) { "toggleLayoutMode() -> currentLayoutMode: $mode" }
        PreferenceUtil.set(KEY_MEMO_LAYOUT_MODE, mode.name)

        // 이벤트 전송
        when (mode) {
            MainLayoutMode.GRID -> EventUtil.sendEvent(
                this@MemoListActivity,
                EventUtil.CATEGORY_MAIN,
                EventUtil.ACTION_MODE_GRID
            )

            MainLayoutMode.LIST -> EventUtil.sendEvent(
                this@MemoListActivity,
                EventUtil.CATEGORY_MAIN,
                EventUtil.ACTION_MODE_LIST
            )

            else -> {}
        }

        refreshMemoList()
    }

    private fun toggleGroupMode() {
        val mode: MainGroupMode =
            if (currentGroupMode == DATE) TAG else DATE
        LogTrack.i(NAME) { "toggleGroupMode() -> currentGroupMode: $mode" }
        PreferenceUtil.set(KEY_MEMO_GROUP_MODE, mode.name)

        // 이벤트 전송
        when (mode) {
            DATE -> EventUtil.sendEvent(
                this@MemoListActivity,
                EventUtil.CATEGORY_MAIN,
                EventUtil.ACTION_MODE_DATE
            )

            TAG -> EventUtil.sendEvent(
                this@MemoListActivity,
                EventUtil.CATEGORY_MAIN,
                EventUtil.ACTION_MODE_TAG
            )

            else -> {}
        }

        refreshMemoList()
    }


    private fun openMenuOption() {
        if (!this.isAlive) return
        MemoMenuView(this@MemoListActivity).apply {
            setOnItemClickListener { position, label ->
                when (label) {
                    // 메모정렬
                    getString(R.string.haru_menu_options_sort) -> showSortingDialog()

                    // 메모편집
                    getString(R.string.haru_menu_options_edit) -> {
                        when (currentGroupMode) {
                            DATE -> {
                                if (currentLayoutMode == MainLayoutMode.GRID) {
                                    gridAdapter.enterSelectMode()
                                } else if (currentLayoutMode == MainLayoutMode.LIST) {
                                    listAdapter.enterSelectMode()
                                }
                            }

                            TAG -> {
                                toastShort(
                                    this@MemoListActivity,
                                    getString(R.string.haru_menu_options_edit_available)
                                )
                                return@setOnItemClickListener
                            }
                        }
                    }

                    // 설정
                    getString(R.string.haru_menu_options_settings) -> {
                        SettingActivity.start(this@MemoListActivity)
                    }

                    // 휴지통
                    getString(R.string.haru_menu_options_trash) -> {
                        if (PreferenceUtil.get(KEY_PASSWORD_SWITCH, false)) {
                            // 이미 비밀번호 설정됨 → 휴지통 비밀번호 액티비티로 이동
                            TrashPasswordActivity.start(
                                activity = this@MemoListActivity,
                                mode = SettingHeaderView.SetHeaderMode.TRASH
                            )
                        } else {
                            // 비밀번호 미설정 → 커스텀 다이얼로그로 유도
                            showPasswordGuideDialog()
                        }
                    }

                    // AI
                    getString(R.string.haru_menu_ai) -> {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            Util.toastShort(
                                this@MemoListActivity,
                                "Android 6.0 (API 23) 이상 기기에서만 지원됩니다."
                            )
                            return@setOnItemClickListener
                        }
                        showAiQnaDialog()
                    }

                    else -> false
                }
            }

            show(
                anchor = binding.headerView.findViewById(R.id.btnAction3),
                mode = MemoMenuView.Mode.OPTIONS
            )
        }
    }

    /** MemoAICustomDialog 사용 */
    private fun showAiQnaDialog() {
        val dialog = MemoAICustomDialog(this).apply {
            setTitle("AI에게 질문하기")
            setMessage("무엇이든 물어보세요.")
            setBottomSubMessage("메모 관련 질문은 고정 답변으로 안내합니다.")

            // Chip 클릭 처리
            setOnChipClick { chip ->
                val question = chipToQuestionMap[chip] ?: chip
                onDestroy()
                lifecycleScope.launch {
                    showProgressView()
                    val answer = try {
                        handleMemoQuestion(question) ?: askAi(question)
                    } finally {
                        hideProgressView()
                    }
                    EventUtil.sendEvent(
                        this@MemoListActivity,
                        EventUtil.CATEGORY_MAIN,
                        EventUtil.ACTION_USE_AI_QNA
                    )
                    showAiAnswerDialog(answer)
                }
            }

            setButtons(
                cancelText = getString(android.R.string.cancel),
                confirmText = getString(R.string.ask),
                onCancel = { onDestroy() },
                onConfirm = { userQuestion ->
                    lifecycleScope.launch {
                        onDestroy()
                        showProgressView()
                        val answer = try {
                            handleMemoQuestion(userQuestion) ?: askAi(userQuestion)
                        } finally {
                            hideProgressView()
                        }
                        EventUtil.sendEvent(
                            this@MemoListActivity,
                            EventUtil.CATEGORY_MAIN,
                            EventUtil.ACTION_USE_AI_QNA
                        )
                        showAiAnswerDialog(answer)
                    }
                }
            )
        }
        dialog.show()
    }

    private fun showAiAnswerDialog(answer: String) {
        MemoCustomAiAnswerDialog(this, DialogMode.ANSWER).apply {
            setTitle("답변")
            setMessage(
                HtmlCompat.fromHtml(answer, HtmlCompat.FROM_HTML_MODE_LEGACY)
            )
            setButton(
                cancelText = getString(R.string.haru_ai_close),
                confirmText = getString(R.string.haru_ai_next),
                onCancel = { onDestroy() },
                onConfirm = {
                    onDestroy()
                    showAiQnaDialog()
                }
            )
        }.show()
    }

    /**
     * 메모 관련 질문 고정 처리
     * - 관련 키워드가 포함되면 즉시 답변 반환
     * - 없으면 null 반환 → AI 호출로 이어짐
     */
    private fun handleMemoQuestion(userQuestion: String): String? {
        val lower = userQuestion.lowercase()

        return when {
            // 작성 / 수정
            lower.contains("작성") -> "오른 쪽 하단 보라색 버튼을 눌러 새 메모를 작성할 수 있어요!"
            lower.contains("수정") -> "작성된 메모를 클릭해 상세 화면에 진입한 후 본문을 눌러 수정해보세요."
            lower.contains("저장") -> "작성 또는 수정 후 상단 저장 버튼 또는 뒤로가기 시 바로 메모 내용이 저장됩니다!"

            // 백업 / 복원
            lower.contains("백업") -> "설정 > 백업 설정에서 Google 드라이브 또는 기기 저장소로 백업할 수 있어요.<br>또한 자동 백업이 활성화 되어있는 경우 기기 저장소로 메모 작성 및 수정 시 자동으로 백업이 진행됩니다!"
            lower.contains("복구") || lower.contains("복원") -> "설정 > 백업 설정 > 데이터 불러오기에서 복원을 진행할 수 있어요."

            // 삭제 / 휴지통
            lower.contains("삭제") || lower.contains("휴지통") -> "메모 삭제 시 휴지통으로 이동하며, 휴지통 메뉴에서 복구 또는 영구 삭제할 수 있어요."

            // 잠금 / 보안
            lower.contains("잠금") || lower.contains("비밀") -> "설정 -> 비밀번호 설정에서 비밀번호를 설정하시면 중요한 메모를 암호화할 수 있어요!<br>또한 비밀번호 설정 후 비밀번호를 잊어버리셨을 경우 기기 인증을 이용해 비밀번호를 재설정 하실 수 있습니다."

            // 사진 / 이미지 첨부
            lower.contains("사진") || lower.contains("이미지") -> "메모 작성 또는 수정 시 노출되는 툴바에서 카메라 혹은 이미지 버튼을 클릭해 사진을 첨부할 수 있어요."

            // 태그
            lower.contains("태그") || lower.contains("폴더") -> "메모에 태그를 추가하고 싶을 경우 메모 작성 또는 수정 시 # + 텍스트를 작성하실 경우 자동으로 태그가 생성되요!<br>태그 기능을 자세하게 알고 싶으시다면 설정 -> 고객센터 -> 도움말 -> 자주 찾는 도움말의 태그 관련 내용을 참고해주세요."

            // "메모"라는 단어만 있고 다른 키워드가 없을 때
            lower.contains("메모") -> "메모와 관련된게 궁금하시다면 설정 -> 고객센터 -> 도움말을 참고해주세요!"

            // 기타
            else -> null
        }
    }

    private suspend fun askAi(userQuestion: String): String {
        return try {
            val response = generativeModel.generateContent(
                """
            아래 질문에 대해 반드시 한국어로 **5줄 요약**으로 답해주세요.
            - 각 줄은 하나의 완전한 문장이 되도록 하세요.
            - 정확히 5줄만 출력하세요.
            
            질문:
            $userQuestion
            """.trimIndent()
            )

            val rawText = response.text ?: "답변을 가져오지 못했어요 😢"

            // 1) 줄 단위 분리
            val lines = rawText.split("\n")
                .map { it.trim().trimStart('-', '•') }
                .filter { it.isNotBlank() }

            // 2) 마침표 단위로 보정 (". " 기준 split)
            val sentenceSplit = lines.flatMap { line ->
                line.split(Regex("(?<=\\.)\\s+"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }

            // 3) 정확히 5줄 맞추기
            val fiveLines = when {
                sentenceSplit.size >= 5 -> sentenceSplit.take(5)
                else -> sentenceSplit + List(5 - sentenceSplit.size) { "" }
            }

            // 4) 불릿 제거, 문장 끝에 엔터로 구분
            val formatted = fiveLines.joinToString("<br>")

            // 5) **텍스트** → <b>텍스트</b>
            val withBold = formatted.replace(Regex("\\*\\*(.+?)\\*\\*"), "<b>$1</b>")

            return withBold

        } catch (e: Exception) {
            val msg = e.message ?: ""
            return when {
                msg.contains("PERMISSION_DENIED", true) ||
                        msg.contains("quota", true) ||
                        msg.contains("exceed", true) -> {
                    "무료 사용 한도가 모두 소진되었습니다. 잠시 후 다시 이용해 주세요."
                }

                else -> {
                    "AI 호출 중 오류가 발생했어요:\n${e.message}"
                }
            }
        }
    }

    private suspend fun getMemosListBySection(sectionType: MainSectionType): List<MemoEntity> {
        LogTrack.i(NAME) { "getMemosListBySection -> { sectionType: $sectionType, currentSortType: $memoSortType } " }
        val memos = when (sectionType) {

            // 비밀메모
            FixedSectionType.SECRET -> when (memoSortType) {
                MemoSortType.LATEST_CREATE -> memoDao.getLockMemosByCreatedDesc()
                MemoSortType.OLDEST_CREATE -> memoDao.getLockMemosByCreatedAsc()
                MemoSortType.LATEST_UPDATE -> memoDao.getLockMemosByModifiedDesc()
                MemoSortType.OLDEST_UPDATE -> memoDao.getLockMemosByModifiedAsc()
                MemoSortType.CUSTOM -> memoDao.getLockMemosByCustomDesc()
                else -> memoDao.getLockMemosByCreatedDesc()
            }

            // 즐겨찾기
            FixedSectionType.IMPORTANT -> when (memoSortType) {
                MemoSortType.LATEST_CREATE -> memoDao.getImportantMemosByCreatedDesc()
                MemoSortType.OLDEST_CREATE -> memoDao.getImportantMemosByCreatedAsc()
                MemoSortType.LATEST_UPDATE -> memoDao.getImportantMemosByModifiedDesc()
                MemoSortType.OLDEST_UPDATE -> memoDao.getImportantMemosByModifiedAsc()
                MemoSortType.CUSTOM -> memoDao.getImportantMemosByCustomDesc()
                else -> memoDao.getImportantMemosByCreatedDesc()
            }

            // 전체
            else -> when (memoSortType) {
                MemoSortType.LATEST_CREATE -> memoDao.getAllMemos()
                MemoSortType.OLDEST_CREATE -> memoDao.getAllMemosCreatedAsc()
                MemoSortType.LATEST_UPDATE -> memoDao.getAllMemosModifiedDesc()
                MemoSortType.OLDEST_UPDATE -> memoDao.getAllMemosModifiedAsc()
                MemoSortType.CUSTOM -> memoDao.getAllMemosByCustomDesc()
                else -> memoDao.getAllMemos()
            }

        }
        return memos
    }


    private fun setBackKeyListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val savedLayoutMode = getSavedLayoutMode()
                val savedGroupMode = getSavedGroupMode()
                when (savedLayoutMode) {
                    MainLayoutMode.GRID -> gridAdapter.exitSelectMode()
                    MainLayoutMode.LIST -> listAdapter.exitSelectMode()
                    MainLayoutMode.EDIT -> {
                        // nothing
                    }
                }
                LogTrack.i(NAME) { "setBackKeyListener::handleOnBackPressed -> savedLayoutMode: $savedLayoutMode" }
                setHeaderModeView(layoutMode = savedLayoutMode, groupMode = savedGroupMode)
            }
        })
    }


    private fun setDragItemListener() {
        val listItemTouchHelper = ItemTouchHelper(
            MemoListAdapter.MemoItemTouchHelperCallback(
                adapter = listAdapter,
                otherAdapter = gridAdapter
            )
        )
        listItemTouchHelper.attachToRecyclerView(binding.rvMemoList)
        listAdapter.setItemTouchHelper(
            helper = listItemTouchHelper
        )

        val griditemTouchHelper = ItemTouchHelper(
            MemoGridAdapter.MemoItemTouchHelperCallback(
                adapter = gridAdapter,
                otherAdapter = listAdapter
            )
        )
        griditemTouchHelper.attachToRecyclerView(binding.rvMemoGrid)
        gridAdapter.setItemTouchHelper(
            helper = griditemTouchHelper
        )
    }


    override fun onSelectedItem(selectCount: Int, itemCount: Int) {
        LogTrack.i(NAME) { "setEditModeView<${getSavedLayoutMode()}> -> setItemCheckBoxSelectedListener::onSelected { selectCount: $selectCount, itemCount: $itemCount }" }
        setHeaderModeView(
            layoutMode = MainLayoutMode.EDIT,
            value = mapOf("selectCount" to selectCount, "itemCount" to itemCount)
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // 새 메모하기
            binding.newMemoContainer.id -> {
                if (!PreferenceUtil.get(KEY_SHOW_TUTORIAL_MEMO_MAIN, false)) {
                    return
                }

                MemoDetailActivity.start(
                    activity = this@MemoListActivity,
                    mode = MemoMode.CREATE_MEMO
                )
            }
        }
    }

    private fun maybeStartLockScreenService() {
        val isEnabled = PreferenceUtil.get(KEY_USE_LOCKSCREEN_MEMO, false)

        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isEnabled && hasNotificationPermission && hasOverlayPermission && hasLocationPermission) {
            val intent = Intent(this, LockScreenService::class.java)
            ContextCompat.startForegroundService(this, intent)
        } else {
            if (isEnabled) {
                PreferenceUtil.set(KEY_USE_LOCKSCREEN_MEMO, false)

                // 서비스가 실행 중이면 중지
                val stopIntent = Intent(this, LockScreenService::class.java)
                stopService(stopIntent)
            }
        }
    }

    private fun showPasswordGuideDialog() {
        if (!isAlive) return

        passwordGuideDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_dialog_title_set_password))
            setMessage(getString(R.string.haru_dialog_message_need_password_for_trash))
            setSubMessage(getString(R.string.haru_dialog_submessage_set_password_now))
            setBottomSubMessage(getString(R.string.haru_dialog_bottom_message_common_password_info))

            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_dialog_set_password),
                onCancel = {
                    onDestroy()
                },
                onConfirm = {
                    onDestroy()
                    PasswordActivity.start(
                        this@MemoListActivity,
                        SettingHeaderView.SetHeaderMode.PASSWORD_REGI
                    )
                }
            )

            show()
        }
    }


    private fun showSortingDialog() {
        if (!isAlive) return

        memoSortingCustomDialog = MemoSortingCustomDialog(this@MemoListActivity).apply {
            setButtonListener(
                onLatestCreate = {
                    lifecycleScope.launch {
                        memoSortType = MemoSortType.LATEST_CREATE
                        listAdapter.setSortType(sortType = MemoSortType.LATEST_CREATE)
                        gridAdapter.setSortType(sortType = MemoSortType.LATEST_CREATE)
                        EventUtil.sendEvent(
                            this@MemoListActivity,
                            EventUtil.CATEGORY_MAIN,
                            EventUtil.ACTION_SORT_CREATE_LATEST
                        )
                        refreshMemoList()
                        dismiss()
                    }
                },

                onOldestCreate = {
                    lifecycleScope.launch {
                        memoSortType = MemoSortType.OLDEST_CREATE
                        listAdapter.setSortType(sortType = MemoSortType.OLDEST_CREATE)
                        gridAdapter.setSortType(sortType = MemoSortType.OLDEST_CREATE)
                        EventUtil.sendEvent(
                            this@MemoListActivity,
                            EventUtil.CATEGORY_MAIN,
                            EventUtil.ACTION_SORT_CREATE_OLDEST
                        )
                        refreshMemoList()
                        dismiss()
                    }
                },

                onLatestUpdate = {
                    lifecycleScope.launch {
                        memoSortType = MemoSortType.LATEST_UPDATE
                        listAdapter.setSortType(sortType = MemoSortType.LATEST_UPDATE)
                        gridAdapter.setSortType(sortType = MemoSortType.LATEST_UPDATE)
                        EventUtil.sendEvent(
                            this@MemoListActivity,
                            EventUtil.CATEGORY_MAIN,
                            EventUtil.ACTION_SORT_MODIFY_LATEST
                        )
                        refreshMemoList()
                        dismiss()
                    }
                },

                onOldestUpdate = {
                    lifecycleScope.launch {
                        memoSortType = MemoSortType.OLDEST_UPDATE
                        listAdapter.setSortType(sortType = MemoSortType.OLDEST_UPDATE)
                        gridAdapter.setSortType(sortType = MemoSortType.OLDEST_UPDATE)
                        EventUtil.sendEvent(
                            this@MemoListActivity,
                            EventUtil.CATEGORY_MAIN,
                            EventUtil.ACTION_SORT_MODIFY_OLDEST
                        )
                        refreshMemoList()
                        dismiss()
                    }
                },

                onCustom = {
                    lifecycleScope.launch {
                        memoSortType = MemoSortType.CUSTOM
                        listAdapter.setSortType(sortType = MemoSortType.CUSTOM)
                        gridAdapter.setSortType(sortType = MemoSortType.CUSTOM)
                        EventUtil.sendEvent(
                            this@MemoListActivity,
                            EventUtil.CATEGORY_MAIN,
                            EventUtil.ACTION_SORT_CUSTOM
                        )
                        refreshMemoList()
                        dismiss()
                    }
                }
            )
        }

        memoSortingCustomDialog?.show()
    }


    private fun showDeleteDialog() {

        fun deleteUnlockedMemo(unlockedItems: List<MemoEntity>) {
            if (unlockedItems.isEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                unlockedItems.forEach { item ->
                    val deleted = item.copy(
                        isDeleted = true,
                        deleted = System.currentTimeMillis()
                    )
                    memoDao.updateMemo(deleted)
                }

                // widget update
                MemoWidgetUpdater().observeMemoChanges(this@MemoListActivity)
                withContext(Dispatchers.Main) {
                    // refresh
                    refreshMemoList()
                    // event
                    toastShort(
                        this@MemoListActivity,
                        getString(R.string.haru_memo_delete_move_to_trash)
                    )
                }
            }
        }

        if (!this.isAlive) return

        val selectedItems = when (getSavedLayoutMode()) {
            MainLayoutMode.GRID -> gridAdapter.getSelectedItems()
            MainLayoutMode.LIST -> listAdapter.getSelectedItems()
            else -> emptyList()
        }

        if (selectedItems.isEmpty()) return

        val dialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_dialog_delete_title))
            setMessage(getString(R.string.haru_dialog_delete_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_dialog_common_delete),
                onCancel = {
                    dismiss()
                },
                onConfirm = {
                    val lockedItems = selectedItems.filter { it.isLocked }
                    val unlockedItems = selectedItems.filter { !it.isLocked }

                    if (selectedItems.any { it.isLocked }) {
                        // 잠긴 메모가 있다면 startDelete() 메서드 사용
                        PasswordActivity.startDelete(
                            activity = this@MemoListActivity,
                            memoItems = lockedItems
                        )

                        if (selectedItems.any { !it.isLocked }) {
                            deleteUnlockedMemo(unlockedItems = unlockedItems)
                        }
                    } else {
                        // 잠금 안 된 메모는 즉시 삭제
                        deleteUnlockedMemo(unlockedItems = unlockedItems)
                    }
                    EventUtil.sendEvent(
                        this@MemoListActivity,
                        EventUtil.CATEGORY_MAIN,
                        EventUtil.ACTION_DELETE_MEMO
                    )
                    dismiss()
                }
            )
        }
        dialog.show()
    }


    private fun onMemoAllSelected(isChecked: Boolean) {
        val saveMode = getSavedLayoutMode()
        LogTrack.i(NAME) { "setOnAction1ClickListener -> onMemoAllSelected -> saveMode: $saveMode" }

        when (saveMode) {
            MainLayoutMode.GRID -> {
                if (isChecked) {
                    gridAdapter.selectAll()
                } else {
                    gridAdapter.unSelectAll()
                }
            }

            MainLayoutMode.LIST -> {
                if (isChecked) {
                    listAdapter.selectAll()
                } else {
                    listAdapter.unSelectAll()
                }
            }

            else -> {

            }
        }
    }

    private fun shareMemos() {
        LogTrack.d { "shareMemos() 시작" }

        val saveMode = getSavedLayoutMode()
        val selectedItems = when (saveMode) {
            MainLayoutMode.GRID -> gridAdapter.getSelectedItems()
            MainLayoutMode.LIST -> listAdapter.getSelectedItems()
            else -> emptyList()
        }
        LogTrack.d { "선택된 메모 개수: ${selectedItems.size}" }

        if (selectedItems.isEmpty()) {
            toastShort(this, getString(R.string.haru_share_text))
            return
        }

        lifecycleScope.launch {
            try {
                val pdfUris = arrayListOf<Uri>()
                val pdfDir = File(cacheDir, "pdf").apply { if (!exists()) mkdirs() }

                val displayWidth = resources.displayMetrics.widthPixels
                val paddingPx = (16 * resources.displayMetrics.density).toInt()

                // 오프스크린 RecyclerView
                val rv = RecyclerView(this@MemoListActivity).apply {
                    layoutManager = LinearLayoutManager(this@MemoListActivity)
                    overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                    itemAnimator = null
                    clipToPadding = false
                    setPadding(
                        paddingPx,
                        paddingPx,
                        paddingPx,
                        (32 * resources.displayMetrics.density).toInt()
                    )
                    addItemDecoration(HorizontalSpaceItemDecoration(space = 10))
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                val adapter = MemoBlockAdapter(this@MemoListActivity).apply {
                    setMemoMode(MemoMode.READ_MEMO)
                    setAttachedRecyclerView(rv)
                    setExporting(true) //  Export 모드 켜기: 내부 이미지 RV 우회, 동기 GridLayout 렌더
                }
                rv.adapter = adapter

                // 임시 호스트(투명) — 실제 윈도우에 붙여 한 프레임 돌림
                val host = FrameLayout(this@MemoListActivity).apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                val decorView = window?.decorView as ViewGroup
                withContext(Dispatchers.Main) { decorView.addView(host) }

                try {
                    for (memo in selectedItems) {
                        LogTrack.d { "메모 처리 시작: uuid=${memo.uuid}, title=${memo.title}" }

                        val blocks = buildBlocksFromMemoForPdf(memo)
                        val bgColorInt =
                            runCatching { memo.bgColor.toColorInt() }.getOrDefault(Color.WHITE)

                        val bitmap = withContext(Dispatchers.Main) {
                            // 리스트 제출
                            adapter.submitAndAwait(blocks)
                            // RV를 호스트에 부착하고 1프레임 보장 (내부 export Grid도 즉시 만들어짐)
                            if (rv.parent == null) host.addView(rv)
                            awaitNextFrame(host)

                            // 힌트 제거(빈 EditText 만)
                            for (i in 0 until adapter.itemCount) {
                                rv.findViewHolderForAdapterPosition(i)?.itemView?.let { itemView ->
                                    itemView.findViewsByType(EditText::class.java).forEach { et ->
                                        if (et.text.isNullOrEmpty() && et.hint?.isNotEmpty() == true) et.hint =
                                            ""
                                    }
                                }
                            }

                            // 최종 측정/레이아웃
                            rv.measure(
                                View.MeasureSpec.makeMeasureSpec(
                                    displayWidth,
                                    View.MeasureSpec.EXACTLY
                                ),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                            )
                            rv.layout(0, 0, displayWidth, rv.measuredHeight)

                            // 캡처
                            captureRecyclerViewWithBackground(rv, bgColorInt, 0)
                        }

                        // PDF 저장 (IO)
                        val uri = withContext(Dispatchers.IO) {
                            val pdfFile = File(pdfDir, "haru_memo_${memo.uuid}.pdf")
                            bitmapToPdf(bitmap, pdfFile, bgColorInt)
                            FileProvider.getUriForFile(
                                this@MemoListActivity,
                                "$packageName.fileprovider",
                                pdfFile
                            )
                        }
                        bitmap.recycle()
                        pdfUris.add(uri)

                        // 다음 메모 전 정리(중요)
                        withContext(Dispatchers.Main) {
                            adapter.submitBlockList(emptyList())
                            rv.recycledViewPool.clear()
                            awaitNextFrame(rv)
                        }
                    }
                } finally {
                    // 호스트 제거
                    withContext(Dispatchers.Main) {
                        try {
                            host.removeAllViews()
                            decorView.removeView(host)
                        } catch (_: Throwable) {
                        }
                    }
                }

                // Export 모드 OFF & 어댑터 리셋
                withContext(Dispatchers.Main) {
                    adapter.setExporting(false)
                    adapter.submitBlockList(emptyList())
                    rv.recycledViewPool.clear()
                }

                if (pdfUris.isEmpty()) {
                    toastShort(this@MemoListActivity, getString(R.string.haru_share_text))
                    return@launch
                }

                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "application/pdf"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, pdfUris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        getString(R.string.haru_menu_share)
                    )
                )

                LogTrack.d { "총 PDF 생성 개수: ${pdfUris.size}" }

            } catch (t: Throwable) {
                LogTrack.d { "공유 중 예외: ${t.message}" }
                toastShort(this@MemoListActivity, "공유 실패: ${t.localizedMessage}")
            }
        }
    }

    private suspend fun awaitNextFrame(view: View) =
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            view.post {
                android.view.Choreographer.getInstance().postFrameCallback {
                    if (cont.isActive) cont.resume(Unit) { cause, _, _ ->
                        null?.let { it1 ->
                            it1(
                                cause
                            )
                        }
                    }
                }
            }
        }


    // submitList 커밋 완료까지 suspend
    private suspend fun <T> ListAdapter<T, *>.submitAndAwait(items: List<T>) =
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            submitList(items) {
                if (cont.isActive) cont.resume(Unit) { cause, _, _ ->
                    null?.let {
                        it(
                            cause
                        )
                    }
                }
            }
        }

    /**
     * PDF 전용 블록 구성 - 동기 이미지 로드
     */
    private fun buildBlocksFromMemoForPdf(memo: MemoEntity): List<MemoBlock> {
        val blocks = mutableListOf<MemoBlock>()

        // 날짜
        blocks += MemoBlock.DateBlock(date = Util.formatDate(memo.created))

        // 내용 (행 기반 구성, 체크박스 복원)
        val rows = mutableListOf<BodyRow>()
        memo.title.takeIf { !it.isNullOrBlank() }?.let { title ->
            rows += RowCodecUtil.parse(title)
        }
        memo.body.forEach { body ->
            rows += RowCodecUtil.parse(body.text)
        }
        if (rows.isNotEmpty()) blocks += MemoBlock.BodyBlock(bodyRows = rows)

        // 이미지 (기존과 동일: 블록 인덱스 기준으로 삽입)
        val imageIndexList = memo.imagePath.toSortedMap()
        imageIndexList.forEach { (index, pathList) ->
            val uriList = pathList.mapNotNull { path ->
                try {
                    val uri = path.toUri()
                    LogTrack.d { "이미지 Uri 로드 시도: $path" }
                    uri.toString()
                } catch (e: Exception) {
                    LogTrack.d { "이미지 Uri 변환 실패: $path (${e.message})" }
                    null
                }
            }

            if (uriList.isNotEmpty()) {
                val insertPos = (index + MemoDetailActivity.IMAGE_END_POS).coerceIn(0, blocks.size)
                blocks.add(insertPos, MemoBlock.ImageUriBlock(uris = uriList.toMutableList()))
                LogTrack.d { "이미지 블록 추가 at pos=$insertPos (${uriList.size}개 이미지)" }
            }
        }

        return blocks
    }

    private fun getRotatedBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val exif = context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
                ?: return null
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val raw =
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    ?: return null

            val matrix = Matrix().apply {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                }
            }
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        } catch (_: Exception) {
            null
        }
    }

    private fun bitmapToPdf(bitmap: Bitmap, pdfFile: File, bgColor: Int) {
        val minPageHeight = 1123 // 예시: A4 72dpi 기준 세로 최소
        val pageHeight = maxOf(bitmap.height, minPageHeight)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, pageHeight, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawColor(bgColor)
        // 중앙 정렬 or 상단 정렬 선택 가능
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

    private fun copyMemo() {
        if (!this@MemoListActivity.isAlive) return

        val selectedItems = when (getSavedLayoutMode()) {
            MainLayoutMode.GRID -> gridAdapter.getSelectedItems()
            MainLayoutMode.LIST -> listAdapter.getSelectedItems()
            else -> emptyList()
        }
        if (selectedItems.isEmpty()) {
            toastShort(
                this@MemoListActivity,
                getString(R.string.haru_header_selected_mode_copy_memo_failed)
            )
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            selectedItems.forEach { original ->
                val newMemo = original.copy(
                    _id = 0,
                    uuid = BaseUtil.getUUID(),
                    title = original.title + " " + getString(R.string.haru_header_selected_mode_copy_memo),
                    body = original.body.toList(),
                    imagePath = original.imagePath.mapValues { it.value.toList() },
                    created = now,
                    modified = now,
                    read = now,
                    custom = now
                )
                memoDao.insertMemo(newMemo)
            }

            withContext(Dispatchers.Main) {
                toastShort(
                    this@MemoListActivity,
                    getString(R.string.haru_header_selected_mode_copy_memo_success)
                )
                refreshMemoList()
            }
        }
    }


    // 동적 섹션(= 날짜/회고/태그 등)만 모두 '펼침=true'로 1회 초기화
    private fun setExpandAllDynamicSection(
        sections: List<MemoSectionListItem.SectionHeader>
    ) {
        LogTrack.i { "$NAME -> setExpandAllDynamicSection -> { isDynamicSectionInit: $isDynamicSectionInit }" }
        if (isDynamicSectionInit) return

        sections.forEach { header ->
            val isFixedSection = header.sectionType is FixedSectionType
            if (!isFixedSection && header.needExpandable) {
                header.isExpanded = true
                PreferenceUtil.set(getDynamicSectionKey(header.sectionType.key), true)
            }
        }
        isDynamicSectionInit = true
    }

    private fun refreshMemoList(isBeginning: Boolean = false) {
        lifecycleScope.launch {
            /** Exit SelectMode */
            gridAdapter.exitSelectMode()
            listAdapter.exitSelectMode()

            /** Update List */
            val secretList = getMemosListBySection(sectionType = FixedSectionType.SECRET)
            val importantList = getMemosListBySection(sectionType = FixedSectionType.IMPORTANT)
            val memoList = getMemosListBySection(sectionType = DynamicSectionType.ALL)
            val fixedSections = mutableListOf<MemoSectionListItem.SectionHeader>()
            val dynamicSections = mutableListOf<MemoSectionListItem.SectionHeader>()

            if (secretList.isNotEmpty()) {
                fixedSections += MemoSectionListItem.SectionHeader(
                    sectionType = FixedSectionType.SECRET,
                    title = FixedSectionType.SECRET.sectionName,
                    isExpanded = if (isBeginning) false else true,
                    needExpandable = true,
                    memos = secretList
                )
            }

            if (importantList.isNotEmpty()) {
                fixedSections += MemoSectionListItem.SectionHeader(
                    sectionType = FixedSectionType.IMPORTANT,
                    title = FixedSectionType.IMPORTANT.sectionName,
                    isExpanded = if (isBeginning) false else true,
                    needExpandable = true,
                    memos = importantList
                )
            }


            val savedLayoutMode = getSavedLayoutMode()
            val savedGroupMode = getSavedGroupMode()

            if (memoSortType == MemoSortType.CUSTOM && memoList.isNotEmpty()) {
                dynamicSections += MemoSectionListItem.SectionHeader(
                    sectionType = DynamicSectionType.NONE,
                    title = "",
                    isExpanded = false,
                    needExpandable = false,
                    memos = memoList
                )
            } else {
                dynamicSections += when (savedGroupMode) {
                    DATE -> groupMemosByDateSection(memoList, memoSortType)
                    TAG -> groupMemosByTagSection(memoList, memoSortType)
                }
            }

            var sectionList = fixedSections + dynamicSections
            // 동적 섹션 전체 펼침을 저장
            setExpandAllDynamicSection(sectionList)


            /** Update Header */
            LogTrack.i(NAME) { "refreshMemoList -> { savedLayoutMode $savedLayoutMode, savedGroupMode: $savedGroupMode }" }
            when (savedLayoutMode) {
                MainLayoutMode.GRID -> {
                    sectionList = gridAdapter.getExpandStateSection(sectionList)
                    gridAdapter.exitSelectMode()
                    gridAdapter.updateItems(sectionList)

                }

                MainLayoutMode.LIST -> {
                    sectionList = listAdapter.getExpandStateSection(sectionList)
                    listAdapter.exitSelectMode()
                    listAdapter.updateItems(sectionList)
                }

                MainLayoutMode.EDIT -> {
                    // nothing
                }
            }
            setHeaderModeView(layoutMode = savedLayoutMode, groupMode = savedGroupMode)
        }
    }

    private fun maybeShowBackupGuideDialog() {
        if (!PreferenceUtil.get(PreferenceUtil.KEY_SHOW_BACKUP_GUIDE, false)) return

        val lastBackupTime = PreferenceUtil.get(PreferenceUtil.KEY_LAST_LOCAL_BACKUP_TIME, "없음")
        if (lastBackupTime.isBlank() || lastBackupTime == "없음") return

        val backupInstant = Util.parseToInstant(lastBackupTime) ?: return
        val now = Instant.now()
        val daysSinceBackup = ChronoUnit.DAYS.between(backupInstant, now)
        if (daysSinceBackup < 3) return

        val lastDismissTime = PreferenceUtil.get(PreferenceUtil.KEY_BACKUP_GUIDE_DISMISS_TIME, "")
        val dismissInstant = Util.parseToInstant(lastDismissTime)
        if (dismissInstant != null) {
            val daysSinceDismiss = ChronoUnit.DAYS.between(dismissInstant, now)
            if (daysSinceDismiss < 3) return
        }

        if (isFinishing) return

        // 다이얼로그 띄우기
        MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_show_backup_guide_title))
            setMessage(getString(R.string.haru_show_backup_guide_message))
            setSubMessage(getString(R.string.haru_show_backup_guide_sub_message))
            setButton(
                cancelText = getString(R.string.haru_show_backup_guide_cancel),
                confirmText = getString(R.string.haru_show_backup_guide_confirm),
                onCancel = {
                    saveBackupGuideDismissTime()
                    dismiss()
                },
                onConfirm = {
                    saveBackupGuideDismissTime()
                    SettingBackupActivity.start(this@MemoListActivity)
                    dismiss()
                }
            )
            show()
        }
    }

    private fun saveBackupGuideDismissTime() {
        val now = SimpleDateFormat("yyyy년 M월 d일 (E) h:mm a", Locale.KOREA).format(Date())
        PreferenceUtil.set(PreferenceUtil.KEY_BACKUP_GUIDE_DISMISS_TIME, now)
    }

    // 날짜 파싱 함수
    private fun parseDateString(dateStr: String): Date? {
        return try {
            SimpleDateFormat("yyyy년 M월 d일 (E) h:mm a", Locale.KOREA).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    // API 26 미만용 날짜 차이 계산
    private fun calculateDaysSince(date: Date): Long {
        val nowCalendar = Calendar.getInstance().apply { setToStartOfDay() }
        val pastCalendar = Calendar.getInstance().apply {
            time = date
            setToStartOfDay()
        }

        val diffMillis = nowCalendar.timeInMillis - pastCalendar.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMillis)
    }

    // Calendar 확장 함수: 시분초 제거
    private fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }


    private fun List<MemoEntity>.sortByMemoSortType(sortType: MemoSortType): List<MemoEntity> {
        return when (sortType) {
            MemoSortType.LATEST_CREATE -> this.sortedByDescending { it.created }
            MemoSortType.OLDEST_CREATE -> this.sortedBy { it.created }
            MemoSortType.LATEST_UPDATE -> this.sortedByDescending { it.modified }
            MemoSortType.OLDEST_UPDATE -> this.sortedBy { it.modified }
            else -> this
        }
    }


    // 날짜별
    private fun groupMemosByDateSection(
        memos: List<MemoEntity>,
        sortType: MemoSortType
    ): List<MemoSectionListItem.SectionHeader> {
        fun isExpandedFor(sectionKey: String, defaultExpanded: Boolean): Boolean {
            return PreferenceUtil.get(getDynamicSectionKey(sectionKey), defaultExpanded)
        }

        val today = LocalDate.now()
        val currentYear = today.year

        val getDate: (MemoEntity) -> LocalDate = when (sortType) {
            MemoSortType.LATEST_CREATE, MemoSortType.OLDEST_CREATE -> {
                { memo ->
                    Instant.ofEpochMilli(memo.created).atZone(ZoneId.systemDefault()).toLocalDate()
                }
            }

            MemoSortType.LATEST_UPDATE, MemoSortType.OLDEST_UPDATE -> {
                { memo ->
                    Instant.ofEpochMilli(memo.modified).atZone(ZoneId.systemDefault()).toLocalDate()
                }
            }

            else -> {
                { memo ->
                    Instant.ofEpochMilli(memo.created).atZone(ZoneId.systemDefault()).toLocalDate()
                }
            }
        }

        val result = mutableListOf<MemoSectionListItem.SectionHeader>()

        val todayList = mutableListOf<MemoEntity>()
        val yesterdayList = mutableListOf<MemoEntity>()
        val weekList = mutableListOf<MemoEntity>()
        val monthList = mutableListOf<MemoEntity>()
        val monthMap = mutableMapOf<String, MutableList<MemoEntity>>() // 올해 월별
        val yearMap = mutableMapOf<String, MutableList<MemoEntity>>()   // 과거 연도별

        for (memo in memos) {
            val date = getDate(memo)
            val daysAgo = ChronoUnit.DAYS.between(date, today)

            when (daysAgo) {
                0L -> todayList += memo
                1L -> yesterdayList += memo
                in 2..6 -> weekList += memo
                in 7..29 -> monthList += memo
                else -> {
                    if (date.year == currentYear) {
                        val monthKey = "${date.monthValue}월"
                        monthMap.getOrPut(monthKey) { mutableListOf() } += memo
                    } else {
                        val yearKey = "${date.year}년"
                        yearMap.getOrPut(yearKey) { mutableListOf() } += memo
                    }
                }
            }
        }

        if (todayList.isNotEmpty()) {
            result += MemoSectionListItem.SectionHeader(
                sectionType = DynamicSectionType.TODAY,
                title = DynamicSectionType.TODAY.sectionName,
                isExpanded = isExpandedFor(DynamicSectionType.TODAY.key, true),
                needExpandable = true,
                memos = todayList.sortByMemoSortType(sortType)
            )
        }
        if (yesterdayList.isNotEmpty()) {
            result += MemoSectionListItem.SectionHeader(
                sectionType = DynamicSectionType.YESTERDAY,
                title = DynamicSectionType.YESTERDAY.sectionName,
                isExpanded = isExpandedFor(DynamicSectionType.YESTERDAY.key, true),
                needExpandable = true,
                memos = yesterdayList.sortByMemoSortType(sortType)
            )
        }
        if (weekList.isNotEmpty()) {
            result += MemoSectionListItem.SectionHeader(
                sectionType = DynamicSectionType.LAST_7_DAYS,
                title = DynamicSectionType.LAST_7_DAYS.sectionName,
                isExpanded = isExpandedFor(DynamicSectionType.LAST_7_DAYS.key, true),
                needExpandable = true,
                memos = weekList.sortByMemoSortType(sortType)
            )
        }
        if (monthList.isNotEmpty()) {
            result += MemoSectionListItem.SectionHeader(
                sectionType = DynamicSectionType.LAST_30_DAYS,
                title = DynamicSectionType.LAST_30_DAYS.sectionName,
                isExpanded = isExpandedFor(DynamicSectionType.LAST_30_DAYS.key, true),
                needExpandable = true,
                memos = monthList.sortByMemoSortType(sortType)
            )
        }

        // 올해 월별 정렬 (12월 → 1월)
        val monthSections = monthMap.entries
            .sortedByDescending { it.key.replace("월", "").toIntOrNull() ?: 0 }
            .map { (key, memos) ->
                MemoSectionListItem.SectionHeader(
                    sectionType = DynamicRetroSectionType(key, key),
                    title = key,
                    isExpanded = isExpandedFor(DynamicRetroSectionType(key, key).key, true),
                    needExpandable = true,
                    memos = memos.sortByMemoSortType(sortType)
                )
            }


        // 연도별 정렬 (2024년 → 2023년)
        val yearSections = yearMap.entries
            .sortedByDescending { it.key.replace("년", "").toIntOrNull() ?: 0 }
            .map { (key, memos) ->
                MemoSectionListItem.SectionHeader(
                    sectionType = DynamicRetroSectionType(key, key),
                    title = key,
                    isExpanded = isExpandedFor(DynamicRetroSectionType(key, key).key, true),
                    needExpandable = true,
                    memos = memos.sortByMemoSortType(sortType)
                )
            }

        return when (sortType) {
            MemoSortType.OLDEST_CREATE, MemoSortType.OLDEST_UPDATE -> {
                // 오래된 순 → 섹션 역순
                yearSections + monthSections + result.reversed()
            }

            else -> {
                // 최신순 → 기본 섹션 순서 유지
                result + monthSections + yearSections
            }
        }
    }


    // 태그별
    // MemoListActivity 안에 추가 (suspend)
    private suspend fun groupMemosByTagSection(
        memos: List<MemoEntity>,
        sortType: MemoSortType
    ): List<MemoSectionListItem.SectionHeader> {
        fun isExpandedFor(sectionKey: String, defaultExpanded: Boolean): Boolean {
            return PreferenceUtil.get(getDynamicSectionKey(sectionKey), defaultExpanded)
        }

        // 1) 태깅/태그 테이블 읽어서 memoId -> [tagName] 맵 구성
        val taggingList = taggingDao.getAllTaggings()
        val tagList = tagDao.getAllTags()
        val tagIdToName = tagList.associate { it._id to it.tagName }
        val memoIdToTagNames: Map<Int, List<String>> = taggingList
            .groupBy { it.memoid }
            .mapValues { (_, taggings) -> taggings.mapNotNull { tagIdToName[it.tagid] } }

        // 2) 태그명 -> 메모 리스트로 그룹핑 (태그 없으면 "태그 없음")
        val grouped: MutableMap<String, MutableList<MemoEntity>> = linkedMapOf()
        val noneTagLabel = getString(R.string.haru_section_section_untagged)

        for (memo in memos) {
            val tags = memoIdToTagNames[memo._id].orEmpty()
            if (tags.isEmpty()) {
                grouped.getOrPut(noneTagLabel) { mutableListOf() }.add(memo)
            } else {
                for (tag in tags) {
                    grouped.getOrPut(tag) { mutableListOf() }.add(memo)
                }
            }
        }

        if (grouped.isEmpty()) return emptyList()

        // 3) 섹션 정렬: 태그명 오름차순(“태그 없음”은 맨 뒤로)
        val sortedKeys = grouped.keys.sortedWith(
            compareBy<String> { it == noneTagLabel }.thenBy { it }
        )

        // 4) 섹션 생성 (키는 고유해야 하므로 "tag:<태그명>" 형태 사용)
        return sortedKeys.map { tagName ->
            val list = grouped[tagName].orEmpty().sortByMemoSortType(sortType)
            val sectionKey = "tag:$tagName"
            val displayTitle = if (tagName == noneTagLabel) tagName else "# $tagName"
            MemoSectionListItem.SectionHeader(
                sectionType = DynamicRetroSectionType(sectionKey, displayTitle),
                title = displayTitle,
                isExpanded = isExpandedFor(sectionKey, true),
                needExpandable = true,
                memos = list
            )
        }
    }


}
