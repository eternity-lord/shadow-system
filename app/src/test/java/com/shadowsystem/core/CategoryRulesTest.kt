package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryRulesTest {

    @Test
    fun emptyNameIsRejectedAfterTrimming() {
        assertEquals(CategoryRules.Check.EMPTY, CategoryRules.check("   ", emptySet()))
    }

    @Test
    fun nameLongerThanTwentyIsRejected() {
        assertEquals(CategoryRules.Check.OK, CategoryRules.check("a".repeat(20), emptySet()))
        assertEquals(CategoryRules.Check.TOO_LONG, CategoryRules.check("a".repeat(21), emptySet()))
    }

    @Test
    fun duplicateNameIsRejected() {
        assertEquals(CategoryRules.Check.EXISTS, CategoryRules.check(" خوراک ", setOf("خوراک")))
    }

    @Test
    fun renamingToTheSameNameIsAllowed() {
        assertEquals(CategoryRules.Check.OK, CategoryRules.check("خوراک", setOf("خوراک"), renamingFrom = "خوراک"))
    }

    @Test
    fun renamingToAnotherExistingNameIsRejected() {
        assertEquals(
            CategoryRules.Check.EXISTS,
            CategoryRules.check("سایر", setOf("خوراک", "سایر"), renamingFrom = "خوراک")
        )
    }

    @Test
    fun defaultSetHasUniqueNamesAndBothKinds() {
        val names = CategoryRules.DEFAULTS.map { it.name }
        assertEquals(names.size, names.toSet().size)
        assertTrue(CategoryRules.DEFAULTS.any { it.isIncome })
        assertTrue(CategoryRules.DEFAULTS.any { !it.isIncome })
        assertTrue(names.all { it.length <= CategoryRules.MAX_NAME_LENGTH })
    }
}
