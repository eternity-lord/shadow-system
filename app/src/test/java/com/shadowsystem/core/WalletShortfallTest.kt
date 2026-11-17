package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Test

class WalletShortfallTest {

    @Test
    fun noWalletMeansNoShortfall() {
        assertEquals(0L, FinanceRules.walletShortfall(500L, null))
    }

    @Test
    fun balanceThatCoversTheRestHasNoShortfall() {
        assertEquals(0L, FinanceRules.walletShortfall(500L, 800L))
        assertEquals(0L, FinanceRules.walletShortfall(500L, 500L))
    }

    @Test
    fun shortfallIsTheDifference() {
        assertEquals(200L, FinanceRules.walletShortfall(500L, 300L))
    }
}
