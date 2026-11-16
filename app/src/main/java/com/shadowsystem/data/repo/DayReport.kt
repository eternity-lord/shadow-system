package com.shadowsystem.data.repo

/** Numbers shown in the full daily report for one game day. */
data class DayReport(
    val score: Int?,
    val yesterdayScore: Int?,
    val dungeonMinutes: Int,
    val dungeonsCompleted: Int,
    val rareLoot: Int,
    val questsDone: Int,
    val questsTotal: Int,
    val xpToday: Int
)
