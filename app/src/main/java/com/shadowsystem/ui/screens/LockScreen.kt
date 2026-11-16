package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.ui.theme.ShadowColors
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.shadowsystem.util.BiometricGate

/** PIN gate shown when the app returns from background. Four digits. */
@Composable
fun LockScreen(onSubmit: (String) -> Boolean, onBiometricOk: () -> Unit) {
    val context = LocalContext.current
    val canUseBiometric = remember { BiometricGate.available(context) }
    var pin by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    LaunchedEffect(pin) {
        if (pin.length == 4 && !onSubmit(pin)) {
            wrong = true
            pin = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.lock_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "●".repeat(pin.length) + "○".repeat(4 - pin.length),
            style = MaterialTheme.typography.headlineMedium,
            color = ShadowColors.Secondary
        )
        Text(
            if (wrong) stringResource(R.string.lock_wrong) else stringResource(R.string.lock_hint),
            color = if (wrong) ShadowColors.Danger else ShadowColors.TextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { d -> Key(d, Modifier.weight(1f)) { if (pin.length < 4) { pin += d; wrong = false } } }
            }
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            if (canUseBiometric) {
                TextButton(
                    onClick = { (context as? FragmentActivity)?.let { BiometricGate.prompt(it, onBiometricOk) } },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.bio_button), color = ShadowColors.TextMuted) }
            } else {
                Spacer(Modifier.weight(1f))
            }
            Key("0", Modifier.weight(1f)) { if (pin.length < 4) { pin += "0"; wrong = false } }
            TextButton(
                onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) {
                Text(stringResource(R.string.erase), color = ShadowColors.TextMuted)
            }
        }
    }
}

@Composable
private fun Key(label: String, modifier: Modifier, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = 56.dp)) {
        Text(label, style = MaterialTheme.typography.headlineMedium)
    }
}
