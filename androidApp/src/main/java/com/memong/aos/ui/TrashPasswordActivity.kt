package com.memong.aos.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import com.avatye.adcash.BannerAdSize
import com.avatye.haru.log.LogTrack
import com.memong.aos.BuildConfig
import com.memong.aos.R
import com.memong.aos.data.extension.start
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.PreferenceUtil.KEY_PASSWORD
import com.memong.aos.databinding.ActivityTrashPasswordBinding
import com.memong.aos.ui.custom.header.SettingHeaderView

internal class TrashPasswordActivity : BaseActivity() {

    override val NAME: String
        get() = TrashPasswordActivity::class.java.simpleName

    private lateinit var binding: ActivityTrashPasswordBinding

    private val input = mutableListOf<Int>()

    companion object {
        private const val EXTRA_MODE = "mode"

        fun start(
            activity: Activity,
            mode: SettingHeaderView.SetHeaderMode,
            close: Boolean = false
        ) {
            activity.start(
                intent = Intent(activity, TrashPasswordActivity::class.java).apply {
                    putExtra(EXTRA_MODE, mode.name)
                },
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP,
                close = close
            )
        }
    }

    private fun setBackKeyListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogTrack.i(NAME) { "setBackKeyListener::handleOnBackPressed" }
                finish()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrashPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureWindowInsets(binding.trashPasswordRootView, paddingDp = 0)

        setBackKeyListener()

        binding.headerTrash.apply {
            setMode(SettingHeaderView.SetHeaderMode.TRASH)
            setOnBackClickListener { finish() }
        }

        binding.textGuide.text = getString(R.string.haru_password_input)

        // bottom Banner
        requestBannerAd(
            placementId = BuildConfig.ADCASH_BOTTOM_BANNER_PID,
            bannerAdSize = BannerAdSize.DYNAMIC,
            bannerView = binding.bottomBannerView
        )

        setupKeypad()
        updateDots()
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


    private fun setupKeypad() {
        val b = binding
        val digitButtons = listOf(
            b.btn0, b.btn1, b.btn2, b.btn3,
            b.btn4, b.btn5, b.btn6, b.btn7, b.btn8, b.btn9
        )

        digitButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (input.size < 4) {
                    input.add(index)
                    updateDots()
                    if (input.size == 4) handleInputComplete()
                }
            }
        }

        b.btnDelete.setOnClickListener {
            if (input.isNotEmpty()) {
                input.removeAt(input.lastIndex)
                updateDots()
            }
        }
    }

    private fun updateDots() {
        for (i in 0 until binding.dotIndicatorLayout.childCount) {
            val dot = binding.dotIndicatorLayout.getChildAt(i)
            val resId = if (i < input.size) R.drawable.shape_dot_w_circle else R.drawable.shape_dot_circle
            dot.setBackgroundResource(resId)
        }
    }

    private fun handleInputComplete() {
        val currentInput = input.joinToString("")
        val savedPassword = PreferenceUtil.get(KEY_PASSWORD, "")
        if (currentInput == savedPassword) {
            TrashActivity.start(this)
            finish()
        } else {
            resetInput(R.string.haru_password_not_match, shake = true)
        }
    }

    private fun resetInput(@StringRes messageResId: Int, shake: Boolean = false) {
        input.clear()
        updateDots()
        binding.textGuide.text = getString(messageResId)
        if (shake) shakeView(binding.textGuide)
    }

    private fun shakeView(view: View) {
        view.startAnimation(
            TranslateAnimation(0f, 10f, 0f, 0f).apply {
                duration = 50
                repeatMode = Animation.REVERSE
                repeatCount = 5
            }
        )
    }
}
