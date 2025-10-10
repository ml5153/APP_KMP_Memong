package com.avatye.haru.network.api

import android.content.Context
import android.content.SharedPreferences
import com.avatye.haru.network.res.ResLSWeather
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object WeatherCacheManager {

    private const val PREF_NAME = "haru_weather_cache"
    private const val KEY_LAST_WEATHER = "last_weather"
    private const val KEY_LAST_TIMESTAMP = "last_weather_timestamp"

    private val json = Json { ignoreUnknownKeys = true }

    /** 캐시 저장 */
    fun save(context: Context, weather: ResLSWeather) {
        runCatching {
            val prefs = getPrefs(context)
            val encoded = json.encodeToString(weather)
            prefs.edit()
                .putString(KEY_LAST_WEATHER, encoded)
                .putLong(KEY_LAST_TIMESTAMP, System.currentTimeMillis())
                .apply()
        }.onFailure {
            // 캐시 저장 중 오류는 앱 크래시에 영향을 주지 않도록 무시
        }
    }

    /** 캐시 로드 */
    fun load(context: Context): ResLSWeather? {
        return runCatching {
            val prefs = getPrefs(context)
            val encoded = prefs.getString(KEY_LAST_WEATHER, null)
            if (encoded.isNullOrEmpty()) null
            else json.decodeFromString<ResLSWeather>(encoded)
        }.getOrNull()
    }

    /** 마지막 업데이트 후 경과 시간(분) */
    fun lastUpdateElapsedMinutes(context: Context): Long {
        val prefs = getPrefs(context)
        val last = prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
        return if (last == 0L) Long.MAX_VALUE
        else (System.currentTimeMillis() - last) / 60000L
    }

    /** 내부 SharedPreferences 핸들러 */
    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}