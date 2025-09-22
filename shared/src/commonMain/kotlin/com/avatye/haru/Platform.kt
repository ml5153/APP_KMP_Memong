package com.avatye.haru

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform