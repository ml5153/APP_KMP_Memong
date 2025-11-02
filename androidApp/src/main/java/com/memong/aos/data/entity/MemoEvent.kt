package com.memong.aos.data.entity

sealed class MemoEvent {
    data object AllMemoUpdated : MemoEvent()
    data object FirstMemoSaved : MemoEvent()
    data class MemoUpdated(val memoId: Int) : MemoEvent()
    data class PasswordEvent(val memoId: Int, val isLocked: Boolean) : MemoEvent()
    data object PasswordMemoDeleteEvent : MemoEvent()
    data object PasswordResetEvent : MemoEvent()
    data object EnableGoogleAccountEvent : MemoEvent()
}