package com.avatye.haru.memo.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.memo.BuildConfig
import com.avatye.haru.memo.R
import com.avatye.haru.memo.databinding.ActivityLegacyRestoreGuideBinding
import com.avatye.haru.memo.data.extension.start

internal class LegacyRestoreGuideActivity : BaseActivity() {

    override val NAME: String
        get() = LegacyRestoreGuideActivity::class.java.simpleName

    companion object {
        private const val EXTRA_CALLER = "EXTRA_CALLER"

        fun start(activity: Activity, caller: String, close: Boolean = false) {
            activity.start(
                intent = Intent(activity, LegacyRestoreGuideActivity::class.java).apply {
                    putExtra(EXTRA_CALLER, caller)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    private var binding: ActivityLegacyRestoreGuideBinding? = null
    private var currentStep = 0
    private var lastStep = 0

    private val guideImages = listOf(
        R.drawable.legacy_restore_guide_1,
        R.drawable.legacy_restore_guide_2,
        R.drawable.legacy_restore_guide_3,
        R.drawable.legacy_restore_guide_4,
        R.drawable.legacy_restore_guide_5,
        R.drawable.legacy_restore_guide_6
    )

    private val buttonTexts = listOf(
        "불러오기 방법보기",
        "다음", "다음", "다음", "다음", "닫기"
    )

    private val buttonColors = listOf(
        R.color.haru_primary_orange,
        R.color.haru_primary_gray,
        R.color.haru_primary_gray,
        R.color.haru_primary_gray,
        R.color.haru_primary_gray,
        R.color.haru_primary_orange
    )

    private lateinit var gestureDetector: GestureDetector

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = ActivityLegacyRestoreGuideBinding.inflate(layoutInflater)
        binding = b
        setContentView(b.root)
        configureWindowInsets(b.legacyRestoreGuideRootView, paddingDp = 0)

        updateStepUI()

        // 버튼 클릭
        b.buttonSingle.setOnClickListener {
            if (currentStep < guideImages.lastIndex) {
                currentStep++
                updateStepUI()
            } else {
                finish()
            }
        }

        // 제스처 감지기 초기화
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                return if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffX > 0) onSwipeRight() else onSwipeLeft()
                    true
                } else {
                    false
                }
            }
        })

        // 터치 이벤트 위임
        b.legacyRestoreGuideRootView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
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
        binding?.bottomBannerView?.onDestroy()
    }

    private fun onSwipeLeft() {
        if (currentStep < guideImages.lastIndex) {
            currentStep++
            updateStepUI()
        }
    }

    private fun onSwipeRight() {
        if (currentStep > 0) {
            currentStep--
            updateStepUI()
        }
    }

    private fun updateStepUI() {
        val b = binding ?: return

        val direction = when {
            currentStep > lastStep -> 1 // 왼쪽에서 들어옴 (다음)
            currentStep < lastStep -> -1 // 오른쪽에서 들어옴 (이전)
            else -> 0
        }

        // 이전 이미지 뷰 애니메이션 아웃
        b.imageGuide.animate()
            .translationX((-direction * b.imageGuide.width).toFloat())
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                // 이미지 변경
                b.imageGuide.setImageResource(guideImages[currentStep])

                // 다시 오른쪽에서 들어오게 애니메이션 인
                b.imageGuide.translationX = (direction * b.imageGuide.width).toFloat()
                b.imageGuide.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()

        b.buttonSingle.text = buttonTexts[currentStep]
        val color = ContextCompat.getColor(this, buttonColors[currentStep])
        b.buttonSingle.setBackgroundColor(color)

        lastStep = currentStep
    }
}
