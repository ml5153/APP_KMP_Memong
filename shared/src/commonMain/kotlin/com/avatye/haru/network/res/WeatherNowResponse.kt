package com.avatye.haru.network.res

import kotlinx.serialization.Serializable

@Serializable
data class WeatherNowResponse(
    val weather: List<WeatherDesc> = emptyList(),
    val main: MainTemp,
    val wind: WindResp? = null,
    val dt: Long,
    val name: String? = null
)

@Serializable
data class MainTemp(
    val temp: Double? = null,
    val feels_like: Double? = null,
    val temp_min: Double? = null,
    val temp_max: Double? = null,
    val humidity: Int? = null
)

@Serializable
data class WindResp(val speed: Double? = null, val deg: Int? = null)