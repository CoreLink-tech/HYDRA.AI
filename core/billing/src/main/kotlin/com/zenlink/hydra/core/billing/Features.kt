package com.zenlink.hydra.core.billing

import com.zenlink.hydra.core.model.Entitlement

/** Things the app can gate. Offline chat with a downloaded model is deliberately not one of them. */
enum class Feature {
    /** Chatting with a model that is already on the device. Never gated. */
    OFFLINE_CHAT,
    /** Downloading a Pro-tier model. */
    PRO_MODEL_DOWNLOAD,
    /** Creating links to share conversations. Free plan has a live-share cap. */
    ONLINE_SHARE,
}

/**
 * Pure entitlement rules, no Android. Invariant: a downloaded model keeps working after a
 * subscription expires. Expiry only closes new Pro downloads and Pro sharing capacity.
 */
object EntitlementPolicy {
    const val FREE_LIVE_SHARES = 3
    const val PRO_LIVE_SHARES = 50

    fun isAllowed(feature: Feature, entitlement: Entitlement, nowMs: Long): Boolean =
        when (feature) {
            Feature.OFFLINE_CHAT -> true // downloaded models never expire with the subscription
            Feature.PRO_MODEL_DOWNLOAD -> entitlement.isProActive(nowMs)
            Feature.ONLINE_SHARE -> true // capacity is checked with liveShareLimit
        }

    fun liveShareLimit(entitlement: Entitlement, nowMs: Long): Int =
        if (entitlement.isProActive(nowMs)) PRO_LIVE_SHARES else FREE_LIVE_SHARES
}
