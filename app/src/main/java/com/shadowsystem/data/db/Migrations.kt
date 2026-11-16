package com.shadowsystem.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Schema history. Version 1 shipped only in development builds; v2 adds planning tables. */
object Migrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`category` TEXT NOT NULL, `limitRial` INTEGER NOT NULL, PRIMARY KEY(`category`))")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `recurring_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, `amountRial` INTEGER NOT NULL, `category` TEXT NOT NULL, " +
                    "`isIncome` INTEGER NOT NULL, `monthly` INTEGER NOT NULL, `anchorEpochDay` INTEGER NOT NULL, " +
                    "`posted` INTEGER NOT NULL, `active` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `savings_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, `targetRial` INTEGER NOT NULL, `savedRial` INTEGER NOT NULL, " +
                    "`createdDay` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, `totalRial` INTEGER NOT NULL, `paidRial` INTEGER NOT NULL, " +
                    "`iOwe` INTEGER NOT NULL)"
            )
            db.execSQL("CREATE TABLE IF NOT EXISTS `kv` (`key` TEXT NOT NULL, `value` INTEGER NOT NULL, PRIMARY KEY(`key`))")
        }
    }
}
