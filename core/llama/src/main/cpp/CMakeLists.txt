cmake_minimum_required(VERSION 3.22.1)
project(hydra_llama LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# llama.cpp is vendored by scripts/vendor-llama.sh. Fail early with a clear message if missing.
set(LLAMA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../third_party/llama.cpp")
if(NOT EXISTS "${LLAMA_DIR}/include/llama.h")
  message(FATAL_ERROR
    "llama.cpp not found at ${LLAMA_DIR}. Run scripts/vendor-llama.sh first (see README).")
endif()

# Only build the libraries we link against. No tools, examples, tests or HTTP server.
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_UI OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)      # keeps the APK free of libgomp
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)      # cross-compiling for Android
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)

add_subdirectory("${LLAMA_DIR}" llama.cpp EXCLUDE_FROM_ALL)

add_library(hydra_llama SHARED
    hydra_llama_jni.cpp
    engine.cpp
)

target_include_directories(hydra_llama PRIVATE
    "${LLAMA_DIR}/include"
    "${LLAMA_DIR}/ggml/include"
    "${LLAMA_DIR}/common"
)

target_link_libraries(hydra_llama PRIVATE llama ggml android log)
target_compile_options(hydra_llama PRIVATE -Wall -Wextra -fvisibility=hidden)
