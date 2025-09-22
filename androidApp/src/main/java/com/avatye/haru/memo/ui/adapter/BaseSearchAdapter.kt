package com.avatye.haru.memo.ui.adapter

import com.avatye.haru.memo.data.entity.MemoEntity
import com.avatye.haru.memo.data.entity.MemoSectionListItem

private lateinit var searchAdapter: BaseSearchAdapter
private var gridAdapter: MemoGridAdapter? = null
private var listAdapter: MemoListAdapter? = null

interface BaseSearchAdapter {
    fun setOnMemoClickListener(listener: (MemoEntity) -> Unit)
    fun setOnRequestPasswordCheck(listener: (MemoEntity) -> Unit)
    fun updateFlatItems(items: List<MemoSectionListItem>)
    fun updateMemoTags(tags: Map<Int, List<String>>)
}