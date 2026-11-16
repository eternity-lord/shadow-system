package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.core.ChequeRules
import com.shadowsystem.core.Jalali
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.ChequeEntity
import com.shadowsystem.ui.components.ConfirmDialog
import com.shadowsystem.ui.components.JalaliDatePicker
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.WalletViewModel

/** Wallets with balances, transfers between them, and cheques with Jalali due dates. */
@Composable
fun WalletsScreen(vm: WalletViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }
    var newOpening by remember { mutableStateOf("") }
    var walletError by remember { mutableStateOf(false) }
    var from by remember { mutableStateOf<Long?>(null) }
    var to by remember { mutableStateOf<Long?>(null) }
    var moveAmount by remember { mutableStateOf("") }
    var moveError by remember { mutableStateOf(false) }
    var chequeTitle by remember { mutableStateOf("") }
    var chequeAmount by remember { mutableStateOf("") }
    var chequeIssued by remember { mutableStateOf(false) }
    var chequeError by remember { mutableStateOf(false) }
    var due by remember(state.today) { mutableStateOf(vm.todayJalali(state.today)) }
    var pendingDelete by remember { mutableStateOf<ChequeEntity?>(null) }

    pendingDelete?.let { c ->
        ConfirmDialog(
            text = stringResource(R.string.cheque_delete_confirm),
            confirmText = stringResource(R.string.money_yes_delete),
            onConfirm = { vm.deleteCheque(c.id); pendingDelete = null },
            onDismiss = { pendingDelete = null }
        )
    }

    fun fmt(rial: Long) = if (state.hidden) "•••" else com.shadowsystem.core.PersianText.formatMoney(rial)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.wallets_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.back)) }
            }
        }
        item { Text(stringResource(R.string.wallets_section), style = MaterialTheme.typography.titleMedium) }
        items(state.wallets, key = { it.id }) { w ->
            ShadowCard {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(w.name, modifier = Modifier.weight(1f))
                    Text(fmt(state.balances[w.id] ?: w.openingRial))
                }
            }
        }
        item {
            ShadowCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(newName, { newName = it; walletError = false }, label = { Text(stringResource(R.string.wallet_name_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(newOpening, { newOpening = it; walletError = false }, label = { Text(stringResource(R.string.wallet_opening_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (walletError) Text(stringResource(R.string.wallet_invalid), color = ShadowColors.Danger)
                    Button(
                        onClick = { vm.addWallet(newName, newOpening) { ok -> if (ok) { newName = ""; newOpening = "" } else walletError = true } },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) { Text(stringResource(R.string.wallet_add)) }
                }
            }
        }
        item {
            ShadowCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.transfer_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.wallet_from))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.wallets.forEach { w ->
                            FilterChip(selected = from == w.id, onClick = { from = w.id }, label = { Text(w.name) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                    Text(stringResource(R.string.wallet_to))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.wallets.forEach { w ->
                            FilterChip(selected = to == w.id, onClick = { to = w.id }, label = { Text(w.name) }, modifier = Modifier.heightIn(min = 48.dp))
                        }
                    }
                    OutlinedTextField(moveAmount, { moveAmount = it; moveError = false }, label = { Text(stringResource(R.string.transfer_amount_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.transfer_note), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
                    if (moveError) Text(stringResource(R.string.transfer_invalid), color = ShadowColors.Danger)
                    Button(
                        onClick = { vm.transfer(from, to, moveAmount) { ok -> if (ok) moveAmount = "" else moveError = true } },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) { Text(stringResource(R.string.transfer_action)) }
                }
            }
        }
        item { Text(stringResource(R.string.cheque_title), style = MaterialTheme.typography.titleMedium) }
        item {
            ShadowCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(chequeTitle, { chequeTitle = it; chequeError = false }, label = { Text(stringResource(R.string.cheque_title_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(chequeAmount, { chequeAmount = it; chequeError = false }, label = { Text(stringResource(R.string.cheque_amount_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = chequeIssued, onClick = { chequeIssued = true }, label = { Text(stringResource(R.string.cheque_issued_me)) }, modifier = Modifier.heightIn(min = 48.dp))
                        FilterChip(selected = !chequeIssued, onClick = { chequeIssued = false }, label = { Text(stringResource(R.string.cheque_received)) }, modifier = Modifier.heightIn(min = 48.dp))
                    }
                    Text(stringResource(R.string.cheque_due))
                    JalaliDatePicker(due) { due = it }
                    if (chequeError) Text(stringResource(R.string.wallet_invalid), color = ShadowColors.Danger)
                    Button(
                        onClick = {
                            vm.addCheque(chequeTitle, chequeAmount, due, chequeIssued) { ok ->
                                if (ok) { chequeTitle = ""; chequeAmount = "" } else chequeError = true
                            }
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) { Text(stringResource(R.string.cheque_add)) }
                }
            }
        }
        if (state.cheques.isEmpty()) {
            item { Text(stringResource(R.string.cheque_empty), color = ShadowColors.TextMuted) }
        }
        items(state.cheques, key = { it.id }) { c ->
            ShadowCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${c.title} · ${fmt(c.amountRial)}", style = MaterialTheme.typography.titleMedium)
                    val dueJ = Jalali.fromGregorian(java.time.LocalDate.ofEpochDay(c.dueEpochDay))
                    Text("${if (c.issuedByMe) stringResource(R.string.cheque_issued_me) else stringResource(R.string.cheque_received)} · ${Jalali.format(dueJ)} · ${statusLabel(vm.statusOf(c, state.today))}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { vm.setCleared(c.id, !c.cleared) }, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text(stringResource(if (c.cleared) R.string.cheque_unclear else R.string.cheque_clear))
                        }
                        TextButton(onClick = { pendingDelete = c }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.money_yes_delete)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun statusLabel(s: ChequeRules.Status): String = stringResource(
    when (s) {
        ChequeRules.Status.CLEARED -> R.string.cheque_status_cleared
        ChequeRules.Status.OVERDUE -> R.string.cheque_status_overdue
        ChequeRules.Status.DUE_SOON -> R.string.cheque_status_soon
        ChequeRules.Status.LATER -> R.string.cheque_status_later
    }
)
