package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SoldierStageTest {
    @Test
    fun stageBoundariesMatchSpec() {
        assertEquals(1, SoldierData.stageForLevel(1))
        assertEquals(1, SoldierData.stageForLevel(9))
        assertEquals(2, SoldierData.stageForLevel(10))
        assertEquals(2, SoldierData.stageForLevel(24))
        assertEquals(3, SoldierData.stageForLevel(25))
        assertEquals(4, SoldierData.stageForLevel(50))
        assertEquals(4, SoldierData.stageForLevel(79))
        assertEquals(5, SoldierData.stageForLevel(80))
    }
}
