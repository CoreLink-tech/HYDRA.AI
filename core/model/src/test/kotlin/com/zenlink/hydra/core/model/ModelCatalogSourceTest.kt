package com.zenlink.hydra.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ModelCatalogSourceTest {
    @Test
    fun localSourceMatchesShippedCatalogue() {
        val source: ModelCatalogSource = LocalModelCatalogSource
        assertEquals(ModelCatalog.all, source.current())
        assertNotNull(source.byId(ModelCatalog.FREE_MODEL_ID))
    }
}
