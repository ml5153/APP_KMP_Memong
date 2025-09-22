package com.memong.aos.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.avatye.adcash.BannerAdSize
import com.memong.aos.BuildConfig
import com.memong.aos.MemoEventFlow
import com.memong.aos.R
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_GD_BACKUP_SWITCH
import com.memong.aos.databinding.ActivityGoogleBackupGuideBinding
import com.memong.aos.ui.custom.dialog.MemoCustomDialog
import kotlinx.coroutines.launch

internal class GoogleBackupGuideActivity : BaseActivity() {

    override val NAME: String
        get() = GoogleBackupGuideActivity::class.java.simpleName

    private var binding: ActivityGoogleBackupGuideBinding? = null
    private var permissionDialog: MemoCustomDialog? = null
    private var step = 1

    companion object {
        private const val EXTRA_CALLER = "EXTRA_CALLER"

        fun start(activity: Activity, caller: String, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, GoogleBackupGuideActivity::class.java).apply {
                    putExtra(EXTRA_CALLER, caller)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = ActivityGoogleBackupGuideBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.googleBackupGuideRootView, paddingDp = 0)

        b.imageGuide.setImageResource(R.drawable.gd_backup_guide1)
        b.buttonContainer.visibility = View.VISIBLE
        b.buttonSingle.visibility = View.GONE

        b.buttonLeft.setOnClickListener {
            step = 2
            b.imageGuide.setImageResource(R.drawable.gd_backup_guide2)
            b.buttonContainer.visibility = View.GONE
            b.buttonSingle.visibility = View.VISIBLE
            b.buttonSingle.text = getString(R.string.haru_backup_guide_next)
            b.buttonSingle.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.haru_primary_gray
                )
            )

            (b.imageGuide.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomToTop = b.buttonSingle.id
                bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            }
            b.imageGuide.requestLayout()
        }

        val handleBackupStart = {
            if (hasContactPermission()) {
                val caller = intent.getStringExtra(EXTRA_CALLER)
                when (caller) {
                    SettingBackupActivity::class.java.simpleName -> {
                        SettingBackupActivity.start(this, fromGoogleBackupGuide = true, forceGoogleSignIn = true)
                    }
                }
                // 구글 드라이브 백업 연동 이벤트
                lifecycleScope.launch {
                    MemoEventFlow.emit(MemoEvent.EnableGoogleAccountEvent)
                }
                finish()
            } else {
                showPermissionDialog()
            }
        }

        b.buttonRight.setOnClickListener { handleBackupStart() }

        b.buttonSingle.setOnClickListener {
            if (step == 2) {
                step = 3
                b.imageGuide.setImageResource(R.drawable.gd_backup_guide3)
                b.buttonSingle.text = getString(R.string.haru_backup_guide_start_backup)
                b.buttonSingle.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.haru_primary_orange
                    )
                )
            } else {
                handleBackupStart()
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
        permissionDialog?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
    }

    private fun hasContactPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionDialog() {
        if (isFinishing) return

        val errorColor = ContextCompat.getColor(this, R.color.haru_warning_red)

        val permissionDialog = MemoCustomDialog(this).apply {
            setTitleSpannable(
                getString(R.string.haru_backup_guide_dialog_permission_title),
                getString(R.string.haru_backup_guide_dialog_permission_required),
                errorColor
            )
            setMessage(getString(R.string.haru_backup_guide_dialog_permission_message))
            setSubMessage("")
            setBottomSubMessage("")
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_backup_guide_dialog_permission_go),
                onCancel = {
                    PreferenceUtil.set(KEY_GD_BACKUP_SWITCH, false)
                    dismiss()
                    finish()
                },
                onConfirm = {
                    dismiss()
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    })
                }
            )
        }
        permissionDialog.show()
    }
}