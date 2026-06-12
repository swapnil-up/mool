package com.mool.core.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

actual fun createPlatformClient(): HttpClient = HttpClient(Darwin) {
    // Certificate pinning via NSURLSession delegate — needs iOS-specific setup
    applyCommonConfig()
}
