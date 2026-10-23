#include <jni.h>

#include <android/log.h>

#include <memory>
#include <mutex>
#include <string>

#include "engine.h"

namespace {

std::mutex gMutex;
std::unique_ptr<hydra::Engine> gEngine;

std::string toStd(JNIEnv* env, jstring s) {
    if (!s) return std::string();
    const char* chars = env->GetStringUTFChars(s, nullptr);
    std::string out(chars);
    env->ReleaseStringUTFChars(s, chars);
    return out;
}

jstring toJ(JNIEnv* env, const std::string& s) { return env->NewStringUTF(s.c_str()); }

}  // namespace

extern "C" {

// Kotlin: external fun nativeLoad(path: String, contextTokens: Int, threads: Int, batchTokens: Int): String
JNIEXPORT jstring JNICALL
Java_com_zenlink_hydra_core_llama_NativeBridge_nativeLoad(JNIEnv* env, jobject, jstring path,
                                                          jint contextTokens, jint threads,
                                                          jint batchTokens) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (!gEngine) gEngine = std::make_unique<hydra::Engine>();
    hydra::Engine::LoadOptions opts;
    opts.contextTokens = contextTokens;
    opts.threads = threads;
    opts.batchTokens = batchTokens;
    return toJ(env, gEngine->load(toStd(env, path), opts));
}

// Kotlin: external fun nativeGenerate(prompt: String, temperature: Float, topP: Float, topK: Int,
//                                     maxNewTokens: Int, seed: Int, sink: TokenSink): String
// Pieces are delivered to sink.onToken(String): Boolean (false cancels). Returns the stop reason.
JNIEXPORT jstring JNICALL
Java_com_zenlink_hydra_core_llama_NativeBridge_nativeGenerate(JNIEnv* env, jobject, jstring prompt,
                                                              jfloat temperature, jfloat topP,
                                                              jint topK, jint maxNewTokens,
                                                              jint seed, jobject sink) {
    jclass sinkClass = env->GetObjectClass(sink);
    jmethodID onToken = env->GetMethodID(sinkClass, "onToken", "(Ljava/lang/String;)Z");
    env->DeleteLocalRef(sinkClass);
    if (!onToken) return toJ(env, "internal error: sink");

    std::lock_guard<std::mutex> lock(gMutex);
    if (!gEngine || !gEngine->isLoaded()) return toJ(env, "model not loaded");

    hydra::Engine::SampleOptions opts;
    opts.temperature = temperature;
    opts.topP = topP;
    opts.topK = topK;
    opts.maxNewTokens = maxNewTokens;
    opts.seed = static_cast<uint32_t>(seed);

    std::string reason = gEngine->generate(toStd(env, prompt), opts, [&](const std::string& piece) {
        jstring jp = env->NewStringUTF(piece.c_str());
        jboolean keepGoing = env->CallBooleanMethod(sink, onToken, jp);
        env->DeleteLocalRef(jp);
        return keepGoing == JNI_TRUE;
    });
    return toJ(env, reason);
}

JNIEXPORT void JNICALL
Java_com_zenlink_hydra_core_llama_NativeBridge_nativeCancel(JNIEnv*, jobject) {
    // Deliberately lock-free: cancel must not wait behind a running generate().
    if (gEngine) gEngine->cancel();
}

JNIEXPORT void JNICALL
Java_com_zenlink_hydra_core_llama_NativeBridge_nativeRelease(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(gMutex);
    if (gEngine) gEngine->release();
}

}  // extern "C"
