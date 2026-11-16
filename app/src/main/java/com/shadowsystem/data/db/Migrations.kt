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

    /** v3: dungeon journal note column and the inbox table. Nullable column, no default, matching the entity. */
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `dungeon_sessions` ADD COLUMN `note` TEXT")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `inbox` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`text` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
            )
        }
    }

    /** v4: soldier link on quests, and the soldiers table (custom name and summon time). */
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `quests` ADD COLUMN `soldierKey` TEXT")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `soldiers` (`key` TEXT NOT NULL, `customName` TEXT, " +
                    "`summonedAt` INTEGER, PRIMARY KEY(`key`))"
            )
        }
    }

    /** v5: wallets, optional wallet on transactions (null = first wallet), transfers and cheques. */
    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `wallets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, `openingRial` INTEGER NOT NULL)"
            )
            db.execSQL("ALTER TABLE `transactions` ADD COLUMN `walletId` INTEGER")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `transfers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`fromWalletId` INTEGER NOT NULL, `toWalletId` INTEGER NOT NULL, `amountRial` INTEGER NOT NULL, " +
                    "`epochDay` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `cheques` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT NOT NULL, `amountRial` INTEGER NOT NULL, `dueEpochDay` INTEGER NOT NULL, " +
                    "`issuedByMe` INTEGER NOT NULL, `cleared` INTEGER NOT NULL)"
            )
        }
    }
}
