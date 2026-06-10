package com.mool

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
