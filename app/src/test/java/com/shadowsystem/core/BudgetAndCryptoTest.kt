package com.shadowsystem.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BudgetAndCryptoTest {
    @Test
    fun budgetWarnsAtEightyAndStopsAtHundred() {
        assertEquals(FinanceRules.BudgetState.OK, FinanceRules.budgetState(0, 0))
        assertEquals(FinanceRules.BudgetState.OK, FinanceRules.budgetState(79, 100))
        assertEquals(FinanceRules.BudgetState.WARN, FinanceRules.budgetState(80, 100))
        assertEquals(FinanceRules.BudgetState.WARN, FinanceRules.budgetState(99, 100))
        assertEquals(FinanceRules.BudgetState.OVER, FinanceRules.budgetState(100, 100))
    }

    @Test
    fun goalPercentIsClamped() {
        assertEquals(25, FinanceRules.goalPercent(50, 200))
        assertEquals(100, FinanceRules.goalPercent(300, 200))
        assertEquals(0, FinanceRules.goalPercent(10, 0))
    }

    @Test
    fun backupRoundTripAndWrongPassword() {
        val plain = "سایه‌سیستم backup".toByteArray(Charsets.UTF_8)
        val password = "رمز-test-123".toCharArray()
        val sealed = Crypto.encrypt(plain, password)
        assertArrayEquals(plain, Crypto.decrypt(sealed, password))
        assertThrows(Exception::class.java) { Crypto.decrypt(sealed, "wrong".toCharArray()) }
    }

    @Test
    fun rejectsDataThatIsNotABackup() {
        assertThrows(IllegalArgumentException::class.java) {
            Crypto.decrypt(ByteArray(200) { 1 }, "x".toCharArray())
        }
    }
}
