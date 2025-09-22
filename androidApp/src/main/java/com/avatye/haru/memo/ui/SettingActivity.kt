package com.avatye.haru.memo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.MemoEventFlow
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.entity.MemoEvent
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.data.utils.Util.Companion.toastShort
import com.avatye.haru.memo.databinding.ActivitySetBinding
import com.avatye.haru.memo.ui.custom.header.SettingHeaderView

internal class SettingActivity : BaseActivity() {

    override val NAME: String
        get() = SettingActivity::class.java.simpleName

    private var binding: ActivitySetBinding? = null

    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, SettingActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    private fun setBackKeyListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogTrack.i(NAME) { "setBackKeyListener::handleOnBackPressed }" }
                MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                finish()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = ActivitySetBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.setRootView, paddingDp = 0)
        setBackKeyListener()

        b.headerSet.apply {
            setMode(SettingHeaderView.SetHeaderMode.DEFAULT)
            setOnBackClickListener {
                MemoEventFlow.emit(MemoEvent.AllMemoUpdated)
                finish()
            }
        }

        // 클릭 리스너 일관된 방식 적용
        b.itemSetMemo.setOnClickListener { SettingMemoActivity.start(this) }
        b.itemSetBackup.setOnClickListener { SettingBackupActivity.start(this) }
        b.itemSetPassword.setOnClickListener { SettingPasswordActivity.start(this) }
//        b.itemSetTag.setOnClickListener { SettingTagActivity.start(this) }
        b.itemSetServiceInfo.setOnClickListener { SettingServiceInfoActivity.start(this) }
        b.itemReview.setOnClickListener {
            toastShort(this@SettingActivity, getString(R.string.haru_developing_function))
        }

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = b.bottomBannerView
        )
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
        binding?.bottomBannerView?.onDestroy()
        binding?.headerSet?.onDestroy()
    }
}