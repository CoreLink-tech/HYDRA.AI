package com.zenlink.hydra.core.llama

/**
 * Thin JNI surface. Symbols are bound in hydra_llama_jni.cpp by name, so keep
 * the package and class in sync with that file.
 */
internal object NativeBridge {
    init {
        System.loadLibrary("hydra_llama")
    }

    /** Returns "" on success, otherwise the reason it failed. */
    external fun nativeLoad(
        path: String,
        contextTokens: Int,
        threads: Int,
        batchTokens: Int,
    ): String

    /** Returns the stop reason: end, length, cancelled, or an error description. */
    external fun nativeGenerate(
        prompt: String,
        temperature: Float,
        topP: Float,
        topK: Int,
        maxNewTokens: Int,
        seed: Int,
        sink: TokenSink,
    ): String

    external fun nativeCancel()

    external fun nativeRelease()
}

/** Called from native code for every decoded piece. Return false to stop generation. */
internal fun interface TokenSink {
    fun onToken(piece: String): Boolean
}
