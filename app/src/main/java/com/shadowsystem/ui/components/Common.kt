package com.shadowsystem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.ui.theme.ShadowColors

/** Rounded surface card used across screens. */
@Composable
fun ShadowCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ShadowColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() }
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = ShadowColors.TextMuted,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/** Primary action with a 48dp minimum touch target. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ShadowColors.Primary)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun XpBar(progress: Float, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = ShadowColors.Secondary,
            trackColor = ShadowColors.SurfaceHigh
        )
    }
}

/** Confirmation dialog for any destructive action. */
@Composable
fun ConfirmDialog(
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ShadowColors.Surface,
        text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(confirmText, color = ShadowColors.Danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(com.shadowsystem.R.string.cancel))
            }
        }
    )
}

/** Shows a single system line, dismissable by tap. */
@Composable
fun MessageBanner(text: String, onDismiss: () -> Unit) {
    ShadowCard(modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = ShadowColors.Gold, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("×", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun VerticalGap(dp: Int = 12) {
    Spacer(Modifier.height(dp.dp))
}

/** Screen width in dp, used to size grids without lazy layouts. */
@Composable
fun screenWidthDp(): Int = LocalConfiguration.current.screenWidthDp

/** Reads the reduce-motion setting so animations can be shortened everywhere. */
@Composable
fun rememberReduceMotion(): Boolean {
    val app = LocalContext.current.applicationContext as com.shadowsystem.ShadowApp
    val flow = app.container.prefs.reduceMotion
    return flow.collectAsStateWithLifecycle().value
}
