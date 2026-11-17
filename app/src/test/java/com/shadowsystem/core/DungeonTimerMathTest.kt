package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DungeonTimerMathTest {
    @Test
    fun pauseTimeDoesNotCountAsWork() {
        // started at 0, paused at 60 s, resumed at 100 s, now 130 s: 60 s of work + 30 s running = 90 s
        assertEquals(90, DungeonTimerMath.elapsedSeconds(0L, 40_000L, null, 130_000L))
        // still paused since 60 s with 0 earlier pause: work is frozen at 60 s
        assertEquals(60, DungeonTimerMath.elapsedSeconds(0L, 0L, 60_000L, 500_000L))
    }

    @Test
    fun pausedSecondsIncludesOngoingPause() {
        assertEquals(40, DungeonTimerMath.pausedSeconds(10_000L, 50_000L, 80_000L))
        assertEquals(0, DungeonTimerMath.pausedSeconds(0L, null, 80_000L))
    }

    @Test
    fun regenIsCappedAndCosmetic() {
        assertEquals(0, DungeonTimerMath.regenPercent(29))
        assertEquals(1, DungeonTimerMath.regenPercent(30))
        assertEquals(10, DungeonTimerMath.regenPercent(10_000))
        assertEquals(100, DungeonTimerMath.displayedHpPercent(900, 0, 0))
        assertEquals(70, DungeonTimerMath.displayedHpPercent(900, 360, 600)) // 60% left + 10% regen cap
    }
}
