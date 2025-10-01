package com.avatye.haru.network.res

import kotlinx.serialization.Serializable

@Serializable
data class AirResponse(val list: List<AirData> = emptyList())