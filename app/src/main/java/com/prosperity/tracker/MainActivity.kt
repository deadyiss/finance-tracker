package com.prosperity.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prosperity.tracker.ui.screens.AddScreen
import com.prosperity.tracker.ui.screens.BudgetScreen
import com.prosperity.tracker.ui.screens.HistoryScreen
import com.prosperity.tracker.ui.screens.HomeScreen
import com.prosperity.tracker.ui.screens.ReportsScreen
import com.prosperity.tracker.ui.theme.AppBackground
import com.prosperity.tracker.ui.theme.Mint
import com.prosperity.tracker.ui.theme.OnMintContainer
import com.prosperity.tracker.ui.theme.OnSurfaceVariant
import com.prosperity.tracker.ui.theme.Primary
import com.prosperity.tracker.ui.theme.ProsperityTheme
import com.prosperity.tracker.viewmodel.FinanceViewModel
import com.prosperity.tracker.viewmodel.FinanceViewModelFactory

private enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Beranda", Icons.Filled.Home),
    HISTORY("history", "Riwayat", Icons.Filled.ReceiptLong),
    ADD("add", "Tambah", Icons.Filled.Add),
    BUDGET("budget", "Anggaran", Icons.Filled.AccountBalanceWallet),
    REPORTS("reports", "Laporan", Icons.Filled.Analytics)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as ProsperityApp).repository
        setContent {
            ProsperityTheme {
                val vm: FinanceViewModel = viewModel(factory = FinanceViewModelFactory(repository))
                AppScaffold(vm)
            }
        }
    }
}

@Composable
private fun AppScaffold(vm: FinanceViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.hierarchy?.firstOrNull()?.route

    Scaffold(
        containerColor = AppBackground,
        topBar = { AppHeader() },
        bottomBar = {
            BottomBar(currentRoute) { dest ->
                // No popUpTo: each switch is pushed onto the back stack, so the
                // hardware back button returns to the previously viewed screen
                // instead of exiting. launchSingleTop avoids duplicate top entries.
                navController.navigate(dest.route) { launchSingleTop = true }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = Dest.HOME.route,
                // No transition animations on purpose: tab switches feel instant
                // rather than the default fade/slide which the user found slow.
                enterTransition = { androidx.compose.animation.EnterTransition.None },
                exitTransition = { androidx.compose.animation.ExitTransition.None },
                popEnterTransition = { androidx.compose.animation.EnterTransition.None },
                popExitTransition = { androidx.compose.animation.ExitTransition.None }
            ) {
                composable(Dest.HOME.route) { HomeScreen(vm) }
                composable(Dest.HISTORY.route) { HistoryScreen(vm) }
                composable(Dest.ADD.route) {
                    AddScreen(vm, onSaved = {
                        navController.navigate(Dest.HOME.route) { launchSingleTop = true }
                    })
                }
                composable(Dest.BUDGET.route) { BudgetScreen(vm) }
                composable(Dest.REPORTS.route) { ReportsScreen(vm) }
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text("Prosperity Tracker", color = Primary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomBar(currentRoute: String?, onSelect: (Dest) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dest.entries.forEach { dest ->
            if (dest == Dest.ADD) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-10).dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Mint)
                            .clickable { onSelect(dest) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(dest.icon, contentDescription = dest.label, tint = OnMintContainer, modifier = Modifier.size(28.dp))
                    }
                }
            } else {
                val selected = currentRoute == dest.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(dest) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        dest.icon,
                        contentDescription = dest.label,
                        tint = if (selected) Primary else OnSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        dest.label,
                        color = if (selected) Primary else OnSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
