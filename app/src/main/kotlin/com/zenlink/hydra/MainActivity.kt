package com.zenlink.hydra

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zenlink.hydra.core.designsystem.HydraIcons
import com.zenlink.hydra.core.designsystem.HydraMotion
import com.zenlink.hydra.core.designsystem.HydraNavItem
import com.zenlink.hydra.core.designsystem.HydraNavigationBar
import com.zenlink.hydra.core.designsystem.HydraTheme
import com.zenlink.hydra.core.designsystem.LocalReducedMotion
import com.zenlink.hydra.core.designsystem.ThemeMode
import com.zenlink.hydra.core.designsystem.hydra
import com.zenlink.hydra.core.designsystem.hydraTween
import com.zenlink.hydra.core.designsystem.resolveDark
import com.zenlink.hydra.core.device.DeviceChecker
import com.zenlink.hydra.core.device.TierRecommender
import com.zenlink.hydra.core.model.AppSettings
import com.zenlink.hydra.core.model.Entitlement
import com.zenlink.hydra.core.model.InstalledModel
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelState
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    @Volatile
    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the splash only until settings have loaded (so a returning user never sees a flash of
        // onboarding). The timer is a safety net: the splash can never be held longer than this.
        splash.setKeepOnScreenCondition { keepSplash }
        Handler(Looper.getMainLooper()).postDelayed({ keepSplash = false }, 1500)
        enableEdgeToEdge()

        setContent {
            var crashReport by remember { mutableStateOf(CrashLog.read(this@MainActivity)) }
            val report = crashReport
            if (report != null) {
                // A crash report is shown before anything touches the database.
                LaunchedEffect(Unit) { keepSplash = false }
                HydraTheme {
                    CrashReportScreen(report = report) {
                        CrashLog.clear(this@MainActivity)
                        crashReport = null
                    }
                }
            } else {
                AppContent(onReady = { keepSplash = false })
            }
        }
    }
}

/** Loads settings, applies the chosen theme and system-bar style, then shows the app. */
@Composable
private fun AppContent(onReady: () -> Unit) {
    val activity = LocalContext.current as ComponentActivity
    val app = activity.application as HydraApp
    val settings by produceState<AppSettings?>(initialValue = null) {
        app.settings.observe().collect { value = it }
    }
    val loaded = settings
    LaunchedEffect(loaded != null) { if (loaded != null) onReady() }

    val mode = ThemeMode.fromKey(loaded?.themeMode)
    val dark = mode.resolveDark()
    // Keep status and navigation bar icons readable when the chosen theme differs from the phone's.
    DisposableEffect(dark) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
        )
        onDispose { }
    }

    HydraTheme(mode = mode) {
        if (loaded != null) HydraRoot(app, loaded)
    }
}

/** Builds a view-model from the app-owned dependencies. Keeps MainActivity free of wiring details. */
private fun <T : ViewModel> factory(make: () -> T) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM = make() as VM
}

private val navItems = listOf(
    HydraNavItem(Route.Chat.path, "Chat", HydraIcons.Chat),
    HydraNavItem(Route.Models.path, "Models", HydraIcons.Models),
    HydraNavItem(Route.Settings.path, "Settings", HydraIcons.Settings),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HydraRoot(app: HydraApp, initial: AppSettings) {
    val context = LocalContext.current
    val nav = rememberNavController()
    val settingsRepo = app.settings
    // Chosen once, from settings that have already loaded, so the start screen never flips.
    val start = remember { AppRouter.startRoute(initial.onboardingComplete).path }
    val online by rememberIsOnline()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: start
    val installed by produceState(initialValue = emptyList<InstalledModel>()) {
        app.models.observeAll().collect { value = it }
    }
    val hasModel = installed.any { it.state == ModelState.INSTALLED }
    val reduced = LocalReducedMotion.current

    // The tab bar steps aside while the keyboard is open so the composer sits right above the keys.
    val showBars = AppRouter.tabFor(current) != null && !WindowInsets.isImeVisible

    /** Switches tabs without piling up screens, and keeps each tab's state (the open chat, scroll). */
    fun goTab(route: String) {
        nav.navigate(route) {
            popUpTo(Route.Chat.path) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.hydra.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBars,
                enter = fadeIn(hydraTween<Float>(reduced, HydraMotion.Short)) + expandVertically(hydraTween(reduced, HydraMotion.Short)),
                exit = fadeOut(hydraTween<Float>(reduced, HydraMotion.Short)) + shrinkVertically(hydraTween(reduced, HydraMotion.Short)),
            ) {
                Column {
                    AppRouter.offlineIndicator(online, hasModel)?.let { OfflineStrip(it) }
                    HydraNavigationBar(items = navItems, selectedRoute = current, onSelect = ::goTab)
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            enterTransition = { fadeIn(hydraTween<Float>(reduced, HydraMotion.Medium)) },
            exitTransition = { fadeOut(hydraTween<Float>(reduced, HydraMotion.Short)) },
            popEnterTransition = { fadeIn(hydraTween<Float>(reduced, HydraMotion.Medium)) },
            popExitTransition = { fadeOut(hydraTween<Float>(reduced, HydraMotion.Short)) },
        ) {
            composable(Route.Onboarding.path) {
                val vm: OnboardingViewModel = viewModel(factory = factory {
                    OnboardingViewModel(
                        settings = settingsRepo,
                        downloads = app.downloads,
                        deviceProfile = { DeviceChecker(app).snapshot() },
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
                    online = online,
                    onOpenModels = { goTab(Route.Models.path) },
                )
            }
            composable(Route.Models.path) {
                val vm: ModelsViewModel = viewModel(factory = factory {
                    // Same device check onboarding uses, so a model that cannot run here says so.
                    val rec = TierRecommender.recommend(DeviceChecker(app).snapshot(), ModelCatalog.all)
                    ModelsViewModel(
                        catalogue = ModelCatalog.all,
                        models = app.models,
                        settings = settingsRepo,
                        downloads = app.downloads,
                        freeStorageBytes = { app.filesDir.usableSpace },
                        recommendedTier = rec.recommended,
                        fittingTiers = rec.fitting.toSet(),
                    )
                })
                ModelsScreen(viewModel = vm)
            }
            composable(Route.Settings.path) {
                val vm: SettingsViewModel = viewModel(factory = factory {
                    SettingsViewModel(
                        settings = settingsRepo,
                        downloads = app.downloads,
                        // Room refuses to clear tables on the main thread.
                        wipeLocalData = { withContext(Dispatchers.IO) { app.database.clearAllTables() } },
                        observed = settingsRepo.observe(),
                        installedBytes = app.models.observeAll().map { list ->
                            list.filter { it.state == ModelState.INSTALLED }.sumOf { it.bytesDownloaded }
                        },
                        freeStorageBytes = { app.filesDir.usableSpace },
                    )
                })
                SettingsScreen(
                    viewModel = vm,
                    // Billing is still the mock gateway, so the plan is Free until Play Billing is wired.
                    subscription = SettingsPlanner.subscriptionStatus(
                        entitlement = Entitlement(plan = Plan.FREE, expiresAtMs = null),
                        nowMs = System.currentTimeMillis(),
                    ),
                    shareCapacity = "",
                    onOpenModels = { goTab(Route.Models.path) },
                    appVersion = BuildConfig.VERSION_NAME,
                    privacyPolicyUrl = BuildConfig.PRIVACY_POLICY_URL,
                    onOpenUrl = { openUrl(context, it) },
                )
            }
        }
    }
}

/** A slim strip above the tab bar. Offline chat still works with a downloaded model. */
@Composable
private fun OfflineStrip(message: String) {
    val c = MaterialTheme.hydra
    Row(
        Modifier.fillMaxWidth().background(c.surfaceElevated).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(HydraIcons.Offline, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
    }
}

/** Live network state. Updates as the phone gains or loses a connection. */
@Composable
private fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = isOnlineNow(context)) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        if (cm == null) {
            value = false
            return@produceState
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                value = true
            }

            override fun onLost(network: Network) {
                value = isOnlineNow(context)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        awaitDispose { cm.unregisterNetworkCallback(callback) }
    }
}

private fun isOnlineNow(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // No browser installed. Nothing useful to do.
    }
}
