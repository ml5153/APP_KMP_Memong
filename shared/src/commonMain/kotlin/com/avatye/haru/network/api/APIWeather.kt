package com.avatye.haru.network.api

import com.avatye.haru.log.LogTrack
import com.avatye.haru.network.res.*
import com.caffeine.common.sdk.network.ktor.Pigeon
import com.caffeine.common.sdk.network.ktor.PigeonMethod
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Open-Meteo 기반 날씨 API
 *
 * - 현재 온도 / 최저 / 최고
 * - 미세먼지 / 초미세먼지 (pm10, pm2.5)
 * - 어제 평균 기온 및 온도 차이
 *
 * Crash-free / Thread-safe / Null-safe 보장
 */
object APIWeather {

    private const val TAG = "APIWeather"
    private val json = Json { ignoreUnknownKeys = true }

    fun requestLSWeather(
        lat: Double,
        lon: Double,
        onSuccess: (ResLSWeather) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val baseTimeZone = "Asia/Seoul"

        // 1. API URL 구성
        val nowUrl =
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weathercode" +
                    "&daily=temperature_2m_min,temperature_2m_max" +
                    "&timezone=$baseTimeZone"

        val airUrl =
            "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=$lat&longitude=$lon" +
                    "&hourly=pm10,pm2_5&timezone=$baseTimeZone"

        val yesterdayDate = Clock.System.now()
            .toLocalDateTime(TimeZone.of(baseTimeZone))
            .date.minus(DatePeriod(days = 1))

        val yesterdayUrl =
            "https://archive-api.open-meteo.com/v1/archive?latitude=$lat&longitude=$lon" +
                    "&start_date=$yesterdayDate&end_date=$yesterdayDate" +
                    "&daily=temperature_2m_max,temperature_2m_min,temperature_2m_mean" +
                    "&timezone=$baseTimeZone"

        // 2. 응답 캐시
        var forecast: MeteoForecastResponse? = null
        var air: MeteoAirResponse? = null
        var yesterdayResp: MeteoYesterdayResponse? = null

        var isFailed = false  // 중복 onFailure 방지

        fun tryComplete() {
            if (forecast != null && air != null && yesterdayResp != null) {
                try {
                    val mapped = mapToResLSWeather(lat, lon, forecast!!, air!!, yesterdayResp!!)
                    onSuccess(mapped)
                } catch (e: Exception) {
                    if (!isFailed) {
                        isFailed = true
                        LogTrack.e { "[$TAG] Mapping failed: ${e.message}" }
                        onFailure(e)
                    }
                }
            }
        }

        // 3. 현재 / 예보
        Pigeon(PigeonMethod.GET, nowUrl, "", "").enqueue { result ->
            result.onSuccess {
                try {
                    forecast = json.decodeFromString(MeteoForecastResponse.serializer(), it)
                    tryComplete()
                } catch (e: Exception) {
                    if (!isFailed) {
                        isFailed = true
                        LogTrack.e { "[$TAG] Forecast parse error: ${e.message}" }
                        onFailure(e)
                    }
                }
            }
            result.onFailure {
                if (!isFailed) {
                    isFailed = true
                    LogTrack.e { "[$TAG] Forecast request failed: ${it.message}" }
                    onFailure(it)
                }
            }
        }

        // 4. 대기질
        Pigeon(PigeonMethod.GET, airUrl, "", "").enqueue { result ->
            result.onSuccess {
                try {
                    air = json.decodeFromString(MeteoAirResponse.serializer(), it)
                    tryComplete()
                } catch (e: Exception) {
                    if (!isFailed) {
                        isFailed = true
                        LogTrack.e { "[$TAG] Air parse error: ${e.message}" }
                        onFailure(e)
                    }
                }
            }
            result.onFailure {
                if (!isFailed) {
                    isFailed = true
                    LogTrack.e { "[$TAG] Air request failed: ${it.message}" }
                    onFailure(it)
                }
            }
        }

        // 5. 어제 기온
        Pigeon(PigeonMethod.GET, yesterdayUrl, "", "").enqueue { result ->
            result.onSuccess {
                try {
                    yesterdayResp = json.decodeFromString(MeteoYesterdayResponse.serializer(), it)
                    tryComplete()
                } catch (e: Exception) {
                    if (!isFailed) {
                        isFailed = true
                        LogTrack.e { "[$TAG] Yesterday parse error: ${e.message}" }
                        onFailure(e)
                    }
                }
            }
            result.onFailure {
                if (!isFailed) {
                    isFailed = true
                    LogTrack.e { "[$TAG] Yesterday request failed: ${it.message}" }
                    onFailure(it)
                }
            }
        }
    }

    // ===============================
    // JSON Response Models
    // ===============================

    @Serializable
    data class MeteoForecastResponse(
        val current: CurrentWeather? = null,
        val daily: DailyWeather? = null
    ) {
        @Serializable
        data class CurrentWeather(
            val temperature_2m: Double? = null,
            val weathercode: Int? = null
        )

        @Serializable
        data class DailyWeather(
            val temperature_2m_min: List<Double>? = null,
            val temperature_2m_max: List<Double>? = null
        )
    }

    @Serializable
    data class MeteoAirResponse(
        val hourly: AirHourly? = null
    ) {
        @Serializable
        data class AirHourly(
            val pm10: List<Double?>? = null,
            val pm2_5: List<Double?>? = null
        )
    }

    @Serializable
    data class MeteoYesterdayResponse(
        val daily: YesterdayDaily? = null
    ) {
        @Serializable
        data class YesterdayDaily(
            val temperature_2m_mean: List<Double>? = null
        )
    }

    // ===============================
    // Mapping Logic
    // ===============================

    private fun mapToResLSWeather(
        lat: Double,
        lon: Double,
        forecast: MeteoForecastResponse,
        air: MeteoAirResponse,
        yesterday: MeteoYesterdayResponse
    ): ResLSWeather {

        val nowTemp = forecast.current?.temperature_2m?.toInt()
        val minTemp = forecast.daily?.temperature_2m_min?.firstOrNull()?.toInt()
        val maxTemp = forecast.daily?.temperature_2m_max?.firstOrNull()?.toInt()
        val pm10 = safeAvg(air.hourly?.pm10)
        val pm25 = safeAvg(air.hourly?.pm2_5)
        val yesterdayMean = yesterday.daily?.temperature_2m_mean?.firstOrNull()?.toFloat()
        val diff = if (nowTemp != null && yesterdayMean != null) nowTemp - yesterdayMean else null

        return ResLSWeather(
            latitude = lat,
            longitude = lon,
            location = Location(
                displayAddress = "현재 위치",
                timeZone = TimeZoneInfo("Asia/Seoul")
            ),
            weather = Weather(
                now = WeatherNow(
                    type = mapWeatherCode(forecast.current?.weathercode),
                    temp = Temp(
                        now = nowTemp,
                        min = minTemp,
                        max = maxTemp,
                        yes = yesterdayMean?.toInt(),
                        diff = diff?.toInt()
                    ),
                    sky = Sky(name = mapWeatherName(forecast.current?.weathercode)),
                    humidity = Humidity(null),
                    wind = Wind(null, null),
                    date = Clock.System.now().epochSeconds.toString()
                ),
                hourly = emptyList(),
                weekly = emptyList()
            ),
            air = Air(
                now = AirNow(
                    pm10 = AirQuality(
                        value = pm10,
                        grade = mapPm10Grade(pm10)
                    ),
                    pm25 = AirQuality(
                        value = pm25,
                        grade = mapPm25Grade(pm25)
                    )
                )
            )
        )
    }


    // ===============================
    // Helpers
    // ===============================

    /** null-safe 평균값 계산 */
    private fun safeAvg(values: List<Double?>?): Float? {
        val valid = values?.filterNotNull()
        return if (!valid.isNullOrEmpty()) valid.average().toFloat() else null
    }

    /** Open-Meteo 날씨 코드 → 타입 문자열 */
    private fun mapWeatherCode(code: Int?): String = when (code) {
        0 -> "clear"
        1, 2 -> "partly_cloudy"
        3 -> "cloudy"
        45, 48 -> "fog"
        51, 53, 55, 56, 57 -> "drizzle"
        61, 63, 65, 80, 81, 82 -> "rain"
        66, 67 -> "freezing_rain"
        71, 73, 75, 77, 85, 86 -> "snow"
        95, 96, 99 -> "thunder"
        else -> "cloudy"
    }

    /** 날씨 코드 → 한글 설명 */
    private fun mapWeatherName(code: Int?): String = when (code) {
        0 -> "맑음"
        1, 2 -> "구름 조금"
        3 -> "흐림"
        45, 48 -> "안개"
        51, 53, 55 -> "이슬비"
        56, 57 -> "진눈깨비"
        61, 63, 65 -> "비"
        66, 67 -> "얼음비"
        71, 73, 75, 77 -> "눈"
        80, 81, 82 -> "소나기"
        85, 86 -> "눈"
        95, 96, 99 -> "뇌우"
        else -> "흐림"
    }

    /** PM10 단계 */
    private fun mapPm10Grade(value: Float?): Int? = when {
        value == null -> null
        value <= 30 -> 2
        value <= 80 -> 3
        value <= 150 -> 5
        else -> 6
    }

    /** PM2.5 단계 */
    private fun mapPm25Grade(value: Float?): Int? = when {
        value == null -> null
        value <= 15 -> 2
        value <= 35 -> 3
        value <= 75 -> 5
        else -> 6
    }
}
