package com.zenlink.hydra.feature.models

import com.zenlink.hydra.core.model.ModelCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class QuantizationTest {
    @Test
    fun readsTheLabelFromRealFileNames() {
        assertEquals("Q4_K_M", quantizationOf("qwen2.5-0.5b-instruct-q4_k_m.gguf"))
        assertEquals("Q4_K_M", quantizationOf("Phi-3.5-mini-instruct-Q4_K_M.gguf"))
    }

    @Test
    fun returnsNullWhenThereIsNoLabel() {
        assertNull(quantizationOf("model.gguf"))
    }

    @Test
    fun everyCatalogueModelHasALabel() {
        ModelCatalog.all.forEach { assertNotNull(it.displayName, quantizationOf(it.fileName)) }
    }

    @Test
    fun fitFlagFollowsTheFittingTiers() {
        val fitting = setOf(ModelCatalog.all.first().tier)
        val rows = ModelsPlanner.rows(ModelCatalog.all, emptyMap(), null, 50L * 1024 * 1024 * 1024, null, fitting)
        assertEquals(true, rows.first().fitsDevice)
        assertEquals(false, rows.last().fitsDevice)
        assertNull(ModelsPlanner.rows(ModelCatalog.all, emptyMap(), null, 0L, null).first().fitsDevice)
    }
}
