package com.zenlink.hydra.core.sharing

/**
 * Builds and parses share links. The key lives only in the fragment (#k=...), so the relay
 * never sees it. The link is https://<site>/s/<id>#k=<key>.
 */
object ShareLink {
    private const val FRAGMENT_PREFIX = "#k="

    fun build(siteBase: String, shareId: String, key: ByteArray): String =
        siteBase.trimEnd('/') + "/s/" + shareId + FRAGMENT_PREFIX + ShareCrypto.encodeKey(key)

    data class Parsed(val shareId: String, val key: ByteArray)

    /** Returns null for anything that is not a well-formed share link. */
    fun parse(link: String): Parsed? {
        val hash = link.indexOf('#')
        if (hash < 0) return null
        val fragment = link.substring(hash)
        if (!fragment.startsWith(FRAGMENT_PREFIX)) return null
        val path = link.substring(0, hash)
        val marker = "/s/"
        val idStart = path.lastIndexOf(marker)
        if (idStart < 0) return null
        val id = path.substring(idStart + marker.length)
        if (!ID_RE.matches(id)) return null
        val key = runCatching { ShareCrypto.decodeKey(fragment.removePrefix(FRAGMENT_PREFIX)) }.getOrNull()
            ?: return null
        return Parsed(id, key)
    }

    private val ID_RE = Regex("^[A-Za-z0-9_-]{16,64}$")
}
