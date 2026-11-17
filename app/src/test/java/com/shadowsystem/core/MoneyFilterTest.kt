package com.shadowsystem.core

import com.shadowsystem.data.db.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFilterTest {

    private fun tx(
        amount: Long = 100_000,
        category: String = "خوراک",
        note: String = "",
        income: Boolean = false,
        day: Long = 100,
        wallet: Long? = 1
    ) = TransactionEntity(
        amountRial = amount, category = category, note = note, isIncome = income,
        epochDay = day, createdAt = 0, walletId = wallet
    )

    @Test
    fun emptyFilterMatchesEverything() {
        assertFalse(MoneyFilter().isAdvanced)
        assertTrue(MoneyFilter().matches(tx(), today = 100))
    }

    @Test
    fun categoryAndWalletMustBeExact() {
        assertTrue(MoneyFilter(category = "خوراک").matches(tx(), 100))
        assertFalse(MoneyFilter(category = "حمل‌ونقل").matches(tx(), 100))
        assertFalse(MoneyFilter(walletId = 2).matches(tx(wallet = 1), 100))
        assertTrue(MoneyFilter(walletId = 1).matches(tx(wallet = 1), 100))
    }

    @Test
    fun amountRangeIsInclusive() {
        val f = MoneyFilter(minRial = 100_000, maxRial = 200_000)
        assertTrue(f.matches(tx(amount = 100_000), 100))
        assertTrue(f.matches(tx(amount = 200_000), 100))
        assertFalse(f.matches(tx(amount = 99_999), 100))
        assertFalse(f.matches(tx(amount = 200_001), 100))
    }

    @Test
    fun datePresetCountsTodayAsFirstDay() {
        val f = MoneyFilter(datePresetDays = 7)
        assertEquals(94L, f.fromDay(today = 100))
        assertTrue(f.matches(tx(day = 94), 100))
        assertFalse(f.matches(tx(day = 93), 100))
        assertNull(MoneyFilter().fromDay(100))
    }

    @Test
    fun tagsAreHashWordsIgnoringCase() {
        assertEquals(listOf("قهوه", "work"), MoneyFilter.tagsOf("قهوه با #قهوه و #Work و #work"))
        assertTrue(MoneyFilter(tag = "work").matches(tx(note = "کار #Work"), 100))
        assertFalse(MoneyFilter(tag = "work").matches(tx(note = "کار"), 100))
        assertEquals("work", MoneyFilter.cleanTag("  #Work "))
        assertNull(MoneyFilter.cleanTag("  # "))
    }

    @Test
    fun incomeOnlyFilter() {
        assertTrue(MoneyFilter(incomeOnly = true).matches(tx(income = true), 100))
        assertFalse(MoneyFilter(incomeOnly = true).matches(tx(income = false), 100))
    }
}
