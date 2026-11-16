package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
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

/** Active dungeon run. Uses wall-clock start so backgrounding does not drift the timer. */
data class DungeonRun(
    val rank: Rank,
    val plannedMinutes: Int,
    val startedAtMillis: Long,
    val elapsedSeconds: Int = 0
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
    val boostActive: Boolean = false
)

private data class SideState(val trial: RankTrialState, val boost: Boolean)

class GameViewModel(private val game: GameRepository) : ViewModel() {

    private val day = game.today()
    private val message = MutableStateFlow<String?>(null)
    private val run = MutableStateFlow<DungeonRun?>(null)
    private val undo = MutableStateFlow<UndoInfo?>(null)
    private val refresh = MutableStateFlow(0)
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
        trialFlow()
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
            boostActive = side.boost
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NowState())

    /** Re-reads the rank trial whenever profile or quests change. */
    /** Forces the derived rank-trial and boost state to be re-read after an action finishes. */
    private fun bump() {
        refresh.value = refresh.value + 1
    }

    private fun trialFlow() = merge(game.observeProfile().map { }, game.observeQuests(day).map { }, refresh)
        .onStart { emit(Unit) }
        .map { SideState(game.rankTrialState(), game.isDungeonBoostActive()) }

    val stats: StateFlow<Map<Stat, Long>> = game.observeStats()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val yesterday = MutableStateFlow<Int?>(null)
    val yesterdayScore: StateFlow<Int?> = yesterday

    init {
        viewModelScope.launch { yesterday.value = game.scoreOf(day - 1) }
    }

    fun generate(mood: Mood) {
        viewModelScope.launch {
            game.generateDay(mood)
            bump()
            message.value = if (mood == Mood.HARD) PhraseBank.hardDay() else PhraseBank.morning()
        }
    }

    /** Shop purchase. Returns false when gold is short or the boost is already active. */
    fun buyDungeonBoost(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = game.buyDungeonBoost()
            bump()
            onResult(ok)
        }
    }

    fun dismissMessage() {
        message.value = null
    }

    fun finishOnboarding() {
        viewModelScope.launch { game.finishOnboarding() }
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

    fun startDungeon(rank: Rank) {
        if (run.value != null) return
        run.value = DungeonRun(
            rank = rank,
            plannedMinutes = GameMath.dungeonMinutes(rank),
            startedAtMillis = System.currentTimeMillis()
        )
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (true) {
                val current = run.value ?: break
                val elapsed = ((System.currentTimeMillis() - current.startedAtMillis) / 1000).toInt()
                run.value = current.copy(elapsedSeconds = elapsed)
                delay(1000)
            }
        }
    }

    /** Ends the run. [completed] is true only when the planned time was reached. */
    fun endDungeon(completed: Boolean) {
        val current = run.value ?: return
        ticker?.cancel()
        run.value = null
        viewModelScope.launch {
            val done = current.elapsedSeconds.coerceAtMost(current.plannedSeconds)
            game.recordDungeon(current.rank.name, current.plannedMinutes, done, completed)
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
                GameViewModel(app.container.game) as T
        }
    }
}
