Hydra.AI

Hydra.AI runs a small language model entirely on your Android phone. After the model is downloaded once, chat works offline and nothing you type leaves the device. Optional online sharing sends an end-to-end encrypted copy of a conversation: the key is generated on your phone and only travels in the link fragment, so the relay stores ciphertext it cannot read.

Status: core modules (billing, sharing, monitoring, designsystem) are built and unit tested on JVM. Feature modules and the :app wiring are in progress on this branch. See NOTES.md for everything that is still stubbed.

REPO LAYOUT

app/                     Android app module: navigation and onboarding wiring
core/llama/              JNI wrapper around llama.cpp (libhydra_llama.so, prebuilt arm64-v8a)
core/download/           Resumable model downloads with checksum verification
core/device/             Device check: RAM, storage, CPU, recommended model tier
core/database/           Room storage for chats, messages, installed models, settings
core/model/              Pure Kotlin domain types shared across modules
core/billing/            Play Billing wrapper, mock gateway for dev, Free vs Pro policy
core/sharing/            Nearby transport interface, E2E-encrypted online share client
core/monitoring/         Opt-in crash reporting stub, never sends prompt content
core/designsystem/       Material 3 dark theme, typography, common components
feature/onboarding/      Device check, recommended models, download, first run
feature/chat/            Streaming chat UI wired to core:llama
feature/models/          Model catalogue, download management
feature/settings/        Preferences, consent, subscription
scripts/vendor-llama.sh  Fetches and builds llama.cpp into core/llama
web/                     Next.js relay and marketing site (see Web setup below)

ANDROID STUDIO BUILD

Requirements
- JDK 17
- Android SDK with platform 35
- NDK 27.0.12077973 (install via SDK Manager, NDK (Side by side))
- CMake 3.22.1 (SDK Manager, CMake)

Steps
1. Clone the repo and check out folk/android-app.
2. Run the llama.cpp vendoring script from the repo root:
   bash scripts/vendor-llama.sh
   This clones llama.cpp at the pinned tag (b10232, override with LLAMA_TAG) into core/llama/third_party/llama.cpp. Sources only; no binaries are committed.
3. Open the project folder in Android Studio and let Gradle sync.
4. Build a debug APK from the command line:
   ./gradlew assembleDebug
5. Install with Run in Android Studio, or adb install app/build/outputs/apk/debug/app-debug.apk.

Pure JVM unit tests run without the SDK and NDK:
   ./gradlew :core:billing:testDebugUnitTest :core:sharing:testDebugUnitTest :core:monitoring:testDebugUnitTest

WEB SETUP

The web/ directory is a Next.js 16 app (React 19, next-auth, Drizzle with libsql/Turso, Stripe). It hosts the share relay under /api/v1/shares and the billing verification routes under /api/v1/billing. The Android app talks only to that API contract.
  cd web
  npm install
  cp .env.example .env.local   then fill in the variables named in web/.env.example
  npm run dev

web/.env.example lists: TURSO_DATABASE_URL, TURSO_AUTH_TOKEN, AUTH_SECRET, ADMIN_USER, ADMIN_PASSWORD, R2_PUBLIC_BASE_URL, NEXT_PUBLIC_SITE_URL, PLAY_RTDN_SECRET, GOOGLE_PLAY_PACKAGE_NAME, GOOGLE_PLAY_SERVICE_ACCOUNT_EMAIL, GOOGLE_PLAY_SERVICE_ACCOUNT_PRIVATE_KEY. Set them in your own environment; never commit real values.
The relay and billing routes live on the folk/cloud branch.

LICENSE

See the repository for license terms.
