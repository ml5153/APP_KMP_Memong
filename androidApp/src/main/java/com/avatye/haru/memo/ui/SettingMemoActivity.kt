package com.avatye.haru.memo.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.R
import com.avatye.haru.memo.data.extension.start
import com.avatye.haru.memo.data.utils.EventUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_CHECKED_MEMO_AUTO_SORT
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_QUICK_PAGE
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_TURN_OFF_TITLE
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_USE_LOCKSCREEN_MEMO
import com.avatye.haru.memo.data.utils.Util.Companion.toastShort
import com.avatye.haru.memo.databinding.ActivitySetMemoBinding
import com.avatye.haru.memo.service.LockScreenService
import com.avatye.haru.memo.ui.custom.dialog.MemoCustomDialog
import com.avatye.haru.memo.ui.custom.header.SettingHeaderView
import com.avatye.haru.memo.ui.custom.menu.MemoMenuView

internal class SettingMemoActivity : BaseActivity() {

    override val NAME: String
        get() = SettingMemoActivity::class.java.simpleName

    private var binding: ActivitySetMemoBinding? = null
    private var memoFontSizePopup: MemoMenuView? = null
    private var overlayPermissionDialog: MemoCustomDialog? = null
    private var notificationPermissionDialog: MemoCustomDialog? = null
    private var locationPermissionDialog: MemoCustomDialog? = null

    companion object {
        fun start(activity: Activity, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, SettingMemoActivity::class.java),
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = ActivitySetMemoBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.setMemoRootView, paddingDp = 0)

        setupHeader()
        setupFontSizeSetting()
        setupTurnOffTitleSetting()
        setupCheckMemoAutoSort()
        setUpPagingSetting()
        setupNotiMemoSetting()
        setupLockscreenMemoSetting()
        setupPointHomeSetting()

        //TODO 임시로직-지울것 (스위치 on/off 시 토스트노출)
        tempCode()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = b.bottomBannerView
        )

    }

    private fun tempCode() {
//        binding?.notiMemoSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
//            toastShort(this@SettingMemoActivity, getString(R.string.haru_developing_function))
//        }
//        binding?.pointHomeSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
//            toastShort(this@SettingMemoActivity, getString(R.string.haru_developing_function))
//        }
    }

    override fun onPause() {
        super.onPause()
        binding?.bottomBannerView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.headerSetMemo?.onDestroy()
        overlayPermissionDialog?.onDestroy()
        notificationPermissionDialog?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
        memoFontSizePopup = null
        binding = null
    }

    override fun onResume() {
        super.onResume()
        validateLockScreenMemoPermissionState()
        binding?.bottomBannerView?.onResume()
    }

    private fun setupHeader() {
        binding?.headerSetMemo?.apply {
            setMode(SettingHeaderView.SetHeaderMode.MEMO)
            setOnBackClickListener { finish() }
        }
    }

    private fun setupFontSizeSetting() {
        val b = binding ?: return

        val savedLabel = PreferenceUtil.get(
            PreferenceUtil.KEY_MEMO_FONT_SIZE,
            getString(R.string.haru_menu_font_size_m)
        )
        b.textFontSizeStatus.apply {
            text = savedLabel
            textSize = getFontSizeFromLabel(savedLabel)
        }

        memoFontSizePopup = MemoMenuView(this).apply {
            setOnItemClickListener { _, label ->
                b.textFontSizeStatus.text = label
                b.textFontSizeStatus.textSize = getFontSizeFromLabel(label)
                PreferenceUtil.set(PreferenceUtil.KEY_MEMO_FONT_SIZE, label)
            }
        }

        b.itemMDTextSize.setOnClickListener {
            if (!isFinishing) {
                memoFontSizePopup?.show(
                    anchor = b.textFontSizeStatus,
                    mode = MemoMenuView.Mode.MEMO_FONT_SIZE,
                    selectedLabel = b.textFontSizeStatus.text.toString()
                )
            }
        }
    }

    private fun setupTurnOffTitleSetting() {
        val b = binding ?: return

        b.turnOffTitleSwitch.isChecked = PreferenceUtil.get(KEY_TURN_OFF_TITLE, true)

        b.turnOffTitleSwitch.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtil.set(KEY_TURN_OFF_TITLE, isChecked)
            if (isChecked) {
                EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_TITLE_ON)
            } else {
                EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_TITLE_OFF)
            }
        }

        b.itemTurnOffTitle.setOnClickListener {
            b.turnOffTitleSwitch.toggle()
        }
    }

    private fun setupCheckMemoAutoSort() {
        val b = binding ?: return

        b.checkedMemoAutoSortSwitch.isChecked = PreferenceUtil.get(KEY_CHECKED_MEMO_AUTO_SORT, true)

        b.checkedMemoAutoSortSwitch.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtil.set(KEY_CHECKED_MEMO_AUTO_SORT, isChecked)
            if (isChecked) {
                EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_CHECKBOX_ON)
            } else {
                EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_CHECKBOX_OFF)
            }
        }

        b.itemCheckedMemoAutoSort.setOnClickListener {
            b.checkedMemoAutoSortSwitch.toggle()
        }
    }

    private fun setUpPagingSetting() {
        val b = binding ?: return

        b.linkQuickPageSwitch.isChecked = PreferenceUtil.get(KEY_QUICK_PAGE, true)

        b.linkQuickPageSwitch.setOnCheckedChangeListener { _, isChecked ->
            PreferenceUtil.set(KEY_QUICK_PAGE, isChecked)
            if (isChecked) {
                EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_PAGE_ON)
            } else {
                EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_PAGE_OFF)
            }
        }

        b.itemLinkQuickPage.setOnClickListener {
            b.linkQuickPageSwitch.toggle()
        }
    }

    private fun getFontSizeFromLabel(label: String): Float = when (label) {
        getString(R.string.haru_menu_font_size_xs) -> 12f
        getString(R.string.haru_menu_font_size_s) -> 14f
        getString(R.string.haru_menu_font_size_m) -> 16f
        getString(R.string.haru_menu_font_size_l) -> 18f
        getString(R.string.haru_menu_font_size_xl) -> 20f
        else -> 15f
    }

    private fun restoreSwitchState() {
        binding?.lsSwitch?.isChecked = false
        PreferenceUtil.set(KEY_USE_LOCKSCREEN_MEMO, false)
    }

    private fun handleLockScreenMemoSwitchToggle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            showNotificationPermissionDialog()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showLocationPermissionDialog()
            return
        }

        // 모든 권한이 허용된 경우
        binding?.lsSwitch?.isChecked = true
        PreferenceUtil.set(KEY_USE_LOCKSCREEN_MEMO, true)
        EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_LOCKSCREEN_ON)
        startLockScreenServiceIfNeeded()
    }

    private fun showNotificationPermissionDialog() {
        if (isFinishing) return
        restoreSwitchState()
        notificationPermissionDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_notification_permission_title))
            setMessage(getString(R.string.haru_notification_permission_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_go_to_settings),
                onCancel = {
                    restoreSwitchState()
                    onDestroy()
                },
                onConfirm = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                    startActivity(intent)
                    onDestroy()
                }
            )
            show()
        }
    }


    private fun showOverlayPermissionDialog() {
        if (isFinishing) return

        overlayPermissionDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_overlay_permission_title))
            setMessage(getString(R.string.haru_overlay_permission_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_go_to_settings),
                onCancel = {
                    restoreSwitchState()
                    onDestroy()
                },
                onConfirm = {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                    onDestroy()
                }
            )
            show()
        }
    }

    private fun showLocationPermissionDialog() {
        if (isFinishing) return

        locationPermissionDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_location_permission_title))
            setMessage(getString(R.string.haru_location_permission_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_go_to_settings),
                onCancel = {
                    restoreSwitchState()
                    onDestroy()
                },
                onConfirm = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                    onDestroy()
                }
            )
            show()
        }
    }


    private fun setupNotiMemoSetting() {
        val b = binding ?: return
//        b.itemNotiMemo.setOnClickListener {
//            toastShort(this@SettingMemoActivity, getString(R.string.haru_developing_function))
//        }
    }

    private fun setupLockscreenMemoSetting() {
        val b = binding ?: return

        b.lsSwitch.isChecked = PreferenceUtil.get(KEY_USE_LOCKSCREEN_MEMO, false)

        b.lsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handleLockScreenMemoSwitchToggle()
            } else {
                PreferenceUtil.set(KEY_USE_LOCKSCREEN_MEMO, false)
                EventUtil.sendEvent(this@SettingMemoActivity, EventUtil.CATEGORY_SET_MEMO, EventUtil.ACTION_SET_LOCKSCREEN_OFF)
                stopLockScreenServiceIfRunning()
            }
        }

        b.itemLSMemo.setOnClickListener {
            b.lsSwitch.toggle()
        }
    }


    private fun setupPointHomeSetting() {
//        val b = binding ?: return
//        b.itemPointHome.setOnClickListener {
//            toastShort(this@SettingMemoActivity, getString(R.string.haru_developing_function))
//        }
    }

    private fun stopLockScreenServiceIfRunning() {
        val intent = Intent(this, LockScreenService::class.java)
        stopService(intent)
    }

    private fun validateLockScreenMemoPermissionState() {
        val hasNotificationPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        val hasOverlayPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(this)
            } else {
                true
            }

        val isEnabledByUser = PreferenceUtil.get(KEY_USE_LOCKSCREEN_MEMO, false)
        val shouldBeEnabled = hasNotificationPermission && hasOverlayPermission && isEnabledByUser

        binding?.lsSwitch?.apply {
            if (isChecked != shouldBeEnabled) {
                isChecked = shouldBeEnabled
            }
        }

        // 저장은 유저의 의도만 반영
        PreferenceUtil.set(KEY_USE_LOCKSCREEN_MEMO, shouldBeEnabled)
    }

    private fun startLockScreenServiceIfNeeded() {
        if (!PreferenceUtil.get(KEY_USE_LOCKSCREEN_MEMO, false)) return

        val intent = Intent(this, LockScreenService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

}