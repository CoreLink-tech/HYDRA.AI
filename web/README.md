# Hydra.AI web (cloud)

Marketing site, app API (for the Android app) and admin dashboard for Hydra.AI by ZenLink Technologies.
Stack: Next.js 16 (App Router), React 19, TypeScript, Tailwind v4, Drizzle ORM, Turso (libsql).

Privacy rule: this service never stores prompts, responses or chat history. Shared chats are stored as
AES-GCM ciphertext only. Keys stay on the device and in the share link fragment.

## Setup

Requires Node 20.9 or newer.

1. Install dependencies. The lockfile is npm's (package-lock.json), so use npm:

       npm install

   If you prefer pnpm: `pnpm install` works too, but approve the esbuild build scripts first
   (`pnpm approve-builds`) or pnpm will skip them.

2. Create the Turso database:

       brew install tursodatabase/tap/turso   # or: curl -sSfL https://get.tur.so/install.sh | bash
       turso auth login
       turso db create hydra-web
       turso db show hydra-web --url          # -> TURSO_DATABASE_URL
       turso db tokens create hydra-web       # -> TURSO_AUTH_TOKEN

   For quick local work you can skip Turso and use `TURSO_DATABASE_URL=file:local.db`.

3. Copy the env template and fill it in:

       cp .env.example .env.local

   Generate AUTH_SECRET with `openssl rand -base64 48`. Set ADMIN_USER and ADMIN_PASSWORD, or the admin
   area stays closed (fails closed by design).

4. Apply migrations and optionally seed one draft model row:

       npm run db:migrate
       npm run db:seed

5. Start the dev server:

       npm run dev

   Site: http://localhost:3000. Admin: http://localhost:3000/admin (basic auth). Health: /api/v1/health.

## Scripts

- `npm run dev`: development server
- `npm run build` / `npm start`: production build and serve
- `npm run typecheck`: strict TypeScript check
- `npm run db:generate`: create a new migration after editing src/db/schema.ts
- `npm run db:migrate`: apply migrations to TURSO_DATABASE_URL
- `npm run db:studio`: Drizzle Studio

## API (v1, for the Android app)

- POST /api/v1/auth/device: anonymous account for one install, body { deviceId }
- POST /api/v1/auth/signup and /api/v1/auth/login: email accounts, body { email, password }
- POST /api/v1/auth/logout: revoke the bearer token
- GET /api/v1/me: account summary (Bearer)
- GET /api/v1/subscription: plan and expiry (Bearer)
- GET /api/v1/models: versioned catalogue with sha256 and R2 download URLs. Optional Bearer; Pro-only
  download URLs appear only for Pro accounts
- POST /api/v1/billing/google-play/verify: verify a purchase token with Google (Bearer)
- POST /api/v1/billing/google-play/rtdn?key=...: Play real-time notifications (Pub/Sub push)
- POST /api/v1/shares: upload ciphertext { ciphertext, iv, mode, messageCount, expiresInHours? } (Bearer)
- GET /api/v1/shares: list your shares, metadata only (Bearer)
- GET /api/v1/shares/:id: recipient fetch of ciphertext (410 if revoked, expired or taken down)
- DELETE /api/v1/shares/:id: revoke and delete the blob (Bearer, owner)
- POST /api/v1/shares/:id/report: report a share { reason, details? }

Bearer tokens look like hy_... and are shown once. Only their sha256 is stored.

## Environment

See .env.example for every variable. Required in production: TURSO_DATABASE_URL, TURSO_AUTH_TOKEN,
AUTH_SECRET, ADMIN_USER, ADMIN_PASSWORD, R2_PUBLIC_BASE_URL. Google Play variables are needed only for
billing verification.

## Open items (TODOs in code)

- Google Play: create the service account and Play products (hydra_pro_monthly, hydra_pro_annual),
  then set GOOGLE_PLAY_SERVICE_ACCOUNT_EMAIL and GOOGLE_PLAY_SERVICE_ACCOUNT_PRIVATE_KEY and set up
  the Pub/Sub topic for RTDN. Replace PLAY_RTDN_SECRET check with OIDC verification.
- Cloudflare R2: bucket plus public domain for model files. Pro-only files need signed URLs.
- Rate limiting on auth, share retrieval and reports.
- Email verification and password reset.
- Privacy policy and model licence review (commercialUseReviewed must be ticked before publishing).
- Web viewer for shared chats in /s/[id] (currently an explainer page).
