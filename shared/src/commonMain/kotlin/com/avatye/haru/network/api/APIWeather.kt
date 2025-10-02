package com.avatye.haru.network.api

import com.avatye.haru.network.res.Air
import com.avatye.haru.network.res.AirComponents
import com.avatye.haru.network.res.AirData
import com.avatye.haru.network.res.AirMain
import com.avatye.haru.network.res.AirNow
import com.avatye.haru.network.res.AirQuality
import com.avatye.haru.network.res.AirResponse
import com.avatye.haru.network.res.ForecastResponse
import com.avatye.haru.network.res.Humidity
import com.avatye.haru.network.res.Location
import com.avatye.haru.network.res.ResLSWeather
import com.avatye.haru.network.res.Sky
import com.avatye.haru.network.res.Temp
import com.avatye.haru.network.res.TimeZoneInfo
import com.avatye.haru.network.res.Weather
import com.avatye.haru.network.res.WeatherDaily
import com.avatye.haru.network.res.WeatherNow
import com.avatye.haru.network.res.WeatherNowResponse
import com.avatye.haru.network.res.Wind
import com.caffeine.common.sdk.network.ktor.Pigeon
import com.caffeine.common.sdk.network.ktor.PigeonMethod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

object APIWeather {

    private const val API_KEY = "5ec82c7249e9d17bb927af69c648e869"

    fun requestLSWeather(
        lat: Double,
        lon: Double,
        onSuccess: (ResLSWeather) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val urlNow = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$API_KEY&units=metric"
        val urlForecast = "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=$API_KEY&units=metric"
        val urlAir = "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$API_KEY"

        var nowResp: WeatherNowResponse? = null
        var forecastResp: ForecastResponse? = null
        var airResp: AirResponse? = null

        fun tryComplete() {
            if (nowResp != null && forecastResp != null && airResp != null) {
                try {
                    val mapped = mapToResLSWeather(nowResp!!, forecastResp!!, airResp!!)
                    onSuccess(mapped)
                } catch (e: Exception) {
                    onFailure(e)
                }
            }
        }

        val json = Json { ignoreUnknownKeys = true }

        // 현재 날씨
        Pigeon(PigeonMethod.GET, urlNow, "", "").apply {
            enqueue { result ->
                result.onSuccess { raw ->
                    nowResp = json.decodeFromString(WeatherNowResponse.serializer(), raw)
                    tryComplete()
                }
                result.onFailure { onFailure(it) }
            }
        }

        // 예보
        Pigeon(PigeonMethod.GET, urlForecast, "", "").apply {
            enqueue { result ->
                result.onSuccess { raw ->
                    forecastResp = json.decodeFromString(ForecastResponse.serializer(), raw)
                    tryComplete()
                }
                result.onFailure { onFailure(it) }
            }
        }

        // 대기질
        Pigeon(PigeonMethod.GET, urlAir, "", "").apply {
            enqueue { result ->
                result.onSuccess { raw ->
                    airResp = json.decodeFromString(AirResponse.serializer(), raw)
                    tryComplete()
                }
                result.onFailure { onFailure(it) }
            }
        }
    }

    fun mapToResLSWeather(
        now: WeatherNowResponse,
        forecast: ForecastResponse,
        air: AirResponse
    ): ResLSWeather {

        // 최근 3시간 평균값으로 보정 (에어코리아 유사하게)
        val airSamples = air.list.take(3)
        val airNow: AirData? = if (airSamples.isNotEmpty()) {
            val avgPm10 = airSamples.mapNotNull { it.components.pm10 }
                .ifEmpty { listOf(0.0) }.average().toFloat()
            val avgPm25 = airSamples.mapNotNull { it.components.pm2_5 }
                .ifEmpty { listOf(0.0) }.average().toFloat()
            val co = airSamples.mapNotNull { it.components.co }
                .ifEmpty { listOf(0.0) }.average().toFloat()
            val no2 = airSamples.mapNotNull { it.components.no2 }
                .ifEmpty { listOf(0.0) }.average().toFloat()
            val o3 = airSamples.mapNotNull { it.components.o3 }
                .ifEmpty { listOf(0.0) }.average().toFloat()
            val so2 = airSamples.mapNotNull { it.components.so2 }
                .ifEmpty { listOf(0.0) }.average().toFloat()
            val aqi = airSamples.mapNotNull { it.main.aqi }
                .ifEmpty { listOf(3) } // fallback: 보통
                .average().toInt()

            AirData(
                main = AirMain(aqi),
                components = AirComponents(
                    co = co.toDouble(),
                    no = 0.0,
                    no2 = no2.toDouble(),
                    o3 = o3.toDouble(),
                    so2 = so2.toDouble(),
                    pm2_5 = avgPm25.toDouble(),
                    pm10 = avgPm10.toDouble()
                )
            )
        } else {
            air.list.firstOrNull()
        }

        // 하루 단위 그룹핑 → 한국 시간(KST) 기준으로 변경
        val dailyGroups = forecast.list.groupBy { item ->
            Instant.fromEpochSeconds(item.dt)
                .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                .date
        }

        // 오늘 날짜 (KST 기준)
        val today = Instant.fromEpochSeconds(now.dt)
            .toLocalDateTime(TimeZone.of("Asia/Seoul"))
            .date
        val todayItems = dailyGroups[today].orEmpty()

        val todayMin = todayItems.mapNotNull { it.main.temp_min }
            .minOrNull()?.toInt()
        val todayMax = todayItems.mapNotNull { it.main.temp_max }
            .maxOrNull()?.toInt()

        // 아이콘 매핑
        fun mapIcon(icon: String?): String? = when (icon) {
            "01d" -> "day_clear"
            "01n" -> "night_clear"
            "02d", "03d" -> "cloudy"
            "02n", "03n" -> "night_cloudy"
            "04d", "04n" -> "cloudy"
            "09d", "09n", "10d", "10n" -> "rain"
            "11d", "11n" -> "thunder"
            "13d", "13n" -> "snow"
            "50d", "50n" -> "fog"
            else -> "cloudy"
        }

        // 한국 환경부 기준 PM10 (㎍/㎥)
        fun mapPm10Grade(value: Float?): Int? = when {
            value == null -> null
            value <= 30 -> 2 // 좋음
            value <= 80 -> 3 // 보통
            value <= 150 -> 5 // 나쁨
            else -> 6 // 매우나쁨
        }

        // 한국 환경부 기준 PM2.5 (㎍/㎥)
        fun mapPm25Grade(value: Float?): Int? = when {
            value == null -> null
            value <= 15 -> 2 // 좋음
            value <= 35 -> 3 // 보통
            value <= 75 -> 5 // 나쁨
            else -> 6 // 매우나쁨
        }

        return ResLSWeather(
            location = Location(
                displayAddress = now.name, // Geocoder로 한국어 주소 변환 가능
                timeZone = TimeZoneInfo(name = "Asia/Seoul")
            ),
            weather = Weather(
                now = WeatherNow(
                    type = mapIcon(now.weather.firstOrNull()?.icon),
                    temp = Temp(
                        now = now.main.temp?.toInt(),
                        min = todayMin,
                        max = todayMax
                    ),
                    sky = Sky(name = now.weather.firstOrNull()?.description),
                    humidity = Humidity(value = now.main.humidity),
                    wind = Wind(
                        direction = now.wind?.deg?.toString(),
                        velocity = now.wind?.speed?.toFloat()
                    ),
                    date = now.dt.toString()
                ),
                hourly = forecast.list.take(8).map {
                    WeatherNow(
                        type = mapIcon(it.weather.firstOrNull()?.icon),
                        temp = Temp(now = it.main.temp?.toInt()),
                        humidity = Humidity(value = it.main.humidity),
                        date = it.dt.toString()
                    )
                },
                weekly = dailyGroups.map { (date, items) ->
                    val min = items.mapNotNull { it.main.temp_min }.minOrNull()?.toInt()
                    val max = items.mapNotNull { it.main.temp_max }.maxOrNull()?.toInt()
                    WeatherDaily(
                        type = mapIcon(items.firstOrNull()?.weather?.firstOrNull()?.icon),
                        temp = Temp(min = min, max = max),
                        date = date.toString()
                    )
                }
            ),
            air = Air(
                now = AirNow(
                    pm10 = AirQuality(
                        value = airNow?.components?.pm10?.toFloat(),
                        grade = mapPm10Grade(airNow?.components?.pm10?.toFloat())
                    ),
                    pm25 = AirQuality(
                        value = airNow?.components?.pm2_5?.toFloat(),
                        grade = mapPm25Grade(airNow?.components?.pm2_5?.toFloat())
                    ),
                    co = AirQuality(value = airNow?.components?.co?.toFloat()),
                    no2 = AirQuality(value = airNow?.components?.no2?.toFloat()),
                    o3 = AirQuality(value = airNow?.components?.o3?.toFloat()),
                    so2 = AirQuality(value = airNow?.components?.so2?.toFloat())
                )
            )
        )
    }

    // Double? → Int? 변환 헬퍼
    private fun Double?.toIntOrNull(): Int? = this?.toInt()

}