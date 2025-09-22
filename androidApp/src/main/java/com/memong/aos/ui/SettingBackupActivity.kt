package com.memong.aos.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.view.View
import android.widget.CompoundButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.avatye.adcash.AdError
import com.avatye.adcash.BannerAdSize
import com.avatye.adcash.loader.InterstitialAdLoader
import com.avatye.haru.log.LogTrack
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.memong.aos.BuildConfig
import com.memong.aos.MemoEventFlow
import com.memong.aos.R
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.database.MemoLegacyImporter
import com.memong.aos.data.entity.MemoEvent
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.BackupEncrypt
import com.memong.aos.data.utils.EventUtil
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_GD_BACKUP_SWITCH
import com.memong.aos.data.utils.PreferenceUtil.KEY_GOOGLE_ACCOUNT_EMAIL
import com.memong.aos.data.utils.PreferenceUtil.KEY_LAST_GOOGLE_BACKUP_TIME
import com.memong.aos.data.utils.PreferenceUtil.KEY_LAST_LOCAL_BACKUP_TIME
import com.memong.aos.data.utils.Util.Companion.toastShort
import com.memong.aos.databinding.ActivitySetBackupBinding
import com.memong.aos.helper.MemoWidgetUpdater
import com.memong.aos.ui.custom.dialog.MemoCustomDialog
import com.memong.aos.ui.custom.dialog.MemoCustomRestoreDialog
import com.memong.aos.ui.custom.header.SettingHeaderView
import com.memong.aos.ui.widget.MemoWidgetMedium
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.abs
import com.google.api.services.drive.model.File as DriveFile

internal class SettingBackupActivity : BaseActivity() {

    override val NAME: String
        get() = SettingBackupActivity::class.java.simpleName

    private var binding: ActivitySetBackupBinding? = null
    private var disconnectDialog: MemoCustomDialog? = null
    private var notificationPermissionDialog: MemoCustomDialog? = null
    private var driveService: Drive? = null
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    private var interstitialAdLoader: InterstitialAdLoader? = null

    companion object {
        private const val EXTRA_FROM_GUIDE = "EXTRA_FROM_GOOGLE_BACKUP_GUIDE"
        private const val EXTRA_FORCE_SIGNIN = "EXTRA_FORCE_SIGNIN"

        fun start(
            activity: Activity,
            fromGoogleBackupGuide: Boolean = false,
            forceGoogleSignIn: Boolean = false,
            close: Boolean = false
        ) {
            activity.start(
                intent = Intent(activity, SettingBackupActivity::class.java).apply {
                    putExtra(EXTRA_FROM_GUIDE, fromGoogleBackupGuide)
                    putExtra(EXTRA_FORCE_SIGNIN, forceGoogleSignIn)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    private val gdSwitchListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            val b = binding ?: return@OnCheckedChangeListener

            if (isChecked) {
                // 스위치 UI 롤백
                b.gDBackupAccountSwitch.setOnCheckedChangeListener(null)
                b.gDBackupAccountSwitch.isChecked = false

                GoogleBackupGuideActivity.start(
                    this,
                    SettingBackupActivity::class.java.simpleName
                )
                EventUtil.sendEvent(
                    this@SettingBackupActivity,
                    EventUtil.CATEGORY_SET_BACKUP,
                    EventUtil.ACTION_SET_GOOGLE_ON
                )
                applyGoogleBackupSectionState(false)
            } else {
                showDisconnectConfirmDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            LogTrack.d("SignInResult") { "콜백 호출됨: $result" }

            if (result.data == null) {
                LogTrack.e("SignInResult") { "결과 데이터가 null입니다. 로그인 창이 제대로 종료되지 않았을 수 있음." }
                return@registerForActivityResult
            }

            if (!isNetworkAvailable(this@SettingBackupActivity)) {
                toastShort(this@SettingBackupActivity, getString(R.string.haru_check_network))
                return@registerForActivityResult
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            runCatching {
                val account = task.getResult(ApiException::class.java)
                val email =
                    account.email ?: getString(R.string.haru_restore_google_backup_not_account)

                PreferenceUtil.set(KEY_GOOGLE_ACCOUNT_EMAIL, email)
                binding?.gdAccountText?.text = email
                binding?.gdAccountText?.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.haru_primary_orange
                    )
                )
                LogTrack.d("SignInResult") { "Google Sign-In 성공: ${account.email}" }

                binding?.gDBackupAccountSwitch?.apply {
                    setOnCheckedChangeListener(null)
                    isChecked = true
                    setOnCheckedChangeListener(gdSwitchListener)
                }
                PreferenceUtil.set(KEY_GD_BACKUP_SWITCH, true)

                val credential = GoogleAccountCredential.usingOAuth2(
                    this, listOf(DriveScopes.DRIVE_FILE)
                ).apply {
                    selectedAccount = account.account
                }

                driveService = Drive.Builder(
                    NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("Memong").build()

                lifecycleScope.launch {
                    requestAd(
                        onAdSuccess = {
                            handleGoogleBackupClick()
                        },
                        onAdFailure = {

                        }
                    )
                }
            }.onFailure { e ->
                toastShort(this, "Google 계정 로그인에 실패했습니다.")
                binding?.gDBackupAccountSwitch?.apply {
                    setOnCheckedChangeListener(null)
                    isChecked = false
                    setOnCheckedChangeListener(gdSwitchListener)
                }
                PreferenceUtil.set(KEY_GD_BACKUP_SWITCH, false)
                binding?.gdAccountText?.text =
                    getString(R.string.haru_restore_google_backup_not_account)
                binding?.gdAccountText?.setTextColor("#888888".toColorInt())
                LogTrack.e("SignInResult") { "Google Sign-In 실패: ${e.message}" }
            }
        }

        val b = ActivitySetBackupBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.setBackupRootView, paddingDp = 0)
        observeEventFlow()

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = b.bottomBannerView
        )

        b.headerSetBackup.apply {
            setMode(SettingHeaderView.SetHeaderMode.BACKUP)
            setOnBackClickListener { finish() }
        }

//        b.localFreeSpace.text = getFormattedStorageInfo()
        b.gDBackupAccountSwitch.setOnCheckedChangeListener(gdSwitchListener)

        b.itemGDBackupAccount.setOnClickListener {
            b.gDBackupAccountSwitch.performClick()
        }

        setupSwitchPreferenceToggle(
            containerView = b.itemLocalPhotoBackup,
            switch = b.localPhotoBackupSwitch,
            prefKey = PreferenceUtil.KEY_PHOTO_BACKUP,
            defaultValue = true,
            context = this@SettingBackupActivity
        )

        setupSwitchPreferenceToggle(
            containerView = b.itemLocalAutoBackup,
            switch = b.localAutoBackupSwitch,
            prefKey = PreferenceUtil.KEY_AUTO_BACKUP,
            defaultValue = true,
            context = this@SettingBackupActivity
        )

        setupSwitchPreferenceToggle(
            containerView = b.itemShowBackupGuide,
            switch = b.showBackupGuideSwitch,
            prefKey = PreferenceUtil.KEY_SHOW_BACKUP_GUIDE,
            defaultValue = true,
            context = this@SettingBackupActivity
        )

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            binding?.notiBackupSwitch?.isChecked = false
            PreferenceUtil.set(PreferenceUtil.KEY_AUTO_BACKUP_NOTIFICATION, false)
        }

        setupAutoBackupNotiSwitchPreferenceToggle(
            context = this,
            containerView = b.itemNotiBackup,
            switch = b.notiBackupSwitch,
            prefKey = PreferenceUtil.KEY_AUTO_BACKUP_NOTIFICATION,
            defaultValue = false,
            onPermissionRequired = { showNotificationPermissionDialog() }
        )

        b.itemLocalBackup.setOnClickListener {
            handleLocalBackupClick()
        }

        b.itemGDBackup.setOnClickListener {
            if (PreferenceUtil.get(KEY_GD_BACKUP_SWITCH, true)) {
                requestAd(
                    onAdSuccess = {
                        handleGoogleBackupClick()
                    },
                    onAdFailure = {

                    }
                )
            }
        }

        if (PreferenceUtil.get(KEY_GD_BACKUP_SWITCH, true)) {
            if (getString(R.string.haru_restore_google_backup_not_account) ==
                PreferenceUtil.get(
                    KEY_GOOGLE_ACCOUNT_EMAIL,
                    getString(R.string.haru_restore_google_backup_not_account)
                )
            ) {
                b.gdAccountText.text = getString(R.string.haru_restore_google_backup_not_account)
                binding?.gdAccountText?.setTextColor("#888888".toColorInt())

            } else {
                b.gdAccountText.text = PreferenceUtil.get(
                    KEY_GOOGLE_ACCOUNT_EMAIL,
                    getString(R.string.haru_restore_google_backup_not_account)
                )
                binding?.gdAccountText?.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.haru_primary_orange
                    )
                )
            }
        } else {
            b.gdAccountText.text = getString(R.string.haru_restore_google_backup_not_account)
            binding?.gdAccountText?.setTextColor("#888888".toColorInt())
        }

        val lastLocalBackupTime = PreferenceUtil.get(KEY_LAST_LOCAL_BACKUP_TIME, "없음")
        if (lastLocalBackupTime.isNotBlank()) {
            b.localCurrentBackupDate.text = lastLocalBackupTime
        }

        val lastGoogleBackupTime = PreferenceUtil.get(KEY_LAST_GOOGLE_BACKUP_TIME, "없음")
        if (lastGoogleBackupTime.isNotBlank()) {
            b.gDCurrentBackupDate.text = lastGoogleBackupTime
        }

        b.itemRestore.setOnClickListener {
            // Android 11+이고 외부 저장소 권한이 없을 경우
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                showStorageAccessPermissionDialog()
                return@setOnClickListener
            }
            showRestoreSourceDialog(this)
        }
    }

    override fun onResume() {
        super.onResume()
        val b = binding ?: return
        val storedState = PreferenceUtil.get(KEY_GD_BACKUP_SWITCH, false)
        val finalEnabled = storedState && hasContactPermission()

        b.gDBackupAccountSwitch.setOnCheckedChangeListener(null)
        b.gDBackupAccountSwitch.isChecked = finalEnabled
        b.gDBackupAccountSwitch.setOnCheckedChangeListener(gdSwitchListener)

        // 플래그 감지 시 바로 Google 로그인
        if (intent.getBooleanExtra(EXTRA_FORCE_SIGNIN, false) && finalEnabled) {
            intent.removeExtra(EXTRA_FORCE_SIGNIN) // 재진입 방지
            startGoogleDriveSignIn()
        }

        b.bottomBannerView.onResume()
        interstitialAdLoader?.onResume()

        applyGoogleBackupSectionState(finalEnabled)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.headerSetBackup?.onDestroy()
        binding?.bottomBannerView?.onDestroy()
        interstitialAdLoader?.onDestroy()
        disconnectDialog?.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding?.bottomBannerView?.onPause()
        interstitialAdLoader?.onPause()
    }


    private fun hasContactPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showDisconnectConfirmDialog() {
        if (isFinishing) return

        val b = binding ?: return
        val disconnectDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_backup_dialog_disconnect_title))
            setMessage(getString(R.string.haru_backup_dialog_disconnect_message))
            setSubMessage(getString(R.string.haru_backup_dialog_disconnect_sub))
            setBottomSubMessage("")

            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_backup_dialog_disconnect_confirm),
                onCancel = {
                    dismiss()
                    b.gDBackupAccountSwitch.setOnCheckedChangeListener(null)
                    b.gDBackupAccountSwitch.isChecked = true
                    b.gDBackupAccountSwitch.setOnCheckedChangeListener(gdSwitchListener)
                    applyGoogleBackupSectionState(true)
                },
                onConfirm = {
                    dismiss()
                    PreferenceUtil.set(KEY_GD_BACKUP_SWITCH, false)
                    PreferenceUtil.set(
                        KEY_GOOGLE_ACCOUNT_EMAIL,
                        getString(R.string.haru_restore_google_backup_not_account)
                    )
                    b.gdAccountText.setText(R.string.haru_restore_google_backup_not_account)
                    binding?.gdAccountText?.setTextColor("#888888".toColorInt())
                    PreferenceUtil.set(KEY_LAST_GOOGLE_BACKUP_TIME, "없음")
                    b.gDCurrentBackupDate.text = "없음"
                    applyGoogleBackupSectionState(false)
                    EventUtil.sendEvent(
                        this@SettingBackupActivity,
                        EventUtil.CATEGORY_SET_BACKUP,
                        EventUtil.ACTION_SET_GOOGLE_OFF
                    )
                }
            )
        }
        disconnectDialog.show()
    }

    private fun applyGoogleBackupSectionState(enabled: Boolean) {
        val b = binding ?: return
        val alpha = if (enabled) 1f else 0.4f

        b.itemGDBackup.alpha = alpha
        b.itemGDCurrentBackupDate.alpha = alpha
        b.itemGDBackup.isEnabled = enabled
        b.itemGDCurrentBackupDate.isEnabled = enabled
        if (enabled) {
            b.gdAccountText.text = PreferenceUtil.get(
                KEY_GOOGLE_ACCOUNT_EMAIL,
                getString(R.string.haru_restore_google_backup_not_account)
            )
            binding?.gdAccountText?.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.haru_primary_orange
                )
            )
            b.gDCurrentBackupDate.text = PreferenceUtil.get(KEY_LAST_GOOGLE_BACKUP_TIME, "없음")
        } else {
            PreferenceUtil.set(
                KEY_GOOGLE_ACCOUNT_EMAIL,
                getString(R.string.haru_restore_google_backup_not_account)
            )
            b.gdAccountText.setText(R.string.haru_restore_google_backup_not_account)
            binding?.gdAccountText?.setTextColor("#888888".toColorInt())
            PreferenceUtil.set(KEY_LAST_GOOGLE_BACKUP_TIME, "없음")
            b.gDCurrentBackupDate.text = "없음"
        }
        b.itemGDBackupAccount.isEnabled = true
    }

    private fun getFormattedStorageInfo(): String {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        val totalGB = totalBytes.toDouble() / (1024 * 1024 * 1024)
        val availableGB = availableBytes.toDouble() / (1024 * 1024 * 1024)
        return getString(R.string.haru_backup_storage_info, availableGB, totalGB)
    }

    private fun setupSwitchPreferenceToggle(
        context: Context,
        containerView: View,
        switch: SwitchCompat,
        prefKey: String,
        defaultValue: Boolean = true
    ) {
        // 초기 상태 설정
        switch.isChecked = PreferenceUtil.get(prefKey, defaultValue)

        var isFromUser = true // 이벤트 중복 방지 플래그

        fun sendBackupEvent(newState: Boolean) {
            val action = when (containerView.id) {
                R.id.itemLocalPhotoBackup -> if (newState) {
                    EventUtil.ACTION_SET_PHOTO_BACKUP_ON
                } else {
                    EventUtil.ACTION_SET_PHOTO_BACKUP_OFF
                }

                R.id.itemLocalAutoBackup -> if (newState) {
                    EventUtil.ACTION_SET_AUTO_BACKUP_ON
                } else {
                    EventUtil.ACTION_SET_AUTO_BACKUP_OFF
                }

                R.id.itemShowBackupGuide -> if (newState) {
                    EventUtil.ACTION_SET_BACKUP_GUIDE_ON
                } else {
                    EventUtil.ACTION_SET_BACKUP_GUIDE_OFF
                }

                else -> null
            }
            action?.let { EventUtil.sendEvent(context, EventUtil.CATEGORY_SET_BACKUP, it) }
        }

        fun togglePreference(newState: Boolean) {
            if (!newState && containerView.id == R.id.itemLocalAutoBackup) {
                MemoCustomDialog(context).apply {
                    setTitle(context.getString(R.string.haru_auto_backup_disconnect_title))
                    setMessage(context.getString(R.string.haru_auto_backup_disconnect_message))
                    setSubMessage(context.getString(R.string.haru_auto_backup_disconnect_sub_message))
                    setBottomSubMessage(context.getString(R.string.haru_auto_backup_disconnect_bottom_sub_message))

                    setButton(
                        cancelText = context.getString(R.string.haru_auto_backup_disconnect_cancle),
                        confirmText = context.getString(R.string.haru_auto_backup_disconnect_confirm),
                        onCancel = {
                            dismiss()
                            isFromUser = false
                            switch.isChecked = true // 다시 켬
                            isFromUser = true
                        },
                        onConfirm = {
                            dismiss()
                            isFromUser = false
                            switch.isChecked = false
                            PreferenceUtil.set(prefKey, false)
                            sendBackupEvent(false) // 이벤트 전송
                            isFromUser = true
                        }
                    )
                    show()
                }
            } else {
                isFromUser = false
                switch.isChecked = newState
                PreferenceUtil.set(prefKey, newState)
                sendBackupEvent(newState) // 이벤트 전송
                isFromUser = true
            }
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isFromUser) togglePreference(isChecked)
        }

        containerView.setOnClickListener {
            val newState = !switch.isChecked
            togglePreference(newState)
        }
    }

    private fun setupAutoBackupNotiSwitchPreferenceToggle(
        context: Context,
        containerView: View,
        switch: SwitchCompat,
        prefKey: String,
        defaultValue: Boolean = false,
        onPermissionRequired: () -> Unit
    ) {
        // 초기 상태 설정
        switch.isChecked = PreferenceUtil.get(prefKey, defaultValue)

        fun sendBackupNotiEvent(newState: Boolean) {
            val action = if (newState) {
                EventUtil.ACTION_SET_BACKUP_NOTI_ON
            } else {
                EventUtil.ACTION_SET_BACKUP_NOTI_OFF
            }
            EventUtil.sendEvent(context, EventUtil.CATEGORY_SET_BACKUP, action)
        }

        // 스위치가 직접 눌렸을 때
        switch.setOnCheckedChangeListener { _, isChecked ->
            val hasPermission = NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (hasPermission) {
                PreferenceUtil.set(prefKey, isChecked)
                sendBackupNotiEvent(isChecked) // 이벤트 전송
            } else {
                switch.isChecked = false
                PreferenceUtil.set(prefKey, false)
                onPermissionRequired()
            }
        }

        // 전체 레이아웃 클릭 시 스위치 토글
        containerView.setOnClickListener {
            val newState = !switch.isChecked
            val hasPermission = NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (hasPermission) {
                switch.isChecked = newState
                PreferenceUtil.set(prefKey, newState)
                sendBackupNotiEvent(newState) // 이벤트 전송
            } else {
                switch.isChecked = false
                PreferenceUtil.set(prefKey, false)
                onPermissionRequired()
            }
        }
    }

    private fun handleLocalBackupClick() {
        lifecycleScope.launch {
            val memoDao = MemoDatabase.getInstance(this@SettingBackupActivity).memoDao()
            val memos = memoDao.getAllNoConditionMemos()

            if (memos.isEmpty()) {
                toastShort(this@SettingBackupActivity, getString(R.string.haru_backup_data_empty))
                return@launch
            }

            val backupDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Memong"
            )

            val performBackup: suspend () -> Unit = {
                showProgressView()

                withContext(Dispatchers.IO) {
                    try {
                        if (!backupDir.exists()) {
                            backupDir.mkdirs()
                        }

                        val timestamp =
                            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val backupFile = File(backupDir, "${timestamp}_local_memong.zip")
                        val dbFile = getDatabasePath("memopad.db")
                        val dbDir = dbFile.parentFile!!
                        val walFile = File(dbDir, "memopad.db-wal")
                        val shmFile = File(dbDir, "memopad.db-shm")

                        val filesToZip = mutableListOf<Pair<File, String>>()
                        filesToZip.add(dbFile to "memopad.db")
                        if (walFile.exists()) filesToZip.add(walFile to "memopad.db-wal")
                        if (shmFile.exists()) filesToZip.add(shmFile to "memopad.db-shm")

                        if (PreferenceUtil.get(PreferenceUtil.KEY_PHOTO_BACKUP, true)) {
                            val allImagePaths = memos.flatMap { it.imagePath.values.flatten() }

                            val resolvedImageFiles = allImagePaths.mapNotNull { path ->
                                if (path.startsWith("content://") && path.contains("/cache/")) {
                                    LogTrack.e(NAME) { "Skipping cache URI: $path" }
                                    null
                                } else {
                                    val file = if (path.startsWith("content://")) {
                                        copyContentUriToFile(this@SettingBackupActivity, path)
                                    } else {
                                        File(path).takeIf { it.exists() && it.canRead() }
                                    }
                                    file
                                }
                            }
                                .distinctBy { it.absolutePath }  // 중복 제거
                                .map { it to "images/${it.name}" } // 원본 파일명 유지

                            filesToZip.addAll(resolvedImageFiles)
                            LogTrack.d { "Images to zip: ${resolvedImageFiles.map { it.second }}" }
                        }

                        ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zipOut ->
                            filesToZip.forEach { (file, entryName) ->
                                zipOut.putNextEntry(ZipEntry(entryName))
                                file.inputStream().use { it.copyTo(zipOut) }
                                zipOut.closeEntry()
                                LogTrack.d { "Added to zip: $entryName" }
                            }
                        }
                    } catch (e: Exception) {
                        val reason = when (e) {
                            is FileNotFoundException -> getString(R.string.haru_backup_error_file_not_found)
                            is SecurityException -> getString(R.string.haru_backup_error_permission_denied)
                            is ZipException -> getString(R.string.haru_backup_error_zip)
                            is IOException -> getString(R.string.haru_backup_error_io)
                            is IllegalArgumentException -> getString(R.string.haru_backup_error_invalid_path)
                            is NullPointerException -> getString(R.string.haru_backup_error_null)
                            else -> e.localizedMessage
                                ?: getString(R.string.haru_backup_error_unknown)
                        }

                        LogTrack.e(NAME) { "백업 중 오류 -> $e" }

                        withContext(Dispatchers.Main) {
                            hideProgressView()
                            toastShort(this@SettingBackupActivity, reason)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    hideProgressView()
                    EventUtil.sendEvent(
                        this@SettingBackupActivity,
                        EventUtil.CATEGORY_SET_BACKUP,
                        EventUtil.ACTION_USE_LOCAL_BACKUP
                    )
                    toastShort(this@SettingBackupActivity, getString(R.string.haru_backup_success))

                    val now = Date()
                    val koreanFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
                    val dayOfWeek = SimpleDateFormat("E", Locale.KOREA).format(now)
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                    val formattedTime =
                        "${koreanFormat.format(now)} ($dayOfWeek) ${timeFormat.format(now)}"

                    binding?.localCurrentBackupDate?.text = formattedTime
                    PreferenceUtil.set(KEY_LAST_LOCAL_BACKUP_TIME, formattedTime)
                }
            }

            if (!backupDir.exists() || backupDir.listFiles().isNullOrEmpty()) {
                performBackup()
            } else {
                val dialog = MemoCustomDialog(this@SettingBackupActivity).apply {
                    setTitle(getString(R.string.haru_dialog_backup_data_override_title))
                    setMessage(getString(R.string.haru_dialog_backup_data_override_message))
                    setButton(
                        cancelText = (getString(R.string.haru_dialog_common_cancel)),
                        confirmText = getString(R.string.haru_dialog_backup_data_override_confirm),
                        onCancel = { dismiss() },
                        onConfirm = {
                            dismiss()
                            requestAd(
                                onAdSuccess = {
                                    lifecycleScope.launch { performBackup() }
                                },
                                onAdFailure = {
                                    toastShort(this@SettingBackupActivity, getString(R.string.haru_backup_fail_advertise_failed))
                                }
                            )

                        }
                    )
                }
                dialog.show()
            }
        }
    }


    private fun requestAd(
        onAdSuccess: (Boolean) -> Unit,
        onAdFailure: (AdError) -> Unit,
    ) {
        interstitialAdLoader = makeInterstitialAdLoader(
            onAdLoaded = { executor, adType ->
                LogTrack.i { "$NAME -> requestAd -> interstitialAdLoader::onLoaded{ adType: $adType }" }
                executor.show()
            },
            onAdOpened = {
                LogTrack.i { "$NAME -> requestAd -> interstitialAdLoader::onAdOpened" }
            },
            onAdClosed = { completed ->
                LogTrack.i { "$NAME -> requestAd -> interstitialAdLoader::onClosed{ completed: $completed }" }
                onAdSuccess(completed)
            },
            onAdFailed = { error ->
                LogTrack.e { "$NAME -> requestAd -> interstitialAdLoader::onFailed{ errorCode: ${error.errorCode}, errorMessage: ${error.errorMessage} }" }
                onAdFailure(error)
            },
            onAdClicked = {
                LogTrack.i { "$NAME -> requestAd -> interstitialAdLoader::onClicked" }
            }
        )

        // TODO 광고 비활성화 (추후 광고 탑재시 논의) onAdSuccess(true) 임시코드
        onAdSuccess(true)
//        interstitialAdLoader?.requestAd()
    }

    private fun copyContentUriToFile(context: Context, uriString: String): File? {
        return try {
            val uri = uriString.toUri()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // 파일명 추출
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: return null
            val tempFile = File(context.cacheDir, fileName)

            FileOutputStream(tempFile).use { output ->
                val copiedSize = inputStream.copyTo(output)
                LogTrack.d("Backup") { "Copied URI to temp file: $fileName ($copiedSize bytes)" }
                if (copiedSize == 0L) return null
            }

            tempFile
        } catch (e: Exception) {
            LogTrack.e { "URI 복사 실패: $e" }
            null
        }
    }

    private fun showNotificationPermissionDialog() {
        if (isFinishing) return
        restoreSwitchState()
        notificationPermissionDialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_notification_permission_title))
            setMessage(getString(R.string.haru_notification_auto__permission_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_go_to_settings),
                onCancel = {
                    restoreSwitchState()
                    onDestroy()
                },
                onConfirm = {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // API 26 이상: 앱 알림 설정 화면으로 이동
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        }
                    } else {
                        // API 25 이하: 앱 세부정보 설정 화면으로 이동
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:$packageName".toUri()
                        }
                    }
                    startActivity(intent)
                    onDestroy()
                }
            )
            show()
        }
    }

    private fun showStorageAccessPermissionDialog() {
        if (isFinishing) return
        MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_dialog_access_all_file_title))
            setMessage(getString(R.string.haru_dialog_access_all_file_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_go_to_settings),
                onCancel = {
                    onDestroy()
                },
                onConfirm = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            val intent =
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = "package:$packageName".toUri()
                                }
                            startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            startActivity(intent)
                        }
                    } else {
                        // Android 10 이하: 앱 세부정보 화면으로 이동 (직접 권한 허용 안내)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:$packageName".toUri()
                        }
                        startActivity(intent)
                    }
                    onDestroy()
                }
            )
            show()
        }
    }

    private fun restoreSwitchState() {
        binding?.notiBackupSwitch?.isChecked =
            PreferenceUtil.get(PreferenceUtil.KEY_AUTO_BACKUP_NOTIFICATION, false)
    }

    private fun observeEventFlow() {
        lifecycleScope.launch {
            MemoEventFlow.events.collect { event ->
                LogTrack.i("SettingBackupActivity") { "observeEventFlow → $event" }
                when (event) {
                    is MemoEvent.EnableGoogleAccountEvent -> {
                        if (!isNetworkAvailable(this@SettingBackupActivity)) {
                            toastShort(
                                this@SettingBackupActivity,
                                getString(R.string.haru_check_network)
                            )
                            return@collect  // 혹은 return@launch 등 상황에 따라 맞게 조정
                        }

                        // 로그인은 onResume에서 처리 → 여기선 플래그만 남김
                        PreferenceUtil.set(KEY_GD_BACKUP_SWITCH, true)

                        // 인텐트에 플래그 추가 → onResume에서 체크
                        intent.putExtra(EXTRA_FORCE_SIGNIN, true)
                    }

                    else -> Unit
                }
            }
        }
    }

    // 네트워크 연결 확인 함수
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            // API 21~22 대응용
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    fun startGoogleDriveSignIn() {
        if (!isNetworkAvailable(this@SettingBackupActivity)) {
            toastShort(this@SettingBackupActivity, getString(R.string.haru_check_network))
            return
        }

        LogTrack.d("SignInFlow") { "startGoogleDriveSignIn() 호출됨" }

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        val client = GoogleSignIn.getClient(this, signInOptions)

        // 기존 로그인 세션 제거 → 계정 선택 강제
        client.signOut().addOnCompleteListener {
            val intent = client.signInIntent
            LogTrack.d("SignInFlow") { "signInIntent 준비됨 (after signOut): $intent" }
            signInLauncher.launch(intent)
        }
    }

    private fun handleGoogleBackupClick() {
        // progressBar
        showProgressView()

        if (!isNetworkAvailable(this@SettingBackupActivity)) {
            toastShort(this@SettingBackupActivity, getString(R.string.haru_check_network))
            hideProgressView()
            return
        }

        lifecycleScope.launch {
            if (driveService == null) {
                val account = GoogleSignIn.getLastSignedInAccount(this@SettingBackupActivity)
                if (account != null) {
                    val credential = GoogleAccountCredential.usingOAuth2(
                        this@SettingBackupActivity,
                        listOf(DriveScopes.DRIVE_FILE)
                    ).apply {
                        selectedAccount = account.account
                    }

                    driveService = Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        GsonFactory.getDefaultInstance(),
                        credential
                    ).setApplicationName("Memong").build()
                } else {
                    withContext(Dispatchers.Main) {
                        toastShort(this@SettingBackupActivity, getString(R.string.haru_backup_fail_google_account_failed))
                        hideProgressView()
                    }
                    return@launch
                }
            }
            try {
                val memoDao = MemoDatabase.getInstance(this@SettingBackupActivity).memoDao()
                val memos = memoDao.getAllNoConditionMemos()

                if (memos.isEmpty()) {
                    toastShort(
                        this@SettingBackupActivity,
                        getString(R.string.haru_backup_data_empty)
                    )
                    return@launch
                }

                val timestamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val zipFile = File(cacheDir, "${timestamp}_google_memong.zip")

                val dbFile = getDatabasePath("memopad.db")
                val walFile = File(dbFile.parentFile, "memopad.db-wal")
                val shmFile = File(dbFile.parentFile, "memopad.db-shm")

                val filesToZip = mutableListOf<Pair<File, String>>().apply {
                    add(dbFile to "memopad.db")
                    if (walFile.exists()) add(walFile to "memopad.db-wal")
                    if (shmFile.exists()) add(shmFile to "memopad.db-shm")
                }

                val allImagePaths = memos.flatMap { it.imagePath.values.flatten() }
                val resolvedImages = allImagePaths.mapNotNull { path ->
                    if (path.startsWith("content://") && path.contains("/cache/")) null
                    else if (path.startsWith("content://")) copyContentUriToFile(
                        this@SettingBackupActivity,
                        path
                    )
                    else File(path).takeIf { it.exists() }
                }.distinctBy { it.absolutePath }

                filesToZip.addAll(resolvedImages.map { it to "images/${it.name}" })

                withContext(Dispatchers.IO) {
                    ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                        filesToZip.forEach { (file, name) ->
                            zipOut.putNextEntry(ZipEntry(name))
                            file.inputStream().copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }
                }

                val folderId = getOrCreateHaruMemoFolder()
                if (folderId != null) {
                    uploadFileToDriveFolder(zipFile, "application/zip", folderId)

//                    for (imageFile in resolvedImages) {
//                        val fileName = imageFile.name
//                        val alreadyExists = isFileAlreadyUploaded(fileName, folderId)
//                        if (alreadyExists) {
//                            LogTrack.d("GoogleBackup") { "이미 업로드된 이미지 건너뜀: $fileName" }
//                            continue
//                        }
//
//                        val mimeType = when {
//                            fileName.endsWith(".png") -> "image/png"
//                            fileName.endsWith(".jpg", true) || fileName.endsWith(
//                                ".jpeg",
//                                true
//                            ) -> "image/jpeg"
//
//                            else -> "application/octet-stream"
//                        }
//
//                        uploadFileToDriveFolder(imageFile, mimeType, folderId)
//                    }

                    withContext(Dispatchers.Main) {
                        EventUtil.sendEvent(
                            this@SettingBackupActivity,
                            EventUtil.CATEGORY_SET_BACKUP,
                            EventUtil.ACTION_USE_GOOGLE_BACKUP
                        )
                        toastShort(
                            this@SettingBackupActivity,
                            getString(R.string.haru_backup_success)
                        )

                        val now = Date()
                        val koreanFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
                        val dayOfWeek = SimpleDateFormat("E", Locale.KOREA).format(now)
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                        val formattedTime =
                            "${koreanFormat.format(now)} ($dayOfWeek) ${timeFormat.format(now)}"

                        binding?.gDCurrentBackupDate?.text = formattedTime
                        PreferenceUtil.set(KEY_LAST_GOOGLE_BACKUP_TIME, formattedTime)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        toastShort(this@SettingBackupActivity, "Google Drive 폴더 생성 실패")
                    }
                }
            } catch (e: Exception) {
                val reason = when (e) {
                    is FileNotFoundException -> getString(R.string.haru_backup_error_google_file_not_found)
                    is SecurityException -> getString(R.string.haru_backup_error_google_permission_denied)
                    is ZipException -> getString(R.string.haru_backup_error_google_zip)
                    is IOException -> getString(R.string.haru_backup_error_google_io)
                    is IllegalArgumentException -> getString(R.string.haru_backup_error_google_invalid_path)
                    is NullPointerException -> getString(R.string.haru_backup_error_google_null)
                    else -> e.localizedMessage
                        ?: getString(R.string.haru_backup_error_google_unknown)
                }

                LogTrack.e("GoogleBackup") { "백업 실패: $e" }

                withContext(Dispatchers.Main) {
                    hideProgressView()
                    toastShort(this@SettingBackupActivity, reason)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    hideProgressView()
                }
            }
        }
    }

    private suspend fun isFileAlreadyUploaded(fileName: String, folderId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val query = "name = '$fileName' and '$folderId' in parents and trashed = false"
                val result = driveService?.files()?.list()
                    ?.setQ(query)
                    ?.setFields("files(id)")
                    ?.execute()

                result?.files?.isNotEmpty() == true
            } catch (e: Exception) {
                LogTrack.e("GoogleBackup") { "중복 체크 실패: ${e.message}" }
                false
            }
        }
    }

    suspend fun getOrCreateHaruMemoFolder(): String? = withContext(Dispatchers.IO) {
        try {
            LogTrack.d("GoogleDrive") { "Searching for 'Memong' folder..." }

            val result = driveService?.files()?.list()
                ?.setQ("mimeType='application/vnd.google-apps.folder' and name='Memong' and trashed=false")
                ?.setSpaces("drive")
                ?.setFields("files(id, name)")
                ?.execute()

            val folder = result?.files?.firstOrNull()
            if (folder != null) {
                LogTrack.d("GoogleDrive") { "Folder found: ${folder.name}, id=${folder.id}" }
                return@withContext folder.id
            }

            LogTrack.d("GoogleDrive") { "Folder not found. Creating new one..." }

            val metadata = DriveFile().apply {
                name = "Memong" // 💡 절대 getString 사용하지 말고 하드코딩 확인
                mimeType = "application/vnd.google-apps.folder"
            }

            val created = driveService?.files()?.create(metadata)
                ?.setFields("id")
                ?.execute()

            LogTrack.d("GoogleDrive") { "Created new folder with id=${created?.id}" }

            created?.id
        } catch (e: Exception) {
            LogTrack.e("GoogleDrive") { "폴더 생성 실패: $e" }
            null
        }
    }

    suspend fun uploadFileToDriveFolder(
        file: File,
        mimeType: String,
        folderId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val metadata = DriveFile().apply {
                name = file.name
                parents = listOf(folderId)
                this.mimeType = mimeType
            }

            val content = FileContent(mimeType, file)

            val uploaded = driveService?.files()?.create(metadata, content)
                ?.setFields("id")
                ?.execute()

            LogTrack.i("GoogleDrive") { "업로드 완료: ${file.name}" }
            uploaded?.id
        } catch (e: Exception) {
            LogTrack.e("GoogleDrive") { "업로드 실패: ${file.name}, 이유: $e" }
            null
        }
    }

    private fun showRestoreSourceDialog(context: Context) {
        if (isFinishing) {
            return
        }
        lateinit var dialog: MemoCustomRestoreDialog

        dialog = MemoCustomRestoreDialog(
            context = context,
            onPhoneBackupClick = {
                // 스마트폰 백업 선택 동작 처리
                dialog.dismiss()
                showLocalRestoreConfirmationDialog()
            },
            onGoogleDriveClick = {
                if (PreferenceUtil.get(KEY_GD_BACKUP_SWITCH, false)) {
                    // 구글 드라이브 백업 선택 동작 처리
                    dialog.dismiss()
                    showGoogleRestoreConfirmationDialog()
                } else {
                    toastShort(
                        this@SettingBackupActivity,
                        getString(R.string.haru_restore_google_backup_not_account)
                    )
                }
            },
            onHowToClick = {
                LegacyRestoreGuideActivity.start(
                    activity = this@SettingBackupActivity,
                    caller = "RestoreDialog",
                    close = false
                )
            }
        )

        dialog.setTitleText(getString(R.string.haru_restore_data_select))
        dialog.setBottomSubText(
            getString(R.string.haru_restore_memog_legacy)
        )
        dialog.show()
    }

    private fun showLocalRestoreConfirmationDialog() {
        val dialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_restore_backup_folder_title))
            setMessage(getString(R.string.haru_restore_backup_folder_message))
            setSubMessage(getString(R.string.haru_restore_backup_folder_sub_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_restroe_backup_folder_restore),
                onCancel = { dismiss() },
                onConfirm = {
                    dismiss()
                    requestAd(
                        onAdSuccess = {
                            // 로컬 복구
                            restoreBackupByReplacingDb()
                        },
                        onAdFailure = {
                            toastShort(this@SettingBackupActivity, getString(R.string.haru_backup_fail_advertise_failed))
                        }
                    )

                }
            )
        }
        dialog.show()
    }

    private fun showGoogleRestoreConfirmationDialog() {
        val dialog = MemoCustomDialog(this).apply {
            setTitle(getString(R.string.haru_restore_backup_folder_title))
            setMessage(getString(R.string.haru_restore_backup_folder_message_2))
            setSubMessage(getString(R.string.haru_restore_backup_folder_sub_message))
            setButton(
                cancelText = getString(R.string.haru_dialog_common_cancel),
                confirmText = getString(R.string.haru_restroe_backup_folder_restore),
                onCancel = { dismiss() },
                onConfirm = {
                    dismiss()
                    requestAd(
                        onAdSuccess = {
                            // 구글 복구
                            restoreGoogle()
                        },
                        onAdFailure = {
                            toastShort(this@SettingBackupActivity, getString(R.string.haru_backup_fail_advertise_failed))
                        }
                    )
                }
            )
        }
        dialog.show()
    }

    @SuppressLint("HardwareIds")
    private fun restoreBackupByReplacingDb() {
        lifecycleScope.launch {
            val backupDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Memong"
            )
            val allowPhotoBackup = PreferenceUtil.get(PreferenceUtil.KEY_PHOTO_BACKUP, true)
            var legacyRestored = false

            withContext(Dispatchers.IO) {
                LogTrack.d("RestoreBackup") { "Backup base dir: ${backupDir.absolutePath}, exists=${backupDir.exists()}" }
                withContext(Dispatchers.Main) {
                    showProgressView()
                }

                backupDir.listFiles()?.forEach {
                    LogTrack.d("RestoreBackup") {
                        "Found in Memong: ${it.name}, isFile=${it.isFile}, isDirectory=${it.isDirectory}, length=${it.length()}"
                    }
                }

                val legacyDbFile = backupDir.listFiles()
                    ?.filter { file ->
                        file.isFile &&
                                (file.name == "notepad.db" || file.name.matches(Regex("^\\d+.*notepad\\.db$")))
                    }
                    ?.maxByOrNull { it.lastModified() }

                val legacyImageDir = File(backupDir, "MemoG photo")

                if (legacyDbFile == null || !legacyDbFile.exists()) {
                    LogTrack.d("RestoreBackup") {
                        "No legacy DB found in Memong (available files: ${
                            backupDir.listFiles()?.joinToString { it.name } ?: "none"
                        })"
                    }
                } else if (legacyDbFile.exists() && legacyDbFile.canRead()) {
                    try {
                        LogTrack.d("RestoreBackup") {
                            "Legacy DB selected: ${legacyDbFile.name}, path=${legacyDbFile.absolutePath}, canRead=${legacyDbFile.canRead()}"
                        }
                        LogTrack.d("RestoreBackup") {
                            "Legacy DB path: ${legacyDbFile.absolutePath}, exists=${legacyDbFile.exists()}, canRead=${legacyDbFile.canRead()}"
                        }
                        LogTrack.d("RestoreBackup") {
                            "Legacy image dir: ${legacyImageDir.absolutePath}, exists=${legacyImageDir.exists()}, files=${
                                legacyImageDir.list()?.joinToString()
                            }"
                        }

                        // 복호화 + SQLite 헤더 확인
                        val decryptedFile = File(filesDir, "temp_legacy_decrypted.db")
                        FileInputStream(legacyDbFile).use { input ->
                            FileOutputStream(decryptedFile).use { output ->
                                BackupEncrypt.decrypt(input, output)
                            }
                        }

                        val headerBytes = RandomAccessFile(decryptedFile, "r").use {
                            val header = ByteArray(16)
                            it.read(header)
                            header
                        }
                        val isValidSQLite = String(headerBytes) == "SQLite format 3\u0000"
                        LogTrack.d("RestoreBackup") {
                            "Decryption complete: ${decryptedFile.path}, isValidSQLite=$isValidSQLite"
                        }

                        if (!isValidSQLite) throw IOException(getString(R.string.haru_vaild_sqlite))

                        // 기존 DB 비우기
                        MemoDatabase.closeInstance()
                        LogTrack.d("RestoreBackup") { "Room instance closed" }

                        val db = MemoDatabase.getInstance(applicationContext)
                        val memoDao = db.memoDao()
                        val tagDao = db.tagDao()
                        val taggingDao = db.taggingDao()
                        memoDao.deleteAll()
                        tagDao.deleteAll()
                        taggingDao.deleteAll()
                        LogTrack.d("RestoreBackup") { "Cleared existing Room data" }

                        // 복원 시작
                        val legacyDb = SQLiteDatabase.openDatabase(
                            decryptedFile.absolutePath,
                            null,
                            SQLiteDatabase.OPEN_READONLY
                        )
                        val androidId =
                            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                        val importer = MemoLegacyImporter(
                            legacyDb = legacyDb,
                            memoDao = memoDao,
                            userId = androidId ?: "default"
                        )

                        importer.importAllMemos { imageName ->
                            if (!allowPhotoBackup && (!legacyImageDir.exists() || !legacyImageDir.isDirectory)) {
                                return@importAllMemos null
                            }

                            val imageFile = File(legacyImageDir, imageName)
                            if (imageFile.exists()) {
                                val destFile = File(filesDir.resolve("images"), imageName)
                                imageFile.copyTo(destFile, overwrite = true)
                                val contentUri = FileProvider.getUriForFile(
                                    this@SettingBackupActivity,
                                    "${packageName}.fileprovider",
                                    destFile
                                )
                                LogTrack.d("RestoreBackup") {
                                    "Legacy image copied: $imageName, uri=$contentUri"
                                }
                                contentUri.toString()
                            } else {
                                LogTrack.d("RestoreBackup") { "Legacy image not found: $imageName" }
                                null
                            }
                        }

                        legacyDb.close()
                        LogTrack.d("RestoreBackup") { "Legacy DB import complete" }
                        legacyRestored = true

                        // imagePath URI remapping
                        val memosWithImages =
                            memoDao.getAllNoConditionMemos().filter { it.imagePath.isNotEmpty() }
                        for (memo in memosWithImages) {
                            val newPathMap = mutableMapOf<Int, List<String>>()
                            memo.imagePath.forEach { (key, list) ->
                                val newUris = list.mapNotNull { path ->
                                    val fileName =
                                        path.toUri().lastPathSegment ?: return@mapNotNull null
                                    val file = File(filesDir.resolve("images"), fileName)
                                    if (file.exists()) {
                                        FileProvider.getUriForFile(
                                            this@SettingBackupActivity,
                                            "${packageName}.fileprovider",
                                            file
                                        ).toString()
                                    } else null
                                }
                                if (newUris.isNotEmpty()) newPathMap[key] = newUris
                            }
                            if (newPathMap.isNotEmpty()) {
                                memoDao.updateImagePath(memo.uuid, newPathMap)
                            }
                        }

                        if (!allowPhotoBackup && !legacyImageDir.exists()) {
                            val allMemos = memoDao.getAllNoConditionMemos()
                            for (memo in allMemos) {
                                if (memo.imagePath.isNotEmpty()) {
                                    memoDao.updateImagePath(memo.uuid, emptyMap())
                                    LogTrack.d("RestoreBackup") {
                                        "Cleared imagePath for memo=${memo.uuid} due to no image folder & photo backup off"
                                    }
                                }
                            }
                        }

                        legacyDbFile.delete().also {
                            LogTrack.d("RestoreBackup") { "Original DB deleted=$it" }
                        }
                        decryptedFile.delete().also {
                            LogTrack.d("RestoreBackup") { "Decrypted DB deleted=$it" }
                        }

                        if (legacyRestored && legacyImageDir.exists()) {
                            val imagesDeleted = legacyImageDir.deleteRecursively()
                            LogTrack.d("RestoreBackup") { "Legacy image folder deleted: $imagesDeleted" }
                        }

                        withContext(Dispatchers.Main) {
                            hideProgressView()
                            EventUtil.sendEvent(
                                this@SettingBackupActivity,
                                EventUtil.CATEGORY_SET_BACKUP,
                                EventUtil.ACTION_USE_LEGACY_RESTORE
                            )
                            toastShort(
                                this@SettingBackupActivity,
                                getString(R.string.haru_restore_legacy_data)
                            )
                            MemoListActivity.startRestore(
                                activity = this@SettingBackupActivity,
                                close = true
                            )
                            refreshMemoWidgets(this@SettingBackupActivity)
                        }

                        return@withContext

                    } catch (e: Exception) {
                        val reason = when (e) {
                            is FileNotFoundException -> getString(R.string.haru_restore_error_file_not_found)
                            is SecurityException -> getString(R.string.haru_restore_error_permission_denied)
                            is ZipException -> getString(R.string.haru_restore_error_zip)
                            is IOException -> getString(R.string.haru_restore_error_io)
                            is IllegalArgumentException -> getString(R.string.haru_restore_error_invalid_path)
                            is NullPointerException -> getString(R.string.haru_restore_error_null)
                            else -> e.localizedMessage
                                ?: getString(R.string.haru_restore_error_unknown)
                        }

                        LogTrack.e("RestoreBackup") { "복원 중 오류 -> $e" }

                        withContext(Dispatchers.Main) {
                            hideProgressView()
                            toastShort(this@SettingBackupActivity, "복원 오류: $reason")
                        }
                        return@withContext
                    }
                }

                // ZIP 백업 복원 fallback
                if (!legacyRestored) {
                    val backupFile = findClosestBackupFile() ?: run {
                        withContext(Dispatchers.Main) {
                            hideProgressView()
                            toastShort(
                                this@SettingBackupActivity,
                                getString(R.string.haru_not_using_backup_folder)
                            )
                        }
                        LogTrack.d("RestoreBackup") { "No backup file found" }
                        return@withContext
                    }

                    LogTrack.d("RestoreBackup") { "Selected backup file: ${backupFile.name}" }

                    try {
                        val unzipDir = File(cacheDir, "restore_temp").apply {
                            deleteRecursively()
                            mkdirs()
                        }

                        ZipInputStream(FileInputStream(backupFile)).use { zip ->
                            var entry = zip.nextEntry
                            while (entry != null) {
                                val outFile = File(unzipDir, entry.name)
                                if (entry.isDirectory) outFile.mkdirs()
                                else {
                                    outFile.parentFile?.mkdirs()
                                    FileOutputStream(outFile).use { zip.copyTo(it) }
                                }
                                LogTrack.d("RestoreBackup") { "Extracted: ${entry.name}" }
                                zip.closeEntry()
                                entry = zip.nextEntry
                            }
                        }

                        MemoDatabase.closeInstance()
                        LogTrack.d("RestoreBackup") { "Room instance closed" }

                        val dbDir = getDatabasePath("memopad.db").parentFile!!
                        File(unzipDir, "memopad.db").copyTo(
                            File(dbDir, "memopad.db"),
                            overwrite = true
                        )
                        File(unzipDir, "memopad.db-wal").takeIf { it.exists() }
                            ?.copyTo(File(dbDir, "memopad.db-wal"), overwrite = true)
                        File(unzipDir, "memopad.db-shm").takeIf { it.exists() }
                            ?.copyTo(File(dbDir, "memopad.db-shm"), overwrite = true)

                        val imagesDirInZip = unzipDir.resolve("images")
                        val hasImages = imagesDirInZip.exists() && imagesDirInZip.isDirectory

                        if (allowPhotoBackup || (!allowPhotoBackup && hasImages)) {
                            imagesDirInZip.listFiles()?.forEach { file ->
                                val dest = File(filesDir.resolve("images"), file.name)
                                file.copyTo(dest, overwrite = true)
                                LogTrack.d("RestoreBackup") { "Image copied: ${file.name}" }
                            }
                        }

                        val db = MemoDatabase.getInstance(applicationContext)
                        val memoDao = db.memoDao()

                        val memos = memoDao.getAllNoConditionMemos()
                        if (allowPhotoBackup || (!allowPhotoBackup && hasImages)) {
                            val memosWithImages = memos.filter { it.imagePath.isNotEmpty() }
                            for (memo in memosWithImages) {
                                val newPathMap = mutableMapOf<Int, List<String>>()
                                memo.imagePath.forEach { (key, list) ->
                                    val newUris = list.mapNotNull { path ->
                                        val fileName = path.substringAfterLast('/')
                                        val file = File(filesDir.resolve("images"), fileName)
                                        if (file.exists()) {
                                            FileProvider.getUriForFile(
                                                this@SettingBackupActivity,
                                                "${packageName}.fileprovider",
                                                file
                                            ).toString()
                                        } else {
                                            LogTrack.d("RestoreBackup") { "[URI Remap] Missing file for $fileName" }
                                            null
                                        }
                                    }
                                    if (newUris.isNotEmpty()) {
                                        newPathMap[key] = newUris
                                    }
                                }

                                if (newPathMap.isNotEmpty()) {
                                    memoDao.updateImagePath(memo.uuid, newPathMap)
                                    LogTrack.d("RestoreBackup") {
                                        "Updated imagePath for memo=${memo.uuid}, newPathMap=$newPathMap"
                                    }
                                }
                            }
                        } else {
                            // allowPhotoBackup == false && no image folder → imagePath 강제로 비움
                            for (memo in memos) {
                                if (memo.imagePath.isNotEmpty()) {
                                    memoDao.updateImagePath(memo.uuid, emptyMap())
                                    LogTrack.d("RestoreBackup") {
                                        "Cleared imagePath for memo=${memo.uuid} due to no image folder & photo backup off"
                                    }
                                }
                            }
                        }

                        val lockEnabled =
                            PreferenceUtil.get(PreferenceUtil.KEY_PASSWORD_SWITCH, false)
                        if (!lockEnabled) {
                            memoDao.updateAllLockState(isLocked = false)
                            LogTrack.d("RestoreBackup") {
                                "All memo isLocked fields set to false due to lock preference = false"
                            }
                        }

                        withContext(Dispatchers.Main) {
                            hideProgressView()
                            EventUtil.sendEvent(
                                this@SettingBackupActivity,
                                EventUtil.CATEGORY_SET_BACKUP,
                                EventUtil.ACTION_USE_LOCAL_RESTORE
                            )
                            toastShort(
                                this@SettingBackupActivity,
                                getString(R.string.haru_restroe_success)
                            )
                            MemoListActivity.startRestore(
                                activity = this@SettingBackupActivity,
                                close = true
                            )
                            refreshMemoWidgets(this@SettingBackupActivity)
                        }

                    } catch (e: Exception) {
                        val reason = when (e) {
                            is FileNotFoundException -> getString(R.string.haru_restore_error_file_not_found)
                            is SecurityException -> getString(R.string.haru_restore_error_permission_denied)
                            is ZipException -> getString(R.string.haru_restore_error_zip)
                            is IOException -> getString(R.string.haru_restore_error_io)
                            is IllegalArgumentException -> getString(R.string.haru_restore_error_invalid_path)
                            is NullPointerException -> getString(R.string.haru_restore_error_null)
                            else -> e.localizedMessage
                                ?: getString(R.string.haru_restore_error_unknown)
                        }

                        LogTrack.e("RestoreBackup") { "복원 중 오류 -> $e" }

                        withContext(Dispatchers.Main) {
                            hideProgressView()
                            toastShort(this@SettingBackupActivity, "복원 오류: $reason")
                        }
                    }
                }
            }
        }
    }

    private fun findClosestBackupFile(): File? {
        val backupDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Memong"
        )
        if (!backupDir.exists() || !backupDir.isDirectory) {
            LogTrack.d(
                "RestoreBackup"
            ) { "Backup folder does not exist: ${backupDir.absolutePath}" }
            return null
        }

        val now = System.currentTimeMillis()
        val backupFiles = backupDir.listFiles()
            ?.filter { it.name.endsWith("memong.zip") }
            ?.mapNotNull { file ->
                val timestamp = file.name.substringBeforeLast("_").let {
                    try {
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).parse(it)?.time
                    } catch (_: Exception) {
                        null
                    }
                }
                timestamp?.let { ts -> ts to file }
            }
            ?.sortedBy { abs(it.first - now) }
            ?: return null

        val selected = backupFiles.firstOrNull()?.second
        LogTrack.d(
            "RestoreBackup"
        ) { "Found ${backupFiles.size} backup file(s), selected: ${selected?.name}" }
        return selected
    }

    private fun restoreGoogle() {
        lifecycleScope.launch {

            if (!isNetworkAvailable(this@SettingBackupActivity)) {
                withContext(Dispatchers.Main) {
                    toastShort(this@SettingBackupActivity, getString(R.string.haru_check_network))
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                showProgressView()
            }

            try {
                // 인증 초기화
                ensureDriveServiceInitialized() // suspend 함수라 launch 블록 안에서 바로 호출 가능

                // 0. HaruMemo 폴더 ID 가져오기
                val folderId = withContext(Dispatchers.IO) {
                    getOrCreateHaruMemoFolder()
                }

                if (folderId == null) {
                    withContext(Dispatchers.Main) {
                        toastShort(this@SettingBackupActivity, "Google Drive 폴더를 찾을 수 없어요.")
                        hideProgressView()
                    }
                    return@launch
                }

                // 1. 폴더 안의 {timestamp}_google_haru_memo.zip 형태만 필터링
                // 1. 폴더 안의 백업 zip 파일 목록 가져오기 (IO 컨텍스트로 이동)
                val files = withContext(Dispatchers.IO) {
                    driveService?.files()?.list()
                        ?.setQ("'$folderId' in parents and name contains '_google_memong.zip' and trashed = false")
                        ?.setFields("files(id, name, createdTime)")
                        ?.execute()?.files
                }

                // 2. 최신 zip 파일 선택
                val zipFileMeta = files
                    ?.filter { it.name?.endsWith("_google_memong.zip") == true }
                    ?.maxByOrNull { it.createdTime.value }

                if (zipFileMeta == null) {
                    withContext(Dispatchers.Main) {
                        toastShort(this@SettingBackupActivity, "백업 파일을 찾을 수 없어요.")
                        hideProgressView()
                    }
                    return@launch
                }

                // 2. zip 파일 다운로드
                val tempZipFile = File(cacheDir, zipFileMeta.name)
                withContext(Dispatchers.IO) {
                    driveService?.files()?.get(zipFileMeta.id)
                        ?.executeMediaAndDownloadTo(FileOutputStream(tempZipFile))
                }

                LogTrack.d("RestoreGoogle") { "Downloaded zip: ${tempZipFile.absolutePath}" }

                // 3. 압축 해제
                val unzipDir = File(cacheDir, "restore_google_temp").apply {
                    deleteRecursively()
                    mkdirs()
                }

                ZipInputStream(FileInputStream(tempZipFile)).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val outFile = File(unzipDir, entry.name)
                        if (entry.isDirectory) outFile.mkdirs()
                        else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { zip.copyTo(it) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }

                // 4. Room DB 교체
                MemoDatabase.closeInstance()
                val dbDir = getDatabasePath("memopad.db").parentFile!!
                File(unzipDir, "memopad.db").copyTo(File(dbDir, "memopad.db"), overwrite = true)
                File(unzipDir, "memopad.db-wal").takeIf { it.exists() }
                    ?.copyTo(File(dbDir, "memopad.db-wal"), overwrite = true)
                File(unzipDir, "memopad.db-shm").takeIf { it.exists() }
                    ?.copyTo(File(dbDir, "memopad.db-shm"), overwrite = true)

                // 5. 이미지 복사
                val imageDir = unzipDir.resolve("images")
                val imageFiles = imageDir.listFiles()?.toList() ?: emptyList()
                for (file in imageFiles) {
                    val dest = File(filesDir.resolve("images"), file.name)
                    file.copyTo(dest, overwrite = true)
                }

                // 6. imagePath URI 재맵핑
                withContext(Dispatchers.IO) {
                    val memoDao = MemoDatabase.getInstance(applicationContext).memoDao()

                    // isLocked 상태 초기화 조건
                    val lockEnabled = PreferenceUtil.get(PreferenceUtil.KEY_PASSWORD_SWITCH, false)
                    if (!lockEnabled) {
                        memoDao.updateAllLockState(isLocked = false)
                        LogTrack.d("RestoreBackup") {
                            "All memo isLocked fields set to false due to lock preference = false"
                        }
                    }

                    val memos = memoDao.getAllNoConditionMemos()
                    for (memo in memos) {
                        if (memo.imagePath.isNotEmpty()) {
                            val newPathMap = mutableMapOf<Int, List<String>>()
                            memo.imagePath.forEach { (key, list) ->
                                val newUris = list.mapNotNull { path ->
                                    val fileName = path.substringAfterLast('/')
                                    val file = File(filesDir.resolve("images"), fileName)
                                    if (file.exists()) {
                                        FileProvider.getUriForFile(
                                            this@SettingBackupActivity,
                                            "$packageName.fileprovider",
                                            file
                                        ).toString()
                                    } else null
                                }
                                if (newUris.isNotEmpty()) newPathMap[key] = newUris
                            }
                            if (newPathMap.isNotEmpty()) {
                                memoDao.updateImagePath(memo.uuid, newPathMap)
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    hideProgressView()
                    EventUtil.sendEvent(
                        this@SettingBackupActivity,
                        EventUtil.CATEGORY_SET_BACKUP,
                        EventUtil.ACTION_USE_GOOGLE_RESTORE
                    )
                    toastShort(this@SettingBackupActivity, getString(R.string.haru_restroe_success))
                    MemoListActivity.startRestore(this@SettingBackupActivity, close = true)
                    refreshMemoWidgets(this@SettingBackupActivity)
                }
            } catch (e: Exception) {
                val reason = when (e) {
                    is FileNotFoundException -> getString(R.string.haru_restore_error_google_file_not_found)
                    is SecurityException -> getString(R.string.haru_restore_error_google_permission_denied)
                    is ZipException -> getString(R.string.haru_restore_error_google_zip)
                    is IOException -> getString(R.string.haru_restore_error_google_io)
                    is IllegalArgumentException -> getString(R.string.haru_restore_error_google_invalid_path)
                    is NullPointerException -> getString(R.string.haru_restore_error_google_null)
                    else -> e.localizedMessage
                        ?: getString(R.string.haru_restore_error_google_unknown)
                }

                LogTrack.e("RestoreGoogle") { "복원 실패: $e" }

                withContext(Dispatchers.Main) {
                    hideProgressView()
                    toastShort(this@SettingBackupActivity, "복원 오류: $reason")
                }
            }
        }
    }

    private suspend fun ensureDriveServiceInitialized() = withContext(Dispatchers.IO) {
        if (driveService != null) return@withContext

        val account = GoogleSignIn.getLastSignedInAccount(this@SettingBackupActivity)
        if (account != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                this@SettingBackupActivity,
                listOf(DriveScopes.DRIVE_FILE)
            ).apply {
                selectedAccount = account.account
            }

            driveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("Memong").build()
        } else {
            throw IllegalStateException("Google 계정이 연결되어 있지 않아요.")
        }
    }

    fun refreshMemoWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, MemoWidgetMedium::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(component)

        // 위젯 데이터 새로고침
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widgetMemoList)

        // 강제 업데이트 브로드캐스트
        val updateIntent = Intent(context, MemoWidgetMedium::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        }
        context.sendBroadcast(updateIntent)

        // DB 옵저버 재시작
        MemoWidgetUpdater().observeMemoChanges(context.applicationContext)
    }
}