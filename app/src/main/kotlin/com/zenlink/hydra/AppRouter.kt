package com.zenlink.hydra

/** Top-level destinations. Onboarding is always first until the user finishes it. */
sealed class Route(val path: String) {
    data object Onboarding : Route("onboarding")
    data object Chat : Route("chat")
    data object Models : Route("models")
    data object Settings : Route("settings")

    companion object {
        // A getter, not a stored value. Route and its objects reference each other during class
        // initialisation, so a stored list can capture null for whichever object loads first.
        val tabs: List<Route> get() = listOf(Chat, Models, Settings)
    }
}

/** Pure routing rules for the app shell. No Android types, runs as JVM tests. */
object AppRouter {

    /** Start screen. Onboarding wins until it is marked complete. */
    fun startRoute(onboardingComplete: Boolean): Route =
        if (onboardingComplete) Route.Chat else Route.Onboarding

    /** Bottom bar is hidden during onboarding so the user cannot skip the flow. */
    fun showsBottomBar(route: Route): Boolean = route != Route.Onboarding

    /** Shown when the network is gone. Offline chat stays available with a downloaded model. */
    fun offlineIndicator(online: Boolean, hasDownloadedModel: Boolean): String? = when {
        online -> null
        hasDownloadedModel -> "Offline. Using your downloaded model."
        else -> "Offline. Download a model while you are online to chat without a connection."
    }

    /** Only the bottom tabs can be reselected; onboarding is never a tab. */
    fun tabFor(path: String): Route? = Route.tabs.firstOrNull { it.path == path }
}
