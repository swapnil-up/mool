package com.mool.core.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import okhttp3.CertificatePinner

actual fun createPlatformClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            certificatePinner(
                CertificatePinner.Builder()
                    .add("open.er-api.com", "sha256/2Woa8zzB9I6FSnu4kjHuoPfacdPEOIC3lzSGMDjz5Dw=")
                    .build()
            )
        }
    }
    applyCommonConfig()
}
