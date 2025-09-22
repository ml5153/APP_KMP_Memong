package com.memong.aos.data.enum

internal object MemogLegacyWebUrl {

    const val HOST_WEB: String = "https://memog-web.avatye.com/" // 아바티 서버

    const val HOST_API: String = "https://memog-api.avatye.com/" // API 서버

    //공지사항 WEBVIEW
    const val WEB_NOTICE: String = "notice" // 아바티 Path

    //메모작성 가이드 WEBVIEW
    const val WEB_WRITE_GUIDE: String = "guide/write" // 아바티 Path

    //메모편집 가이드 WEBVIEW
    const val WEB_EDIT_GUIDE: String = "guide/modify" // 아바티 Path

    //백업설정 가이드 WEBVIEW
    const val WEB_BACKUP: String = "guide/backup" // 아바티 Path

    //자주 찾는 도움말 WEBVIEW
    const val WEB_FAQ: String = "faq" // 아바티 Path

    //이용약관 URL
    const val SERVICE_TERMS_URL: String =
        "https://sites.google.com/view/memog-terms/%ED%99%88" // 서비스 정보 -> 이용약관 URL
}