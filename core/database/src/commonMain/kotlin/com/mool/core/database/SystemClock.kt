package com.mool.core.database

import com.mool.core.domain.clock.Clock
class SystemClock : Clock {
    override fun now(): Long = currentTimeMillis()
}
