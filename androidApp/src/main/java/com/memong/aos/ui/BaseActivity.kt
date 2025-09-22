package com.memong.aos.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.window.layout.WindowMetricsCalculator
import com.avatye.adcash.AdError
import com.avatye.adcash.BannerAdSize
import com.avatye.adcash.InterstitialAdType
import com.avatye.adcash.loader.InterstitialAdLoader
import com.avatye.adcash.view.BannerAdView
import com.avatye.haru.log.LogTrack
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.UpdateAvailability
import com.memong.aos.BuildConfig
import com.memong.aos.data.utils.BaseUtil
import com.memong.aos.data.utils.RemoteConfigUtil
import com.memong.aos.ui.custom.progress.CustomProgressView
import com.google.android.play.core.install.model.AppUpdateType as UpdateType

internal abstract class BaseActivity : AppCompatActivity() {
    abstract val NAME: String

    companion object {
        const val BASE_NAME = "BaseActivity"
        private const val UPDATE_REQUEST_CODE = 1001
        private const val TABLET_MIN_WIDTH_DP = 750 // 태블릿/대형 화면 기준
    }

    private var appUpdateManager: AppUpdateManager? = null
    private var progressView: CustomProgressView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면 크기에 따른 Orientation 설정
        applyOrientationByScreenSize()

        enableEdgeToEdge()
        LogTrack.i(tag = BASE_NAME) { "$NAME -> onCreate()" }

        checkForInAppUpdate()

        // 시스템 Back 버튼 차단 로직 추가
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (progressView != null) {
                        // ProgressView 있으면 Back 무시
                        LogTrack.i(tag = BASE_NAME) { "$NAME -> Back Pressed 무시 (ProgressView 활성화 상태)" }
                        // 아예 consume만 하고 반환
                        return
                    }
                    // 없으면 평소대로 Back 동작
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        )
    }


    protected fun requestBannerAd(placementId: String, bannerAdSize: BannerAdSize, bannerView: BannerAdView?) {
        // TODO 광고 비활성화 (추후 광고 탑재시 논의)
//        bannerView?.apply {
//            listener = object : BannerAdView.Listener {
//                override fun onLoaded() {
//                    LogTrack.i(BASE_NAME) { "$NAME -> requestBannerAd::onLoaded" }
//                    bannerView.isVisible = true
//                }
//
//                override fun onFailed(adError: AdError) {
//                    LogTrack.e(BASE_NAME) { "$NAME -> requestBannerAd::onFailed { errorCode: ${adError.errorCode}, errorMessage: ${adError.errorMessage}  }" }
//                    bannerView.isVisible = false
//                }
//
//                override fun onClicked() {
//                    LogTrack.i(BASE_NAME) { "$NAME -> requestBannerAd::onClicked" }
//                }
//            }
//            setPlacementId(placementId = placementId)
//            setBannerAdSize(size = bannerAdSize)
//
//            requestAd()
//        } ?: run {
//            LogTrack.e { "$NAME -> requestBannerAd -> bannerView is null!" }
//        }
    }


    protected fun makeInterstitialAdLoader(
        onAdLoaded: (executor: InterstitialAdLoader.InterstitialExecutor, adType: InterstitialAdType) -> Unit,
        onAdOpened: () -> Unit,
        onAdClosed: (completed: Boolean) -> Unit,
        onAdFailed: (error: AdError) -> Unit,
        onAdClicked: () -> Unit
    ): InterstitialAdLoader {
        // TODO 광고 비활성화 (추후 광고 탑재시 논의) onAdSuccess(true) 임시코드
//        showProgressView()
        return InterstitialAdLoader(
            ownerActivity = this,
            placementId = BuildConfig.ADCASH_FIND_PASSWORD_PID,
            listener = object : InterstitialAdLoader.InterstitialListener {
                override fun onLoaded(
                    executor: InterstitialAdLoader.InterstitialExecutor,
                    adType: InterstitialAdType
                ) {
                    // interstitialExecutor의 show() 함수를 통해 광고를 노출 합니다.
                    LogTrack.i(BASE_NAME) { "$NAME -> makeInterstitialAdLoader -> interstitialAdLoader::onLoaded{ adType: $adType }" }
                    hideProgressView()
                    onAdLoaded(executor, adType)
                }

                override fun onOpened() {
                    // 광고 노출 성공
                    LogTrack.i(BASE_NAME) { "$NAME -> makeInterstitialAdLoader -> interstitialAdLoader::onOpened" }
                    hideProgressView()
                    onAdOpened()
                }

                override fun onClosed(completed: Boolean) {
                    // 광고 종료
                    // completed: 리워드 광고 시청 완료 또는 일반 지면 정상 종료 여부
                    LogTrack.i(BASE_NAME) { "$NAME -> makeInterstitialAdLoader -> interstitialAdLoader::onClosed{ completed: $completed }" }
                    hideProgressView()
                    onAdClosed(completed)

                }

                override fun onFailed(error: AdError) {
                    // 광고 오류
                    LogTrack.e(BASE_NAME) { "$NAME -> makeInterstitialAdLoader -> interstitialAdLoader::onFailed{ errorCode: ${error.errorCode}, errorMessage: ${error.errorMessage} }" }
                    hideProgressView()
                    onAdFailed(error)

                }

                override fun onClicked() {
                    // 광고 클릭
                    LogTrack.i(BASE_NAME) { "$NAME -> makeInterstitialAdLoader -> interstitialAdLoader::onClicked" }
                    onAdClicked()
                }
            }
        )
    }


    protected fun showProgressView() {
        if (progressView != null) return
        progressView = CustomProgressView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isClickable = true
            isFocusable = true
        }

        // 가장 안전한 위치: Activity의 루트 뷰
        (window?.decorView as? ViewGroup)?.addView(progressView)
    }


    protected fun hideProgressView() {
        progressView?.let {
            (it.parent as? ViewGroup)?.removeView(it)
            progressView = null
        }
    }


    override fun onResume() {
        super.onResume()
        // 업데이트 도중 앱이 중단됐다가 돌아올 경우를 대비
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                val updateType = RemoteConfigUtil.getUpdateType()
                LogTrack.i(tag = NAME) { "이전 업데이트 진행 중, 재개 (타입=$updateType)" }
                val options = AppUpdateOptions.newBuilder(updateType).build()
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    this,
                    options,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // 강제 업데이트 취소 시 앱 종료
                LogTrack.w(tag = NAME) { "사용자가 강제 업데이트를 취소함. 앱 종료" }
                finishAffinity()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogTrack.i(tag = NAME) { "$BASE_NAME -> onDestroy()" }
        appUpdateManager = null
        hideProgressView()
    }

    fun configureWindowInsets(view: View, paddingDp: Int) {
        BaseUtil.setEdgeToEdge(view, paddingDp)
    }

    private fun applyOrientationByScreenSize() {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)

        val density = resources.displayMetrics.density
        val widthDp = metrics.bounds.width() / density
        val heightDp = metrics.bounds.height() / density

        LogTrack.i(tag = NAME) {
            "applyOrientationByScreenSize -> widthDp: $widthDp, heightDp: $heightDp, density: $density"
        }

        // 가로 폭(widthDp)만 기준으로 체크
        val isSmallScreen = widthDp < TABLET_MIN_WIDTH_DP

        LogTrack.i(tag = NAME) {
            "applyOrientationByScreenSize -> isSmallScreen: $isSmallScreen (TABLET_MIN_WIDTH_DP: $TABLET_MIN_WIDTH_DP)"
        }

        if (isSmallScreen) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            LogTrack.i(tag = NAME) { "applyOrientationByScreenSize -> 세로 고정 적용됨" }
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            LogTrack.i(tag = NAME) { "applyOrientationByScreenSize -> orientation 제한 없음 (Adaptive)" }
        }
    }

    private fun checkForInAppUpdate() {
        if (!RemoteConfigUtil.shouldForceUpdate()) {
            LogTrack.i(tag = NAME) { "강제 업데이트 조건에 해당하지 않음" }
            return
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            val updateType = RemoteConfigUtil.getUpdateType()

            LogTrack.i(tag = NAME) {
                """
            --- In-App Update Info ---
            availability: ${appUpdateInfo.updateAvailability()}
            availableVersionCode: ${appUpdateInfo.availableVersionCode()}
            packageName: ${appUpdateInfo.packageName()}
            installStatus: ${appUpdateInfo.installStatus()}
            immediateAllowed: ${appUpdateInfo.isUpdateTypeAllowed(UpdateType.IMMEDIATE)}
            flexibleAllowed: ${appUpdateInfo.isUpdateTypeAllowed(UpdateType.FLEXIBLE)}
            선택된 updateType: $updateType
            -------------------------
            """.trimIndent()
            }

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {
                LogTrack.i(tag = NAME) { "업데이트 실행 (타입=$updateType)" }
                val options = AppUpdateOptions.newBuilder(updateType).build()
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        this,
                        options,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    LogTrack.e(tag = NAME) { "업데이트 플로우 시작 실패: ${e.message}" }
                    finishAffinity()
                }
            } else {
                LogTrack.i(tag = NAME) { "업데이트 가능하지 않음" }
            }
        }?.addOnFailureListener { e ->
            LogTrack.e(tag = NAME) { "업데이트 정보 가져오기 실패: ${e.message}" }
        }
    }

}