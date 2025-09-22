package com.memong.aos.data.utils

import android.content.Context
import com.avatye.haru.log.LogTrack
import com.google.android.play.core.install.model.AppUpdateType
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.memong.aos.BuildConfig
import com.memong.aos.R
import java.text.SimpleDateFormat
import java.util.*

data class MainPopupSetting(
    val use_main_popup: Boolean = false,
    val main_popup_target_versions: String = "",
    val main_popup_load_image: String = "",
    val main_popup_landing_page: Int = 0,
    val main_popup_landing_url: String = "",
    val main_popup_keep_date: String = "" // 추가: 노출 기간 설정
)

data class InAppUpdateSetting(
    val force_update_enabled: Boolean = false,
    val min_supported_version: Int = 0,
    val update_message: String = "",
    val update_url: String = "",
    val update_type: Int = 1 // 1 = IMMEDIATE, 2 = FLEXIBLE
)

object RemoteConfigUtil {

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    @Volatile
    private var cachedPopupSetting: MainPopupSetting? = null

    @Volatile
    private var cachedUpdateSetting: InAppUpdateSetting? = null

    fun initRemoteConfiguration(context: Context) {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                LogTrack.d("RemoteConfigUtil") { "Fetch and activate succeeded" }
                cachedPopupSetting = null
                cachedUpdateSetting = null
            } else {
                LogTrack.d("RemoteConfigUtil") { "Fetch failed" }
            }
        }
    }

    /**
     * main_popup_setting JSON 전체를 가져와서 파싱
     */
    fun getMainPopupSetting(): MainPopupSetting {
        cachedPopupSetting?.let { return it }

        val json = remoteConfig.getString("main_popup_setting")
        cachedPopupSetting = if (!json.isNullOrBlank()) {
            try {
                Gson().fromJson(json, MainPopupSetting::class.java)
            } catch (e: Exception) {
                LogTrack.e("RemoteConfigUtil") { "Failed to parse main_popup_setting: ${e.message}" }
                MainPopupSetting()
            }
        } else {
            LogTrack.w("RemoteConfigUtil") { "main_popup_setting is blank, using default." }
            MainPopupSetting()
        }
        return cachedPopupSetting!!
    }

    fun getTargetVersions(): String = getMainPopupSetting().main_popup_target_versions
    fun getHasNewNotice(): Boolean = remoteConfig.getBoolean("has_new_notice")
    fun getSplashTime(): Int = remoteConfig.getLong("splash_time").toInt().coerceAtLeast(1)
    fun getUseAi(): Boolean = remoteConfig.getBoolean("use_ai")

    /**
     * 현재 앱 버전이 main_popup_target_versions 규칙에 포함되는지 확인
     */
    fun isCurrentVersionAllowed(): Boolean {
        val currentVersion = BuildConfig.VERSION_CODE
        val rule = getTargetVersions()
        if (rule.isBlank()) return true

        return rule.split(",").any { part ->
            val trimmed = part.trim()
            if (trimmed.isEmpty()) return@any false

            if ("-" in trimmed) {
                val rangeParts = trimmed.split("-").map { it.trim() }
                if (rangeParts.size == 2) {
                    val start = rangeParts[0].toIntOrNull()
                    val end = rangeParts[1].toIntOrNull()
                    start != null && end != null && currentVersion in start..end
                } else false
            } else {
                trimmed.toIntOrNull() == currentVersion
            }
        }
    }

    /**
     * main_popup_keep_data 기간 체크
     * 형식: yyyy/MM/dd/HH:mm-yyyy/MM/dd/HH:mm
     */
    fun isWithinKeepPeriod(): Boolean {
        val period = getMainPopupSetting().main_popup_keep_date
        if (period.isBlank()) return true // 기간 미설정 → 항상 허용

        return try {
            val parts = period.split("-")
            if (parts.size != 2) return true

            val format = SimpleDateFormat("yyyy/MM/dd/HH:mm", Locale.getDefault())
            val start = format.parse(parts[0].trim())?.time ?: return true
            val end = format.parse(parts[1].trim())?.time ?: return true
            val now = System.currentTimeMillis()

            now in start..end
        } catch (e: Exception) {
            LogTrack.e("RemoteConfigUtil") { "Invalid main_popup_keep_data: $period (${e.message})" }
            true // 파싱 실패 시 안전하게 허용
        }
    }

    /**
     * 팝업 노출 여부 최종 체크
     */
    fun canShowMainPopup(): Boolean {
        val setting = getMainPopupSetting()
        return setting.use_main_popup && isCurrentVersionAllowed() && isWithinKeepPeriod()
    }


    // ----------------------
    // 인앱 업데이트 관련
    // ----------------------

    fun getInAppUpdateSetting(): InAppUpdateSetting {
        cachedUpdateSetting?.let { return it }
        val json = remoteConfig.getString("in_app_update_setting")
        cachedUpdateSetting = if (!json.isNullOrBlank()) {
            try {
                Gson().fromJson(json, InAppUpdateSetting::class.java)
            } catch (e: Exception) {
                LogTrack.e("RemoteConfigUtil") { "Failed to parse in_app_update_setting: ${e.message}" }
                InAppUpdateSetting()
            }
        } else InAppUpdateSetting()
        return cachedUpdateSetting!!
    }

    fun getUpdateType(): Int {
        return if (BuildConfig.DEBUG && BuildConfig.UPDATE_TYPE_OVERRIDE > 0) {
            // 디버그 빌드에서는 Gradle에서 지정한 타입 사용
            when (BuildConfig.UPDATE_TYPE_OVERRIDE) {
                2 -> AppUpdateType.FLEXIBLE
                else -> AppUpdateType.IMMEDIATE
            }
        } else {
            // 릴리즈 빌드에서는 Remote Config 값 사용
            when (getInAppUpdateSetting().update_type) {
                2 -> AppUpdateType.FLEXIBLE
                else -> AppUpdateType.IMMEDIATE
            }
        }
    }

    fun shouldForceUpdate(): Boolean {
        val currentVersion = BuildConfig.VERSION_CODE

        return if (BuildConfig.DEBUG) {
            // 디버그 빌드일 땐 Remote Config 무시, Gradle 값으로만 제어
            if (BuildConfig.FORCE_UPDATE_OVERRIDE) {
                currentVersion < BuildConfig.MIN_SUPPORTED_VERSION_OVERRIDE
            } else {
                false
            }
        } else {
            // 릴리즈 빌드일 땐 Remote Config 값 사용
            val setting = getInAppUpdateSetting()
            setting.force_update_enabled && currentVersion < setting.min_supported_version
        }
    }
}