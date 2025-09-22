package com.avatye.haru.memo.data.enum

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MemoMode(val value: Int) : Parcelable {
    NONE(0),
    CREATE_MEMO(1),
    MODIFY_MEMO(2),
    READ_MEMO(3),
}