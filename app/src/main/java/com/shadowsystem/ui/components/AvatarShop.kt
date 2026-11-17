package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.AssetImage
import com.shadowsystem.core.AssetRegistry
import com.shadowsystem.core.AvatarData
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.ShopItem
import com.shadowsystem.ui.theme.ShadowColors

/** Avatar cosmetics bought with Gold. Heavy items are shown as locked (boss or trial rewards). */
@Composable
fun AvatarShop(
    gold: Int,
    owned: Set<String>,
    equipped: Set<String>,
    onBuy: (ShopItem) -> Unit,
    onEquip: (ShopItem) -> Unit
) {
    ShadowCard {
        SectionTitle(stringResource(R.string.avatar_shop_title))
        listOf("hair", "headgear", "outfit", "weapon", "aura", "frame", "background").forEach { category ->
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                AssetImage(name = AssetRegistry.categoryAssetName(category), size = 28.dp, contentDescription = null)
                Text(categoryLabel(category), style = MaterialTheme.typography.titleSmall, color = ShadowColors.Gold)
            }
            AvatarData.SHOP_ITEMS.filter { it.category == category }.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (item.heavy) stringResource(R.string.shop_heavy_hint) else item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShadowColors.TextMuted
                        )
                    }
                    when {
                        item.key in owned && item.key in equipped -> TextButton(
                            onClick = { onEquip(item) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text(stringResource(R.string.shop_unequip), color = ShadowColors.Secondary) }
                        item.key in owned -> TextButton(
                            onClick = { onEquip(item) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) { Text(stringResource(R.string.shop_equip)) }
                        item.heavy -> Text(stringResource(R.string.locked_label), color = ShadowColors.TextMuted)
                        else -> Button(
                            onClick = { onBuy(item) },
                            enabled = gold >= item.priceGold,
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShadowColors.Gold)
                        ) {
                            Text(
                                stringResource(R.string.shop_buy_price, PersianText.toFa(item.priceGold)),
                                color = ShadowColors.Background
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun categoryLabel(category: String): String = stringResource(
    when (category) {
        "outfit" -> R.string.avatar_cat_outfit
        "weapon" -> R.string.avatar_cat_weapon
        "aura" -> R.string.avatar_cat_aura
        "frame" -> R.string.avatar_cat_frame
        "hair" -> R.string.avatar_cat_hair
        "headgear" -> R.string.avatar_cat_headgear
        else -> R.string.avatar_cat_background
    }
)
