package com.memong.aos.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.memong.aos.BuildConfig
import com.memong.aos.MemoEventFlow
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.enum.MemoMode
import com.memong.aos.data.extension.getParcelableCompat
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_LAST_DEVICE_AUTH_ELAPSED
import com.memong.aos.data.utils.PreferenceUtil.KEY_PASSWORD
import com.memong.aos.data.utils.PreferenceUtil.KEY_PASSWORD_SWITCH
import com.memong.aos.data.utils.PreferenceUtil.KEY_SIMPLIFY
import com.memong.aos.data.utils.Util
import com.memong.aos.databinding.ActivityPasswordBinding
import com.memong.aos.helper.MemoWidgetUpdater
import com.memong.aos.ui.custom.header.SettingHeaderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PasswordActivity : BaseActivity() {

    override val NAME: String
        get() = PasswordActivity::class.java.simpleName

    private var binding: ActivityPasswordBinding? = null
    private var memoItem: MemoEntity? = null

    private val REQUEST_MEMO_DETAIL_FROM_PASSWORD_SEARCH = 1001

    companion object {
        private const val EXTRA_MODE = "mode"
        internal const val EXTRA_MEMO_ITEM = "memoItem"
        const val EXTRA_MEMO_LIST = "extra_memo_list"
        const val EXTRA_BYPASS_CURRENT = "extra_bypass_current"
        private const val RECENT_AUTH_TIMEOUT_MS = 60_000L // 60초 내만 허용(옵션)

        fun start(
            activity: Activity,
            mode: SettingHeaderView.SetHeaderMode,
            memoItem: MemoEntity? = null,
            close: Boolean = false
        ) {
            val intent = Intent(activity, PasswordActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode.name)
                putExtra(EXTRA_MEMO_ITEM, memoItem)
            }
            activity.start(intent = intent, flags = Intent.FLAG_ACTIVITY_CLEAR_TOP, close = close)
        }

        fun startLockScreen(
            activity: Activity,
            memoItem: MemoEntity
        ) {
            val intent = Intent(activity, PasswordActivity::class.java).apply {
                putExtra(EXTRA_MODE, SettingHeaderView.SetHeaderMode.PASSWORD_CHECK.name)
                putExtra(EXTRA_MEMO_ITEM, memoItem)
            }
            activity.startActivity(intent)
        }

        fun createIntent(
            activity: Activity,
            mode: SettingHeaderView.SetHeaderMode,
            memoItem: MemoEntity? = null
        ): Intent {
            return Intent(activity, PasswordActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode.name)
                memoItem?.let {
                    putExtra(EXTRA_MEMO_ITEM, it)
                }
            }
        }

        fun startWidget(
            context: Context,
            memoItem: MemoEntity? = null
        ) {
            val intent = Intent(context, PasswordActivity::class.java).apply {
                putExtra(EXTRA_MODE, SettingHeaderView.SetHeaderMode.PASSWORD_CHECK.name)
                putExtra(EXTRA_MEMO_ITEM, memoItem)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }

        fun startDelete(
            activity: Activity,
            memoItems: List<MemoEntity>
        ) {
            val intent = Intent(activity, PasswordActivity::class.java).apply {
                putExtra(EXTRA_MODE, SettingHeaderView.SetHeaderMode.PASSWORD_DELETE.name)
                putExtra(EXTRA_MEMO_LIST, ArrayList(memoItems)) // ArrayList로 넘겨야 직렬화 가능
            }
            activity.startActivity(intent)
        }
    }


    private val input = mutableListOf<Int>()
    private var firstPassword: String? = null
    private var mode = SettingHeaderView.SetHeaderMode.PASSWORD
    private var changeStep = 0
    private var newPassword: String = ""
    private var bypassCurrent: Boolean = false
    val memoDao by lazy {
        MemoDatabase.getInstance(this@PasswordActivity).memoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        binding?.let { b ->
            setContentView(b.root)
            b.passwordRootView.let { configureWindowInsets(it, paddingDp = 0) }

            memoItem = intent.getParcelableCompat<MemoEntity>(EXTRA_MEMO_ITEM)

            mode = intent.getStringExtra(EXTRA_MODE)?.let {
                runCatching { SettingHeaderView.SetHeaderMode.valueOf(it) }.getOrNull()
            } ?: SettingHeaderView.SetHeaderMode.PASSWORD_REGI

            // ✨ 추가: 기기 인증 생략 플래그 읽기
            bypassCurrent = intent.getBooleanExtra(EXTRA_BYPASS_CURRENT, false)

            // (옵션) 최근 기기 인증 타임아웃 체크: 60초 내에만 검증 생략 허용
            if (bypassCurrent && mode == SettingHeaderView.SetHeaderMode.PASSWORD_CHANGE) {
                val lastAuthElapsed = PreferenceUtil.get(KEY_LAST_DEVICE_AUTH_ELAPSED, 0L)
                val ok = lastAuthElapsed > 0 &&
                        (android.os.SystemClock.elapsedRealtime() - lastAuthElapsed) <= RECENT_AUTH_TIMEOUT_MS
                if (!ok) bypassCurrent = false
            }

            b.headerPassword.setMode(mode)
            b.headerPassword.setOnBackClickListener { finish() }

            // 🔧 초기 가이드 문구/스텝 세팅
            if (mode == SettingHeaderView.SetHeaderMode.PASSWORD_CHANGE && bypassCurrent) {
                changeStep = 1
                b.textGuide.text = getString(R.string.haru_password_input_new) // “새 비밀번호 입력”
            } else {
                changeStep = 0
                b.textGuide.text = when (mode) {
                    SettingHeaderView.SetHeaderMode.PASSWORD_RESET,
                    SettingHeaderView.SetHeaderMode.PASSWORD_CHANGE,
                    SettingHeaderView.SetHeaderMode.PASSWORD_CHECK ->
                        getString(R.string.haru_password_input_current)

                    else -> getString(R.string.haru_password_input)
                }
            }

            setupKeypad()
            updateDots()

            // bottom Banner ...
            requestBannerAd(
                placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
                bannerAdSize = BannerAdSize.DYNAMIC,
                bannerView = b.bottomBannerView
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.headerPassword?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding?.bottomBannerView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding?.bottomBannerView?.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEMO_DETAIL_FROM_PASSWORD_SEARCH && resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupKeypad() {
        val b = binding ?: return
        val digitButtons = listOf(
            b.btn0, b.btn1, b.btn2, b.btn3,
            b.btn4, b.btn5, b.btn6, b.btn7, b.btn8, b.btn9
        )

        digitButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (input.size < 4) {
                    input.add(index)
                    updateDots()
                    if (input.size == 4) handleInputComplete()
                }
            }
        }

        b.btnDelete.setOnClickListener {
            if (input.isNotEmpty()) {
                input.removeAt(input.lastIndex)
                updateDots()
            }
        }
    }

    private fun updateDots() {
        val b = binding ?: return
        for (i in 0 until b.dotIndicatorLayout.childCount) {
            val dot = b.dotIndicatorLayout.getChildAt(i)
            val resId =
                if (i < input.size) R.drawable.shape_dot_w_circle else R.drawable.shape_dot_circle
            dot.setBackgroundResource(resId)
        }
    }

    private fun handleInputComplete() {
        val currentInput = input.joinToString("")
        when (mode) {
            SettingHeaderView.SetHeaderMode.PASSWORD_REGI -> handlePasswordRegistration(currentInput)
            SettingHeaderView.SetHeaderMode.PASSWORD_RESET -> handlePasswordReset(currentInput)
            SettingHeaderView.SetHeaderMode.PASSWORD_CHANGE -> handlePasswordChange(currentInput)
            SettingHeaderView.SetHeaderMode.PASSWORD_CHECK -> handlePasswordCheck(currentInput)
            SettingHeaderView.SetHeaderMode.PASSWORD_CHECK_SEARCH -> handlePasswordCheck(currentInput) // 추가
            SettingHeaderView.SetHeaderMode.PASSWORD_LOCK,
            SettingHeaderView.SetHeaderMode.PASSWORD_UNLOCK -> handlePasswordLock(currentInput)

            SettingHeaderView.SetHeaderMode.PASSWORD_DELETE -> handlePasswordDelete(currentInput)

            else -> {}
        }
    }

    private fun handlePasswordRegistration(currentInput: String) {
        binding ?: return
        if (firstPassword == null) {
            firstPassword = currentInput
            resetInput(R.string.haru_password_input_confirm)
        } else if (currentInput == firstPassword) {
            PreferenceUtil.set(KEY_PASSWORD, currentInput)
            PreferenceUtil.set(KEY_PASSWORD_SWITCH, true)
            Util.toastShort(this, getString(R.string.haru_password_set))

            lifecycleScope.launch(Dispatchers.IO) {
                val memo = memoDao.getMemoById(memoItem?._id ?: 0) ?: return@launch
                val updated = memo.copy(isLocked = true)
                memoItem = updated
                memoDao.updateMemo(updated)

                withContext(Dispatchers.Main) {
                    LogTrack.i(NAME) { "handlePasswordRegistration -> { isLocked : ${memoItem?.isLocked} } " }
                    MemoEventFlow.emit(
                        MemoEvent.PasswordEvent(
                            memoId = memoItem?._id ?: 0,
                            isLocked = memoItem?.isLocked ?: false
                        )
                    )
                }
            }
            finish()

        } else {
            resetInput(R.string.haru_password_not_match, shake = true)
        }
    }

    private fun handlePasswordReset(currentInput: String) {
        binding ?: return
        val savedPassword = PreferenceUtil.get(KEY_PASSWORD, "")
        if (currentInput == savedPassword) {
            PreferenceUtil.remove(KEY_PASSWORD)
            PreferenceUtil.set(KEY_PASSWORD_SWITCH, false)
            MemoEventFlow.emit(MemoEvent.PasswordResetEvent)

            // DB에서 모든 잠금 메모의 잠금 해제
            CoroutineScope(Dispatchers.IO).launch {
                val db = MemoDatabase.getInstance(this@PasswordActivity)
                db.memoDao().unlockAllLockedMemos()
                MemoWidgetUpdater().observeMemoChanges(this@PasswordActivity)
            }

            Util.toastShort(this, getString(R.string.haru_password_reset_done))
            finish()
        } else {
            resetInput(R.string.haru_password_not_match, shake = true)
        }
    }

    private fun handlePasswordChange(currentInput: String) {
        binding ?: return
        val savedPassword = PreferenceUtil.get(KEY_PASSWORD, "")

        when (changeStep) {
            0 -> {
                // ✨ BYPASS 인 경우 0단계를 스킵하여 바로 새 비번 입력 단계로 이동
                if (bypassCurrent) {
                    changeStep = 1
                    resetInput(R.string.haru_password_input_new)
                    return
                }
                if (currentInput == savedPassword) {
                    changeStep = 1
                    resetInput(R.string.haru_password_input_new)
                } else {
                    resetInput(R.string.haru_password_not_match, shake = true)
                }
            }

            1 -> {
                newPassword = currentInput
                changeStep = 2
                resetInput(R.string.haru_password_input_confirm_new)
            }

            2 -> if (currentInput == newPassword) {
                PreferenceUtil.set(KEY_PASSWORD, newPassword)
                Util.toastShort(this, getString(R.string.haru_password_changed))
                PreferenceUtil.set(KEY_SIMPLIFY, false)
                finish()
            } else {
                changeStep = 1
                resetInput(R.string.haru_password_not_match, shake = true)
            }
        }
    }

    private fun handlePasswordCheck(currentInput: String) {
        val savedPassword = PreferenceUtil.get(KEY_PASSWORD, "")

        if (currentInput == savedPassword) {
            if (mode == SettingHeaderView.SetHeaderMode.PASSWORD_CHECK_SEARCH) {
                PreferenceUtil.set(KEY_SIMPLIFY, true)

                // SearchActivity로 바로 종료하지 않고, 상세 화면 진입
                memoItem?.let {
                    val intent = MemoDetailActivity.getIntent(
                        activity = this,
                        mode = MemoMode.READ_MEMO,
                        memoItem = it
                    )
                    startActivityForResult(intent, REQUEST_MEMO_DETAIL_FROM_PASSWORD_SEARCH)
                }
            } else if (mode == SettingHeaderView.SetHeaderMode.PASSWORD_CHECK) {
                // 기존 방식: 내부에서 직접 Detail 열기
                PreferenceUtil.set(KEY_SIMPLIFY, true)

                memoItem?.let {
                    MemoDetailActivity.start(
                        this,
                        MemoMode.READ_MEMO,
                        memoItem = it,
                        close = true
                    )
                    finish()
                } ?: run {
                    MemoEventFlow.emit(event = MemoEvent.AllMemoUpdated)
                    finish()
                }


            } else {
                // 기타 모드도 setResult 반환
                val intent = Intent().apply {
                    memoItem?.let {
                        putExtra(EXTRA_MEMO_ITEM, it)
                    }
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else {
            resetInput(R.string.haru_password_not_match, shake = true)
        }
    }


    private fun handlePasswordLock(currentInput: String) {
        val savedPassword = PreferenceUtil.get(KEY_PASSWORD, "")

        LogTrack.i(NAME) { "handlePasswordLock -> { mode: $mode, savedPassword: $savedPassword, currentInput: $currentInput }" }
        if (currentInput == savedPassword) {
            if (mode == SettingHeaderView.SetHeaderMode.PASSWORD_LOCK) {
                MemoEventFlow.emit(
                    MemoEvent.PasswordEvent(
                        memoId = memoItem?._id ?: 0,
                        isLocked = true
                    )
                )
                MemoWidgetUpdater().observeMemoChanges(this@PasswordActivity)
                finish()
            } else if (mode == SettingHeaderView.SetHeaderMode.PASSWORD_UNLOCK) {
                MemoEventFlow.emit(
                    MemoEvent.PasswordEvent(
                        memoId = memoItem?._id ?: 0,
                        isLocked = false
                    )
                )
                MemoWidgetUpdater().observeMemoChanges(this@PasswordActivity)
                finish()
            } else {
                // 다른 모드 (e.g. TRASH 등)는 기존처럼 결과 반환
                val intent = Intent().apply {
                    memoItem?.let {
                        putExtra(EXTRA_MEMO_ITEM, it)
                    }
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else {
            resetInput(R.string.haru_password_not_match, shake = true)
        }
    }

    private fun handlePasswordDelete(currentInput: String) {
        val savedPassword = PreferenceUtil.get(KEY_PASSWORD, "")

        if (currentInput == savedPassword) {
            if (mode == SettingHeaderView.SetHeaderMode.PASSWORD_DELETE) {

                val memoList: ArrayList<MemoEntity>? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableArrayListExtra<MemoEntity>(
                            EXTRA_MEMO_LIST,
                            MemoEntity::class.java
                        )
                    } else {
                        intent.getParcelableArrayListExtra<MemoEntity>(EXTRA_MEMO_LIST)
                    }

                if (memoList.isNullOrEmpty()) {
                    finish()
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    memoList.forEach { item ->
                        val deleted = item.copy(
                            isDeleted = true,
                            deleted = System.currentTimeMillis()
                        )
                        memoDao.updateMemo(deleted)
                    }

                    withContext(Dispatchers.Main) {
                        LogTrack.i(NAME) { "handlePasswordDelete -> " }
                        MemoWidgetUpdater().observeMemoChanges(this@PasswordActivity)
                        MemoEventFlow.emit(event = MemoEvent.PasswordMemoDeleteEvent)
                        finish()
                    }
                }
            } else {
                val intent = Intent().apply {
                    memoItem?.let {
                        putExtra(EXTRA_MEMO_ITEM, it)
                    }
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else {
            resetInput(R.string.haru_password_not_match, shake = true)
        }
    }

    private fun resetInput(@StringRes messageResId: Int, shake: Boolean = false) {
        val b = binding ?: return
        input.clear()
        updateDots()
        b.textGuide.text = getString(messageResId)
        if (shake) shakeView(b.textGuide)
    }

    private fun shakeView(view: View) {
        view.startAnimation(
            TranslateAnimation(0f, 10f, 0f, 0f).apply {
                duration = 50
                repeatMode = Animation.REVERSE
                repeatCount = 5
            }
        )
    }
}