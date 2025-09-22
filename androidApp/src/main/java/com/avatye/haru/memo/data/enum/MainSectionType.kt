package com.avatye.haru.memo.data.enum

sealed interface MainSectionType {
    val key: String
    val sectionName: String
}

enum class FixedSectionType(override val key: String, override val sectionName: String) : MainSectionType {
    SECRET("SECRET", "비밀메모"),
    IMPORTANT("IMPORTANT", "즐겨찾기")
}

enum class DynamicSectionType(override val key: String, override val sectionName: String) : MainSectionType {
    NONE("NONE", ""),
    ALL("ALL", ""),
    TODAY("TODAY","오늘"),
    YESTERDAY("YESTERDAY", "어제"),
    LAST_7_DAYS("LAST_7_DAYS", "이전 7일"),
    LAST_30_DAYS("LAST_30_DAYS", "이전 30일"),
}

data class DynamicRetroSectionType(override val key: String, override val sectionName: String) : MainSectionType
