package com.memong.aos.data.entity

import com.memong.aos.data.utils.BaseUtil

sealed class MemoBlock {
    data class DateBlock(val date: String) : MemoBlock()
    data class BodyBlock(
        val id: String = BaseUtil.getUUID(),
        val bodyRows: MutableList<BodyRow> = mutableListOf(BodyRow())
    ) : MemoBlock()
    data class ImageUriBlock(
        val id: String = BaseUtil.getUUID(),
        var uris: MutableList<String> = mutableListOf()
    ) : MemoBlock()
}
