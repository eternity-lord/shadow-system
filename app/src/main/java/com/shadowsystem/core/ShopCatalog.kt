package com.shadowsystem.core

/** One gold-shop item. A boost is consumed by the next event it applies to. */
data class BoostItem(val key: String, val cost: Int)

/** Gold shop catalog. Boost keys are stored in the key-value table. */
object ShopCatalog {
    const val DUNGEON = "dungeon_boost"
    const val QUEST_XP = "quest_boost"
    const val QUEST_GOLD = "gold_boost"
    const val DUNGEON_MULTIPLIER = 1.5
    const val QUEST_XP_MULTIPLIER = 1.5
    const val QUEST_GOLD_MULTIPLIER = 2

    val ITEMS: List<BoostItem> = listOf(
        BoostItem(DUNGEON, 40),
        BoostItem(QUEST_XP, 60),
        BoostItem(QUEST_GOLD, 50)
    )
}
