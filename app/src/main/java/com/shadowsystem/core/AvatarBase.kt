package com.shadowsystem.core

import androidx.compose.ui.graphics.Color

/**
 * Avatar base chosen in onboarding. Until the layered body art exists, the base is shown as a colour tint on the rank image.
 */
object AvatarBase {
    const val COUNT = 3

    /** Tint for a base index, or null for no tint. Unknown indexes fall back to no tint. */
    fun tint(base: Int): Color? = when (base) {
        1 -> Color(0xFF9CC8FF)
        2 -> Color(0xFFFFC08A)
        else -> null
    }
}
