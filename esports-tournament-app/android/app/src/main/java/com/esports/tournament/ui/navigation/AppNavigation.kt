package com.esports.tournament.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.esports.tournament.ui.screens.auth.AuthScreen
import com.esports.tournament.ui.screens.home.HomeScreen
import com.esports.tournament.ui.screens.leaderboard.LeaderboardScreen
import com.esports.tournament.ui.screens.match.MatchDetailScreen
import com.esports.tournament.ui.screens.profile.ProfileScreen
import com.esports.tournament.ui.screens.rewards.SpinWheelScreen
import com.esports.tournament.ui.screens.wallet.WalletScreen
import com.esports.tournament.ui.theme.*

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object MatchDetail : Screen("match/{matchId}") {
        fun createRoute(matchId: String) = "match/$matchId"
    }
    object Profile : Screen("profile")
    object Leaderboard : Screen("leaderboard")
    object Wallet : Screen("wallet")
    object SpinWheel : Screen("spin")
}

data class BottomNavItem(val screen: Screen, val icon: ImageVector, val label: String)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Leaderboard, Icons.Default.Leaderboard, "Ranks"),
    BottomNavItem(Screen.SpinWheel, Icons.Default.Casino, "Spin"),
    BottomNavItem(Screen.Wallet, Icons.Default.AccountBalanceWallet, "Wallet"),
    BottomNavItem(Screen.Profile, Icons.Default.Person, "Profile")
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                GamingBottomBar(
                    items = bottomNavItems,
                    currentDestination = currentDestination,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Auth.route,
            modifier = Modifier.padding(padding),
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onMatchClick = { navController.navigate(Screen.MatchDetail.createRoute(it)) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onWalletClick = { navController.navigate(Screen.Wallet.route) }
                )
            }
            composable(Screen.MatchDetail.route) { backStack ->
                val matchId = backStack.arguments?.getString("matchId") ?: return@composable
                MatchDetailScreen(matchId = matchId, onBack = { navController.popBackStack() })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen()
            }
            composable(Screen.Wallet.route) {
                WalletScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.SpinWheel.route) {
                SpinWheelScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun GamingBottomBar(
    items: List<BottomNavItem>,
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, DarkSurface)
                )
            )
    ) {
        NavigationBar(
            containerColor = DarkSurface.copy(alpha = 0.95f),
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            items.forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) NeonPurple else Color(0xFF666677)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = if (isSelected) NeonPurple else Color(0xFF666677)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = NeonPurple.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
