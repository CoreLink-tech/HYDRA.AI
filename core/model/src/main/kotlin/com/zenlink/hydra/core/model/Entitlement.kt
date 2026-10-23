package com.zenlink.hydra.core.model

enum class Plan { FREE, PRO }

/**
 * What the user has paid for. Core offline chat never depends on this: a user with a downloaded
 * model keeps chatting after a Pro subscription expires. Expiry only gates Pro-only downloads
 * and tools.
 */
data class Entitlement(
    val plan: Plan,
    val expiresAtMs: Long?,
) {
    fun isProActive(nowMs: Long): Boolean =
        plan == Plan.PRO && (expiresAtMs == null || expiresAtMs > nowMs)

    companion object {
        val FREE = Entitlement(Plan.FREE, null)
    }
}
