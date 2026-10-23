package com.zenlink.hydra.feature.models

import com.zenlink.hydra.core.download.StoragePolicy
import com.zenlink.hydra.core.model.InstalledModel
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelState
import com.zenlink.hydra.core.model.ModelTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsPlannerTest {

    private val catalogue = ModelCatalog.all
    private val gb = 1L shl 30

    @Test
    fun activeInstalledModelCannotBeDeletedOrSwitched() {
        val id = ModelCatalog.FREE_MODEL_ID
        val rec = mapOf(id to InstalledModel(id, 1, ModelState.INSTALLED, 100, 100, "/p", null))
        val row = ModelsPlanner.rows(catalogue, rec, id, 50 * gb, null).first { it.descriptor.id == id }
        assertFalse(row.canDelete)
        assertFalse(row.canSwitch)
        assertTrue(row.isActive)
    }

    @Test
    fun otherInstalledModelCanBeDeletedAndSwitched() {
        val free = ModelCatalog.FREE_MODEL_ID
        val other = catalogue.first { it.id != free }.id
        val rec = mapOf(other to InstalledModel(other, 1, ModelState.INSTALLED, 100, 100, "/p", null))
        val row = ModelsPlanner.rows(catalogue, rec, free, 50 * gb, null).first { it.descriptor.id == other }
        assertTrue(row.canDelete)
        assertTrue(row.canSwitch)
    }

    @Test
    fun notDownloadedModelCanStartAndNeverShowsProgress() {
        val row = ModelsPlanner.rows(catalogue, emptyMap(), null, 50 * gb, null).first()
        assertEquals(ModelState.NOT_DOWNLOADED, row.state)
        assertTrue(row.canDownload)
        assertEquals(null, row.progress)
    }

    @Test
    fun downloadingModelReportsFractionDone() {
        val id = catalogue.first().id
        val rec = mapOf(id to InstalledModel(id, 1, ModelState.DOWNLOADING, 250, 1000, null, null))
        val row = ModelsPlanner.rows(catalogue, rec, null, 50 * gb, null).first { it.descriptor.id == id }
        assertEquals(0.25f, row.progress!!, 0.0001f)
        assertFalse(row.canDownload)
    }

    @Test
    fun lowStorageBlocksStart() {
        val row = ModelsPlanner.rows(catalogue, emptyMap(), null, 0L, null).first()
        assertEquals(StoragePolicy.Verdict.BLOCKED_INSUFFICIENT, row.storage)
        assertFalse(ModelsPlanner.canStart(row.storage))
    }

    @Test
    fun recommendedTierIsFlaggedOnMatchingModels() {
        val rows = ModelsPlanner.rows(catalogue, emptyMap(), null, 50 * gb, ModelTier.BALANCED)
        assertTrue(rows.filter { it.recommended }.all { it.descriptor.tier == ModelTier.BALANCED })
    }

    @Test
    fun installedBytesCountsOnlyInstalledModels() {
        val recs = mapOf(
            "a" to InstalledModel("a", 1, ModelState.INSTALLED, 100, 100, "/a", null),
            "b" to InstalledModel("b", 1, ModelState.DOWNLOADING, 999, 1000, null, null),
        )
        assertEquals(100L, ModelsPlanner.installedBytes(recs))
    }
}
