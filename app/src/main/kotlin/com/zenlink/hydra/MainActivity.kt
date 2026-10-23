package com.zenlink.hydra

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zenlink.hydra.core.designsystem.HydraTheme
import com.zenlink.hydra.feature.chat.ChatScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HydraTheme { HydraRoot(isOnline(this)) } }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@Composable
private fun HydraRoot(online: Boolean) {
    val nav = rememberNavController()
    // Onboarding completion is persisted by feature:onboarding; the default here is a first run.
    var onboardingComplete by remember { mutableStateOf(false) }
    val start = AppRouter.startRoute(onboardingComplete).path
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: start
    val hasModel = false // wired to core:database ModelRepository once the Application owns it

    Scaffold(
        bottomBar = {
            if (AppRouter.tabFor(current) != null) {
                NavigationBar {
                    Route.tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.path,
                            onClick = { nav.navigate(tab.path) { launchSingleTop = true } },
                            icon = {},
                            label = { Text(tab.path.replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AppRouter.offlineIndicator(online, hasModel)?.let { Text(it) }
            NavHost(navController = nav, startDestination = start) {
                composable(Route.Onboarding.path) {
                    OnboardingScreen(onFinished = { onboardingComplete = true; nav.navigate(Route.Chat.path) })
                }
                composable(Route.Chat.path) { ChatScreen() }
                composable(Route.Models.path) { ModelsScreen() }
                composable(Route.Settings.path) { SettingsScreen() }
            }
        }
    }
}

/** Placeholder until the models screen Compose UI exists. The planner in feature:models is done. */
@Composable
private fun ModelsScreen() = Text("Models: screen not built yet.")

/** Placeholder until the onboarding Compose UI exists. The step machine in feature:onboarding is done. */
@Composable
private fun OnboardingScreen(onFinished: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onFinished) { Text("Continue") }
}

/** Placeholder until the settings Compose UI exists. The planner in feature:settings is done. */
@Composable
private fun SettingsScreen() = Text("Settings: screen not built yet.")
