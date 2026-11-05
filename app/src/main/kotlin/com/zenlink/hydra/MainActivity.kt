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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.designsystem.HydraTheme
import com.zenlink.hydra.core.device.DeviceChecker
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.Entitlement
import com.zenlink.hydra.core.model.Plan
import com.zenlink.hydra.feature.chat.ChatScreen
import com.zenlink.hydra.feature.chat.ChatViewModel
import com.zenlink.hydra.feature.models.ModelsScreen
import com.zenlink.hydra.feature.models.ModelsViewModel
import com.zenlink.hydra.feature.onboarding.OnboardingScreen
import com.zenlink.hydra.feature.onboarding.OnboardingViewModel
import com.zenlink.hydra.feature.settings.SettingsPlanner
import com.zenlink.hydra.feature.settings.SettingsScreen
import com.zenlink.hydra.feature.settings.SettingsViewModel
import com.zenlink.hydra.core.model.InstalledModel
import com.zenlink.hydra.core.model.ModelState
import kotlinx.coroutines.flow.first

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

/** Builds a view-model from the app-owned dependencies. Keeps MainActivity free of wiring details. */
private fun <T : ViewModel> factory(make: () -> T) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM = make() as VM
}

@Composable
private fun HydraRoot(online: Boolean) {
    val app = LocalContext.current.applicationContext as HydraApp
    val nav = rememberNavController()
    val settingsRepo: SettingsRepository = app.settings
    // Onboarding completion is read reactively from the encrypted settings table.
    val onboardingComplete by produceState(initialValue = false) {
        settingsRepo.observe().collect { value = it.onboardingComplete }
    }
    val start = AppRouter.startRoute(onboardingComplete).path
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: start
    // Whether a model is installed decides the offline banner.
    val installed by produceState(initialValue = emptyList<InstalledModel>()) {
        app.models.observeAll().collect { value = it }
    }
    val hasModel = installed.any { it.state == ModelState.INSTALLED }

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
                    val vm: OnboardingViewModel = viewModel(factory = factory {
                        OnboardingViewModel(
                            settings = settingsRepo,
                            downloads = app.downloads,
                            deviceProfile = { DeviceChecker(app).snapshot() },
                            wifiOnly = { settingsRepo.observe().first().wifiOnlyDownloads },
                            onFinished = { nav.navigate(Route.Chat.path) { popUpTo(0) } },
                        )
                    })
                    OnboardingScreen(viewModel = vm)
                }
                composable(Route.Chat.path) {
                    val vm: ChatViewModel = viewModel(factory = factory {
                        ChatViewModel(app.chats, app.models, settingsRepo, app.engine, initialChatId = null)
                    })
                    ChatScreen(
                        viewModel = vm,
                        onBack = { nav.popBackStack() },
                        onOpenModels = { nav.navigate(Route.Models.path) },
                    )
                }
                composable(Route.Models.path) {
                    val vm: ModelsViewModel = viewModel(factory = factory {
                        ModelsViewModel(
                            catalogue = ModelCatalog.all,
                            models = app.models,
                            settings = settingsRepo,
                            downloads = app.downloads,
                            freeStorageBytes = { app.filesDir.usableSpace },
                            recommendedTier = null,
                        )
                    })
                    ModelsScreen(viewModel = vm)
                }
                composable(Route.Settings.path) {
                    val vm: SettingsViewModel = viewModel(factory = factory {
                        SettingsViewModel(
                            settings = settingsRepo,
                            downloads = app.downloads,
                            wipeLocalData = { app.database.clearAllTables() },
                            observed = settingsRepo.observe(),
                        )
                    })
                    // PHASE-1 NOTE: Entitlement is hard-coded to FREE until Play Billing products
                    // and the real BillingGateway observation are wired. Offline chat remains
                    // available regardless of plan. See NOTES.md and core/billing.
                    SettingsScreen(
                        viewModel = vm,
                        subscription = SettingsPlanner.subscriptionStatus(
                            entitlement = Entitlement(plan = Plan.FREE, expiresAtMs = null),
                            nowMs = System.currentTimeMillis(),
                        ),
                        shareCapacity = "", // placeholder until share relay is deployed
                    )
                }
            }
        }
    }
}
