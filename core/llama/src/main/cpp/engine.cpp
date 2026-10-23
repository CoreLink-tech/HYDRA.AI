#include "engine.h"

#include <android/log.h>

#include <algorithm>
#include <cstring>
#include <functional>

#define LOG_TAG "HydraLlama"
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

namespace hydra {

Engine::~Engine() { release(); }

void Engine::release() {
    if (ctx_) {
        llama_free(ctx_);
        ctx_ = nullptr;
    }
    if (model_) {
        llama_model_free(model_);
        model_ = nullptr;
    }
}

std::string Engine::load(const std::string& path, const LoadOptions& opts) {
    release();
    cancelled_.store(false);

    llama_model_params mparams = llama_model_default_params();
    mparams.n_gpu_layers = 0;  // CPU only: GPU offload is not reliable across Android devices.
    model_ = llama_model_load_from_file(path.c_str(), mparams);
    if (!model_) return "could not open the model file";

    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx = static_cast<uint32_t>(opts.contextTokens);
    cparams.n_batch = static_cast<uint32_t>(opts.batchTokens);
    cparams.n_threads = opts.threads;
    cparams.n_threads_batch = opts.threads;
    ctx_ = llama_init_from_model(model_, cparams);
    if (!ctx_) {
        release();
        return "not enough memory to create the context";
    }
    contextTokens_ = opts.contextTokens;
    return "";
}

namespace {

std::vector<llama_token> tokenize(const llama_vocab* vocab, const std::string& text, bool addSpecial) {
    int32_t needed = -llama_tokenize(vocab, text.c_str(), static_cast<int32_t>(text.size()),
                                     nullptr, 0, addSpecial, true);
    std::vector<llama_token> out(static_cast<size_t>(needed));
    int32_t n = llama_tokenize(vocab, text.c_str(), static_cast<int32_t>(text.size()),
                               out.data(), needed, addSpecial, true);
    out.resize(static_cast<size_t>(std::max(n, 0)));
    return out;
}

std::string pieceOf(const llama_vocab* vocab, llama_token token) {
    char buf[256];
    int32_t n = llama_token_to_piece(vocab, token, buf, sizeof(buf), 0, true);
    if (n < 0) return std::string();
    return std::string(buf, static_cast<size_t>(n));
}

}  // namespace

std::string Engine::generate(const std::string& prompt, const SampleOptions& opts,
                             const std::function<bool(const std::string&)>& onPiece) {
    if (!ctx_ || !model_) return "model not loaded";
    cancelled_.store(false);
    const llama_vocab* vocab = llama_model_get_vocab(model_);

    std::vector<llama_token> tokens = tokenize(vocab, prompt, true);
    if (tokens.empty()) return "empty prompt";
    if (static_cast<int32_t>(tokens.size()) >= contextTokens_) return "prompt is longer than the context window";

    llama_memory_clear(llama_get_memory(ctx_), true);

    // Prefill the whole prompt in batch-sized chunks.
    const int32_t batchMax = static_cast<int32_t>(llama_n_batch(ctx_));
    for (size_t i = 0; i < tokens.size(); i += static_cast<size_t>(batchMax)) {
        int32_t n = static_cast<int32_t>(std::min<size_t>(batchMax, tokens.size() - i));
        llama_batch batch = llama_batch_get_one(tokens.data() + i, n);
        if (llama_decode(ctx_, batch) != 0) return "decode failed while reading the prompt";
    }

    llama_sampler_chain_params sparams = llama_sampler_chain_default_params();
    llama_sampler* chain = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(chain, llama_sampler_init_top_k(opts.topK));
    llama_sampler_chain_add(chain, llama_sampler_init_top_p(opts.topP, 1));
    llama_sampler_chain_add(chain, llama_sampler_init_temp(opts.temperature));
    llama_sampler_chain_add(chain, llama_sampler_init_dist(opts.seed));

    std::string stop = "stopped";
    int32_t produced = 0;
    while (produced < opts.maxNewTokens) {
        if (cancelled_.load()) {
            stop = "cancelled";
            break;
        }
        llama_token id = llama_sampler_sample(chain, ctx_, -1);
        if (llama_vocab_is_eog(vocab, id)) {
            stop = "end";
            break;
        }
        std::string piece = pieceOf(vocab, id);
        if (!piece.empty() && !onPiece(piece)) {
            stop = "cancelled";
            break;
        }
        llama_batch next = llama_batch_get_one(&id, 1);
        if (llama_decode(ctx_, next) != 0) {
            stop = "decode failed";
            break;
        }
        produced++;
    }
    if (produced >= opts.maxNewTokens && stop == "stopped") stop = "length";

    llama_sampler_free(chain);
    return stop;
}

}  // namespace hydra
