package com.shadowsystem.core

/**
 * Colours and sizes for the equipped-item layers drawn over the avatar. These are vector shapes for now;
 * they stand in for layered art until per-item images exist (see ASSETS_MANIFEST.md).
 * Colours are ARGB longs.
 */
object AvatarLayerRules {

    /** The equipped key that starts with [prefix] (for example "outfit_"), or null. */
    fun equippedIn(equipped: Set<String>, prefix: String): String? = equipped.firstOrNull { it.startsWith(prefix) }

    fun outfitArgb(key: String): Long? = when (key) {
        "outfit_street" -> 0xFF5C6B7AL
        "outfit_sport" -> 0xFF2E8B57L
        "outfit_leather" -> 0xFF8B5A2BL
        "outfit_armor" -> 0xFFB0B7C3L
        "outfit_shadow" -> 0xFF1C1433L
        else -> null
    }

    fun weaponArgb(key: String): Long? = when (key) {
        "weapon_dagger" -> 0xFFB8C4D0L
        "weapon_sword" -> 0xFFD9DEE6L
        "weapon_blade" -> 0xFF3B0A5CL
        else -> null
    }

    /** Hair colour for a hair key, or null. */
    fun hairArgb(key: String): Long? = when (key) {
        "hair_short" -> 0xFF1B1B24L
        "hair_spiky" -> 0xFF2A2233L
        else -> null
    }

    /** Headgear colour for a headgear key, or null. */
    fun headgearArgb(key: String): Long? = when (key) {
        "headgear_hood" -> 0xFF15151CL
        "headgear_crown" -> 0xFFFFC857L
        else -> null
    }

    /** Weapon length as a fraction of the avatar height: dagger shortest, dark blade longest. */
    fun weaponLength(key: String): Float = when (key) {
        "weapon_dagger" -> 0.25f
        "weapon_sword" -> 0.45f
        "weapon_blade" -> 0.5f
        else -> 0.35f
    }

    fun auraArgb(key: String): Long? = when (key) {
        "aura_blue" -> 0xFF3D8BFFL
        "aura_purple" -> 0xFF9B59FFL
        "aura_gold" -> 0xFFFFC857L
        "aura_dark" -> 0xFF2A0A3DL
        else -> null
    }

    fun frameArgb(key: String): Long? = when (key) {
        "frame_basic" -> 0xFF7A7A7AL
        "frame_neon" -> 0xFFC040FFL
        "frame_sovereign" -> 0xFFFFD700L
        else -> null
    }

    /** Frame border width in dp. The sovereign frame is the heaviest. */
    fun frameWidthDp(key: String): Float = if (key == "frame_sovereign") 6f else 3f
}
