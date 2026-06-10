package com.mool.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FxRateResponse(
    @SerialName("base_code")
    val baseCode: String,
    val rates: Map<String, Double>,
    @SerialName("time_last_update_unix")
    val timeLastUpdateUnix: Long,
)
