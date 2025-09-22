package com.memong.aos.data.entity

import com.memong.aos.data.utils.BaseUtil

data class BodyRow(
    val id: String = BaseUtil.getUUID(),
    var text: String = "",
    var type: BodyType = BodyType.TEXT,
    var isChecked: Boolean = false
)
