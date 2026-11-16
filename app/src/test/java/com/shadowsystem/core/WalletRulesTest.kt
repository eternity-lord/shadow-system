package com.shadowsystem.core

import com.shadowsystem.core.ChequeRules.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletRulesTest {
    @Test
    fun balanceCombinesOpeningSpendingIncomeAndTransfers() {
        val b = WalletRules.balances(
            openings = mapOf(1L to 1000L, 2L to 0L),
            txs = listOf(
                WalletRules.Tx(1L, 300L, isIncome = false),
                WalletRules.Tx(1L, 500L, isIncome = true)
            ),
            moves = listOf(WalletRules.Move(1L, 2L, 400L))
        )
        assertEquals(800L, b[1L])
        assertEquals(400L, b[2L])
    }

    @Test
    fun transfersNeedTwoWalletsAndPositiveAmount() {
        assertTrue(WalletRules.canTransfer(1L, 2L, 10L))
        assertFalse(WalletRules.canTransfer(1L, 1L, 10L))
        assertFalse(WalletRules.canTransfer(1L, 2L, 0L))
    }

    @Test
    fun chequeStatusFollowsDueDate() {
        assertEquals(Status.CLEARED, ChequeRules.status(dueDay = 5L, today = 10L, cleared = true))
        assertEquals(Status.OVERDUE, ChequeRules.status(dueDay = 9L, today = 10L, cleared = false))
        assertEquals(Status.DUE_SOON, ChequeRules.status(dueDay = 13L, today = 10L, cleared = false))
        assertEquals(Status.LATER, ChequeRules.status(dueDay = 14L, today = 10L, cleared = false))
    }
}
