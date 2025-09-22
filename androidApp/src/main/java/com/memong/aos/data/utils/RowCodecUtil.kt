package com.memong.aos.data.utils

import com.memong.aos.data.entity.BodyRow
import com.memong.aos.data.entity.BodyType

object RowCodecUtil {
    const val HARU_MEMO_CHECKED = "{[H4RU_CHECK3D]}"
    const val HARU_MEMO_UNCHECKED = "{[H4RU_U2CHECK3D]}"

    /** BodyRow -> DB 저장용 문자열 */
    fun serialize(row: BodyRow): String = when (row.type) {
        BodyType.CHECKBOX -> (if (row.isChecked) HARU_MEMO_CHECKED else HARU_MEMO_UNCHECKED) + row.text
        BodyType.TEXT     -> row.text
    }

    /** DB 문자열 -> BodyRow */
    fun parse(raw: String): BodyRow {
        return when {
            raw.startsWith(HARU_MEMO_CHECKED) -> {
                val body = raw.substring(HARU_MEMO_CHECKED.length).trimStart() // ← 토큰 뒤 공백 제거
                BodyRow(text = body, type = BodyType.CHECKBOX, isChecked = true)
            }
            raw.startsWith(HARU_MEMO_UNCHECKED) -> {
                val body = raw.substring(HARU_MEMO_UNCHECKED.length).trimStart() // ← 토큰 뒤 공백 제거
                BodyRow(text = body, type = BodyType.CHECKBOX, isChecked = false)
            }
            else -> BodyRow(text = raw, type = BodyType.TEXT, isChecked = false)
        }
    }
}