package com.avatye.haru.network.res

import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(val list: List<ForecastItem> = emptyList())

@Serializable
data class ForecastItem(
    val dt: Long,
    val main: MainTemp,
    val weather: List<WeatherDesc> = emptyList()
)