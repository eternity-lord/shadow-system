package com.shadowsystem.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.ui.components.BackHeader
import com.shadowsystem.ui.components.ConfirmDialog
import com.shadowsystem.ui.components.MessageBanner
import com.shadowsystem.ui.components.PrimaryButton
import com.shadowsystem.ui.components.SectionTitle
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.util.ShareUtil
import com.shadowsystem.ui.vm.SettingsNotice
import com.shadowsystem.ui.vm.SettingsViewModel

/** Privacy, PIN lock, encrypted backup, restore and CSV export. */
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val hidden by vm.hidden.collectAsStateWithLifecycle()
    val pinSet by vm.pinSet.collectAsStateWithLifecycle()
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.setReminders(granted)
    }
    val notice by vm.notice.collectAsStateWithLifecycle()
    var pin by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var restoreUri by remember { mutableStateOf<Uri?>(null) }

    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) restoreUri = uri
    }

    restoreUri?.let { uri ->
        ConfirmDialog(
            text = stringResource(R.string.restore_confirm),
            confirmText = stringResource(R.string.restore_yes),
            onConfirm = {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                restoreUri = null
                if (bytes != null) vm.restore(bytes, password)
            },
            onDismiss = { restoreUri = null }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackHeader(stringResource(R.string.settings_title), onBack)
        notice?.let { n -> MessageBanner(noticeText(n)) { vm.clearNotice() } }

        ShadowCard {
            SectionTitle(stringResource(R.string.privacy_section))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.hide_balances_setting), modifier = Modifier.weight(1f))
                Switch(checked = hidden, onCheckedChange = { vm.toggleHidden() })
            }
        }

        ShadowCard {
            SectionTitle(stringResource(R.string.reminders_toggle))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.reminders_help), color = ShadowColors.TextMuted, modifier = Modifier.weight(1f))
                Switch(checked = reminders, onCheckedChange = { wantOn ->
                    if (wantOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.setReminders(wantOn)
                    }
                })
            }
        }

        ShadowCard {
            SectionTitle(stringResource(R.string.lock_section))
            Text(
                stringResource(if (pinSet) R.string.lock_enabled else R.string.lock_disabled),
                color = ShadowColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text(stringResource(R.string.pin_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (pinSet) {
                PrimaryButton(stringResource(R.string.pin_remove), onClick = { vm.removePin(pin); pin = "" })
            } else {
                PrimaryButton(stringResource(R.string.pin_set), onClick = { vm.setPin(pin); pin = "" })
            }
        }

        ShadowCard {
            SectionTitle(stringResource(R.string.backup_section))
            Text(stringResource(R.string.backup_help), color = ShadowColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.take(64) },
                label = { Text(stringResource(R.string.backup_password_hint)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryButton(
                stringResource(R.string.backup_export),
                onClick = {
                    vm.exportBackup(password) { bytes ->
                        ShareUtil.share(context, "shadow-system-backup.ssb", bytes, "application/octet-stream")
                    }
                }
            )
            Button(
                onClick = { pickBackup.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text(stringResource(R.string.backup_restore)) }
        }

        ShadowCard {
            SectionTitle(stringResource(R.string.csv_section))
            PrimaryButton(
                stringResource(R.string.csv_export),
                onClick = { vm.exportCsv { csv -> ShareUtil.share(context, "shadow-system-transactions.csv", csv.toByteArray(Charsets.UTF_8), "text/csv") } }
            )
        }
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.back)) }
    }
}

@Composable
private fun noticeText(n: SettingsNotice): String = stringResource(
    when (n) {
        SettingsNotice.PIN_SAVED -> R.string.notice_pin_saved
        SettingsNotice.PIN_INVALID -> R.string.notice_pin_invalid
        SettingsNotice.PIN_WRONG -> R.string.notice_pin_wrong
        SettingsNotice.PIN_REMOVED -> R.string.notice_pin_removed
        SettingsNotice.PASSWORD_SHORT -> R.string.notice_password_short
        SettingsNotice.BACKUP_READY -> R.string.notice_backup_ready
        SettingsNotice.RESTORE_DONE -> R.string.notice_restore_done
        SettingsNotice.RESTORE_FAILED -> R.string.notice_restore_failed
    }
)
