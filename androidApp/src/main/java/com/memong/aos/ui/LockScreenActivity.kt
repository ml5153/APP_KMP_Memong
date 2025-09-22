package com.memong.aos.ui

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.avatye.haru.network.api.APIWeather
import com.avatye.haru.network.res.WeatherNow
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.memong.aos.BuildConfig
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.entity.BodyItem
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.BaseUtil.Companion.getAndroidId
import com.memong.aos.data.utils.BaseUtil.Companion.getHexColorFromRes
import com.memong.aos.data.utils.BaseUtil.Companion.getUUID
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_PM_VISIBLE
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.databinding.ActivityLockScreenBinding
import com.memong.aos.databinding.ActivityPressHomeKeyBinding
import com.memong.aos.databinding.DialogBottomSheetLockScreenMemoListBinding
import com.memong.aos.databinding.DialogBottomSheetLockScreenSetBinding
import com.memong.aos.databinding.DialogBottomSheetLockScreenWriteMemoBinding
import com.memong.aos.helper.LockScreenHelper
import com.memong.aos.helper.MemoWidgetUpdater
import com.memong.aos.receiver.HomeRecentButtonReceiver
import com.memong.aos.receiver.UnlockReceiver
import com.memong.aos.ui.adapter.LockScreenMemoAdapter
import com.memong.aos.ui.adapter.LockScreenMemoListAdapter
import com.memong.aos.ui.custom.item.HorizontalSpacingItemDecoration
import com.memong.aos.ui.custom.item.LockScreenListSpacingItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal class LockScreenActivity : BaseActivity() {

    override val NAME: String
        get() = LockScreenActivity::class.java.simpleName

    private var binding: ActivityLockScreenBinding? = null
    private var favoriteAdapter: LockScreenMemoAdapter? = null
    private var listAdapter: LockScreenMemoListAdapter? = null
    private var isUnlocking = false
    private var unLockPhoneReceiver: UnlockReceiver? = null
    private var unLockCameraReceiver: UnlockReceiver? = null
    private var unLockScreenReceiver: UnlockReceiver? = null
    private var homeRecentReceiver: HomeRecentButtonReceiver? = null
    private var unlockHintAnimator: AnimatorSet? = null

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Intent.ACTION_TIME_TICK == intent?.action) {
                updateDateTimeUI()
            }
        }
    }

    private fun updateDateTimeUI() {
        val now = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
        binding?.textDate?.text = dateFormat.format(now.time)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
        val timeString = timeFormat.format(now.time) // 예: "8:45 AM"

        val parts = timeString.split(" ") // ["8:45", "AM"]

        binding?.textTime?.text = parts.getOrNull(0) ?: ""
        binding?.textAmPm?.text = parts.getOrNull(1) ?: ""
    }

    companion object {

        fun start(
            activity: Activity,
            close: Boolean = false
        ) {
            val intent = Intent(activity, PasswordActivity::class.java)
            activity.start(intent = intent, flags = Intent.FLAG_ACTIVITY_CLEAR_TOP, close = close)
        }

        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityLockScreenBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.lockScreenRootView, paddingDp = 0)

        setupFavoriteMemoList()
        setupSwipeUnlock()

        // 초기 미세먼지 표시 상태 반영
        updateAirQualityVisibility()
        // 날씨
        loadLockScreenWeather()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = b.bottomBannerView
        )


        binding?.iconPhone?.setOnClickListener {
            handlePhoneClick()
        }

        binding?.iconCamera?.setOnClickListener {
            handleCameraClick()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // do nothing
            }
        })

        // 하단 FAB 클릭 → 바텀시트 띄우기
        binding?.iconMemoList?.setOnClickListener {
            if (!isFinishing) {
                showLockScreenMemoListDialog(this@LockScreenActivity)
            }
        }

        binding?.iconMemoWrite?.setOnClickListener {
            if (!isFinishing) {
                showLockScreenMemoWriteDialog(this@LockScreenActivity) { title, content ->

                    val context = this@LockScreenActivity
                    val now = System.currentTimeMillis()

                    val memo = MemoEntity(
                        _id = 0,
                        userid = getAndroidId(context),
                        uuid = getUUID(),
                        title = title,
                        body = listOf(BodyItem(index = 3, text = content)),
                        created = now,
                        modified = now,
                        read = now,
                        custom = now,
                        imagePath = emptyMap(),
                        isLocked = false,
                        isImportant = false,
                        bgColor = getHexColorFromRes(context, R.color.bg_white),
                        category = 1,
                        isDeleted = false,
                        deleted = 0L
                    )

                    lifecycleScope.launch {
                        MemoDatabase.getInstance(context).memoDao().insertMemo(memo)
                        MemoWidgetUpdater().observeMemoChanges(this@LockScreenActivity)
                        EventUtil.sendEvent(this@LockScreenActivity, EventUtil.CATEGORY_LOCKSCREEN, EventUtil.ACTION_LOCKSCREEN_MEMO_CREATE)
                        toastShort(context, "메모가 저장되었습니다.")
                    }
                }
            }
        }

        // 설정 아이콘 클릭 → 바텀시트 띄우기
        binding?.lySettings?.setOnClickListener {
            if (!isFinishing) {
                showSettingLockScreenBottomSheetDialog(this) { isChecked ->
                    LogTrack.d("토글", { "미세먼지 보기: $isChecked" })
                    updateAirQualityVisibility()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unLockPhoneReceiver?.let {
            unregisterReceiver(it)
        }
        unLockCameraReceiver?.let {
            unregisterReceiver(it)
        }
        unLockScreenReceiver?.let {
            unregisterReceiver(it)
        }
        unlockHintAnimator?.cancel()
        unlockHintAnimator = null
        binding?.bottomBannerView?.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        homeRecentReceiver = HomeRecentButtonReceiver(
            this,
            onHomePressed = { handleHomePressed() },
            onRecentPressed = { handleRecentPressed() }
        ).also { it.register() }
        registerReceiver(timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        updateDateTimeUI() // 진입 시 1회 갱신
        startUnlockHintAlphaAnimation()
        binding?.bottomBannerView?.onResume()
    }

    private fun loadLockScreenWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            applyWeatherDefaults()
            return
        }

        val location = getLastKnownLocation()
        if (location == null) {
            applyWeatherDefaults()
            return
        }

        val lat = location.latitude
        val lon = location.longitude

        // 기본값을 먼저 보여주고 → 비동기 응답으로 최신값 적용
        applyWeatherDefaults()

        APIWeather.requestLSWeather(
            lat = lat,
            lon = lon,
            onSuccess = { response ->
                runOnUiThread {

                    val displayAddress = response.location?.displayAddress
                    binding?.textLocation?.text =
                        displayAddress ?: getString(R.string.haru_location_unknown)

                    val tempNow = response.weather?.now?.temp?.now
                    val tempMin = response.weather?.now?.temp?.min
                    val tempMax = response.weather?.now?.temp?.max
                    val tempYes = response.weather?.now?.temp?.yes

                    binding?.textTemperatureMain?.text = "${tempNow ?: "-"}°"
                    binding?.textTemperatureSub?.text = "${tempMin ?: "-"}° / ${tempMax ?: "-"}°"

                    val diff = if (tempNow != null && tempYes != null) {
                        val d = tempNow - tempYes
                        when {
                            d > 0 -> "어제보다 ${d}도 높아요"
                            d < 0 -> "어제보다 ${-d}도 낮아요"
                            else -> "어제와 같아요"
                        }
                    } else {
                        ""
                    }
                    binding?.textTemperatureDiff?.text = diff

                    val pm10Grade = response.air?.now?.pm10?.grade
                    binding?.textPM10?.apply {
                        setText(R.string.haru_pm10)
                        setCompoundDrawablesWithIntrinsicBounds(getPMDrawable(pm10Grade), 0, 0, 0)
                    }
                    binding?.textPMStatus1?.apply {
                        text = getPMText(pm10Grade)
                        setTextColor(getPMTextColor(pm10Grade))
                    }

                    val pm25Grade = response.air?.now?.pm25?.grade
                    binding?.textPM25?.apply {
                        setText(R.string.haru_pm25)
                        setCompoundDrawablesWithIntrinsicBounds(getPMDrawable(pm25Grade), 0, 0, 0)
                    }
                    binding?.textPMStatus2?.apply {
                        text = getPMText(pm25Grade)
                        setTextColor(getPMTextColor(pm25Grade))
                    }

                    val weatherNow = response.weather?.now
                    binding?.imageWeatherIcon?.setImageResource(getWeatherIconRes(weatherNow))
                }
            },
            onFailure = {
                it.printStackTrace()
                runOnUiThread { applyWeatherDefaults() }
            }
        )
    }

    private fun applyWeatherDefaults() {
        binding?.apply {
            // 위치
            textLocation.setText(R.string.haru_location_unknown)

            // 날씨 아이콘
            imageWeatherIcon.setImageResource(R.drawable.ic_ls_weather_cloudy)

            // 온도
            textTemperatureMain.text = "-°"
            textTemperatureSub.text = "-° / -°"
            textTemperatureDiff.text = ""

            // 미세먼지
            textPM10.setText(R.string.haru_pm10)
            textPM10.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pm_no_data, 0, 0, 0)
            textPMStatus1.text = "..."
            textPMStatus1.setTextColor(
                ContextCompat.getColor(
                    this@LockScreenActivity,
                    R.color.haru_gray
                )
            )

            // 초미세먼지
            textPM25.setText(R.string.haru_pm25)
            textPM25.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pm_no_data, 0, 0, 0)
            textPMStatus2.text = "..."
            textPMStatus2.setTextColor(
                ContextCompat.getColor(
                    this@LockScreenActivity,
                    R.color.haru_gray
                )
            )
        }
    }

    private fun getLastKnownLocation(): android.location.Location? {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }
    }

    private fun getPMText(grade: Int?): String = when (grade) {
        1 -> "매우좋음"
        2 -> "좋음"
        3 -> "보통"
        4 -> "조금나쁨"
        5 -> "나쁨"
        6 -> "매우나쁨"
        7 -> "최악"
        else -> "정보없음"
    }

    private fun getPMDrawable(grade: Int?): Int = when (grade) {
        1 -> R.drawable.pm_very_good
        2 -> R.drawable.pm_good
        3 -> R.drawable.pm_soso
        4 -> R.drawable.pm_little_bad
        5 -> R.drawable.pm_bad
        6 -> R.drawable.pm_very_bad
        7 -> R.drawable.pm_a_lot_of_bad
        else -> R.drawable.pm_no_data
    }

    private fun getPMTextColor(grade: Int?): Int = when (grade) {
        1, 2 -> ContextCompat.getColor(this, R.color.haru_blue)
        3 -> ContextCompat.getColor(this, R.color.haru_gray)
        4 -> ContextCompat.getColor(this, R.color.haru_orange)
        5 -> ContextCompat.getColor(this, R.color.haru_red)
        6, 7 -> ContextCompat.getColor(this, R.color.haru_dark_red)
        else -> ContextCompat.getColor(this, R.color.haru_gray)
    }

    fun getWeatherIconRes(now: WeatherNow?): Int {
        val type = now?.type
        val skyType = now?.sky?.type ?: -1
        val rainType = now?.rain?.type ?: 0
        val rainRate = now?.rain?.rate ?: 0
        val isNight = type?.startsWith("night") == true

        val specialTypes = setOf("windy", "thunder", "thunder_rain", "snow")

        return when {
            // 1. 강한 비
            type !in specialTypes && rainType == 1 && rainRate >= 30 ->
                R.drawable.ic_ls_weather_rain_light

            // 2. 약한 비
            type !in specialTypes && rainType == 1 && rainRate in 1..29 -> {
                if (isNight) R.drawable.ic_ls_weather_rain_night
                else R.drawable.ic_ls_weather_rain_sunny
            }

            // 3. 흐림
            type !in specialTypes && skyType == 4 -> R.drawable.ic_ls_weather_cloudy

            // 4. 구름 많음
            type !in specialTypes && skyType == 3 -> {
                if (isNight) R.drawable.ic_ls_weather_night_cloudy
                else R.drawable.ic_ls_weather_sunny_cloudy
            }

            // 5. 구름 조금
            type !in specialTypes && skyType == 2 -> {
                if (isNight) R.drawable.ic_ls_weather_night_cloudy
                else R.drawable.ic_ls_weather_sunny_cloudy
            }

            // 6. 맑음
            type == "day_clear" && type !in specialTypes && skyType <= 1 ->
                R.drawable.ic_ls_weather_sunny

            type == "night_clear" && type !in specialTypes && skyType <= 1 ->
                R.drawable.ic_ls_weather_night_clear

            // 7. 특수 조건
            type == "windy" -> R.drawable.ic_ls_weather_windy
            type == "thunder" -> R.drawable.ic_ls_weather_thunder
            type == "thunder_rain" -> R.drawable.ic_ls_weather_thunder_rain
            type == "snow" -> R.drawable.ic_ls_weather_snow

            // 8. fallback
            else -> R.drawable.ic_ls_weather_cloudy
        }
    }

    private fun setupFavoriteMemoList() {
        val db = MemoDatabase.getInstance(this)
        lifecycleScope.launch {
            val favoriteMemos = withContext(Dispatchers.IO) {
                db.memoDao().getImportantLockScreenMemosByCreatedDesc()
            }

            favoriteAdapter = LockScreenMemoAdapter(
                items = favoriteMemos.toMutableList(),
                memoDao = db.memoDao()
            ) { memoOrNull ->
                if (memoOrNull != null) {
                    handleMemoClick(memoOrNull)
                } else {
                    // 빈 상태 뷰 클릭 시 동작 처리
                    showLockScreenMemoListDialog(this@LockScreenActivity)
                }
                MemoWidgetUpdater().observeMemoChanges(this@LockScreenActivity)
            }

            binding?.recyclerFavoriteMemos?.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = favoriteAdapter
                val spacing = resources.getDimensionPixelSize(R.dimen.lock_memo_item_spacing)
                addItemDecoration(HorizontalSpacingItemDecoration(spacing))
            }
        }
    }


    override fun onPause() {
        super.onPause()
        homeRecentReceiver?.unregister()
        unregisterReceiver(timeTickReceiver)
        binding?.bottomBannerView?.onPause()
    }

    fun showLockScreenMemoListDialog(context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context)

        val binding =
            DialogBottomSheetLockScreenMemoListBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        // 둥근 배경 설정
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val dialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = null
            bottomSheet?.setBackgroundResource(R.drawable.shape_bg_rounded_top_white)

            val fixedHeight = (626 * context.resources.displayMetrics.density).toInt()
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = fixedHeight
            behavior.isDraggable = false
            behavior.isHideable = false

            bottomSheet.layoutParams.height = fixedHeight
            bottomSheet.requestLayout()
        }

        bottomSheetDialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }

        binding.lyBack.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val db = MemoDatabase.getInstance(context)
        listAdapter = LockScreenMemoListAdapter(
            context = this,
            memoDao = db.memoDao(),
            onItemClick = { memo ->
                handleMemoClick(memo)
            },
            onImportantChanged = { updatedMemo ->
                favoriteAdapter?.updateFavorite(updatedMemo)
                MemoWidgetUpdater().observeMemoChanges(this@LockScreenActivity)
            }
        )
        binding.rvMemoList.layoutManager = LinearLayoutManager(context)
        binding.rvMemoList.adapter = listAdapter

        // 간격 추가
        val horizontal = resources.getDimensionPixelSize(R.dimen.memo_item_spacing_horizontal) // 12dp
        val vertical = resources.getDimensionPixelSize(R.dimen.memo_item_spacing_vertical)     // 6dp

        binding.rvMemoList.addItemDecoration(
            LockScreenListSpacingItemDecoration(horizontal, vertical)
        )

        // DB 연결
        lifecycleScope.launch {
            val memos = withContext(Dispatchers.IO) {
                MemoDatabase.getInstance(context).memoDao().getAllUnLockMemos()
            }

            listAdapter?.submitList(memos)
        }

        if (!isFinishing && !isDestroyed) {
            bottomSheetDialog.show()
        }
    }


    fun showLockScreenMemoWriteDialog(
        context: Context,
        onSave: (title: String, content: String) -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)

        val binding =
            DialogBottomSheetLockScreenWriteMemoBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val dialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = null
            bottomSheet?.setBackgroundResource(R.drawable.shape_bg_rounded_top_white)

            // 고정 높이 설정
            val fixedHeight = (626 * context.resources.displayMetrics.density).toInt()
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = fixedHeight
            behavior.isDraggable = false
            behavior.isHideable = false

            bottomSheet.layoutParams.height = fixedHeight
            bottomSheet.requestLayout()
        }

        bottomSheetDialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }

        with(binding) {
            lyBack.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            // 저장 버튼 클릭
            tvSave.setOnClickListener {
                val title = editTitle.text.toString().trim()
                val content = editContent.text.toString().trim()

                if (title.isEmpty() && content.isEmpty()) {
                    toastShort(context, "메모를 작성해주세요")
                } else {
                    onSave(title, content)
                    bottomSheetDialog.dismiss()
                }
            }
        }

        if (!isFinishing && !isDestroyed) {
            bottomSheetDialog.show()
        }
    }


    fun showSettingLockScreenBottomSheetDialog(
        context: Context,
        onToggleChanged: (Boolean) -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)

        val binding = DialogBottomSheetLockScreenSetBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        // 둥근 배경 적용
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val dialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.background = null // 배경 제거
            bottomSheet?.setBackgroundResource(R.drawable.shape_bg_rounded_top_white) // 둥근 배경 적용

            // BottomSheetBehavior 설정
            val fixedHeight = (626 * context.resources.displayMetrics.density).toInt()
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = fixedHeight
            behavior.isDraggable = false
            behavior.isHideable = false

            bottomSheet.layoutParams.height = fixedHeight
            bottomSheet.requestLayout()
        }

        bottomSheetDialog.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK // true면 백버튼 무시됨
        }

        with(binding) {
            // 초기 상태 반영
            switchPmView.isChecked = PreferenceUtil.get(KEY_PM_VISIBLE, true)

            // 스위치 변경 리스너
            switchPmView.setOnCheckedChangeListener { _, isChecked ->
                PreferenceUtil.set(KEY_PM_VISIBLE, isChecked)
                onToggleChanged(isChecked)
            }

            // itemPmSetting 클릭 시 switch 토글 처리
            itemPmSetting.setOnClickListener {
                val newState = !switchPmView.isChecked
                switchPmView.isChecked = newState  // 이 줄이 위의 setOnCheckedChangeListener를 자동 호출함
            }

            // 닫기 버튼
            lyBack.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
        }

        if (!isFinishing && !isDestroyed) {
            bottomSheetDialog.show()
        }
    }

    private fun updateAirQualityVisibility() {
        val isVisible = PreferenceUtil.get(KEY_PM_VISIBLE, true)
        binding?.airQualityContainer?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun handleMemoClick(memo: MemoEntity) {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager?.isKeyguardLocked == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager.requestDismissKeyguard(
                    this,
                    object : KeyguardManager.KeyguardDismissCallback() {
                        override fun onDismissSucceeded() {
                            handleAfterKeyguardDismissed(memo)
                        }
                    }
                )
            } else {
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
                unLockPhoneReceiver = UnlockReceiver().apply {
                    setListener {
                        handleAfterKeyguardDismissed(memo)
                    }
                }
                registerReceiver(unLockPhoneReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
            }
        } else {
            handleAfterKeyguardDismissed(memo)
        }
    }

    private fun handleAfterKeyguardDismissed(memo: MemoEntity) {
        if (memo.isLocked) {
            PasswordActivity.startLockScreen(
                this,
                memoItem = memo
            )
            // LockScreenActivity는 여기서 종료
            finish()
        } else {
            MemoDetailActivity.start(
                this,
                MemoMode.READ_MEMO,
                memoItem = memo,
                close = true
            )
        }
    }


    private fun handleHomePressed() {
        LogTrack.d("LockScreen", { "Home 눌림 → 종료 또는 특정 동작" })
        addHomeScreenView()
    }

    private fun handleRecentPressed() {
        LogTrack.d("LockScreen", { "Recent 눌림 → 종료 또는 특정 동작" })
        addHomeScreenView()
    }

    private fun handlePhoneClick() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguardManager?.isKeyguardLocked == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager.requestDismissKeyguard(
                    this,
                    object : KeyguardManager.KeyguardDismissCallback() {
                        override fun onDismissSucceeded() {
                            startPhoneIntent()
                        }
                    })
            } else {
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
                unLockPhoneReceiver = UnlockReceiver().apply {
                    setListener {
                        startPhoneIntent()
                    }
                }
                registerReceiver(unLockPhoneReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
            }
        } else {
            startPhoneIntent()
        }
    }

    private fun startPhoneIntent() {
        val intent = Intent(Intent.ACTION_DIAL)
        startActivity(intent)
    }

    private fun handleCameraClick() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
            if (keyguardManager?.isKeyguardLocked == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    keyguardManager.requestDismissKeyguard(
                        this,
                        object : KeyguardManager.KeyguardDismissCallback() {
                            override fun onDismissSucceeded() {
                                openCamera()
                            }
                        })
                } else {
                    window.addFlags(
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    )

                    unLockCameraReceiver = UnlockReceiver().apply {
                        setListener {
                            openCamera()
                        }
                    }

                    registerReceiver(unLockCameraReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
                }
            } else {
                openCamera()
            }
        } else {
            // 권한이 없는 경우 권한 요청 → 이 때는 finish() 호출 O
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            finish()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            toastShort(this, "카메라 앱을 실행할 수 없습니다.")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeUnlock() {
        binding?.lockScreenRootView?.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0f
            var distance = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        distance = 0f
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val endX = event.rawX
                        distance = endX - startX

                        if (distance < 0) {
                            // 왼쪽으로 스와이프는 무시
                            return true
                        }

                        // 전체 화면 오른쪽으로 이동
                        binding?.lockScreenRootView?.translationX = distance

                        // 거리 조건 충족 시 해제
                        if (distance > v.width * 0.5 && !isUnlocking) {
                            isUnlocking = true
                            unlockScreen()
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (distance < v.width * 0.5) {
                            // 되돌아오는 애니메이션
                            binding?.lockScreenRootView?.animate()
                                ?.translationX(0f)
                                ?.setInterpolator(AccelerateDecelerateInterpolator())
                                ?.setDuration(300)
                                ?.start()
                        }
                    }
                }
                return true
            }
        })
    }

    private fun unlockScreen() {
        val unlockView = binding?.lockScreenRootView ?: return

        unlockView.animate()
            .translationX(unlockView.width.toFloat())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(300)
            .withEndAction {
                val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
                if (keyguardManager?.isKeyguardLocked == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        keyguardManager.requestDismissKeyguard(
                            this,
                            object : KeyguardManager.KeyguardDismissCallback() {
                                override fun onDismissSucceeded() {
                                    finish()
                                }

                                override fun onDismissCancelled() {
                                    // 필요시 처리
                                }
                            })
                    } else {
                        window.addFlags(
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        )

                        unLockScreenReceiver = UnlockReceiver().apply {
                            setListener {
                                finish()
                            }
                        }
                        registerReceiver(
                            unLockScreenReceiver,
                            IntentFilter(Intent.ACTION_USER_PRESENT)
                        )
                    }
                } else {
                    finish()
                }
            }
            .start()
    }

    private fun startUnlockHintAlphaAnimation() {
        val textView = binding?.textUnlockHint ?: return
        val imageView = binding?.iconUnlockArrow ?: return

        val textAlpha = ObjectAnimator.ofFloat(textView, View.ALPHA, 0.3f, 1f).apply {
            duration = 1000L
            startDelay = 0
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }

        val iconAlpha = ObjectAnimator.ofFloat(imageView, View.ALPHA, 0.3f, 1f).apply {
            duration = 1000L
            startDelay = 200L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(textAlpha, iconAlpha)
        }

        animatorSet.start()
        unlockHintAnimator = animatorSet
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addHomeScreenView() {
        LogTrack.d("LockScreen") { "addHomeScreenView called" }

        try {
            finish() // 현재 LockScreenActivity 종료

            val manager = getSystemService(WINDOW_SERVICE) as WindowManager
            val inflater = LayoutInflater.from(this)
            val binding = ActivityPressHomeKeyBinding.inflate(inflater)
            val lockerView = binding.root

            val alpha = floatArrayOf(1f)

            // 닫기 버튼 동작
            binding.ivClose.setOnClickListener {
                if (lockerView.windowToken != null) {
                    manager.removeView(lockerView)
                }
            }

            // 전체 클릭 시 View 제거
            lockerView.setOnClickListener {
                if (lockerView.windowToken != null) {
                    manager.removeView(lockerView)
                }
            }

            // 터치로 점점 투명해지는 효과
            lockerView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        alpha[0] -= 0.07f
                        v.alpha = maxOf(alpha[0], 0f)
                    }

                    MotionEvent.ACTION_UP -> {
                        if (alpha[0] < 0.6f) {
                            v.alpha = 0f
                            if (lockerView.windowToken != null) {
                                manager.removeView(lockerView)
                            }
                        } else {
                            alpha[0] = 1f
                            v.alpha = 1f
                        }
                    }
                }
                true
            }

            // WindowManager에 뷰 추가
            manager.addView(lockerView, LockScreenHelper.getParams(this))

        } catch (e: Exception) {
            LogTrack.e("LockScreen") { "Error in addHomeScreenView: ${e.message}" }
            e.printStackTrace()
        }
    }

}