package com.shadowsystem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import com.shadowsystem.R

/** Dark-only palette: near-black base with purple and cyan neon accents. */
object ShadowColors {
    val Background = Color(0xFF0A0A10)
    val Surface = Color(0xFF14141F)
    val SurfaceHigh = Color(0xFF1E1E2D)
    val Primary = Color(0xFF7C4DFF)
    val Secondary = Color(0xFF00E5FF)
    val Gold = Color(0xFFFFD54F)
    val Danger = Color(0xFFFF5252)
    val TextPrimary = Color(0xFFEDEBFF)
    val TextMuted = Color(0xFF9E9BB8)
}

val VazirFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val ShadowTypography = Typography(
    headlineMedium = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp)
)

@Composable
fun ShadowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ShadowColors.Primary,
            onPrimary = Color.White,
            secondary = ShadowColors.Secondary,
            tertiary = ShadowColors.Gold,
            background = ShadowColors.Background,
            surface = ShadowColors.Surface,
            surfaceVariant = ShadowColors.SurfaceHigh,
            onBackground = ShadowColors.TextPrimary,
            onSurface = ShadowColors.TextPrimary,
            onSurfaceVariant = ShadowColors.TextMuted,
            error = ShadowColors.Danger
        ),
        typography = ShadowTypography,
        content = content
    )
}
