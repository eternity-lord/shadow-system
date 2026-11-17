package com.shadowsystem.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.ShadowApp
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.MoneyState
import com.shadowsystem.ui.vm.MoneyViewModel

/** Collapsible advanced search: category, wallet, date preset, toman amount range and #tag. */
@Composable
fun MoneyFilterPanel(state: MoneyState, vm: MoneyViewModel) {
    val app = LocalContext.current.applicationContext as ShadowApp
    val categoryFlow = remember { app.container.categories.observeAll() }
    val allCategories by categoryFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var open by rememberSaveable { mutableStateOf(false) }
    var minText by rememberSaveable { mutableStateOf("") }
    var maxText by rememberSaveable { mutableStateOf("") }
    var tagText by rememberSaveable { mutableStateOf("") }
    val adv = state.advanced

    TextButton(onClick = { open = !open }, modifier = Modifier.heightIn(min = 48.dp)) {
        Text(stringResource(if (open) R.string.filter_less else R.string.filter_more))
    }
    if (!open) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.filter_category), style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = adv.category == null,
                onClick = { vm.onCategoryFilter(null) },
                label = { Text(stringResource(R.string.filter_all)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            allCategories.forEach { c ->
                FilterChip(
                    selected = adv.category == c.name,
                    onClick = { vm.onCategoryFilter(c.name) },
                    label = { Text(c.name) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }

        Text(stringResource(R.string.filter_wallet), style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = adv.walletId == null,
                onClick = { vm.onWalletFilter(null) },
                label = { Text(stringResource(R.string.filter_all)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            state.wallets.forEach { w ->
                FilterChip(
                    selected = adv.walletId == w.id,
                    onClick = { vm.onWalletFilter(w.id) },
                    label = { Text(w.name) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }

        Text(stringResource(R.string.filter_date), style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                null to R.string.filter_date_all,
                7 to R.string.filter_date_7,
                30 to R.string.filter_date_30,
                90 to R.string.filter_date_90
            ).forEach { (days, label) ->
                FilterChip(
                    selected = adv.datePresetDays == days,
                    onClick = { vm.onDatePreset(days) },
                    label = { Text(stringResource(label)) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }

        Text(stringResource(R.string.filter_amount), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = minText,
                onValueChange = {
                    minText = it
                    vm.onAmountRange(minText, maxText)
                },
                singleLine = true,
                label = { Text(stringResource(R.string.filter_min)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxText,
                onValueChange = {
                    maxText = it
                    vm.onAmountRange(minText, maxText)
                },
                singleLine = true,
                label = { Text(stringResource(R.string.filter_max)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Text(stringResource(R.string.filter_tag), style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = tagText,
            onValueChange = {
                tagText = it
                vm.onTagFilter(it)
            },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.filter_tag_hint), color = ShadowColors.TextMuted) },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = {
                minText = ""
                maxText = ""
                tagText = ""
                vm.clearAdvanced()
            },
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Text(stringResource(R.string.filter_clear)) }
    }
}
