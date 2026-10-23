package com.zenlink.hydra.feature.settings

import com.zenlink.hydra.core.billing.EntitlementPolicy
import com.zenlink.hydra.core.model.Entitlement
import com.zenlink.hydra.core.model.Plan

/** Pure rules behind the settings screen. No Android types, so it runs as JVM unit tests. */
object SettingsPlanner {

    data class SubscriptionStatus(val title: String, val detail: String, val proActive: Boolean)

    fun subscriptionStatus(entitlement: Entitlement, nowMs: Long): SubscriptionStatus {
        if (entitlement.isProActive(nowMs)) {
            val expiry = entitlement.expiresAtMs
            val detail = if (expiry == null) "Pro is active." else "Pro is active until the renewal date shown in Play."
            return SubscriptionStatus("Pro", detail, true)
        }
        val lapsed = entitlement.plan == Plan.PRO
        return SubscriptionStatus(
            title = "Free",
            detail = if (lapsed) {
                "Your Pro plan ended. Downloaded models still work offline."
            } else {
                "Free plan. Downloaded models work offline with no subscription."
            },
            proActive = false,
        )
    }

    /** Live share capacity line, e.g. "2 of 3 live shares used". */
    fun shareCapacityLine(activeShares: Int, entitlement: Entitlement, nowMs: Long): String {
        val limit = EntitlementPolicy.liveShareLimit(entitlement, nowMs)
        return "$activeShares of $limit live shares used"
    }

    /** Wi-Fi-only is the safe default. Turning it off allows large downloads on mobile data. */
    fun wifiOnlyWarning(wifiOnly: Boolean): String? =
        if (wifiOnly) null else "Models are several GB. Downloads on mobile data can use a lot of your plan."

    /** The data-deletion steps shown before the user confirms. */
    fun deletionSteps(): List<String> = listOf(
        "Delete all chat history stored on this phone.",
        "Delete all downloaded models and their files.",
        "Revoke your live share links on the server.",
        "Reset local settings to defaults.",
    )

    fun deletionAllowed(confirmText: String): Boolean = confirmText.trim().equals("DELETE", ignoreCase = false)
}
