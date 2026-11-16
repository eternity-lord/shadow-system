package com.shadowsystem

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.shadowsystem.ui.nav.AppRoot
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.theme.ShadowTheme

/**
 * Single activity. The whole UI is Persian and right-to-left.
 * FLAG_SECURE blocks screenshots and previews; the PIN lock engages when the app leaves the foreground.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        val app = application as ShadowApp
        setContent {
            ShadowTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize().background(ShadowColors.Background), color = ShadowColors.Background) {
                        AppRoot(app)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val container = (application as ShadowApp).container
        if (container.prefs.isPinSet) container.lock.locked.value = true
    }
}
