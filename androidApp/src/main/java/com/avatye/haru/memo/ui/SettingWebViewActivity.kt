package com.avatye.haru.memo.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebViewClient
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.databinding.ActivitySetWebViewBinding
import com.avatye.haru.memo.ui.custom.header.SettingHeaderView

internal class SettingWebViewActivity : BaseActivity() {

    override val NAME: String
        get() = SettingWebViewActivity::class.java.simpleName

    private var binding: ActivitySetWebViewBinding? = null

    companion object {
        private const val EXTRA_MODE = "mode"

        fun start(activity: Activity, mode: String, close: Boolean = false) {
            val intent = Intent(activity, SettingWebViewActivity::class.java).apply {
                putExtra(EXTRA_MODE, mode)
            }
            activity.start(intent = intent, flags = Intent.FLAG_ACTIVITY_CLEAR_TOP, close = close)
        }

        private val webViewContentMap = mapOf(
            R.string.haru_webview_mode_notice to WebViewContent(
                SettingHeaderView.SetHeaderMode.NOTICE,
                "https://sites.google.com/view/harumemo-notice/%ED%99%88"
            ),
            R.string.haru_webview_mode_terms to WebViewContent(
                SettingHeaderView.SetHeaderMode.TERMS,
                "https://sites.google.com/view/harumemo-terms/%ED%99%88"
            ),
            R.string.haru_webview_mode_backup to WebViewContent(
                SettingHeaderView.SetHeaderMode.GUIDE_B,
                "https://sites.google.com/view/harumemo-guide-backup/%ED%99%88"
            ),
            R.string.haru_webview_mode_faq to WebViewContent(
                SettingHeaderView.SetHeaderMode.GUIDE_F,
                "https://sites.google.com/view/harumemo-faq/%ED%99%88"
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySetWebViewBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.setWebViewRootView, paddingDp = 0)

        setupWebView()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = b.bottomBannerView
        )

        val modeKey = intent.getStringExtra(EXTRA_MODE)
        val content = webViewContentMap.entries.firstOrNull {
            getString(it.key) == modeKey
        }?.value

        b.headerSetWeb.setMode(content?.mode ?: SettingHeaderView.SetHeaderMode.DEFAULT)
        b.webView.loadUrl(content?.url ?: "about:blank")

        b.headerSetWeb.setOnBackClickListener { finish() }
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
        binding?.headerSetWeb?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val b = binding ?: return
        b.webView.settings.javaScriptEnabled = true
        b.webView.webViewClient = WebViewClient()
    }


    private data class WebViewContent(
        val mode: SettingHeaderView.SetHeaderMode,
        val url: String
    )
}
