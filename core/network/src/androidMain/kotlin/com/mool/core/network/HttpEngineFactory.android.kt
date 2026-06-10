package com.mool.core.network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

actual fun platformHttpEngine(): HttpClientEngine = OkHttp.create()
