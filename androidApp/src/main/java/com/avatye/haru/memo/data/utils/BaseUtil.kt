package com.avatye.haru.memo.data.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.avatye.haru.memo.ui.ImageDetailActivity
import java.util.UUID

internal class BaseUtil {

    companion object {
        fun setEdgeToEdge(view: View, paddingDp: Int) {
            val context = view.context
            val window = (context as? Activity)?.window ?: return

            val insetsController = WindowCompat.getInsetsController(window, view)

            // 액티비티가 ImageDetailActivity면 다크 테마 적용
            val isImageDetail = context is ImageDetailActivity
            insetsController.isAppearanceLightStatusBars = !isImageDetail
            insetsController.isAppearanceLightNavigationBars = !isImageDetail

            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val paddingPx = Util.dpToPx(view.context, paddingDp)
                val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val statusBarHeight = systemInsets.top + paddingPx
                val navBarHeight = systemInsets.bottom + paddingPx
                v.setPadding(paddingPx, statusBarHeight, paddingPx, navBarHeight)
                insets
            }
        }

        @SuppressLint("HardwareIds")
        fun getAndroidId(context: Context): String {
            try {
                return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }


        fun getUUID(): String {
            try {
                return UUID.randomUUID().toString()
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }

        fun getHexColorFromRes(context: Context, @ColorRes colorResId: Int): String {
            return try {
                val colorInt = ContextCompat.getColor(context, colorResId)
                String.format("#%06X", 0xFFFFFF and colorInt)
            } catch (e: Exception) {
                e.printStackTrace()
                "#FFFFFF" // 기본값
            }
        }

    }
}
