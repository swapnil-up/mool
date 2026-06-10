package com.mool.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSetting(key: String): Flow<String?>
    suspend fun setSetting(key: String, value: String)
}
