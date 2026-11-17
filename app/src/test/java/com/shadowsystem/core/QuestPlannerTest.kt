package com.shadowsystem.core

import com.shadowsystem.core.GameMath.Rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuestPlannerTest {

    @Test
    fun greatDayHasThreeMandatoryAndFiveOptional() {
        val plan = QuestPlanner.plan(1, Mood.GREAT, Random(1))
        assertEquals(3, plan.count { it.mandatory })
        assertEquals(5, plan.count { !it.mandatory })
    }

    @Test
    fun normalDayHasThreeAndThree() {
        val plan = QuestPlanner.plan(1, Mood.NORMAL, Random(2))
        assertEquals(3, plan.count { it.mandatory })
        assertEquals(3, plan.count { !it.mandatory })
    }

    @Test
    fun hardDayIsOneAndOneLowEnergyOrEasy() {
        val plan = QuestPlanner.plan(1, Mood.HARD, Random(3))
        assertEquals(2, plan.size)
        assertTrue(plan.all { it.template.lowEnergy || it.template.rank == Rank.E })
    }

    @Test
    fun noDuplicatesAndRankCapped() {
        val plan = QuestPlanner.plan(1, Mood.GREAT, Random(4))
        assertEquals(plan.size, plan.map { it.template.title }.toSet().size)
        assertTrue(plan.all { it.template.rank == Rank.E || it.template.rank == Rank.D })
    }

    @Test
    fun templatesHaveSensibleSteps() {
        assertTrue(Templates.ALL.size >= 40)
        Templates.ALL.forEach { t ->
            assertTrue("steps ${t.title}", t.steps.size in 1..5)
            assertTrue("minutes ${t.title}", t.minutes in 1..60)
        }
    }
}
