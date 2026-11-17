package com.shadowsystem.ui.vm

import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.BoostItem
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.RankTrialState
import com.shadowsystem.core.GameMath.Rank
import com.shadowsystem.core.GameMath.Stat
import com.shadowsystem.core.Mood
import com.shadowsystem.core.PhraseBank
import com.shadowsystem.data.db.DailyScoreEntity
import com.shadowsystem.data.db.ProfileEntity
import com.shadowsystem.data.db.QuestEntity
import com.shadowsystem.data.db.QuestStepEntity
import com.shadowsystem.data.repo.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.shadowsystem.core.DungeonTimerMath
import com.shadowsystem.util.DungeonTimerStore

/** Active dungeon run. Uses wall-clock start so backgrounding does not drift the timer. */
data class DungeonRun(
    val rank: Rank,
    val plannedMinutes: Int,
    val startedAtMillis: Long,
    val elapsedSeconds: Int = 0,
    val paused: Boolean = false,
    val pausedSeconds: Int = 0,
    val soldierKey: String? = null,
    val questId: Long? = null
) {
    val plannedSeconds: Int get() = plannedMinutes * 60
    val finished: Boolean get() = elapsedSeconds >= plannedSeconds
}

/** The last ticked step, kept for [UNDO_WINDOW_MS] so the user can take it back. */
data class UndoInfo(
    val step: QuestStepEntity,
    val gained: Int,
    val finished: Boolean,
    val stat: String,
    val gold: Int
)

data class NowState(
    val loaded: Boolean = false,
    val profile: ProfileEntity = ProfileEntity(),
    val quests: List<QuestEntity> = emptyList(),
    val steps: List<QuestStepEntity> = emptyList(),
    val score: DailyScoreEntity? = null,
    val dungeonSeconds: Int = 0,
    val message: String? = null,
    val run: DungeonRun? = null,
    val maxRank: Rank = Rank.E,
    val undo: UndoInfo? = null,
    val trial: RankTrialState = RankTrialState(),
    val boosts: Set<String> = emptySet(),
    val loot: LootInfo? = null
)

private data class SideState(val trial: RankTrialState, val boosts: Set<String>)

/** Loot from the last finished dungeon run, shown until the user closes it. */
data class LootInfo(val sessionId: Long, val xp: Int, val gold: Int, val rare: Boolean)

class GameViewModel(private val game: GameRepository, private val timer: DungeonTimerStore) : ViewModel() {

    private val day = game.today()
    private val message = MutableStateFlow<String?>(null)
    private val run = MutableStateFlow<DungeonRun?>(null)
    private val undo = MutableStateFlow<UndoInfo?>(null)
    private val refresh = MutableStateFlow(0)
    private val loot = MutableStateFlow<LootInfo?>(null)
    private var ticker: Job? = null
    private var undoTimer: Job? = null

    val state: StateFlow<NowState> = combine(
        game.observeProfile(),
        game.observeQuests(day),
        game.observeSteps(day),
        game.observeScore(day),
        game.observeDungeonSeconds(day),
        message,
        run,
        undo,
        trialFlow(),
        loot
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val profile = values[0] as ProfileEntity
        @Suppress("UNCHECKED_CAST")
        val quests = values[1] as List<QuestEntity>
        @Suppress("UNCHECKED_CAST")
        val steps = values[2] as List<QuestStepEntity>
        val score = values[3] as DailyScoreEntity?
        val dungeonSeconds = values[4] as Int
        val msg = values[5] as String?
        val activeRun = values[6] as DungeonRun?
        val undoInfo = values[7] as UndoInfo?
        @Suppress("UNCHECKED_CAST")
        val side = values[8] as SideState
        val trial = side.trial
        @Suppress("UNCHECKED_CAST")
        val lootInfo = values[9] as LootInfo?
        NowState(
            loaded = true,
            profile = profile,
            quests = quests,
            steps = steps,
            score = score,
            dungeonSeconds = dungeonSeconds,
            message = msg,
            run = activeRun,
            maxRank = trial.effective,
            undo = undoInfo,
            trial = trial,
            boosts = side.boosts,
            loot = lootInfo
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NowState())

    /** Re-reads the rank trial whenever profile or quests change. */
    /** Forces the derived rank-trial and boost state to be re-read after an action finishes. */
    private fun bump() {
        refresh.value = refresh.value + 1
    }

    private fun trialFlow() = merge(game.observeProfile().map { }, game.observeQuests(day).map { }, refresh)
        .onStart { emit(Unit) }
        .map { SideState(game.rankTrialState(), game.activeBoosts()) }

    val stats: StateFlow<Map<Stat, Long>> = game.observeStats()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val yesterday = MutableStateFlow<Int?>(null)
    val yesterdayScore: StateFlow<Int?> = yesterday

    init {
        viewModelScope.launch { yesterday.value = game.scoreOf(day - 1) }
        // Process was killed during a run: restore it and restart the notification service.
        if (timer.snapshot.value != null) {
            timer.ensureService()
            startTicker()
        }
    }

    fun generate(mood: Mood) {
        viewModelScope.launch {
            game.generateDay(mood)
            bump()
            message.value = if (mood == Mood.HARD) PhraseBank.hardDay() else PhraseBank.morning()
        }
    }

    /** Shop purchase. [onResult] gets false when gold is short or the boost is already active. */
    fun buyBoost(item: BoostItem, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = game.buyBoost(item)
            bump()
            onResult(ok)
        }
    }

    /** Closes the loot screen. A non-blank journal note is saved to that run. */
    fun dismissLoot(note: String) {
        val current = loot.value ?: return
        loot.value = null
        viewModelScope.launch {
            if (note.isNotBlank()) game.saveDungeonNote(current.sessionId, note)
        }
    }

    fun dismissMessage() {
        message.value = null
    }

    fun finishOnboarding(focus: Int, struggle: Int, minutes: Int) {
        viewModelScope.launch { game.finishOnboarding(focus, struggle, minutes) }
    }

    fun tickStep(step: QuestStepEntity) {
        viewModelScope.launch {
            val quest = state.value.quests.firstOrNull { it.id == step.questId } ?: return@launch
            val result = game.tickStep(step) ?: return@launch
            game.refreshScore()
            bump()
            message.value = when {
                result.levelAfter > result.levelBefore -> PhraseBank.levelUp()
                result.questFinished -> PhraseBank.questDone()
                else -> null
            }
            showUndo(UndoInfo(step, result.xpGained, result.questFinished, quest.stat, quest.goldReward))
        }
    }

    fun undoLast() {
        val info = undo.value ?: return
        undoTimer?.cancel()
        undo.value = null
        viewModelScope.launch {
            game.undoStep(info.step, info.gained, info.finished, info.stat, info.gold)
            game.refreshScore()
            bump()
            message.value = null
        }
    }

    private fun showUndo(info: UndoInfo) {
        undo.value = info
        undoTimer?.cancel()
        undoTimer = viewModelScope.launch {
            delay(UNDO_WINDOW_MS)
            undo.value = null
        }
    }

    fun makeSmaller(quest: QuestEntity) {
        viewModelScope.launch {
            game.makeSmaller(quest)
            bump()
        }
    }

    private fun runFrom(snap: DungeonTimerStore.Snapshot, now: Long): DungeonRun = DungeonRun(
        rank = Rank.valueOf(snap.rank),
        plannedMinutes = snap.plannedMinutes,
        startedAtMillis = snap.startedAt,
        elapsedSeconds = DungeonTimerStore.elapsedSeconds(snap, now),
        paused = snap.pausedAt != null,
        pausedSeconds = DungeonTimerMath.pausedSeconds(snap.pausedTotal, snap.pausedAt, now),
        soldierKey = snap.soldierKey,
        questId = snap.questId
    )

    private fun startTicker() {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (true) {
                val snap = timer.snapshot.value
                if (snap == null) {
                    run.value = null
                    break
                }
                run.value = runFrom(snap, System.currentTimeMillis())
                delay(1000)
            }
        }
    }

    private val gateMinutesState = MutableStateFlow<Map<String, Int>>(emptyMap())

    /** User-chosen gate lengths by rank name. Missing ranks use the default length. */
    val gateMinutes: StateFlow<Map<String, Int>> = gateMinutesState.asStateFlow()

    init {
        viewModelScope.launch { gateMinutesState.value = game.gateMinutes() }
    }

    fun setGateMinutes(rank: Rank, minutes: Int) {
        val clean = com.shadowsystem.core.GateRules.clamp(minutes)
        gateMinutesState.value = gateMinutesState.value + (rank.name to clean)
        viewModelScope.launch { game.setGateMinutes(rank.name, clean) }
    }

    /** Starts a run. [soldierKey] and [questId] are optional links shown on the run. */
    fun startDungeon(rank: Rank, minutes: Int = GameMath.dungeonMinutes(rank), soldierKey: String? = null, questId: Long? = null) {
        if (timer.snapshot.value != null) return
        timer.start(rank.name, minutes, System.currentTimeMillis(), soldierKey, questId)
        startTicker()
    }

    fun setQuestSoldier(questId: Long, key: String?) {
        viewModelScope.launch { game.setQuestSoldier(questId, key) }
    }

    /** Pause or resume. While paused, the work clock stops and the monster regenerates slightly (cosmetic). */
    fun togglePause() {
        val snap = timer.snapshot.value ?: return
        val now = System.currentTimeMillis()
        if (snap.pausedAt == null) timer.pause(now) else timer.resume(now)
        timer.snapshot.value?.let { run.value = runFrom(it, now) }
    }

    /** Ends the run. [completed] is true only when the planned time was reached. */
    fun endDungeon(completed: Boolean) {
        val snap = timer.snapshot.value ?: return
        val current = runFrom(snap, System.currentTimeMillis())
        ticker?.cancel()
        timer.clear()
        run.value = null
        viewModelScope.launch {
            val done = current.elapsedSeconds.coerceAtMost(current.plannedSeconds)
            val result = game.recordDungeon(current.rank.name, current.plannedMinutes, done, completed, current.soldierKey, current.questId)
            loot.value = LootInfo(result.sessionId, result.xp, result.gold, result.rare)
            game.refreshScore()
            bump()
            if (completed) message.value = PhraseBank.questDone()
        }
    }

    companion object {
        const val UNDO_WINDOW_MS = 5_000L

        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                GameViewModel(app.container.game, app.container.timer) as T
        }
    }
}
