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

    @Test
    fun everyOutfitHasALookImageInTheDrawableFolder() {
        // Unit tests run from the module directory.
        AvatarData.SHOP_ITEMS.filter { it.category == "outfit" }.forEach { item ->
            val file = java.io.File("src/main/res/drawable-nodpi/avatar_look_${item.key}.png")
            assertTrue("missing look image for ${item.key}", file.exists())
        }
    }

    @Test
    fun everyWeaponHasASpriteInTheDrawableFolder() {
        AvatarData.SHOP_ITEMS.filter { it.category == "weapon" }.forEach { item ->
            val name = "avatar_weapon_" + item.key.removePrefix("weapon_")
            val file = java.io.File("src/main/res/drawable-nodpi/$name.png")
            assertTrue("missing weapon sprite $name", file.exists())
        }
    }

    @Test
    fun everyAuraAndFrameHasASpriteInTheDrawableFolder() {
        AvatarData.SHOP_ITEMS.filter { it.category == "aura" || it.category == "frame" }.forEach { item ->
            val file = java.io.File("src/main/res/drawable-nodpi/avatar_${item.key}.png")
            assertTrue("missing sprite for ${item.key}", file.exists())
        }
    }

    @Test
    fun moodPoseImagesExistForStandingDeterminedAndTired() {
        listOf("standing", "determined", "tired", "low").forEach { p ->
            assertTrue("missing pose $p", java.io.File("src/main/res/drawable-nodpi/avatar_pose_$p.png").exists())
        }
    }
}
