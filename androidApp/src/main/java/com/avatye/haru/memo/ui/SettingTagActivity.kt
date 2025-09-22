package com.avatye.haru.memo.ui


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.databinding.ActivitySettingTagBinding
import com.avatye.haru.memo.ui.TagManagementActivity.TagManageMode
import com.avatye.haru.memo.ui.custom.header.SettingHeaderView

internal class SettingTagActivity : BaseActivity() {

    override val NAME: String
        get() = SettingTagActivity::class.java.simpleName

    private lateinit var binding: ActivitySettingTagBinding

    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, SettingTagActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingTagBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.setTagRootView, paddingDp = 0)

        binding.headerSetTag.apply {
            setMode(SettingHeaderView.SetHeaderMode.TAG)
            setOnBackClickListener { finish() }
        }

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )

        // 추가 및 삭제 클릭 시 TagManagementActivity 실행
        binding.itemAddTag.setOnClickListener {
            TagManagementActivity.start(this, TagManageMode.ADD)
        }

        binding.itemDeleteTag.setOnClickListener {
            TagManagementActivity.start(this, TagManageMode.DELETE)
        }
    }


    override fun onPause() {
        super.onPause()
        binding.bottomBannerView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomBannerView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.bottomBannerView.onDestroy()
    }


}
