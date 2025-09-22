package com.avatye.haru.network

import com.avatye.haru.network.res.ResSettings
import com.caffeine.common.sdk.log.LogTracer
import com.caffeine.common.sdk.network.ktor.Pigeon
import com.caffeine.common.sdk.network.ktor.PigeonMethod
import kotlinx.serialization.json.Json

object APISettings {
    fun requestSettings(
        onSuccess: (ResSettings) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        requestSettings { result ->
            result.onSuccess(onSuccess)
            result.onFailure(onFailure)
        }
    }
    private fun requestSettings(callback: (Result<ResSettings>) -> Unit) {
        Pigeon(
            argMethod = PigeonMethod.GET,
            argUrl = "https://api-qa.reward.avatye.com/app/settings",
            argAcceptVersion = "1.0.0",
            argToken = "Basic MWE4YzlkMzBiNWU1MTFlOTlmM2FiNDViYTk0MDQ2OWU6YTU2NjU5ZmVjZmE5MTFlOQ=="
        ).apply {
            enqueue { result ->
                result.onSuccess { raw ->
                    LogTracer.i { "APISettings -> requestSettings::onSuccess" }
                    val json = Json { ignoreUnknownKeys = true }
                    val response = json.decodeFromString<ResSettings>(raw)
                    callback(Result.success(response))
                }
                result.onFailure { error ->
                    LogTracer.e { "APISettings -> requestSettings::onFailure" }
                    callback(Result.failure(error))
                }
            }
        }
    }


}