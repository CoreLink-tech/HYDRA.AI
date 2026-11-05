package com.zenlink.hydra.core.model

/**
 * Source of curated model descriptors. The shipped [ModelCatalog] is the offline snapshot.
 * A future network implementation can refresh from `GET /api/v1/models` while keeping this
 * interface so feature modules do not hard-code HTTP.
 */
interface ModelCatalogSource {
    /** Current catalogue entries (may be cached). */
    fun current(): List<ModelDescriptor>

    /** Lookup by id against the current snapshot. */
    fun byId(id: String): ModelDescriptor? = current().firstOrNull { it.id == id }
}

/** Default offline catalogue used until a network-backed source is configured. */
object LocalModelCatalogSource : ModelCatalogSource {
    override fun current(): List<ModelDescriptor> = ModelCatalog.all
}
