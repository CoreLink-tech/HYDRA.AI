package com.zenlink.hydra.core.model

import kotlinx.serialization.Serializable

/** Plain-language model tiers shown to users. Users never pick raw model files. */
enum class ModelTier(val label: String, val blurb: String) {
    FAST("Fast", "Quick answers, summaries and rewrites on almost any phone"),
    BALANCED("Balanced", "The everyday assistant for writing, study and planning"),
    ADVANCED("Advanced", "Deeper, higher-quality answers for phones with plenty of memory"),
}

/**
 * A curated, versioned GGUF model entry. The catalogue is shipped in the app and can be
 * refreshed from the Hydra API later. [version] changes whenever the file, chat template
 * or metadata changes, so the app can offer an update instead of silently breaking.
 */
@Serializable
data class ModelDescriptor(
    val id: String,
    val displayName: String,
    val publisher: String,
    val version: Int,
    val description: String,
    val fileName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val sha256: String,
    val tier: ModelTier,
    val contextTokens: Int,
    val minRamMb: Int,
    val minFreeStorageMb: Int,
    val minAndroidSdk: Int,
    val license: String,
    val licenseUrl: String,
    val strengths: List<String>,
    val limitations: List<String>,
    val requiresPro: Boolean,
) {
    /** Space the installed file occupies, with a margin for temporary files during download. */
    val installedSizeBytes: Long get() = sizeBytes + sizeBytes / 10
}

/** Curated launch catalogue. Each entry must pass the licence review listed in NOTES.md. */
object ModelCatalog {
    const val FREE_MODEL_ID = "qwen2.5-0.5b-instruct-q4km"

    val all: List<ModelDescriptor> = listOf(
        ModelDescriptor(
            id = FREE_MODEL_ID,
            displayName = "Hydra Fast",
            publisher = "Qwen (Alibaba Cloud) via Hugging Face",
            version = 1,
            description = "Small and quick. Good for short questions, summaries and rewriting.",
            fileName = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            sizeBytes = 491_400_032L,
            sha256 = "74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db",
            tier = ModelTier.FAST,
            contextTokens = 4096,
            minRamMb = 3072,
            minFreeStorageMb = 1500,
            minAndroidSdk = 26,
            license = "Apache 2.0",
            licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
            strengths = listOf("Very fast", "Light on battery", "Runs on older phones"),
            limitations = listOf("Simple reasoning only", "Can make factual mistakes"),
            requiresPro = false,
        ),
        ModelDescriptor(
            id = "qwen2.5-1.5b-instruct-q4km",
            displayName = "Hydra Balanced",
            publisher = "Qwen (Alibaba Cloud) via Hugging Face",
            version = 1,
            description = "The default assistant for writing, study explanations and planning.",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            sizeBytes = 1_117_320_736L,
            sha256 = "6a1a2eb6d15622bf3c96857206351ba97e1af16c30d7a74ee38970e434e9407e",
            tier = ModelTier.BALANCED,
            contextTokens = 4096,
            minRamMb = 6144,
            minFreeStorageMb = 2500,
            minAndroidSdk = 26,
            license = "Apache 2.0",
            licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0",
            strengths = listOf("Good writing quality", "Explains study topics clearly", "Solid code help"),
            limitations = listOf("Long documents may be cut short", "Not a replacement for expert advice"),
            requiresPro = true,
        ),
        ModelDescriptor(
            id = "phi-3.5-mini-instruct-q4km",
            displayName = "Hydra Advanced",
            publisher = "Microsoft via Hugging Face (quantized by bartowski)",
            version = 1,
            description = "Higher-quality reasoning and longer answers for phones with plenty of memory.",
            fileName = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
            downloadUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf",
            sizeBytes = 2_393_232_672L,
            sha256 = "e4165e3a71af97f1b4820da61079826d8752a2088e313af0c7d346796c38eff5",
            tier = ModelTier.ADVANCED,
            contextTokens = 4096,
            minRamMb = 8192,
            minFreeStorageMb = 4000,
            minAndroidSdk = 31,
            license = "MIT",
            licenseUrl = "https://opensource.org/license/mit",
            strengths = listOf("Stronger reasoning", "Better long-form answers", "Good code explanations"),
            limitations = listOf("Slower on mid-range phones", "Heavier on battery and heat"),
            requiresPro = true,
        ),
    )

    fun byId(id: String): ModelDescriptor? = all.firstOrNull { it.id == id }
}
