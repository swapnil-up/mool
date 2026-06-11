package com.mool.core.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

actual fun createPlatformClient(): HttpClient = HttpClient(Darwin) {
    // TODO: add certificate pinning via NSURLSession delegate
    applyCommonConfig()
}
