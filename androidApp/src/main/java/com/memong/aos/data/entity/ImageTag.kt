package com.memong.aos.data.entity

import android.graphics.Bitmap

internal data class ImageTag(
    val sessionId: String,
    val uri: String   // Bitmap → String (Uri 경로)
)
