package com.shadowsystem.core

/** The user's saved choices for one soldier: custom name and summon time (null = still an egg). */
data class SavedSoldier(val customName: String?, val summonedAt: Long?)

/** One soldier as shown in the roster. */
data class SoldierRow(
    val def: SoldierDef,
    val name: String,
    val customName: String?,
    val summoned: Boolean,
    val locked: Boolean,
    val progress: SoldierProgress.Progress,
    val canSummon: Boolean
) {
    /** Eggs show no stage (0). */
    val displayStage: Int get() = if (summoned) progress.stage else 0
}

object SoldierRoster {
    const val BELLION_KEY = "bellion"
    const val BELLION_ACTIVE_DAYS = 100

    /**
     * Builds the roster. Bellion (overall consistency) counts every linked quest and stays locked
     * until the user reaches rank S and has [BELLION_ACTIVE_DAYS] active days in total.
     */
    fun build(
        defs: List<SoldierDef>,
        saved: Map<String, SavedSoldier>,
        linked: List<LinkedQuest>,
        totalActiveDays: Int,
        isRankS: Boolean,
        today: Long
    ): List<SoldierRow> = defs.map { def ->
        val mine = if (def.key == BELLION_KEY) linked else linked.filter { it.soldierKey == def.key }
        val progress = SoldierProgress.progress(mine, today)
        val entry = saved[def.key]
        val summoned = entry?.summonedAt != null
        val locked = def.key == BELLION_KEY && !(isRankS && totalActiveDays >= BELLION_ACTIVE_DAYS)
        SoldierRow(
            def = def,
            name = entry?.customName ?: def.name,
            customName = entry?.customName,
            summoned = summoned,
            locked = locked,
            progress = progress,
            canSummon = !summoned && !locked && progress.activeDays >= SoldierProgress.SUMMON_ACTIVE_DAYS
        )
    }
}
