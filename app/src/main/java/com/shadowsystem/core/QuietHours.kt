package com.shadowsystem.core

/** Quiet hours (spec section 11): no reminder notifications inside this window. Hours are 0–23. */
object QuietHours {
    const val DEFAULT_START = 22
    const val DEFAULT_END = 7

    /** True when [hour] falls inside [start, end). The window may cross midnight; equal bounds mean no quiet time. */
    fun isQuiet(hour: Int, start: Int, end: Int): Boolean = when {
        start == end -> false
        start < end -> hour in start until end
        else -> hour >= start || hour < end
    }

    /** Moves an hour by [delta], wrapping around the day. */
    fun shift(hour: Int, delta: Int): Int = Math.floorMod(hour + delta, 24)
}
