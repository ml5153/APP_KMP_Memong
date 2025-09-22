package com.memong.aos.data.entity

import com.memong.aos.data.enum.MainSectionType

sealed class MemoSectionListItem {
    data object EmptyItem : MemoSectionListItem()

    data class SectionHeader(
        val sectionType: MainSectionType,               // key
        val title: String,                      // sectionName
        var isExpanded: Boolean = true,         // 접기/펴기 상태
        val needExpandable: Boolean = false,    // 접기/펴기 사용 여부
        val memos: List<MemoEntity>
    ): MemoSectionListItem()

    data class MemoItem(val memo: MemoEntity) : MemoSectionListItem()


}
