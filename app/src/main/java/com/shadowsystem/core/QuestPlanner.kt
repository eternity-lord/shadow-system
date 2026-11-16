package com.shadowsystem.core

import com.shadowsystem.core.GameMath.Rank
import kotlin.random.Random

/** Daily mood chosen by the user; decides how many quests are generated. */
enum class Mood(val mandatory: Int, val optional: Int) {
    GREAT(3, 5),
    NORMAL(3, 3),
    HARD(1, 1);

    val isHard: Boolean get() = this == HARD
}

data class PlannedQuest(val template: QuestTemplate, val mandatory: Boolean)

/** Builds the day's quests from templates. Pure logic, no Android dependencies. */
object QuestPlanner {

    private val rankOrder = listOf(Rank.E, Rank.D, Rank.C, Rank.B, Rank.A, Rank.S)

    fun maxRankFor(level: Int): Rank = GameMath.rankForLevel(level)

    fun plan(level: Int, mood: Mood, random: Random = Random): List<PlannedQuest> {
        val maxRank = maxRankFor(level)
        val eligible = Templates.ALL.filter { rankOrder.indexOf(it.rank) <= rankOrder.indexOf(maxRank) }
        if (mood.isHard) {
            val easy = eligible.filter { it.lowEnergy || it.rank == Rank.E }.shuffled(random)
            val pool = easy.ifEmpty { eligible.shuffled(random) }
            return pool.take(mood.mandatory).map { PlannedQuest(it, true) } +
                pool.drop(mood.mandatory).take(mood.optional).map { PlannedQuest(it, false) }
        }
        val shuffled = eligible.shuffled(random)
        // Mandatory: prefer distinct stats so the day covers the body and mind.
        val mandatory = mutableListOf<QuestTemplate>()
        for (t in shuffled) {
            if (mandatory.size == mood.mandatory) break
            if (mandatory.none { it.stat == t.stat }) mandatory += t
        }
        for (t in shuffled) {
            if (mandatory.size == mood.mandatory) break
            if (t !in mandatory) mandatory += t
        }
        val optional = shuffled.filter { it !in mandatory }.take(mood.optional)
        return mandatory.map { PlannedQuest(it, true) } + optional.map { PlannedQuest(it, false) }
    }
}
