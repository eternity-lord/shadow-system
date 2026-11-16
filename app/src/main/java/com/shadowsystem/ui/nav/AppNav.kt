package com.shadowsystem.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shadowsystem.R
import com.shadowsystem.ShadowApp
import com.shadowsystem.ui.screens.DungeonScreen
import com.shadowsystem.ui.screens.HunterScreen
import com.shadowsystem.ui.screens.MoneyScreen
import com.shadowsystem.ui.screens.NowScreen
import com.shadowsystem.ui.screens.QuestsScreen
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.GameViewModel
import com.shadowsystem.ui.vm.MoneyViewModel

/** The five bottom tabs of the app (spec section 11). */
enum class Tab(val route: String, val labelRes: Int, val icon: ImageVector) {
    NOW("now", R.string.tab_now, Icons.Filled.Home),
    QUESTS("quests", R.string.tab_quests, Icons.Filled.List),
    DUNGEON("dungeon", R.string.tab_dungeon, Icons.Filled.PlayArrow),
    MONEY("money", R.string.tab_money, Icons.Filled.ShoppingCart),
    HUNTER("hunter", R.string.tab_hunter, Icons.Filled.Person)
}

@Composable
fun AppRoot(app: ShadowApp) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    val game: GameViewModel = viewModel(factory = GameViewModel.factory(app))
    val money: MoneyViewModel = viewModel(factory = MoneyViewModel.factory(app))

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = ShadowColors.Surface) {
                Tab.values().forEach { tab ->
                    val selected = current?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes), style = androidx.compose.material3.MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.padding(0.dp)
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
            composable(Tab.NOW.route) { NowScreen(game) }
            composable(Tab.QUESTS.route) { QuestsScreen(game) }
            composable(Tab.DUNGEON.route) { DungeonScreen(game) }
            composable(Tab.MONEY.route) { MoneyScreen(money) }
            composable(Tab.HUNTER.route) { HunterScreen(game) }
        }
    }
}
