package com.memong.aos.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.avatye.adcash.BannerAdSize
import com.memong.aos.BuildConfig
import com.memong.aos.R
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.databinding.ActivitySetHelpBinding
import com.memong.aos.ui.custom.header.SettingHeaderView

internal class SettingHelpActivity : BaseActivity() {

    override val NAME: String
        get() = SettingHelpActivity::class.java.simpleName

    private var binding: ActivitySetHelpBinding? = null

    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, SettingHelpActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySetHelpBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.setHelpRootView, paddingDp = 0)

        b.headerSetHelp.apply {
            setMode(SettingHeaderView.SetHeaderMode.HELP)
            setOnBackClickListener { finish() }
        }

        setHelpItemListeners(
            b.itemBackupGuide to getString(R.string.haru_help_mode_backup),
            b.itemFaq to getString(R.string.haru_help_mode_faq)
        )

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
        binding?.headerSetHelp?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
    }

    private fun setHelpItemListeners(vararg pairs: Pair<View, String>) {
        pairs.forEach { (view, mode) ->
            view.setOnClickListener {
                if (view == binding?.itemBackupGuide) {
                    EventUtil.sendEvent(
                        this@SettingHelpActivity,
                        EventUtil.CATEGORY_SET_SERVICE,
                        EventUtil.ACTION_BACKUP_GUIDE
                    )
                } else {
                    EventUtil.sendEvent(
                        this@SettingHelpActivity,
                        EventUtil.CATEGORY_SET_SERVICE,
                        EventUtil.ACTION_FAQ
                    )
                }
                SettingWebViewActivity.start(this, mode)
            }
        }
    }
}