package com.mool.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.mool.core.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    db: MoolDatabase,
) : SettingsRepository {

    private val queries = db.appSettingsQueries

    override fun observeSetting(key: String): Flow<String?> {
        return queries.getSetting(key)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
    }

    override suspend fun setSetting(key: String, value: String) {
        withContext(Dispatchers.Default) {
            queries.setSetting(key, value)
        }
    }
}
