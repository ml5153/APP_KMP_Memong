package com.avatye.haru.memo.data.entity

import android.graphics.Bitmap
import com.avatye.haru.memo.data.utils.BaseUtil

sealed class MemoLegacyBlock {
    data class DateBlock(val date: String) : MemoLegacyBlock()
    data class TitleBlock(var title: String = "") : MemoLegacyBlock()
    data class TagBlock(
        val id: String = BaseUtil.getUUID(),
        var tags: MutableList<String>? = mutableListOf(),
        val createdAt: Long = 0,
        var isChecked: Boolean = false
    ) : MemoLegacyBlock()

    data class BodyBlock(val id: String = BaseUtil.getUUID(), var body: String = "") : MemoLegacyBlock()
    data class ImageBitmapBlock(val id: String = BaseUtil.getUUID(), var bitmaps: List<Bitmap>) : MemoLegacyBlock()
}
