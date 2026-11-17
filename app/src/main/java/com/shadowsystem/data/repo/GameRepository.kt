package com.shadowsystem.data.repo

import com.shadowsystem.core.GateRules
import com.shadowsystem.core.AvatarData
import com.shadowsystem.core.ShopItem
import com.shadowsystem.core.DayRules
import com.shadowsystem.core.FinanceRules
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.GameMath.Stat
import com.shadowsystem.core.Mood
import com.shadowsystem.core.QuestPlanner
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.DailyScoreEntity
import com.shadowsystem.data.db.DungeonSessionEntity
import com.shadowsystem.core.RankTrial
import com.shadowsystem.core.ShopCatalog
import com.shadowsystem.core.BoostItem
import com.shadowsystem.core.DungeonRules
import kotlin.random.Random
import com.shadowsystem.core.RankTrialState
import com.shadowsystem.data.db.KeyValueEntity
import com.shadowsystem.data.db.ProfileEntity
import com.shadowsystem.data.db.QuestEntity
import com.shadowsystem.data.db.QuestStepEntity
import com.shadowsystem.data.db.StatProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

/** Result of ticking a step, used for UI feedback. */
data class ProgressResult(val xpGained: Int, val questFinished: Boolean, val levelBefore: Int, val levelAfter: Int)

/**
 * Owns all game state: profile, stats, quests, dungeon sessions and daily scores.
 * Quest XP: each step pays 20% of the reward spread across steps; the last step pays the rest.
 */
class GameRepository(
    private val db: AppDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val profileDao = db.profileDao()
    private val statDao = db.statDao()
    private val questDao = db.questDao()
    private val scoreDao = db.dailyScoreDao()
    private val dungeonDao = db.dungeonDao()
    private val kvDao = db.keyValueDao()

    fun today(): Long = DayRules.gameDay(clock())

    fun observeProfile(): Flow<ProfileEntity> = profileDao.observe().map { it ?: ProfileEntity() }

    fun observeStats(): Flow<Map<Stat, Long>> = statDao.observeAll().map { rows ->
        Stat.values().associateWith { s -> rows.firstOrNull { it.stat == s.name }?.xp ?: 0L }
    }

    fun observeQuests(day: Long): Flow<List<QuestEntity>> = questDao.observeDay(day)

    fun observeSteps(day: Long): Flow<List<QuestStepEntity>> = questDao.observeStepsForDay(day)

    fun observeScore(day: Long): Flow<DailyScoreEntity?> = scoreDao.observe(day)

    fun observeDungeonSeconds(day: Long): Flow<Int> = dungeonDao.observeSecondsForDay(day)

    suspend fun profile(): ProfileEntity = profileDao.get() ?: ProfileEntity()

    suspend fun scoreOf(day: Long): Int? = scoreDao.get(day)?.score

    /** Marks onboarding done and stores the answer to the focus question (0 = habits, 1 = money, 2 = health). */
    suspend fun finishOnboarding(focus: Int, struggle: Int, minutes: Int) {
        kvDao.upsert(KeyValueEntity(ONBOARDING_FOCUS_KEY, focus.toLong()))
        kvDao.upsert(KeyValueEntity(ONBOARDING_STRUGGLE_KEY, struggle.toLong()))
        kvDao.upsert(KeyValueEntity(ONBOARDING_MINUTES_KEY, minutes.toLong()))
        val p = profile()
        profileDao.upsert(p.copy(onboarded = true))
    }

    private suspend fun unlockedIndex(): Int = kvDao.get(RankTrial.KEY_UNLOCKED)?.value?.toInt() ?: 0

    private suspend fun trialStart(): Long = kvDao.get(RankTrial.KEY_START)?.value ?: 0L

    private suspend fun setKey(key: String, value: Long) = kvDao.upsert(KeyValueEntity(key, value))

    /** Current rank trial for the UI. Quests finished since the trial started count toward it. */
    suspend fun rankTrialState(): RankTrialState {
        val levelIdx = RankTrial.levelRankIndex(profile().totalXp)
        val unlocked = unlockedIndex()
        val pending = RankTrial.isPending(levelIdx, unlocked)
        val start = trialStart()
        val done = if (pending && start > 0) questDao.doneSince(start) else 0
        return RankTrialState(
            pending = pending,
            target = RankTrial.targetRank(unlocked),
            done = done,
            effective = RankTrial.effectiveRank(levelIdx, unlocked)
        )
    }

    /**
     * Starts, advances or completes the rank trial after XP or quest changes. Unlocks are permanent.
     * Recursion is bounded: each completed trial raises the unlocked index toward the level rank.
     */
    suspend fun updateRankTrial() {
        val day = today()
        val unlocked = unlockedIndex()
        val levelIdx = RankTrial.levelRankIndex(profile().totalXp)
        if (!RankTrial.isPending(levelIdx, unlocked)) {
            if (trialStart() != 0L) setKey(RankTrial.KEY_START, 0L)
            return
        }
        var start = trialStart()
        if (start == 0L || RankTrial.windowExpired(start, day)) {
            start = day
            setKey(RankTrial.KEY_START, start)
        }
        if (questDao.doneSince(start) >= RankTrial.TARGET_QUESTS) {
            setKey(RankTrial.KEY_UNLOCKED, (unlocked + 1).toLong())
            setKey(RankTrial.KEY_START, 0L)
            updateRankTrial()
        }
    }

    suspend fun isBoostActive(key: String): Boolean = kvDao.get(key)?.value == 1L

    suspend fun activeBoosts(): Set<String> =
        ShopCatalog.ITEMS.map { it.key }.filter { isBoostActive(it) }.toSet()

    /** Shop purchase. Returns false when gold is short or the boost is already active. */
    suspend fun buyBoost(item: BoostItem): Boolean {
        if (isBoostActive(item.key) || profile().gold < item.cost) return false
        setKey(item.key, 1L)
        addXp(0, "", gold = -item.cost)
        return true
    }

    /**
     * The first step of a quest consumes the quest boosts: XP ×1.5 and/or gold ×2.
     * Undoing that first step does not give the boost back.
     */
    private suspend fun applyQuestBoosts(quest: QuestEntity): QuestEntity {
        if (quest.stepsDone > 0) return quest
        val xpBoost = isBoostActive(ShopCatalog.QUEST_XP)
        val goldBoost = isBoostActive(ShopCatalog.QUEST_GOLD)
        if (!xpBoost && !goldBoost) return quest
        if (xpBoost) setKey(ShopCatalog.QUEST_XP, 0L)
        if (goldBoost) setKey(ShopCatalog.QUEST_GOLD, 0L)
        return quest.copy(
            xpReward = if (xpBoost) GameMath.applyMultiplier(quest.xpReward, ShopCatalog.QUEST_XP_MULTIPLIER) else quest.xpReward,
            goldReward = if (goldBoost) quest.goldReward * ShopCatalog.QUEST_GOLD_MULTIPLIER else quest.goldReward
        )
    }

    /** Generates the day's quests once. Returns false if they already exist. */
    suspend fun generateDay(mood: Mood): Boolean {
        val day = today()
        if (questDao.countForDay(day) > 0) return false
        val profile = profile()
        val comeback = DayRules.isComeback(profile.lastActiveDay, day)
        val level = GameMath.levelForXp(profile.totalXp)
        var comebackLeft = if (comeback) DayRules.COMEBACK_QUESTS else 0
        val cap = RankTrial.effectiveRank(RankTrial.levelRankIndex(profile.totalXp), unlockedIndex())
        for (planned in QuestPlanner.plan(level, mood, rankCap = cap)) {
            val t = planned.template
            val reward = GameMath.questReward(t.rank)
            val boosted = comebackLeft > 0
            val id = questDao.insertQuest(
                QuestEntity(
                    title = t.title,
                    description = t.description,
                    stat = t.stat.name,
                    rank = t.rank.name,
                    minutes = t.minutes,
                    mandatory = planned.mandatory,
                    lowEnergy = t.lowEnergy,
                    gameDay = day,
                    xpReward = reward.xp,
                    goldReward = reward.gold,
                    stepsTotal = t.steps.size,
                    comebackApplied = boosted,
                    soldierKey = t.soldierKey
                )
            )
            questDao.insertSteps(
                t.steps.mapIndexed { i, text -> QuestStepEntity(questId = id, position = i, text = text) }
            )
            if (boosted) comebackLeft--
        }
        scoreDao.upsert(DailyScoreEntity(day, 0, mood.name))
        return true
    }

    /** Ticks one step and pays its XP. Returns null if the step was already done. */
    suspend fun tickStep(step: QuestStepEntity): ProgressResult? {
        val stored = questDao.get(step.questId) ?: return null
        if (stored.status == QuestEntity.STATUS_DONE || step.done) return null
        val levelBefore = GameMath.levelForXp(profile().totalXp)
        questDao.updateStep(step.copy(done = true))
        val quest = applyQuestBoosts(stored)

        val multiplier = if (quest.comebackApplied) DayRules.COMEBACK_MULTIPLIER else 1.0
        val total = GameMath.applyMultiplier(quest.xpReward, multiplier)
        val k = quest.stepsDone + 1
        val n = quest.stepsTotal
        val finished = k >= n
        val cumulative = if (finished) total else (total * 0.2 * k / n).roundToInt()
        val gained = (cumulative - quest.xpAwarded).coerceAtLeast(0)

        questDao.updateQuest(
            quest.copy(
                stepsDone = k,
                xpAwarded = maxOf(cumulative, quest.xpAwarded),
                status = if (finished) QuestEntity.STATUS_DONE else quest.status,
                completedAt = if (finished) clock() else 0L
            )
        )
        addXp(gained, quest.stat, gold = if (finished) quest.goldReward else 0)
        if (finished) touchActiveDay()
        updateRankTrial()
        return ProgressResult(gained, finished, levelBefore, GameMath.levelForXp(profile().totalXp))
    }

    /** Reverts a step tick (the 5-second undo). Mirrors [tickStep] exactly. */
    suspend fun undoStep(step: QuestStepEntity, gained: Int, finished: Boolean, stat: String, gold: Int) {
        val quest = questDao.get(step.questId) ?: return
        questDao.updateStep(step.copy(done = false))
        questDao.updateQuest(
            quest.copy(
                stepsDone = (quest.stepsDone - 1).coerceAtLeast(0),
                xpAwarded = (quest.xpAwarded - gained).coerceAtLeast(0),
                status = if (finished) QuestEntity.STATUS_OPEN else quest.status,
                completedAt = if (finished) 0L else quest.completedAt
            )
        )
        addXp(-gained, stat, gold = if (finished) -gold else 0)
        updateRankTrial()
    }

    /** "Make it smaller": drops the undone steps after the next one, so only one step remains. */
    suspend fun makeSmaller(quest: QuestEntity) {
        val next = questDao.steps(quest.id).firstOrNull { !it.done } ?: return
        questDao.deleteUndoneStepsAfter(quest.id, next.position)
        questDao.updateQuest(quest.copy(stepsTotal = quest.stepsDone + 1))
    }

    /** Adds XP to total + stat, and gold. Negative values are used by undo. */
    suspend fun addXp(xp: Int, statName: String, gold: Int) {
        val p = profile()
        profileDao.upsert(
            p.copy(
                totalXp = (p.totalXp + xp).coerceAtLeast(0L),
                gold = (p.gold + gold).coerceAtLeast(0)
            )
        )
        if (xp != 0 && statName.isNotEmpty()) {
            val s = statDao.get(statName) ?: StatProgressEntity(statName)
            statDao.upsert(s.copy(xp = (s.xp + xp).coerceAtLeast(0L)))
        }
    }

    private suspend fun touchActiveDay() {
        val day = today()
        val p = profile()
        if (p.lastActiveDay == day) return
        profileDao.upsert(p.copy(lastActiveDay = day, activeDays = p.activeDays + 1))
    }

    /** Recomputes today's score from stored data, saves it and returns it. */
    suspend fun refreshScore(): Int {
        val day = today()
        val quests = questDao.forDay(day)
        val mood = scoreDao.get(day)?.mood ?: Mood.NORMAL.name
        val dungeonSeconds = dungeonDao.secondsForDay(day)
        val vitQuests = quests.filter { it.stat == Stat.VIT.name }
        val loggedMoney = db.transactionDao().dayCount(day) > 0
        val score = GameMath.dailyScore(
            GameMath.DailyScoreInput(
                completedXp = quests.filter { it.status == QuestEntity.STATUS_DONE }.sumOf { it.xpReward },
                plannedXp = quests.sumOf { it.xpReward },
                dungeonMinutes = dungeonSeconds / 60,
                dungeonGoalMinutes = DUNGEON_GOAL_MINUTES,
                loggedTransactionToday = loggedMoney,
                withinBudgetPace = !anyBudgetOver(),
                vitQuestsDone = vitQuests.count { it.status == QuestEntity.STATUS_DONE },
                vitQuestsPlanned = vitQuests.size,
                hardDay = mood == Mood.HARD.name
            )
        )
        scoreDao.upsert(DailyScoreEntity(day, score, mood, dungeonSeconds / 60, loggedMoney))
        awardShadowBonus(day, score)
        return score
    }

    /** Shadow of Yesterday: beating yesterday's score pays +10 Gold, once per day. */
    private suspend fun awardShadowBonus(day: Long, score: Int) {
        val yesterday = scoreDao.get(day - 1)?.score ?: return
        if (score <= yesterday) return
        if (kvDao.get(SHADOW_BONUS_KEY)?.value == day) return
        addXp(0, "", gold = GameMath.shadowBonus(score, yesterday))
        kvDao.upsert(KeyValueEntity(SHADOW_BONUS_KEY, day))
    }

    private suspend fun anyBudgetOver(): Boolean {
        val budgets = db.budgetDao().all().filter { it.limitRial > 0 }
        if (budgets.isEmpty()) return false
        val (from, to) = FinanceRules.monthRange(0, clock())
        val spent = db.transactionDao().range(from, to)
            .filter { !it.isIncome }
            .groupBy { it.category }
            .mapValues { e -> e.value.sumOf { it.amountRial } }
        return budgets.any { FinanceRules.budgetState(spent[it.category] ?: 0L, it.limitRial) == FinanceRules.BudgetState.OVER }
    }

    /**
     * Saves a dungeon run. Completed runs pay double the rank's quest XP to INT;
     * cancelled runs pay only the share of time spent.
     */
    /** Saves a dungeon run and pays XP and gold. Returns what was earned, for the loot screen. */
    suspend fun recordDungeon(
        rankName: String,
        plannedMinutes: Int,
        doneSeconds: Int,
        completed: Boolean,
        soldierKey: String? = null,
        questId: Long? = null
    ): DungeonResult {
        val rank = GameMath.Rank.valueOf(rankName)
        val full = GameMath.questReward(rank).xp * 2
        val xp = if (completed) full else GameMath.partialDungeonXp(plannedMinutes, doneSeconds / 60, full)
        val boosted = isBoostActive(ShopCatalog.DUNGEON)
        val paidXp = if (boosted && xp > 0) GameMath.applyMultiplier(xp, ShopCatalog.DUNGEON_MULTIPLIER) else xp
        if (boosted && xp > 0) setKey(ShopCatalog.DUNGEON, 0L)
        val gold = if (completed) GameMath.questReward(rank).gold else 0
        val rare = completed && DungeonRules.isRareLoot(Random.nextInt(100))
        val id = dungeonDao.insert(
            DungeonSessionEntity(
                gameDay = today(),
                rank = rankName,
                plannedMinutes = plannedMinutes,
                doneSeconds = doneSeconds,
                completed = completed,
                xpGained = paidXp,
                createdAt = clock(),
                soldierKey = soldierKey,
                questId = questId,
                rareLoot = rare
            )
        )
        if (rare) setKey(RARE_LOOT_KEY, (kvDao.get(RARE_LOOT_KEY)?.value ?: 0L) + 1)
        if (paidXp > 0) addXp(paidXp, Stat.INT.name, gold = gold)
        if (doneSeconds >= 60) touchActiveDay()
        updateRankTrial()
        return DungeonResult(sessionId = id, xp = paidXp, gold = gold, rare = rare)
    }

    /** Stores the optional journal note ("what did I build?") for a finished run. */
    /** Gate length chosen by the user, per rank (kv `gate_minutes_<RANK>`). */
    suspend fun gateMinutes(): Map<String, Int> = GameMath.Rank.values().mapNotNull { r ->
        kvDao.get(gateKey(r.name))?.value?.let { r.name to it.toInt() }
    }.toMap()

    suspend fun setGateMinutes(rankName: String, minutes: Int) = setKey(gateKey(rankName), GateRules.clamp(minutes).toLong())

    suspend fun setQuestSoldier(questId: Long, key: String?) = questDao.setSoldier(questId, key)

    /** Everything the full daily report shows for one game day. */
    suspend fun dailyReport(day: Long): DayReport {
        val quests = questDao.forDay(day)
        val sessions = dungeonDao.sessionsForDay(day)
        return DayReport(
            score = scoreDao.get(day)?.score,
            yesterdayScore = scoreDao.get(day - 1)?.score,
            dungeonMinutes = sessions.sumOf { it.doneSeconds } / 60,
            dungeonsCompleted = sessions.count { it.completed },
            rareLoot = sessions.count { it.rareLoot },
            questsDone = quests.count { it.status == QuestEntity.STATUS_DONE },
            questsTotal = quests.size,
            xpToday = quests.sumOf { it.xpAwarded } + sessions.sumOf { it.xpGained }
        )
    }

    suspend fun saveDungeonNote(sessionId: Long, note: String) {
        dungeonDao.setNote(sessionId, note.trim().take(NOTE_MAX_LENGTH).ifBlank { null })
    }

    private val KEY_CLASS_WEEK = "class_week"
    private val KEY_CLASS_A = "class_a"
    private val KEY_CLASS_B = "class_b"

    private fun ownedKey(key: String) = "owned_$key"
    private fun equipKey(category: String, key: String) = "equip_${category}_$key"

    suspend fun isOwned(key: String): Boolean = kvDao.get(ownedKey(key))?.value == 1L

    suspend fun ownedAvatarKeys(): Set<String> {
        val out = mutableSetOf<String>()
        for (item in AvatarData.SHOP_ITEMS) if (isOwned(item.key)) out.add(item.key)
        return out
    }

    suspend fun equippedAvatarKeys(): Set<String> {
        val out = mutableSetOf<String>()
        for (item in AvatarData.SHOP_ITEMS) {
            if (kvDao.get(equipKey(item.category, item.key))?.value == 1L) out.add(item.key)
        }
        return out
    }

    /** Buys an avatar item with Gold. Heavy items are never sold for Gold. Returns false when not allowed. */
    suspend fun buyAvatarItem(item: ShopItem): Boolean {
        if (item.heavy || isOwned(item.key) || profile().gold < item.priceGold) return false
        setKey(ownedKey(item.key), 1L)
        addXp(0, "", gold = -item.priceGold)
        return true
    }

    /** Puts on an owned item (one per category). Tapping the worn item takes it off. */
    suspend fun toggleEquip(item: ShopItem): Boolean {
        if (!isOwned(item.key)) return false
        val wasOn = kvDao.get(equipKey(item.category, item.key))?.value == 1L
        for (other in AvatarData.SHOP_ITEMS) {
            if (other.category == item.category) setKey(equipKey(other.category, other.key), 0L)
        }
        if (!wasOn) setKey(equipKey(item.category, item.key), 1L)
        return true
    }

    /** The class stats are fixed for each week (7 game days) and recomputed when a new week starts. */
    suspend fun weeklyTopStats(current: Set<Stat>): Set<Stat> {
        if (current.size != 2) return current
        val week = today() / 7
        if (kvDao.get(KEY_CLASS_WEEK)?.value == week) {
            val a = kvDao.get(KEY_CLASS_A)?.value?.toInt()
            val b = kvDao.get(KEY_CLASS_B)?.value?.toInt()
            if (a != null && b != null) return setOf(Stat.values()[a], Stat.values()[b])
        }
        val pair = current.toList()
        setKey(KEY_CLASS_WEEK, week)
        setKey(KEY_CLASS_A, pair[0].ordinal.toLong())
        setKey(KEY_CLASS_B, pair[1].ordinal.toLong())
        return current
    }

    companion object {
        const val ONBOARDING_FOCUS_KEY = "onboarding_focus"
        const val ONBOARDING_STRUGGLE_KEY = "onboarding_struggle"
        const val ONBOARDING_MINUTES_KEY = "onboarding_minutes"
        private fun gateKey(rank: String) = "gate_minutes_$rank"
        const val DUNGEON_GOAL_MINUTES = 25
        const val RARE_LOOT_KEY = "rare_loot_count"
        const val NOTE_MAX_LENGTH = 140
        const val SHADOW_BONUS_KEY = "shadow_bonus_day"
    }
}

/** Result of a finished dungeon run. */
data class DungeonResult(val sessionId: Long, val xp: Int, val gold: Int, val rare: Boolean)
