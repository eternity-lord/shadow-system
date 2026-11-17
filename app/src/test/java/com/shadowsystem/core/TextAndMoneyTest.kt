package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextAndMoneyTest {

    @Test
    fun persianDigitsRoundTrip() {
        assertEquals("۱۴۰۵", PersianText.toFa(1405))
        assertEquals("1405", PersianText.parseDigits("۱۴۰۵"))
    }

    @Test
    fun groupedThousandsWithPersianDigits() {
        assertEquals("۱٬۲۳۴٬۵۶۷", PersianText.grouped(1234567))
        assertEquals("۱۰۰", PersianText.grouped(100))
    }

    @Test
    fun parsesMixedDigitInput() {
        assertEquals(12345L, PersianText.parseLong("۱۲٬۳۴۵"))
        assertEquals(12345L, PersianText.parseLong("12,345"))
        assertNull(PersianText.parseLong("abc"))
    }

    @Test
    fun moneyStoredInRialsDisplayedInToman() {
        assertEquals(1234560L, MoneyRules.tomanToRial(123456))
        assertEquals("۱۲۳٬۴۵۶ تومان", PersianText.formatMoney(1234560))
    }

    @Test
    fun loggingXpCappedPerDay() {
        assertEquals(2, MoneyRules.logXpFor(0))
        assertEquals(2, MoneyRules.logXpFor(4))
        assertEquals(0, MoneyRules.logXpFor(5))
    }

    @Test
    fun amountValidation() {
        assertFalse(MoneyRules.isValidAmount(0))
        assertFalse(MoneyRules.isValidAmount(null))
        assertTrue(MoneyRules.isValidAmount(1))
    }

    @Test
    fun durationFormatting() {
        assertEquals("۱ ساعت و ۵ دقیقه", PersianText.durationMinutes(65))
        assertEquals("۲۵ دقیقه", PersianText.durationMinutes(25))
    }
}
