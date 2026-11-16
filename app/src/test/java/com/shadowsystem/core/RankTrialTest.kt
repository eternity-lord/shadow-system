package com.shadowsystem.core

import com.shadowsystem.core.GameMath.Rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RankTrialTest {
    @Test
    fun pendingOnlyWhenLevelRankIsAboveUnlocked() {
        assertFalse(RankTrial.isPending(levelIdx = 0, unlockedIdx = 0))
        assertTrue(RankTrial.isPending(levelIdx = 1, unlockedIdx = 0))
        assertFalse(RankTrial.isPending(levelIdx = 1, unlockedIdx = 1))
    }

    @Test
    fun effectiveRankIsCappedByUnlockedRank() {
        assertEquals(Rank.E, RankTrial.effectiveRank(levelIdx = 3, unlockedIdx = 0))
        assertEquals(Rank.C, RankTrial.effectiveRank(levelIdx = 3, unlockedIdx = 2))
        assertEquals(Rank.B, RankTrial.effectiveRank(levelIdx = 2, unlockedIdx = 5))
    }

    @Test
    fun targetIsTheNextRankAndStopsAtS() {
        assertEquals(Rank.D, RankTrial.targetRank(0))
        assertEquals(Rank.S, RankTrial.targetRank(4))
        assertEquals(Rank.S, RankTrial.targetRank(5))
    }

    @Test
    fun windowExpiresAfterSevenDays() {
        assertFalse(RankTrial.windowExpired(start = 100, today = 106))
        assertTrue(RankTrial.windowExpired(start = 100, today = 107))
    }
}
