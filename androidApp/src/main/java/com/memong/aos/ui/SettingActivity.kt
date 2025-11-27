package com.memong.aos.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.memong.aos.BuildConfig
import com.memong.aos.MemoEventFlow
import com.memong.aos.R
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.databinding.ActivitySetBinding
import com.memong.aos.ui.custom.header.SettingHeaderView
import androidx.core.net.toUri

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
            try {
                // 플레이스토어 앱 실행
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "market://details?id=com.memong.aos".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // 플레이스토어 앱이 없으면 웹 브라우저로
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=com.memong.aos".toUri()
                )
                startActivity(intent)
            }
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