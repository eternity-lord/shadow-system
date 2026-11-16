package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DungeonRulesTest {
    @Test
    fun monsterHpDrainsWithTime() {
        assertEquals(100, DungeonRules.monsterHpPercent(900, 0))
        assertEquals(50, DungeonRules.monsterHpPercent(900, 450))
        assertEquals(0, DungeonRules.monsterHpPercent(900, 900))
        assertEquals(0, DungeonRules.monsterHpPercent(900, 2000))
        assertEquals(0, DungeonRules.monsterHpPercent(0, 10))
    }

    @Test
    fun rareLootChanceIsTenPercent() {
        assertTrue(DungeonRules.isRareLoot(9))
        assertFalse(DungeonRules.isRareLoot(10))
    }
}
