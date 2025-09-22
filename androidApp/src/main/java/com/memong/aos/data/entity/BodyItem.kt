package com.memong.aos.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BodyItem(
    val index: Int,
    val text: String
) : Parcelable