package com.memong.aos.data.enum

import com.memong.aos.R

internal enum class ActivityTransitionType(val value: Pair<Int, Int>) {
    SLIDE_ENTER(
        Pair(
            R.anim.activity_slide_enter_enter,
            R.anim.activity_slide_enter_exit
        )
    ),
    SLIDE_EXIT(
        Pair(
            R.anim.activity_slide_exit_enter,
            R.anim.activity_slide_exit_exit
        )
    ),
    FADE_EXIT(
        Pair(
            R.anim.activity_fade_in,
            R.anim.activity_fade_out
        )
    ),
    FADE_IN(
        Pair(
            R.anim.activity_fade_in,
            R.anim.activity_fade_out
        )
    );
}