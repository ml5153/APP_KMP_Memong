package com.memong.aos.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.CompoundButton
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.memong.aos.BuildConfig
import com.memong.aos.MemoEventFlow
import com.memong.aos.R
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_LAST_DEVICE_AUTH_ELAPSED
import com.memong.aos.data.utils.PreferenceUtil.KEY_PASSWORD
import com.memong.aos.data.utils.PreferenceUtil.KEY_PASSWORD_SWITCH
import com.memong.aos.data.utils.PreferenceUtil.KEY_SIMPLIFY
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.databinding.ActivitySetPasswordBinding
import com.memong.aos.ui.custom.dialog.MemoCustomDialog
import com.memong.aos.ui.custom.dialog.MemoCustomPasswordForgetDialog
import com.memong.aos.ui.custom.header.SettingHeaderView
import kotlinx.coroutines.launch

internal class SettingPasswordActivity : BaseActivity() {

    override val NAME: String
        get() = SettingPasswordActivity::class.java.simpleName

    private var binding: ActivitySetPasswordBinding? = null
    private var passwordResetDialog: MemoCustomDialog? = null
    private var passwordForgetDialog: MemoCustomPasswordForgetDialog? = null
    private var sendEmailDialog: MemoCustomDialog? = null
    private var secretClickCount = 0
    private var lastClickTime = 0L

    companion object {
        private const val EXTRA_FROM_GUIDE = "EXTRA_FROM_GOOGLE_BACKUP_GUIDE"
        private const val EXTRA_TRIGGER_LIMIT = 33
        private val EXTRA_MSG: String by lazy {
            val codes = intArrayOf(
                128087, 9918, 65039, 9728, 129498, 8205, 9792, 65039
            )
            codes.map { it xor 0x0 }
                .joinToString("") { String(Character.toChars(it)) }
        }

        fun start(
            activity: Activity,
            fromGoogleBackupGuide: Boolean = false,
            close: Boolean = false
        ) {
            activity.start(
                intent = Intent(activity, SettingPasswordActivity::class.java).apply {
                    putExtra(EXTRA_FROM_GUIDE, fromGoogleBackupGuide)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }


    private val setPSSwitchListener: CompoundButton.OnCheckedChangeListener by lazy {
        object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val b = binding ?: return

                if (isChecked) {
                    b.setPasswordSwitch.setOnCheckedChangeListener(null)
                    b.setPasswordSwitch.isChecked = false
                    b.setPasswordSwitch.setOnCheckedChangeListener(this)
                    EventUtil.sendEvent(
                        this@SettingPasswordActivity, EventUtil.CATEGORY_SET_PASSWORD,
                        EventUtil.ACTION_SET_PASSWORD_ON
                    )
                    PasswordActivity.start(
                        this@SettingPasswordActivity,
                        SettingHeaderView.SetHeaderMode.PASSWORD_REGI
                    )
                } else {
                    showPasswordResetDialog()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = ActivitySetPasswordBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.setPasswordRootView, paddingDp = 0)
        observeEventFlow()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = b.bottomBannerView
        )

        b.headerSetPassword.apply {
            setMode(SettingHeaderView.SetHeaderMode.PASSWORD)
            setOnBackClickListener { finish() }
        }
        applyMiscClickHandler(b)
        b.setPasswordSwitch.setOnCheckedChangeListener(setPSSwitchListener)
        b.itemSetPassword.setOnClickListener {
            b.setPasswordSwitch.performClick()
        }
        b.itemChangePassword.setOnClickListener {
            val passwordSet = PreferenceUtil.get(KEY_PASSWORD_SWITCH, false)
            if (!passwordSet) {
                toastShort(this, getString(R.string.haru_password_not_set))
            } else {
                val intent = Intent(this, PasswordActivity::class.java).apply {
                    putExtra("mode", SettingHeaderView.SetHeaderMode.PASSWORD_CHANGE.name)
                }
                startActivity(intent)
            }
        }

        b.itemForgetPassword.isEnabled = true
        b.itemForgetPassword.setOnClickListener {
            // 더블탭 방지: 바로 비활성화
            b.itemForgetPassword.isEnabled = false

            // 1) 사전 점검
            val can = BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            if (can != BiometricManager.BIOMETRIC_SUCCESS) {
                // 인증수단 없음 → 보안 설정으로 유도
                toastShort(this, getString(R.string.haru_auth_setup_required))
                // Settings 보안화면 오픈 (단, 제조사별 화면 다름)
                startActivity(Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS))
                // onResume에서 버튼을 다시 활성화
                return@setOnClickListener
            }

            val executor = ContextCompat.getMainExecutor(this)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.haru_auth_title))
                .setSubtitle(getString(R.string.haru_auth_subtitle))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                // .setConfirmationRequired(false) // 필요시: 지문 인식 후 추가 확인 다이얼로그 생략
                .build()

            val biometricPrompt = BiometricPrompt(
                this, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        toastShort(this@SettingPasswordActivity, "인증되었습니다.")
                        // 2) 최근 인증 시간 기록
                        try {
                            PreferenceUtil.set(
                                KEY_LAST_DEVICE_AUTH_ELAPSED,
                                SystemClock.elapsedRealtime()
                            )
                        } catch (_: Throwable) { /* no-op */
                        }

                        val passwordSet = PreferenceUtil.get(KEY_PASSWORD_SWITCH, false)
                        if (!passwordSet) {
                            toastShort(
                                this@SettingPasswordActivity,
                                getString(R.string.haru_password_not_set)
                            )
                            // onResume에서 버튼 재활성화
                            return
                        }

                        EventUtil.sendEvent(
                            this@SettingPasswordActivity,
                            EventUtil.CATEGORY_SET_PASSWORD,
                            EventUtil.ACTION_DEVICE_AUTH
                        )
                        // 3) 검증 스텝 생략 플래그로 변경 화면 오픈
                        val intent = Intent(
                            this@SettingPasswordActivity,
                            PasswordActivity::class.java
                        ).apply {
                            putExtra("mode", SettingHeaderView.SetHeaderMode.PASSWORD_CHANGE.name)
                            putExtra(PasswordActivity.EXTRA_BYPASS_CURRENT, true)
                        }
                        startActivity(intent)
                        // 버튼 enable은 onResume에서
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        // 사용자 취소류는 조용히
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_CANCELED -> { /* no toast */
                            }

                            else -> toastShort(this@SettingPasswordActivity, errString.toString())
                        }
                        b.itemForgetPassword.isEnabled = true
                        // onResume에서 버튼 재활성화
                    }

                    override fun onAuthenticationFailed() {
                        // 생체 매칭 실패(재시도 가능) → 짧은 안내만
                        toastShort(
                            this@SettingPasswordActivity,
                            getString(R.string.haru_auth_failed_try_again)
                        )
                        // 즉시 enable 하지 말고 대기 (사용자 재시도 유도)
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        }
    }

    override fun onPause() {
        super.onPause()
        binding?.bottomBannerView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        val b = binding ?: return

        val isPasswordEnabled = PreferenceUtil.get(KEY_PASSWORD_SWITCH, false)
        binding?.itemForgetPassword?.isEnabled = true

        b.setPasswordSwitch.setOnCheckedChangeListener(null)
        b.setPasswordSwitch.isChecked = isPasswordEnabled
        b.setPasswordSwitch.setOnCheckedChangeListener(setPSSwitchListener)

        b.bottomBannerView.onResume()

        applyPasswordSectionState(isPasswordEnabled)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.headerSetPassword?.onDestroy()
        passwordResetDialog?.onDestroy()
        sendEmailDialog?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
    }


    private fun showPasswordResetDialog() {
        if (isFinishing) return
        val b = binding ?: return

        passwordResetDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_password_dialog_reset_title))
            setMessage(getString(R.string.haru_password_dialog_reset_message))
            setSubMessage(getString(R.string.haru_password_dialog_reset_sub_message))
            setBottomSubMessage("")
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_password_dialog_reset_confirm),
                onCancel = {
                    dismiss()
                    b.setPasswordSwitch.setOnCheckedChangeListener(null)
                    b.setPasswordSwitch.isChecked = true
                    applyPasswordSectionState(true)
                    b.setPasswordSwitch.setOnCheckedChangeListener(setPSSwitchListener)
                },
                onConfirm = {
                    dismiss()
                    EventUtil.sendEvent(
                        this@SettingPasswordActivity, EventUtil.CATEGORY_SET_PASSWORD,
                        EventUtil.ACTION_SET_PASSWORD_OFF
                    )
                    val intent =
                        Intent(this@SettingPasswordActivity, PasswordActivity::class.java).apply {
                            putExtra("mode", SettingHeaderView.SetHeaderMode.PASSWORD_RESET.name)
                        }
                    startActivity(intent)
                }
            )
        }
        passwordResetDialog?.show()
    }

    private fun showPasswordForgetDialog() {
        if (isFinishing) return
        val b = binding ?: return

        passwordForgetDialog = MemoCustomPasswordForgetDialog(this).apply {
            setTitle(getString(R.string.haru_password_dialog_forget_title))
            setMessage(getString(R.string.haru_password_dialog_forget_message))
            val savedPassword = PreferenceUtil.get(KEY_PASSWORD, "")
            val subMessage =
                getString(R.string.haru_password_dialog_forget_sub_message, savedPassword)
            setSubMessage(subMessage)
            setBottomSubMessage(getString(R.string.haru_password_dialog_forget_bottom_sub_message))
            setButton(
                confirmText = getString(R.string.haru_password_dialog_forget_confirm),
                onConfirm = {
                    dismiss()
                }
            )
        }
        passwordForgetDialog?.show()
    }

    private fun applyPasswordSectionState(enabled: Boolean) {
        val b = binding ?: return
        val alpha = if (enabled) 1f else 0.4f

        listOf(
            b.itemChangePassword,
            b.itemForgetPassword,
            b.itemTitleForgetPassword,
        ).forEach {
            it.alpha = alpha
            it.isEnabled = enabled
            it.isClickable = enabled
        }

    }

    private fun applyMiscClickHandler(b: ActivitySetPasswordBinding) {
        b.tvSetPasswordEe.setOnClickListener {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime > 2000) {
                secretClickCount = 0
            }
            lastClickTime = now
            secretClickCount++

            if (secretClickCount >= EXTRA_TRIGGER_LIMIT) {
                secretClickCount = 0
                toastShort(this@SettingPasswordActivity, EXTRA_MSG)
            }
        }
    }

    private fun observeEventFlow() {
        lifecycleScope.launch {
            MemoEventFlow.events.collect { event ->
                LogTrack.i("SettingBackupActivity") { "observeEventFlow → $event" }
                when (event) {
                    is MemoEvent.PasswordResetEvent -> PreferenceUtil.set(KEY_SIMPLIFY, false)
                    else -> {}
                }
            }
        }
    }


}