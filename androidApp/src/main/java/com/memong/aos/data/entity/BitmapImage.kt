package com.memong.aos.data.entity

import com.memong.aos.data.enum.MemoMode

data class BitmapImage(
    val uri: String,      // Bitmap → Uri 로 교체
    val mode: MemoMode
)