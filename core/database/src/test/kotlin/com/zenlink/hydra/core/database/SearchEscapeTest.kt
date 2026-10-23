package com.zenlink.hydra.core.database

import com.zenlink.hydra.core.database.repo.escapeLike
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEscapeTest {
    @Test
    fun wildcardsAreEscaped() {
        assertEquals("100\\%\\_sure", escapeLike("100%_sure"))
    }

    @Test
    fun backslashIsEscapedFirst() {
        assertEquals("a\\\\b", escapeLike("a\\b"))
    }

    @Test
    fun plainTextUnchanged() {
        assertEquals("hello world", escapeLike("hello world"))
    }
}
