package com.avatye.haru.memo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.databinding.ActivitySetHelpBinding
import com.avatye.haru.memo.ui.SettingServiceInfoActivity
import com.avatye.haru.memo.ui.custom.header.SettingHeaderView

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