package com.memong.aos.data.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object EventUtil {

    // ===== 카테고리 =====
    const val CATEGORY_STATISTICS = "statistics"
    const val CATEGORY_MAIN = "main"
    const val CATEGORY_DETAIL = "detail"
    const val CATEGORY_SEARCH = "search"
    const val CATEGORY_TRASH = "trash"
    const val CATEGORY_SET_MEMO = "set_memo"
    const val CATEGORY_SET_BACKUP = "set_backup"
    const val CATEGORY_SET_PASSWORD = "set_password"
    const val CATEGORY_SET_SERVICE = "set_service"
    const val CATEGORY_WIDGET = "widget"
    const val CATEGORY_LOCKSCREEN = "lockscreen"

    // ===== 액션 =====

    // 지표
    const val ACTION_DAU = "dau"
    const val ACTION_WAU = "wau"
    const val ACTION_MAU = "mau"

    // 메인 + 메모 상세
    const val ACTION_MODE_DATE = "mode_date"
    const val ACTION_MODE_TAG = "mode_tag"
    const val ACTION_MODE_GRID = "mode_grid"
    const val ACTION_MODE_LIST = "mode_list"
    const val ACTION_SORT_CREATE_LATEST = "sort_create_latest"
    const val ACTION_SORT_CREATE_OLDEST = "sort_create_oldest"
    const val ACTION_SORT_MODIFY_LATEST = "sort_modify_latest"
    const val ACTION_SORT_MODIFY_OLDEST = "sort_modify_oldest"
    const val ACTION_SORT_CUSTOM = "sort_custom"
    const val ACTION_SHARE_MEMO = "share_memo"
    const val ACTION_COPY_MEMO = "copy_memo"
    const val ACTION_DELETE_MEMO = "delete_memo"
    const val ACTION_TOOL_DRAW = "tool_draw"
    const val ACTION_TOOL_CHECKBOX = "tool_checkbox"
    const val ACTION_TOOL_BULLET = "tool_bullet"
    const val ACTION_TOOL_GALLERY = "tool_gallery"
    const val ACTION_TOOL_CAMERA = "tool_camera"
    const val ACTION_TOOL_BACKGROUND = "tool_background"
    const val ACTION_PAGE_CALL = "page_call"
    const val ACTION_PAGE_EMAIL = "page_email"
    const val ACTION_MEMO_DETAIL_SEARCH = "memo_detail_search"
    const val ACTION_MEMO_CREATE = "memo_create"
    const val ACTION_MEMO_MODIFY = "memo_modify"
    const val ACTION_MEMO_READ = "memo_read"
    const val ACTION_USE_AI_QNA = "use_ai_qna"
    const val ACTION_USE_AI_SUMMATION = "use_ai_summation"
    const val ACTION_IMPORTANT_ON = "important_on"
    const val ACTION_IMPORTANT_OFF = "important_off"
    const val ACTION_LOCK_ON = "lock_on"
    const val ACTION_LOCK_OFF = "lock_off"

    // 검색
    const val ACTION_TAG_CLICK = "tag_click"

    // 휴지통
    const val ACTION_TRASH_RESTORE = "trash_restore"
    const val ACTION_TRASH_DELETE = "trash_delete"
    const val ACTION_TRASH_ALL_DELETE = "trash_all_delete"

    // 메모 설정
    const val ACTION_SET_LOCKSCREEN_ON = "set_lockscreen_on"
    const val ACTION_SET_LOCKSCREEN_OFF = "set_lockscreen_off"
    const val ACTION_SET_TITLE_ON = "set_title_on"
    const val ACTION_SET_TITLE_OFF = "set_title_off"
    const val ACTION_SET_CHECKBOX_ON = "set_checkbox_on"
    const val ACTION_SET_CHECKBOX_OFF = "set_checkbox_off"
    const val ACTION_SET_PAGE_ON = "set_page_on"
    const val ACTION_SET_PAGE_OFF = "set_page_off"

    // 백업 설정
    const val ACTION_SET_GOOGLE_ON = "set_google_on"
    const val ACTION_SET_GOOGLE_OFF = "set_google_off"
    const val ACTION_USE_GOOGLE_BACKUP = "use_google_backup"
    const val ACTION_USE_LOCAL_BACKUP = "use_local_backup"
    const val ACTION_SET_PHOTO_BACKUP_ON = "set_photo_backup_on"
    const val ACTION_SET_PHOTO_BACKUP_OFF = "set_photo_backup_off"
    const val ACTION_SET_AUTO_BACKUP_ON = "set_auto_backup_on"
    const val ACTION_SET_AUTO_BACKUP_OFF = "set_auto_backup_off"
    const val ACTION_USE_AUTO_BACKUP = "use_auto_backup"
    const val ACTION_USE_GOOGLE_RESTORE = "use_google_restore"
    const val ACTION_USE_LOCAL_RESTORE = "use_local_restore"
    const val ACTION_USE_LEGACY_RESTORE = "use_legacy_restore"
    const val ACTION_SET_BACKUP_GUIDE_ON = "set_backup_guide_on"
    const val ACTION_SET_BACKUP_GUIDE_OFF = "set_backup_guide_off"
    const val ACTION_SET_BACKUP_NOTI_ON = "set_backup_noti_on"
    const val ACTION_SET_BACKUP_NOTI_OFF = "set_backup_noti_off"

    // 비밀번호 설정
    const val ACTION_SET_PASSWORD_ON = "set_password_on"
    const val ACTION_SET_PASSWORD_OFF = "set_password_off"
    const val ACTION_DEVICE_AUTH = "device_auth"

    // 고객센터
    const val ACTION_NOTICE = "notice"
    const val ACTION_BACKUP_GUIDE = "backup_guide"
    const val ACTION_FAQ = "faq"

    // 위젯
    const val ACTION_SMALL_WIDGET = "small_widget"
    const val ACTION_MEDIUM_WIDGET = "medium_widget"

    // 잠금화면
    const val ACTION_LOCKSCREEN_MEMO_CREATE = "lockscreen_memo_create"
    const val ACTION_LOCKSCREEN_IMPORTANT_ON = "lockscreen_important_on"
    const val ACTION_LOCKSCREEN_IMPORTANT_OFF = "lockscreen_important_off"

    /**
     * Firebase Analytics 이벤트 전송
     *
     * @param context : Context
     * @param category : 이벤트 카테고리
     * @param action : 이벤트 액션
     * @param label : (선택) 이벤트 라벨
     */
    fun sendEvent(context: Context, category: String, action: String, label: String? = null) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle().apply {
            putString("category", category)
            putString("action", action)
            label?.let { putString("label", it) }
        }
        firebaseAnalytics.logEvent("haru_event", bundle)
    }
}