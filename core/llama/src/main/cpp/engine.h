#pragma once

#include <atomic>
#include <cstdint>
#include <functional>
#include <string>
#include <vector>

#include "llama.h"

namespace hydra {

/** Owns one loaded GGUF model and one decoding context. Not thread-safe: one caller at a time. */
class Engine {
public:
    struct LoadOptions {
        int32_t contextTokens = 4096;
        int32_t threads = 4;
        int32_t batchTokens = 512;
    };

    struct SampleOptions {
        float temperature = 0.7f;
        float topP = 0.9f;
        int32_t topK = 40;
        int32_t maxNewTokens = 768;
        uint32_t seed = 0;
    };

    Engine() = default;
    ~Engine();

    Engine(const Engine&) = delete;
    Engine& operator=(const Engine&) = delete;

    /** Returns empty string on success, otherwise a human-readable reason. */
    std::string load(const std::string& path, const LoadOptions& opts);

    /** Feeds a chat-formatted prompt and streams pieces through [onPiece]. Returns the reason it stopped. */
    std::string generate(const std::string& prompt, const SampleOptions& opts,
                         const std::function<bool(const std::string&)>& onPiece);

    void cancel() { cancelled_.store(true); }
    void release();
    bool isLoaded() const { return model_ != nullptr; }

private:
    llama_model* model_ = nullptr;
    llama_context* ctx_ = nullptr;
    std::atomic<bool> cancelled_{false};
    int32_t contextTokens_ = 0;
};

}  // namespace hydra
