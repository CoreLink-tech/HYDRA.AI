NOTES: stubbed or unfinished items

Nothing below is configured in this repo. Each item must be set up before a release build works end to end.

Play Billing and Play Console
- Play service account: GOOGLE_PLAY_SERVICE_ACCOUNT_EMAIL and GOOGLE_PLAY_SERVICE_ACCOUNT_PRIVATE_KEY (see web/.env.example). Not set in this repo.
- Package name: GOOGLE_PLAY_PACKAGE_NAME. Not set.
- Subscription product ids and base plans are not created in Play Console. core:billing uses a mock gateway in dev builds.
- Play Developer API credentials are needed by the server to verify purchase tokens (web /api/v1/billing).

R2 / object storage
- Model download base URL: R2_PUBLIC_BASE_URL (see web/.env.example). Not set. core:download cannot fetch real models until it is set and checksums are filled in.

Relay
- Online share relay: web/src/app/api/v1/shares on folk/cloud. Not deployed. NEXT_PUBLIC_SITE_URL and the app's base URL are not set.
- Nearby Connections transport is an interface only; the Google Play Services implementation is not written.

Email verification
- Account email verification is not wired. No mail provider is configured in web/.env.example. AUTH_SECRET is also required by next-auth.

RTDN secret
- Real-time developer notifications: web/src/app/api/v1/billing/google-play/rtdn/route.ts. Needs PLAY_RTDN_SECRET (see web/.env.example). Not set.

Crash reporting
- core:monitoring has no backend. Sentry or Crashlytics DSN is not set, so the default build reports nothing even when the user opts in.

Feature modules and app wiring
- feature:onboarding, feature:chat, feature:models, feature:settings, and the :app navigation graph are not finished at the time of this commit. See the commit log for what has landed.
- Full APK assembly is not verified in the build sandbox. Run ./gradlew assembleDebug on a machine with the Android SDK and NDK.
