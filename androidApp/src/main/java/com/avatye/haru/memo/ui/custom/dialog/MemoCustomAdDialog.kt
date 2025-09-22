package com.avatye.haru.memo.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.avatye.adcash.AdError
import com.avatye.adcash.BannerAdSize
import com.avatye.adcash.view.BannerAdView
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.data.utils.Util
import com.avatye.haru.memo.databinding.DialogMemoCustomAdBinding

internal class MemoCustomAdDialog(
    context: Context,
    val placementId: String,
    val bannerAdSize: BannerAdSize,
) : Dialog(context) {

    private val binding: DialogMemoCustomAdBinding =
        DialogMemoCustomAdBinding.inflate(LayoutInflater.from(context))

    private var radiusPx = Util.dpToPx(context, 12).toFloat()

    companion object {
        const val NAME = "MemogCustomAdDialog"
    }

    init {
        setContentView(binding.root)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 메시지 카드 전체 둥글게
        binding.messageContainer.setAllCornersRadius(radiusPx)

        with(binding.finishPopupBanner) {
            listener = object : BannerAdView.Listener {
                override fun onLoaded() {
                    LogTrack.i { "$NAME -> init ->  BannerAdView.Listener::onLoaded" }

                    // 광고 보임
                    binding.finishPopupBanner.isVisible = true

                    // 광고 상단만 둥글게
                    binding.finishPopupBanner.setTopCornersRadius(radiusPx)

                    // 메시지 카드 하단만 둥글게
                    binding.messageContainer.setBottomCornersRadius(radiusPx)
                }

                override fun onFailed(adError: AdError) {
                    LogTrack.e {
                        "$NAME -> init ->  BannerAdView.Listener::onFailed { errorCode: ${adError.errorCode}, errorMessage: ${adError.errorMessage}  }"
                    }

                    // 광고 숨김
                    binding.finishPopupBanner.isVisible = false

                    // 메시지 카드 전체 둥글게
                    binding.messageContainer.setAllCornersRadius(radiusPx)
                }

                override fun onClicked() {
                    LogTrack.i { "$NAME -> init ->  BannerAdView.Listener::onClicked" }
                }
            }
            setPlacementId(placementId = placementId)
            setBannerAdSize(size = bannerAdSize)
            requestAd()
        }
    }

    fun setTitleSpannable(normal: String, highlight: String, highlightColor: Int) {
        val full = normal + highlight
        val spannable = SpannableString(full).apply {
            setSpan(
                ForegroundColorSpan(Color.BLACK),
                0, normal.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(highlightColor),
                normal.length, full.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.tvTitle.text = spannable
        binding.tvTitle.visibility = if (spannable.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
        binding.tvTitle.visibility = if (title.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = if (message.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setMessageTopMarginDp(marginDp: Int) {
        val marginPx = Util.dpToPx(context, marginDp)
        binding.tvMessage.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = marginPx
        }
    }

    fun setSubMessage(sub: String) {
        binding.tvMessageSub.text = sub
        binding.tvMessageSub.visibility = if (sub.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setBottomSubMessage(sub: String) {
        binding.tvBottomSub.text = sub
        binding.tvBottomSub.visibility = if (sub.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun setButton(
        cancelText: String,
        confirmText: String,
        onCancel: (() -> Unit)? = null,
        onConfirm: (() -> Unit)? = null
    ) {
        binding.btnCancel.text = cancelText
        binding.btnConfirm.text = confirmText

        binding.btnCancel.setOnClickListener {
            binding.btnCancel.isEnabled = false
            onCancel?.invoke()
        }

        binding.btnConfirm.setOnClickListener {
            binding.btnConfirm.isEnabled = false
            onConfirm?.invoke()
        }
    }

    fun View.setTopCornersRadius(radius: Float) {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                // 안전한 방식: 전체 라운드 후 하단 마스킹
                val width = view.width
                val height = view.height
                if (width > 0 && height > 0) {
                    outline.setRoundRect(0, 0, width, height + radius.toInt(), radius)
                }
            }
        }
    }

    fun View.setBottomCornersRadius(radius: Float) {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                // 안전한 방식: 전체 라운드 후 상단 마스킹
                val width = view.width
                val height = view.height
                if (width > 0 && height > 0) {
                    outline.setRoundRect(0, -radius.toInt(), width, height, radius)
                }
            }
        }
    }

    fun View.setAllCornersRadius(radius: Float) {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val width = view.width
                val height = view.height
                if (width > 0 && height > 0) {
                    outline.setRoundRect(0, 0, width, height, radius)
                }
            }
        }
    }

    fun onPause() {
        LogTrack.i { "$NAME -> onPause" }
        binding.finishPopupBanner.onPause()
    }

    fun onResume() {
        LogTrack.i { "$NAME -> onResume" }
        binding.finishPopupBanner.onResume()
    }

    fun onDestroy() {
        LogTrack.i { "$NAME -> onDestroy" }
        binding.btnCancel.setOnClickListener(null)
        binding.btnConfirm.setOnClickListener(null)
        this@MemoCustomAdDialog.dismiss()
        binding.finishPopupBanner.onDestroy()
    }
}

