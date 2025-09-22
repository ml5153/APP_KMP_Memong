package com.avatye.haru.memo

import com.avatye.haru.memo.data.entity.MemoEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MemoEventFlow {
    private val _events = MutableSharedFlow<MemoEvent>(replay = 0, extraBufferCapacity = 128)
    val events = _events.asSharedFlow()

    fun emit(event: MemoEvent) {
        _events.tryEmit(event)
    }
}
