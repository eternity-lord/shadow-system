package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.BoostItem
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.ShopCatalog
import com.shadowsystem.ui.components.SectionTitle
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.theme.ShadowColors

/** Gold shop: boosts that are used up by the next matching event. */
@Composable
fun GoldShop(gold: Int, activeBoosts: Set<String>, onBuy: (BoostItem, (Boolean) -> Unit) -> Unit) {
    ShadowCard {
        SectionTitle(stringResource(R.string.shop_title))
        ShopCatalog.ITEMS.forEach { item ->
            ShopRow(item, gold, active = item.key in activeBoosts) { onResult -> onBuy(item, onResult) }
        }
    }
}

@Composable
private fun ShopRow(item: BoostItem, gold: Int, active: Boolean, onBuy: ((Boolean) -> Unit) -> Unit) {
    var bought by remember { mutableStateOf(false) }
    val (nameRes, descRes) = when (item.key) {
        ShopCatalog.DUNGEON -> R.string.shop_boost_name to R.string.shop_boost_desc
        ShopCatalog.QUEST_XP -> R.string.shop_quest_name to R.string.shop_quest_desc
        else -> R.string.shop_gold_name to R.string.shop_gold_desc
    }
    Text(stringResource(nameRes), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(descRes), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
    val canBuy = !active && gold >= item.cost
    Button(
        onClick = { onBuy { ok -> bought = ok } },
        enabled = canBuy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    ) {
        Text(
            if (active) stringResource(R.string.shop_active)
            else stringResource(R.string.shop_buy, PersianText.toFa(item.cost))
        )
    }
    if (bought && active) {
        Text(stringResource(R.string.shop_bought), color = ShadowColors.Secondary, style = MaterialTheme.typography.bodyMedium)
    }
    if (!active && gold < item.cost) {
        Text(stringResource(R.string.shop_not_enough), color = ShadowColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
