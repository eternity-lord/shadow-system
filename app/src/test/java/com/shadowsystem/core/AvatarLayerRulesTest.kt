package com.shadowsystem.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarLayerRulesTest {

    @Test
    fun equippedKeyIsFoundByPrefix() {
        val equipped = setOf("outfit_leather", "aura_gold", "bg_gate")
        assertEquals("outfit_leather", AvatarLayerRules.equippedIn(equipped, "outfit_"))
        assertNull(AvatarLayerRules.equippedIn(equipped, "weapon_"))
    }

    @Test
    fun everyShopOutfitWeaponAuraAndFrameHasAColour() {
        AvatarData.SHOP_ITEMS.forEach { item ->
            val colour = when (item.category) {
                "outfit" -> AvatarLayerRules.outfitArgb(item.key)
                "weapon" -> AvatarLayerRules.weaponArgb(item.key)
                "aura" -> AvatarLayerRules.auraArgb(item.key)
                "frame" -> AvatarLayerRules.frameArgb(item.key)
                else -> 0L
            }
            assertTrue("missing colour for ${item.key}", colour != null)
        }
    }

    @Test
    fun weaponsGrowFromDaggerToDarkBlade() {
        assertTrue(AvatarLayerRules.weaponLength("weapon_dagger") < AvatarLayerRules.weaponLength("weapon_sword"))
        assertTrue(AvatarLayerRules.weaponLength("weapon_sword") < AvatarLayerRules.weaponLength("weapon_blade"))
    }

    @Test
    fun sovereignFrameIsHeaviest() {
        assertEquals(6f, AvatarLayerRules.frameWidthDp("frame_sovereign"), 0.001f)
        assertEquals(3f, AvatarLayerRules.frameWidthDp("frame_basic"), 0.001f)
    }
}
