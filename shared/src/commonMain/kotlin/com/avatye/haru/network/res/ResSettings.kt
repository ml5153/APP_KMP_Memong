package com.avatye.haru.network.res

import kotlinx.serialization.Serializable
@Serializable
data class ResSettings(
    val feed: Feed? = null,
    val invite: Invite? = null
)

@Serializable
data class Feed(
    val listLimitCount: Int = 0,
    val useInAppBrowser: Boolean = false
)

@Serializable
data class Invite(
    val useInvite: Boolean = false,
    val dailyLimit: Int = 0
)