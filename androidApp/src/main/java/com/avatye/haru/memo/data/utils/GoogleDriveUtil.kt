package com.avatye.haru.memo.data.utils

object GoogleDriveUtil {
    /**
     * Google Drive 공유 URL을 Glide에서 사용할 수 있는 직접 다운로드 URL로 변환
     * @param shareUrl Google Drive 공유 URL (예: https://drive.google.com/file/d/FILE_ID/view?usp=sharing)
     * @return 변환된 다운로드 URL (예: https://drive.google.com/uc?export=download&id=FILE_ID)
     */
    fun toDirectUrl(shareUrl: String): String {
        val regex = Regex("/d/([a-zA-Z0-9_-]+)")
        val matchResult = regex.find(shareUrl)
        val fileId = matchResult?.groups?.get(1)?.value
        return if (!fileId.isNullOrEmpty()) {
            "https://drive.google.com/uc?export=download&id=$fileId"
        } else {
            shareUrl // 변환 실패 시 원본 반환
        }
    }
}
