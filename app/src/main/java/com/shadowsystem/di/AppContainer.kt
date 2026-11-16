package com.shadowsystem.di

import android.content.Context
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.MoneyRepository

/** Manual dependency container (no DI framework; keeps the build dependency-light). */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.create(context.applicationContext)
    val game: GameRepository = GameRepository(database)
    val money: MoneyRepository = MoneyRepository(database, game)
}
