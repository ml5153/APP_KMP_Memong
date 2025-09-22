package com.avatye.haru.memo.data.entity

sealed class TagItem {
    data class SelectableTag(
        val id: Int,
        val tag: String,
        val createdAt: Long,
        var isChecked: Boolean = false
    ) : TagItem()

    data class AddableTag(val tag: String) : TagItem()
}
