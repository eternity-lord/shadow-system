package com.shadowsystem.core

/** One completed quest linked to a soldier (a habit). */
data class LinkedQuest(val soldierKey: String, val gameDay: Long, val title: String, val stepsDone: Int)

/**
 * Soldier progress is derived from completed linked quests, so it can never drift from the quest history.
 * - XP: each completed linked quest gives [XP_PER_LINKED_QUEST]; level uses the spec curve round(50·n^1.3).
 * - Power (active days): distinct game days with a completed linked quest.
 * - Wisdom: variety (distinct quest titles ×2) plus completed steps.
 * - State: gap since the last active day (0–1 awake, 2–4 tired, 5+ sleeping). Sleeping is cosmetic.
 */
object SoldierProgress {
    const val XP_PER_LINKED_QUEST = 20L
    const val SUMMON_ACTIVE_DAYS = 7
    const val TIRED_AFTER_DAYS = 2L
    const val SLEEP_AFTER_DAYS = 5L

    enum class State { AWAKE, TIRED, SLEEPING }

    data class Progress(
        val linkedDone: Int,
        val activeDays: Int,
        val xp: Long,
        val level: Int,
        val levelXp: Long,
        val levelNeed: Int,
        val stage: Int,
        val wisdomScore: Int,
        val wisdomTier: Int,
        val lastActiveDay: Long?,
        val state: State
    )

    fun progress(quests: List<LinkedQuest>, today: Long): Progress {
        val xp = quests.size * XP_PER_LINKED_QUEST
        val level = GameMath.soldierLevelForXp(xp)
        var into = xp
        for (n in 1 until level) into -= GameMath.soldierXpForLevel(n)
        val last = quests.maxOfOrNull { it.gameDay }
        val wisdom = quests.map { it.title }.distinct().size * 2 + quests.sumOf { it.stepsDone }
        return Progress(
            linkedDone = quests.size,
            activeDays = quests.map { it.gameDay }.distinct().size,
            xp = xp,
            level = level,
            levelXp = into,
            levelNeed = GameMath.soldierXpForLevel(level),
            stage = GameMath.soldierStage(level),
            wisdomScore = wisdom,
            wisdomTier = wisdomTier(wisdom),
            lastActiveDay = last,
            state = stateFor(last, today)
        )
    }

    /** Wisdom overlay tier: 0 none, 1 simple, 2 medium, 3 full. */
    fun wisdomTier(score: Int): Int = when {
        score >= 100 -> 3
        score >= 40 -> 2
        score >= 10 -> 1
        else -> 0
    }

    fun stateFor(lastActiveDay: Long?, today: Long): State {
        if (lastActiveDay == null) return State.SLEEPING
        val gap = today - lastActiveDay
        return when {
            gap >= SLEEP_AFTER_DAYS -> State.SLEEPING
            gap >= TIRED_AFTER_DAYS -> State.TIRED
            else -> State.AWAKE
        }
    }
}
