package com.memong.aos.data.entity

import android.view.View
import com.memong.aos.data.enum.TutorialFocusType

data class TutorialStep(
    val target: View,
    val title: String,
    val desc: String,
    val focus: TutorialFocusType
)
