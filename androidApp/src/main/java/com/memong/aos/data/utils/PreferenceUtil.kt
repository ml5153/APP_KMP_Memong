package com.memong.aos.data.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

internal object PreferenceUtil {
    private const val PREF_NAME = "MemoGPrefs"
    private lateinit var prefs: SharedPreferences

    const val KEY_IS_FIRST_LAUNCH = "$PREF_NAME:KEY_IS_FIRST_LAUNCH"
    const val KEY_MEMO_LAYOUT_MODE = "$PREF_NAME:KEY_MEMO_LAYOUT_MODE"
    const val KEY_MEMO_GROUP_MODE = "$PREF_NAME:KEY_MEMO_GROUP_MODE"
    const val KEY_MEMO_FONT_SIZE = "$PREF_NAME:KEY_MEMO_FONT_SIZE"
    const val KEY_GD_BACKUP_SWITCH = "$PREF_NAME:KEY_GD_BACKUP_SWITCH"
    const val KEY_PASSWORD = "$PREF_NAME:KEY_PASSWORD"
    const val KEY_PASSWORD_SWITCH = "$PREF_NAME:KEY_PASSWORD_SWITCH"
    const val KEY_TURN_OFF_TITLE = "$PREF_NAME:KEY_TURN_OFF_TITLE"
    const val KEY_QUICK_PAGE = "$PREF_NAME:KEY_QUICK_PAGE"
    const val KEY_USE_LOCKSCREEN_MEMO = "$PREF_NAME:KEY_USE_LOCKSCREEN_MEMO"

    const val KEY_MEMO_SORTING_CURRENT_TYPE = "$PREF_NAME:KEY_MEMO_SORTING_CURRENT_TYPE"
    const val KEY_PM_VISIBLE = "$PREF_NAME:KEY_PM_VISIBLE"
    const val KEY_MEMO_RESTORE = "$PREF_NAME:KEY_MEMO_RESTORE"
    const val KEY_MEMO_PERMANENT_DELETE = "$PREF_NAME:KEY_MEMO_PERMANENT_DELETE"
    const val KEY_SIMPLIFY = "$PREF_NAME:KEY_SIMPLIFY"
    const val KEY_PHOTO_BACKUP = "$PREF_NAME:KEY_PHOTO_BACKUP"
    const val KEY_AUTO_BACKUP = "$PREF_NAME:KEY_AUTO_BACKUP"
    const val KEY_LAST_LOCAL_BACKUP_TIME = "$PREF_NAME:KEY_LAST_LOCAL_BACKUP_TIME"
    const val KEY_SHOW_BACKUP_GUIDE = "$PREF_NAME:KEY_SHOW_BACKUP_GUIDE"
    const val KEY_BACKUP_GUIDE_DISMISS_TIME = "$PREF_NAME:KEY_BACKUP_GUIDE_DISMISS_TIME"
    const val KEY_AUTO_BACKUP_NOTIFICATION = "$PREF_NAME:KEY_AUTO_BACKUP_NOTIFICATION"
    const val KEY_GOOGLE_ACCOUNT_EMAIL = "$PREF_NAME:KEY_GOOGLE_ACCOUNT_EMAIL"
    const val KEY_LAST_GOOGLE_BACKUP_TIME = "$PREF_NAME:KEY_LAST_GOOGLE_BACKUP_TIME"
    const val KEY_MAIN_POPUP_CLOSED = "$PREF_NAME:KEY_MAIN_POPUP_CLOSED"
    const val KEY_LAST_USE_MAIN_POPUP = "$PREF_NAME:KEY_LAST_USE_MAIN_POPUP"
    const val KEY_NOTICE_DISMISSED = "$PREF_NAME:KEY_NOTICE_DISMISSED"
    const val KEY_LAST_DEVICE_AUTH_ELAPSED = "$PREF_NAME:KEY_LAST_DEVICE_AUTH_ELAPSED"
    const val KEY_CHECKED_MEMO_AUTO_SORT = "$PREF_NAME:KEY_CHECKED_MEMO_AUTO_SORT"

    const val KEY_SECTION_SECRET_MEMO_IS_EXPANDED = "$PREF_NAME:KEY_SECTION_SECRET_MEMO_IS_EXPANDED"
    const val KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED = "$PREF_NAME:KEY_SECTION_IMPORTANT_MEMO_IS_EXPANDED"

    const val KEY_LAST_DAU_DATE = "$PREF_NAME:KEY_LAST_DAU_DATE"
    const val KEY_LAST_WAU_WEEK = "$PREF_NAME:KEY_LAST_WAU_WEEK"
    const val KEY_LAST_MAU_MONTH = "$PREF_NAME:KEY_LAST_MAU_MONTH"

    // region { Dynamic Preference }
    const val KEY_DYNAMIC_SECTIONS_IS_INIT = "$PREF_NAME:KEY_DYNAMIC_SECTIONS_IS_INIT"
    fun getDynamicSectionKey(sectionKey: String) = "$PREF_NAME:KEY_DYNAMIC_SECTION_MEMO_IS_EXPANDED_$sectionKey"
    // endregion

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun set(key: String, value: Any) {
        prefs.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }

    fun <T> get(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> prefs.getString(key, defaultValue) as T
            is Int -> prefs.getInt(key, defaultValue) as T
            is Boolean -> prefs.getBoolean(key, defaultValue) as T
            is Float -> prefs.getFloat(key, defaultValue) as T
            is Long -> prefs.getLong(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
