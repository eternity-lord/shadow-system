package com.shadowsystem.di

import android.content.Context
import com.shadowsystem.data.backup.BackupService
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.prefs.AppPrefs
import com.shadowsystem.data.prefs.LockState
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.InboxRepository
import com.shadowsystem.data.repo.MoneyRepository
import com.shadowsystem.data.repo.PlanRepository

/** Manual dependency container (no DI framework; keeps the build dependency-light). */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.create(context.applicationContext)
    val game = GameRepository(database)
    val money = MoneyRepository(database, game)
    val plan = PlanRepository(database, game)
    val inbox = InboxRepository(database)
    val soldiers = com.shadowsystem.data.repo.SoldierRepository(database)
    val achievements = com.shadowsystem.data.repo.AchievementRepository(database, game)
    val boss = com.shadowsystem.data.repo.BossRepository(database, game)
    val timer = com.shadowsystem.util.DungeonTimerStore(context.applicationContext)
    val backup = BackupService(database)
    val prefs = AppPrefs(context.applicationContext)
    val lock = LockState(initiallyLocked = prefs.isPinSet)
}
