package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shadowsystem.core.GameMath
import com.shadowsystem.ui.theme.ShadowTheme

@Preview(name = "Primary button", showBackground = true)
@Composable
private fun PrimaryButtonPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        PrimaryButton(text = "شروع", onClick = {})
        PrimaryButton(text = "بعدی", onClick = {}, enabled = false)
    }
}

@Preview(name = "Stat radar", showBackground = true)
@Composable
private fun StatRadarPreview() = ShadowTheme {
    StatRadar(values = listOf(0.8f, 0.4f, 0.6f, 0.3f, 0.9f), modifier = Modifier.padding(16.dp))
}

@Preview(name = "Avatar stack", showBackground = true)
@Composable
private fun AvatarStackPreview() = ShadowTheme {
    Row(modifier = Modifier.padding(16.dp)) {
        AvatarStack(rank = GameMath.Rank.B, topStats = listOf("STR" to "قدرت ۱۲", "VIT" to "سلامت ۸"), aura = true)
        Text("B", modifier = Modifier.padding(start = 12.dp))
    }
}

@Preview(name = "Rank up", showBackground = true)
@Composable
private fun RankUpPreview() = ShadowTheme {
    RankUpOverlay(rank = GameMath.Rank.B, onDismiss = {})
}
