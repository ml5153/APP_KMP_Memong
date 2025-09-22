package com.avatye.haru.memo.data.entity

import android.graphics.Bitmap
import com.avatye.haru.memo.data.enum.MemoMode

data class BitmapImage(
    val uri: String,      // Bitmap → Uri 로 교체
    val mode: MemoMode
)