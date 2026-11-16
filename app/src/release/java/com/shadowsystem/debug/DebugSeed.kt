package com.shadowsystem.debug

import com.shadowsystem.data.db.AppDatabase

/** Release builds ship no seed data. The settings button that calls this is only shown in debug builds. */
@Suppress("UNUSED_PARAMETER")
object DebugSeed {
    suspend fun seed(db: AppDatabase) = Unit
}
