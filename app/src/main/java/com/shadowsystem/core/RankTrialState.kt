package com.shadowsystem.core

/** What the UI needs to show the rank trial. */
data class RankTrialState(
    val pending: Boolean = false,
    val target: GameMath.Rank = GameMath.Rank.D,
    val done: Int = 0,
    val effective: GameMath.Rank = GameMath.Rank.E
)
