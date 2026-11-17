package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class AvatarBaseTest {

    @Test
    fun firstBaseHasNoTintAndUnknownIndexFallsBack() {
        assertNull(AvatarBase.tint(0))
        assertNull(AvatarBase.tint(99))
        assertNull(AvatarBase.tint(-1))
    }

    @Test
    fun coloredBasesHaveDistinctTints() {
        val blue = AvatarBase.tint(1)
        val amber = AvatarBase.tint(2)
        assertNotNull(blue)
        assertNotNull(amber)
        assertEquals(false, blue == amber)
        assertEquals(3, AvatarBase.COUNT)
    }
}
