package com.avatye.haru.network.res

import kotlinx.serialization.Serializable

@Serializable
data class WeatherDesc(val main: String? = null, val description: String? = null, val icon: String? = null)

@Serializable
data class AirData(val main: AirMain, val components: AirComponents)

@Serializable
data class AirMain(val aqi: Int? = null)

@Serializable
data class AirComponents(
    val co: Double? = null,
    val no: Double? = null,
    val no2: Double? = null,
    val o3: Double? = null,
    val so2: Double? = null,
    val pm2_5: Double? = null,
    val pm10: Double? = null
)
