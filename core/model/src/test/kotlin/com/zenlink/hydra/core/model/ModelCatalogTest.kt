package com.zenlink.hydra.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelCatalogTest {

    @Test
    fun freeModelExistsAndIsNotProOnly() {
        val free = ModelCatalog.byId(ModelCatalog.FREE_MODEL_ID)
        assertNotNull(free)
        assertFalse(free!!.requiresPro)
        assertEquals(ModelTier.FAST, free.tier)
    }

    @Test
    fun everyEntryHasSha256AndPositiveSize() {
        ModelCatalog.all.forEach { m ->
            assertTrue("${m.id} missing sha256", m.sha256.length == 64)
            assertTrue("${m.id} size must be positive", m.sizeBytes > 0)
            assertTrue("${m.id} fileName should end with .gguf", m.fileName.endsWith(".gguf"))
        }
    }

    @Test
    fun atLeastOneFreeAndOneProModel() {
        assertTrue(ModelCatalog.all.any { !it.requiresPro })
        assertTrue(ModelCatalog.all.any { it.requiresPro })
    }

    @Test
    fun byIdReturnsNullForUnknown() {
        assertEquals(null, ModelCatalog.byId("does-not-exist"))
    }
}
