package com.avatye.haru.network.res

import kotlinx.serialization.Serializable

@Serializable
data class ResLSWeather(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: Location? = null,
    val weather: Weather? = null,
    val air: Air? = null,
    val forecast: String? = null,
    val season: String? = null,
    val holiday: String? = null
)

// ---------------- Location ----------------

@Serializable
data class Location(
    val displayAddress: String? = null,
    val timeZone: TimeZoneInfo? = null
)

@Serializable
data class TimeZoneInfo(
    val name: String? = null,
    val offset: Int? = null
)

// ---------------- Weather ----------------

@Serializable
data class Weather(
    val now: WeatherNow? = null,
    val hourly: List<WeatherNow>? = null,
    val weekly: List<WeatherDaily>? = null,
    val announce: WeatherAnnounce? = null
)

@Serializable
data class WeatherNow(
    val type: String? = null,          // 아이콘 매핑용 ex. day_clear, cloudy 등
    val temp: Temp? = null,
    val sky: Sky? = null,
    val rain: Rain? = null,
    val wind: Wind? = null,
    val humidity: Humidity? = null,
    val lightning: Boolean? = null,
    val date: String? = null,
    val lifeIndex: LifeIndex? = null
)

@Serializable
data class Temp(
    val now: Int? = null,
    val sen: Float? = null,
    val min: Int? = null,
    val max: Int? = null,
    val yes: Int? = null,             // 어제 기온
    val diff: Int? = null             // 🌡️ 어제 대비 온도차 (+면 더 따뜻함, -면 더 추움)
)

@Serializable
data class Sky(
    val type: Int? = null,     // 예: 1=맑음, 2=구름 조금, 3=구름 많음, 4=흐림
    val name: String? = null
)

@Serializable
data class Rain(
    val type: Int? = null,     // 예: 0=없음, 1=비, 2=비/눈, 3=눈
    val qty: Int? = null,      // mm
    val qtyText: String? = null,
    val rate: Int? = null      // %
)

@Serializable
data class Wind(
    val direction: String? = null,
    val velocity: Float? = null,
    val velocityString: String? = null
)

@Serializable
data class Humidity(
    val value: Int? = null,
    val text: String? = null
)

@Serializable
data class LifeIndex(
    val uv: Int? = null,
    val uvText: String? = null
)

@Serializable
data class WeatherDaily(
    val type: String? = null,
    val temp: Temp? = null,
    val sky: Sky? = null,
    val rain: Rain? = null,
    val date: String? = null
)

@Serializable
data class WeatherAnnounce(
    val now: String? = null,
    val hourly: String? = null,
    val weekly: String? = null
)

// ---------------- Air ----------------

@Serializable
data class Air(
    val now: AirNow? = null,
    val forecast: AirForecast? = null,
    val station: AirStation? = null
)

@Serializable
data class AirNow(
    val pm10: AirQuality? = null,
    val pm25: AirQuality? = null,
    val so2: AirQuality? = null,
    val co: AirQuality? = null,
    val no2: AirQuality? = null,
    val o3: AirQuality? = null
)

@Serializable
data class AirForecast(
    val days: List<AirForecastDay>? = null
)

@Serializable
data class AirForecastDay(
    val title: String? = null,         // today, tomorrow 등
    val pm10Grade: Int? = null,
    val pm25Grade: Int? = null
)

@Serializable
data class AirStation(
    val name: String? = null,
    val type: Int? = null,
    val announceDate: String? = null
)

@Serializable
data class AirQuality(
    val value: Float? = null, // 또는 Double? 로 변경 가능
    val grade: Int? = null
)
