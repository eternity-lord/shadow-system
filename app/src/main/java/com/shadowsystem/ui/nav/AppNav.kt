package com.shadowsystem.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shadowsystem.R
import com.shadowsystem.ShadowApp
import com.shadowsystem.ui.screens.DungeonScreen
import com.shadowsystem.ui.screens.HunterScreen
import com.shadowsystem.ui.screens.LockScreen
import com.shadowsystem.ui.screens.MoneyPlanScreen
import com.shadowsystem.ui.screens.MoneyScreen
import com.shadowsystem.ui.screens.NowScreen
import com.shadowsystem.ui.screens.OnboardingScreen
import com.shadowsystem.ui.screens.QuestsScreen
import com.shadowsystem.ui.screens.ReportsScreen
import com.shadowsystem.ui.screens.SettingsScreen
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.GameViewModel
import com.shadowsystem.ui.vm.InboxViewModel
import com.shadowsystem.ui.vm.MoneyViewModel
import com.shadowsystem.ui.vm.PlanViewModel
import com.shadowsystem.ui.vm.ReportsViewModel
import com.shadowsystem.ui.vm.SettingsViewModel

/** The five bottom tabs of the app (spec section 11). */
enum class Tab(val route: String, val labelRes: Int, val icon: ImageVector) {
    NOW("now", R.string.tab_now, Icons.Filled.Home),
    QUESTS("quests", R.string.tab_quests, Icons.Filled.List),
    DUNGEON("dungeon", R.string.tab_dungeon, Icons.Filled.PlayArrow),
    MONEY("money", R.string.tab_money, Icons.Filled.ShoppingCart),
    HUNTER("hunter", R.string.tab_hunter, Icons.Filled.Person)
}

/** Sub-screens reached from tabs; they have no bottom bar entry. */
object Routes {
    const val PLAN = "plan"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
}

/** Top-level gate: loading, PIN lock, onboarding, then the main app. */
@Composable
fun AppRoot(app: ShadowApp) {
    val game: GameViewModel = viewModel(factory = GameViewModel.factory(app))
    val gameState by game.state.collectAsStateWithLifecycle()
    val locked by app.container.lock.locked.collectAsStateWithLifecycle()

    when {
        !gameState.loaded -> Unit
        locked -> LockScreen(
            onSubmit = { pin ->
                val ok = app.container.prefs.checkPin(pin)
                if (ok) app.container.lock.locked.value = false
                ok
            },
            onBiometricOk = { app.container.lock.locked.value = false }
        )
        !gameState.profile.onboarded -> OnboardingScreen { game.finishOnboarding() }
        else -> MainNavigation(app, game)
    }
}

@Composable
private fun MainNavigation(app: ShadowApp, game: GameViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    val money: MoneyViewModel = viewModel(factory = MoneyViewModel.factory(app))
    val inboxVm: InboxViewModel = viewModel(factory = InboxViewModel.factory(app))
    val profileVm: com.shadowsystem.ui.vm.ProfileViewModel = viewModel(factory = com.shadowsystem.ui.vm.ProfileViewModel.factory(app))
    val bossVm: com.shadowsystem.ui.vm.BossViewModel = viewModel(factory = com.shadowsystem.ui.vm.BossViewModel.factory(app))
    val soldierVm: com.shadowsystem.ui.vm.SoldierViewModel = viewModel(factory = com.shadowsystem.ui.vm.SoldierViewModel.factory(app))

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = ShadowColors.Surface) {
                Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = current.isTab(tab),
                        onClick = { navigateTab(nav, tab.route) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes), style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Tab.NOW.route,
            modifier = Modifier.padding(inner)
        ) {
            composable(Tab.NOW.route) { NowScreen(game, inboxVm, bossVm) }
            composable(Tab.QUESTS.route) { QuestsScreen(game) }
            composable(Tab.DUNGEON.route) { DungeonScreen(game) }
            composable(Tab.MONEY.route) {
                MoneyScreen(
                    vm = money,
                    onPlan = { nav.navigate(Routes.PLAN) },
                    onReports = { nav.navigate(Routes.REPORTS) }
                )
            }
            composable(Tab.HUNTER.route) { HunterScreen(game, soldierVm, profileVm) { nav.navigate(Routes.SETTINGS) } }
            composable(Routes.PLAN) {
                MoneyPlanScreen(viewModel(factory = PlanViewModel.factory(app))) { nav.popBackStack() }
            }
            composable(Routes.REPORTS) {
                ReportsScreen(viewModel(factory = ReportsViewModel.factory(app))) { nav.popBackStack() }
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel(factory = SettingsViewModel.factory(app))) { nav.popBackStack() }
            }
        }
    }
}

private fun NavDestination?.isTab(tab: Tab): Boolean = this?.hierarchy?.any { it.route == tab.route } == true

private fun navigateTab(nav: NavHostController, route: String) {
    nav.navigate(route) {
        popUpTo(nav.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
