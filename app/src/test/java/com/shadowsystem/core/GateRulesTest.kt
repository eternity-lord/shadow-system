package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Test

class GateRulesTest {
    @Test
    fun clampRoundsToStepAndStaysInRange() {
        assertEquals(5, GateRules.clamp(0))
        assertEquals(120, GateRules.clamp(500))
        assertEquals(15, GateRules.clamp(17))
        assertEquals(20, GateRules.clamp(18))
    }

    @Test
    fun defaultsToRankLengthWithoutOverride() {
        assertEquals(GameMath.dungeonMinutes(GameMath.Rank.E), GateRules.minutesFor(GameMath.Rank.E, emptyMap()))
        assertEquals(45, GateRules.minutesFor(GameMath.Rank.E, mapOf("E" to 43)))
    }
}
