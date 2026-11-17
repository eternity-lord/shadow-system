package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoldierRosterTest {
    private fun quests(key: String, days: Int) = (0 until days).map { LinkedQuest(key, it.toLong(), "t$it", 1) }

    private fun roster(
        linked: List<LinkedQuest>,
        saved: Map<String, SavedSoldier> = emptyMap(),
        totalDays: Int = 0,
        rankS: Boolean = false
    ) = SoldierRoster.build(SoldierData.ALL, saved, linked, totalDays, rankS, today = 100L)

    private fun row(rows: List<SoldierRow>, key: String) = rows.first { it.def.key == key }

    @Test
    fun rosterHasTwelveSoldiersAsEggs() {
        val rows = roster(emptyList())
        assertEquals(12, rows.size)
        assertTrue(rows.all { !it.summoned && it.displayStage == 0 })
    }

    @Test
    fun summonUnlocksAtSevenActiveDays() {
        assertFalse(row(roster(quests("igris", 6)), "igris").canSummon)
        assertTrue(row(roster(quests("igris", 7)), "igris").canSummon)
    }

    @Test
    fun summonedSoldierCannotSummonAgain() {
        val saved = mapOf("igris" to SavedSoldier(customName = null, summonedAt = 1L))
        val r = row(roster(quests("igris", 9), saved), "igris")
        assertTrue(r.summoned)
        assertFalse(r.canSummon)
        assertEquals(1, r.displayStage)
    }

    @Test
    fun renameKeepsDefaultNameWhenBlank() {
        val named = mapOf("tank" to SavedSoldier(customName = "کوه", summonedAt = null))
        assertEquals("کوه", row(roster(emptyList(), named), "tank").name)
        assertEquals("تانک", row(roster(emptyList()), "tank").name)
    }

    @Test
    fun linkedQuestsOnlyCountForTheirSoldier() {
        val rows = roster(quests("igris", 4))
        assertEquals(4, row(rows, "igris").progress.linkedDone)
        assertEquals(0, row(rows, "beru").progress.linkedDone)
    }

    @Test
    fun bellionNeedsRankSAndHundredActiveDays() {
        val all = quests("igris", 100)
        assertTrue(row(roster(all, totalDays = 100, rankS = false), "bellion").locked)
        assertTrue(row(roster(all, totalDays = 99, rankS = true), "bellion").locked)
        assertFalse(row(roster(all, totalDays = 100, rankS = true), "bellion").locked)
    }
}
