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
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.ui.components.SectionTitle
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.theme.ShadowColors

/** Gold shop. One item for now: a 1.5× XP boost for the next dungeon run that pays XP. */
@Composable
fun GoldShop(gold: Int, boostActive: Boolean, onBuy: (onResult: (Boolean) -> Unit) -> Unit) {
    var bought by remember { mutableStateOf(false) }
    ShadowCard {
        SectionTitle(stringResource(R.string.shop_title))
        Text(stringResource(R.string.shop_boost_name), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.shop_boost_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = ShadowColors.TextMuted
        )
        val canBuy = !boostActive && gold >= GameRepository.DUNGEON_BOOST_COST
        Button(
            onClick = {
                onBuy { ok ->
                    bought = ok
                }
            },
            enabled = canBuy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text(
                when {
                    boostActive -> stringResource(R.string.shop_active)
                    else -> stringResource(R.string.shop_buy, PersianText.toFa(GameRepository.DUNGEON_BOOST_COST))
                }
            )
        }
        if (bought) {
            Text(stringResource(R.string.shop_bought), color = ShadowColors.Secondary, style = MaterialTheme.typography.bodyMedium)
        }
        if (!boostActive && gold < GameRepository.DUNGEON_BOOST_COST) {
            Text(stringResource(R.string.shop_not_enough), color = ShadowColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
