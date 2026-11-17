package com.shadowsystem.core

import com.shadowsystem.data.db.TransactionEntity

/**
 * Search filters for the Money tab. All amounts are in rial (the stored unit).
 * A tag is a `#word` in the note; matching ignores case.
 */
data class MoneyFilter(
    val incomeOnly: Boolean? = null,
    val category: String? = null,
    val walletId: Long? = null,
    val minRial: Long? = null,
    val maxRial: Long? = null,
    val datePresetDays: Int? = null,
    val tag: String? = null
) {
    /** True when any filter beyond the type filter is set. Search then covers all time, not one month. */
    val isAdvanced: Boolean
        get() = category != null || walletId != null || minRial != null || maxRial != null ||
            datePresetDays != null || tag != null

    /** First game day included by the date preset, or null for all time. */
    fun fromDay(today: Long): Long? = datePresetDays?.let { today - it + 1 }

    fun matches(t: TransactionEntity, today: Long): Boolean {
        if (incomeOnly != null && t.isIncome != incomeOnly) return false
        if (category != null && t.category != category) return false
        if (walletId != null && t.walletId != walletId) return false
        if (minRial != null && t.amountRial < minRial) return false
        if (maxRial != null && t.amountRial > maxRial) return false
        val from = fromDay(today)
        if (from != null && t.epochDay < from) return false
        if (tag != null && tag !in tagsOf(t.note)) return false
        return true
    }

    companion object {
        private val TAG = Regex("#([\\p{L}\\p{N}_]+)")

        /** Lower-case tags found in a note, without the `#`. */
        fun tagsOf(note: String): List<String> =
            TAG.findAll(note).map { it.groupValues[1].lowercase() }.distinct().toList()

        /** Normalises a user-typed tag: trims, drops a leading `#`, lower-cases. Empty means no tag. */
        fun cleanTag(text: String): String? =
            text.trim().removePrefix("#").trim().lowercase().takeIf { it.isNotEmpty() }
    }
}
