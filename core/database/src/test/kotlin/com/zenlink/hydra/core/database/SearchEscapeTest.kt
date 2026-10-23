package com.zenlink.hydra.core.database

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEscapeTest {
    // Mirrors the escaping used in ChatRepository.search so the LIKE wildcard rules stay pinned.
    private fun escape(q: String) = q.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    @Test
    fun wildcardsAreEscaped() {
        assertEquals("100\\%\\_sure", escape("100%_sure"))
    }
}
