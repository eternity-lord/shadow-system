package com.shadowsystem.core

/** Wallet balances and cheque status (pure, unit tested). Transfers are not spending: they move money between wallets. */
object WalletRules {
    data class Tx(val walletId: Long, val amountRial: Long, val isIncome: Boolean)
    data class Move(val fromId: Long, val toId: Long, val amountRial: Long)

    /** Opening balance + income - spending +/- transfers, per wallet. */
    fun balances(openings: Map<Long, Long>, txs: List<Tx>, moves: List<Move>): Map<Long, Long> {
        val out = openings.toMutableMap()
        for (t in txs) {
            val delta = if (t.isIncome) t.amountRial else -t.amountRial
            out[t.walletId] = (out[t.walletId] ?: 0L) + delta
        }
        for (m in moves) {
            out[m.fromId] = (out[m.fromId] ?: 0L) - m.amountRial
            out[m.toId] = (out[m.toId] ?: 0L) + m.amountRial
        }
        return out
    }

    fun canTransfer(from: Long, to: Long, amountRial: Long): Boolean = from != to && amountRial > 0L
}

object ChequeRules {
    enum class Status { CLEARED, OVERDUE, DUE_SOON, LATER }

    const val SOON_DAYS = 3L

    fun status(dueDay: Long, today: Long, cleared: Boolean): Status = when {
        cleared -> Status.CLEARED
        dueDay < today -> Status.OVERDUE
        dueDay - today <= SOON_DAYS -> Status.DUE_SOON
        else -> Status.LATER
    }
}
