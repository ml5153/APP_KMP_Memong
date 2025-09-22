package com.avatye.haru.memo.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_GOOGLE_ACCOUNT_EMAIL
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_NOTICE_DISMISSED
import com.avatye.haru.memo.data.utils.RemoteConfigUtil
import com.avatye.haru.memo.data.utils.Util.Companion.toastShort
import com.avatye.haru.memo.databinding.ActivitySetServiceInfoBinding
import com.avatye.haru.memo.ui.SettingPasswordActivity
import com.avatye.haru.memo.ui.custom.header.SettingHeaderView

internal class SettingServiceInfoActivity : BaseActivity() {

    override val NAME: String
        get() = SettingServiceInfoActivity::class.java.simpleName

    private var binding: ActivitySetServiceInfoBinding? = null

    companion object {

        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, SettingServiceInfoActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySetServiceInfoBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.setServiceInfoRootView, paddingDp = 0)

        b.headerSetServiceInfo.apply {
            setMode(SettingHeaderView.SetHeaderMode.SERVICE_INFO)
            setOnBackClickListener { finish() }
        }

        // 공지사항 아이콘 표시 여부
        refreshNoticeIcon(b)


        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = b.bottomBannerView
        )

        // 공지사항 클릭 시
        b.itemNotice.setOnClickListener {
            EventUtil.sendEvent(
                this@SettingServiceInfoActivity,
                EventUtil.CATEGORY_SET_SERVICE,
                EventUtil.ACTION_NOTICE
            )
            SettingWebViewActivity.start(this, getString(R.string.haru_webview_mode_notice))

            // 클릭 후 로컬에 dismissed 처리
            PreferenceUtil.set(KEY_NOTICE_DISMISSED, true)
            b.labelNoticeIcon.visibility = View.GONE
        }

        setWebViewClickListeners(
            b.itemTerms to getString(R.string.haru_webview_mode_terms)
        )

        b.itemHelp.setOnClickListener {
            SettingHelpActivity.start(this)
        }

//        b.itemIntro.setOnClickListener {
//            toastShort(
//                this@SettingServiceInfoActivity,
//                getString(R.string.haru_developing_function)
//            )
//        }

        val version = getAppVersion(this)
        b.textVersion.text = getString(R.string.haru_setting_current_version, version)

        b.itemInquiry.setOnClickListener {
            val email = "dai852991@gmail.com"
            val subject = "[메몽 이용문의]"

            val account = PreferenceUtil.get(
                KEY_GOOGLE_ACCOUNT_EMAIL,
                getString(R.string.haru_restore_google_backup_not_account)
            )
            val model = Build.MODEL ?: "Unknown"
            val androidVersion = "Android ${Build.VERSION.RELEASE}"
            val appVersion = try {
                packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: Exception) {
                "Unknown"
            }

            val body = """
        ${"\n".repeat(5)}
        -----아래 정보는 수정하지 마십시오-----
        ACCOUNT : $account
        MODEL : $model
        Android OS ver : $androidVersion
        Memong ver : $appVersion
    """.trimIndent()

            sendInquiryEmail(this, email, subject, body)
        }
    }

    private fun refreshNoticeIcon(b: ActivitySetServiceInfoBinding) {
        val hasNewNotice = RemoteConfigUtil.getHasNewNotice()
        val dismissed = PreferenceUtil.get(KEY_NOTICE_DISMISSED, false)

        // 만약 RemoteConfig가 true인데 이전에 클릭으로 dismissed 처리했다면 보이지 않음
        // RemoteConfig 값이 false였다가 true로 바뀌면 dismissed 초기화
        if (hasNewNotice) {
            if (dismissed) {
                // 이미 눌러서 숨겨진 상태
                b.labelNoticeIcon.visibility = View.GONE
            } else {
                // 새 공지가 있고 아직 확인하지 않음
                b.labelNoticeIcon.visibility = View.VISIBLE
            }
        } else {
            // 공지가 false로 내려오면 dismissed 상태도 초기화
            if (dismissed) {
                PreferenceUtil.set(KEY_NOTICE_DISMISSED, false)
            }
            b.labelNoticeIcon.visibility = View.GONE
        }
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
        binding?.headerSetServiceInfo?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
    }

    private fun setWebViewClickListeners(vararg pairs: Pair<View, String>) {
        pairs.forEach { (view, mode) ->
            view.setOnClickListener {
                SettingWebViewActivity.start(this, mode)
            }
        }
    }

    private fun sendInquiryEmail(
        context: Context,
        email: String,
        subject: String,
        body: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri() // 이메일 앱만 필터링
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "메일 앱을 선택하세요"))
        } catch (e: ActivityNotFoundException) {
            toastShort(context, "메일 앱이 설치되어 있지 않습니다.")
        }
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pkgInfo.versionName ?: getString(R.string.haru_exam_version_name)
        } catch (e: Exception) {
            e.printStackTrace()
            getString(R.string.haru_exam_version_name)
        }
    }
}
