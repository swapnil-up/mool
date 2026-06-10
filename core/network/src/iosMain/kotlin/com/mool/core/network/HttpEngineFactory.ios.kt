package com.mool.core.network

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun platformHttpEngine(): HttpClientEngine = Darwin.create()
