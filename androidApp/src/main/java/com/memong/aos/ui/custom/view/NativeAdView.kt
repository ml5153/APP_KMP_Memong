package com.memong.aos.ui.custom.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.memong.aos.BuildConfig
import com.memong.aos.databinding.LayoutNativeAdBinding

class NativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutNativeAdBinding.inflate(LayoutInflater.from(context), this, true)
    private var nativeAd: NativeAd? = null
    private var bannerView: AdView? = null

    private var nativeAdUnitId: String? = BuildConfig.ADMOB_NATIVE_UNIT_ID
    private var bannerAdUnitId: String? = BuildConfig.ADMOB_BANNER_UNIT_ID

    var loadListener: LoadListener? = null

    interface LoadListener {
        fun onAdLoaded()
        fun onAdFailed(error: LoadAdError)
        fun onAdClicked()
        fun onAdImpression()
    }

    fun loadNativeBanner(activity: Activity) {
        val nativeId = nativeAdUnitId ?: return
        val adLoader = AdLoader.Builder(activity, nativeId)
            .forNativeAd { ad ->
                nativeAd = ad
                bindAdToView(ad)
                loadListener?.onAdLoaded()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadBanner(activity)
                }

                override fun onAdClicked() {
                    loadListener?.onAdClicked()
                }

                override fun onAdImpression() {
                    loadListener?.onAdImpression()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun bindAdToView(ad: NativeAd) {
        val adView = binding.adView

        adView.headlineView = binding.adHeadline
        adView.bodyView = binding.adBody
        adView.mediaView = binding.adMedia

        binding.adHeadline.text = ad.headline
        binding.adBody.text = ad.body

        adView.setNativeAd(ad)
    }

    private fun loadBanner(activity: Activity) {
        removeAllViews()
        bannerView = AdView(activity).apply {
            adUnitId = bannerAdUnitId ?: return
            setAdSize(AdSize.LARGE_BANNER)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    loadListener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadListener?.onAdFailed(error)
                }

                override fun onAdClicked() {
                    loadListener?.onAdClicked()
                }

                override fun onAdImpression() {
                    loadListener?.onAdImpression()
                }
            }
        }

        addView(bannerView)
        bannerView?.loadAd(AdRequest.Builder().build())
    }


    fun onDestroy() {
        nativeAd?.destroy()
        nativeAd = null
        bannerView?.destroy()
        bannerView = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onDestroy()
    }
}
