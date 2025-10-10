package com.memong.aos.data.enum

import com.memong.aos.MemongApplication
import com.memong.aos.R

sealed interface MainSectionType {
    val key: String
    val sectionName: String
}

enum class FixedSectionType(override val key: String) : MainSectionType {
    SECRET("SECRET"),
    IMPORTANT("IMPORTANT");

    override val sectionName: String
        get() = try {
            val ctx = MemongApplication.context()
            when (this) {
                SECRET -> ctx.getString(R.string.haru_section_layout_lock_memo)
                IMPORTANT -> ctx.getString(R.string.haru_section_layout_important_memo)
            }
        } catch (e: Exception) {
            when (this) {
                SECRET -> "잠금 메모"
                IMPORTANT -> "즐겨찾기"
            }
        }
}

enum class DynamicSectionType(override val key: String) : MainSectionType {
    NONE("NONE"),
    ALL("ALL"),
    TODAY("TODAY"),
    YESTERDAY("YESTERDAY"),
    LAST_7_DAYS("LAST_7_DAYS"),
    LAST_30_DAYS("LAST_30_DAYS");

    override val sectionName: String
        get() = try {
            val ctx = MemongApplication.context()
            when (this) {
                NONE -> ""
                ALL -> ""
                TODAY -> ctx.getString(R.string.haru_section_section_today)
                YESTERDAY -> ctx.getString(R.string.haru_section_section_yesterday)
                LAST_7_DAYS -> ctx.getString(R.string.haru_section_section_last7)
                LAST_30_DAYS -> ctx.getString(R.string.haru_section_section_last30)
            }
        } catch (e: Exception) {
            // context 초기화 전 또는 예외 발생 시 fallback (한글 기본값)
            when (this) {
                NONE -> ""
                ALL -> ""
                TODAY -> "오늘"
                YESTERDAY -> "어제"
                LAST_7_DAYS -> "이전 7일"
                LAST_30_DAYS -> "이전 30일"
            }
        }
}


data class DynamicRetroSectionType(override val key: String, override val sectionName: String) : MainSectionType
