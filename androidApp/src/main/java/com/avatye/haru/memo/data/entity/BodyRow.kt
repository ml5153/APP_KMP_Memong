package com.avatye.haru.memo.data.entity

import com.avatye.haru.memo.data.utils.BaseUtil

data class BodyRow(
    val id: String = BaseUtil.getUUID(),
    var text: String = "",
    var type: BodyType = BodyType.TEXT,
    var isChecked: Boolean = false
)
