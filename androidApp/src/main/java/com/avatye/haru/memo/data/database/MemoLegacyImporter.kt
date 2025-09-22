package com.avatye.haru.memo.data.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.avatye.haru.memo.data.dao.MemoDao
import com.avatye.haru.memo.data.entity.BodyItem
import com.avatye.haru.memo.data.entity.MemoEntity
import com.avatye.haru.memo.data.utils.BaseUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.PreferenceUtil.KEY_PASSWORD_SWITCH
import com.avatye.haru.log.LogTrack

class MemoLegacyImporter(
    private val legacyDb: SQLiteDatabase,
    private val memoDao: MemoDao,
    private val userId: String
) {

    companion object {
        private const val TABLE_NAME = "notes"
    }

    suspend fun importAllMemos(imageResolver: (String) -> String?) {
        try {
            val cursor = legacyDb.rawQuery("SELECT * FROM $TABLE_NAME", null)
            val memos = mutableListOf<MemoEntity>()

            cursor.use {
                while (it.moveToNext()) {
                    convertCursorToMemo(it, imageResolver)?.let { memo ->
                        memos.add(memo)
                    }
                }
            }

            LogTrack.d("LegacyImporter") { "Imported ${memos.size} memos from legacy DB" }

            memoDao.insertAll(memos)

        } catch (e: Exception) {
            LogTrack.e("LegacyImporter") { "Error importing memos: ${e.stackTraceToString()}" }
        }
    }

    private fun convertCursorToMemo(
        cursor: Cursor,
        imageResolver: (String) -> String?
    ): MemoEntity? {
        return try {
            val title = sanitizeHtmlBasic(cursor.optString("title"))
            val bodyText = sanitizeHtmlAndTrimFirstLine(cursor.optString("body"))
            val created = cursor.optLong("created")
            val modified = cursor.optLong("modified")
            val read = cursor.optLong("read")
            val custom = cursor.optLong("custom")

            val imageNameRaw = cursor.optString("imageName")
            val imageName = imageNameRaw.substringBefore(";")  // Remove ;90, ;0, etc.

            val isImportant = cursor.optBoolean("isImportant")
            val legacyBgColor = cursor.optInt("bgColor")
            val bgColorHex = convertLegacyBgColor(legacyBgColor)
            val isDeleted = cursor.optBoolean("isDeleted")
            val deleted = if (isDeleted) modified else 0L

            val isLockedOriginal = cursor.optBoolean("isLocked")
            val isLocked = if (PreferenceUtil.get(KEY_PASSWORD_SWITCH, false)) isLockedOriginal else false

            val hasText = bodyText.isNotBlank()
            val hasImageName = imageName.isNotBlank()
            val resolvedImagePath = if (hasImageName) imageResolver(imageName) else null
            val hasValidImage = resolvedImagePath != null

            val imagePathMap = if (hasValidImage) mapOf(0 to listOf(resolvedImagePath!!)) else emptyMap()

            val calculatedCategory = when {
                hasText && hasValidImage -> 2
                hasText -> 1
                hasValidImage -> 2
                else -> 1
            }

            LogTrack.d("LegacyImporter") {
                "Memo -> title=[${title.take(15)}], hasText=$hasText, hasImageName=$hasImageName, " +
                        "resolvedImagePath=${resolvedImagePath?.takeLast(30)}, category=$calculatedCategory"
            }

            MemoEntity(
                _id = 0,
                userid = userId,
                uuid = BaseUtil.getUUID(),
                title = title,
                body = listOf(BodyItem(index = 1, text = bodyText)),
                created = created,
                modified = modified,
                read = read,
                custom = custom,
                imagePath = imagePathMap,
                isLocked = isLocked,
                isImportant = isImportant,
                bgColor = bgColorHex,
                category = calculatedCategory,
                isDeleted = isDeleted,
                deleted = deleted
            )
        } catch (e: Exception) {
            LogTrack.e("LegacyImporter") { "Error converting memo: ${e.stackTraceToString()}" }
            null
        }
    }

    private fun Cursor.optString(column: String): String {
        return runCatching {
            val index = getColumnIndex(column)
            if (index != -1 && !isNull(index)) getString(index) ?: "" else ""
        }.getOrElse { "" }
    }

    private fun Cursor.optLong(column: String): Long {
        return runCatching {
            val index = getColumnIndex(column)
            if (index == -1 || isNull(index)) return@runCatching 0L
            when (getType(index)) {
                Cursor.FIELD_TYPE_INTEGER -> getLong(index)
                Cursor.FIELD_TYPE_STRING -> getString(index).toLongOrNull() ?: 0L
                else -> 0L
            }
        }.getOrElse { 0L }
    }

    private fun Cursor.optInt(column: String): Int {
        return runCatching {
            val index = getColumnIndex(column)
            if (index == -1 || isNull(index)) return@runCatching 0
            when (getType(index)) {
                Cursor.FIELD_TYPE_INTEGER -> getInt(index)
                Cursor.FIELD_TYPE_STRING -> getString(index).toIntOrNull() ?: 0
                else -> 0
            }
        }.getOrElse { 0 }
    }

    private fun Cursor.optBoolean(column: String): Boolean {
        return optInt(column) != 0
    }

    private fun convertLegacyBgColor(value: Int): String {
        return when (value) {
            0, 1 -> "#FFFFFF"
            2 -> "#FDFEBC"
            3 -> "#E0FFDD"
            4 -> "#D7FDFE"
            5 -> "#FCE6BF"
            6 -> "#FFE3E0"
            else -> "#FFFFFF"
        }
    }
}

// HTML 제거 + 개행 정리
private fun sanitizeHtmlBasic(raw: String): String {
    return raw
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace("&nbsp;", " ")
        .replace("&lt;", "")
        .replace("&gt;", "")
        .replace("&amp;", "")
        .replace("&quot;", "")
        .replace("&#39;", "")
        .replace(Regex("%[a-zA-Z0-9]{2,}"), "")
        .replace(Regex("<.*?>"), "")
        .replace(Regex("&[a-zA-Z0-9#]+;"), "")
}

private fun sanitizeHtmlAndTrimFirstLine(raw: String): String {
    val cleaned = sanitizeHtmlBasic(raw)
    val lines = cleaned.lines()
    return if (lines.size > 1) lines.drop(1).joinToString("\n").trim() else ""
}
