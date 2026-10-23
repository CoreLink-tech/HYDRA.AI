package com.zenlink.hydra.core.model

import java.util.Locale

/** Plain-language sizes for the storage lines, e.g. "1.1 GB" or "491 MB". Pure JVM. */
object ByteFormat {
    fun human(bytes: Long): String {
        if (bytes < 0L) return "0 MB"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format(Locale.US, "%.1f GB", mb / 1024.0)
        } else {
            String.format(Locale.US, "%.0f MB", mb)
        }
    }
}
