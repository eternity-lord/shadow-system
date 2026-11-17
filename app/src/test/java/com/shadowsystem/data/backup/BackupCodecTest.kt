package com.shadowsystem.data.backup

import com.shadowsystem.data.db.CategoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    @Test
    fun categoriesRoundTrip() {
        val cats = listOf(
            CategoryEntity("خوراک", false, false, 0xFFE57373L, 0),
            CategoryEntity("حقوق", true, true, 0xFF66BB6AL, 1)
        )
        val text = BackupCodec.encode(
            BackupSnapshot(null, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), categories = cats)
        )
        assertEquals(cats, BackupCodec.decode(text).categories)
    }

    @Test
    fun oldBackupWithoutCategoriesDecodesToNone() {
        assertTrue(BackupCodec.decode("").categories.isEmpty())
    }
}
