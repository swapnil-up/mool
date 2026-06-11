package com.mool.feature.remittance

sealed interface RemittanceIntent {
    data class SetSendAmount(val value: String) : RemittanceIntent
    data class SetSendCurrency(val value: String) : RemittanceIntent
    data class SetReceiveCurrency(val value: String) : RemittanceIntent
    data class SetFeePercent(val value: String) : RemittanceIntent
    data class SetMarginPercent(val value: String) : RemittanceIntent
    data object Calculate : RemittanceIntent
    data object DismissError : RemittanceIntent
}
