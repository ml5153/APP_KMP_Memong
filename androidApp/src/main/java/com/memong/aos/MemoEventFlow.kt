package com.memong.aos

import com.memong.aos.data.entity.MemoEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MemoEventFlow {
    private val _events = MutableSharedFlow<MemoEvent>(replay = 0, extraBufferCapacity = 128)
    val events = _events.asSharedFlow()

    fun emit(event: MemoEvent) {
        _events.tryEmit(event)
    }
}
