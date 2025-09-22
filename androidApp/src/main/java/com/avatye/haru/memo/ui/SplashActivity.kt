package com.avatye.haru.memo.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.avatye.haru.memo.databinding.ActivitySplashBinding
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.RemoteConfigUtil

internal class SplashActivity : BaseActivity() {
    override val NAME: String
        get() = SplashActivity::class.java.simpleName

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.splashRootView, paddingDp = 0)
        refreshMainPopupState()
        val DELAY_MS = RemoteConfigUtil.getSplashTime() * 1000L

        Handler(Looper.getMainLooper()).postDelayed({
            val isFirstLaunch = PreferenceUtil.get(PreferenceUtil.KEY_IS_FIRST_LAUNCH, true)

//            if (isFirstLaunch) {
//                PreferenceUtil.set(PreferenceUtil.KEY_IS_FIRST_LAUNCH, false)
//                startActivity(Intent(this, TutorialActivity::class.java))
//            } else {
//                startActivity(Intent(this, MemoListActivity::class.java))
//            }

            startActivity(Intent(this, MemoListActivity::class.java))

            finish()
        }, DELAY_MS)
    }

    fun refreshMainPopupState() {
        val usePopup = RemoteConfigUtil.getMainPopupSetting().use_main_popup
        val wasUsePopup = PreferenceUtil.get(PreferenceUtil.KEY_LAST_USE_MAIN_POPUP, false)

        // 리모트 컨피그 값이 false -> true 로 바뀐 경우에만 reset
        if (!wasUsePopup && usePopup) {
            PreferenceUtil.set(PreferenceUtil.KEY_MAIN_POPUP_CLOSED, false)
        }

        // 이번 상태를 기록해둠
        PreferenceUtil.set(PreferenceUtil.KEY_LAST_USE_MAIN_POPUP, usePopup)
    }
}