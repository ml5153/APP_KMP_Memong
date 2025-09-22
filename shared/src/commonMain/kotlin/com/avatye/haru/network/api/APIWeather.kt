package com.avatye.haru.network.api

import com.avatye.haru.network.res.ResLSWeather
import com.caffeine.common.sdk.log.LogTracer
import com.caffeine.common.sdk.network.ktor.Pigeon
import com.caffeine.common.sdk.network.ktor.PigeonMethod
import kotlinx.serialization.json.Json

object APIWeather {

    fun requestLSWeather(
        lat: Double,
        lon: Double,
        onSuccess: (ResLSWeather) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        Pigeon(
            argMethod = PigeonMethod.GET,
            argUrl = "https://api.haru.avatye.com/weather?lat=$lat&lon=$lon&airGradeType=2",
            argAcceptVersion = "0.1.0",
            argToken = "Basic OWUyOGYxZDlkZWM4NGFmNTkzZTU4NDVlZjk3ZTUyNWU6ZDhjZjUyODIxMjFkNDE1NDgxNTA2MzBhYjhkYjczMmMzNjNlZDAxNzZkYmM0YTgwODU1YWI0NjczY2QzMTJlOA=="
        ).apply {
            enqueue { result ->
                result.onSuccess { raw ->
                    LogTracer.i { "APIWeather -> requestLSWeather::onSuccess" }
                    val json = Json { ignoreUnknownKeys = true }
                    val response = json.decodeFromString<ResLSWeather>(raw)
                    onSuccess(response)
                }
                result.onFailure { error ->
                    LogTracer.e { "APIWeather -> requestLSWeather::onFailure" }
                    onFailure(error)
                }
            }
        }
    }
}