package com.shadowsystem.core

/** Unlock rules for titles and achievements. Derived from facts, so nothing needs to be stored. */
object TitleRules {
    data class Facts(
        val level: Int,
        val activeDays: Int,
        val rankIndex: Int,
        val questsDone: Int,
        val dungeonsCompleted: Int,
        val transactions: Int,
        val bestScore: Int,
        val beatYesterday: Boolean,
        val goalDone: Boolean,
        val soldiersSummoned: Int,
        val bossDone: Boolean
    )

    /** early_bird needs time-of-day history, which is not tracked yet, so it stays locked. */
    fun titles(f: Facts): Set<String> = buildSet {
        if (f.level >= 5) add("awakened")
        if (f.activeDays >= 10) add("unbroken")
        if (f.transactions >= 30) add("shadow_accountant")
        if (f.dungeonsCompleted >= 10) add("dungeon_runner")
        if (f.rankIndex >= 5) add("monarch")
    }

    fun achievements(f: Facts): Set<String> = buildSet {
        if (f.questsDone >= 1) add("first_quest")
        if (f.dungeonsCompleted >= 1) add("first_dungeon")
        if (f.soldiersSummoned >= 1) add("first_soldier")
        if (f.bestScore >= 80) add("score_80")
        if (f.beatYesterday) add("beat_yesterday")
        if (f.goalDone) add("goal_done")
        if (f.bossDone) add("boss_done")
        if (f.rankIndex >= 2) add("rank_c")
        if (f.rankIndex >= 5) add("rank_s")
        if (f.activeDays >= 100) add("hundred_days")
    }

    /** Mood pose from today's score: standing, determined, tired but satisfied, low energy. Cosmetic only. */
    fun pose(score: Int?): Int = when {
        score == null -> 0
        score >= 80 -> 1
        score >= 50 -> 2
        score >= 20 -> 3
        else -> 4
    }
}
