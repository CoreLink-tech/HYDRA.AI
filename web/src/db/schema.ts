import { sql } from "drizzle-orm";
import { index, integer, sqliteTable, text, uniqueIndex } from "drizzle-orm/sqlite-core";

/**
 * Hydra.AI cloud schema (Turso / libsql, SQLite dialect).
 *
 * Privacy rules encoded here:
 * - No prompts, responses or chat history are stored server-side. Ever.
 * - Shared conversations are stored as ciphertext only (share_blobs). The decryption key
 *   lives in the URL fragment / recipient device and is never sent to the server.
 * - Timestamps are unix epoch milliseconds (integer) so they are portable to the app and easy to compare.
 */

const now = sql`(unixepoch() * 1000)`;

export const users = sqliteTable(
  "users",
  {
    id: text("id").primaryKey(), // random id, generated in app code
    email: text("email"), // null for anonymous device accounts
    displayName: text("display_name"),
    deviceId: text("device_id"), // stable install id from the Android app
    passwordHash: text("password_hash"), // scrypt, only for email accounts
    role: text("role", { enum: ["user", "admin"] }).notNull().default("user"),
    status: text("status", { enum: ["active", "suspended"] }).notNull().default("active"),
    createdAt: integer("created_at", { mode: "number" }).notNull().default(now),
    lastSeenAt: integer("last_seen_at", { mode: "number" }),
  },
  (t) => [
    uniqueIndex("users_email_uq").on(t.email),
    uniqueIndex("users_device_id_uq").on(t.deviceId),
  ],
);

export const sessions = sqliteTable(
  "sessions",
  {
    id: text("id").primaryKey(),
    userId: text("user_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),
    tokenHash: text("token_hash").notNull(), // sha256 of the opaque bearer token; raw token is never stored
    createdAt: integer("created_at", { mode: "number" }).notNull().default(now),
    expiresAt: integer("expires_at", { mode: "number" }).notNull(),
    revokedAt: integer("revoked_at", { mode: "number" }),
    userAgent: text("user_agent"),
  },
  (t) => [
    uniqueIndex("sessions_token_hash_uq").on(t.tokenHash),
    index("sessions_user_idx").on(t.userId),
  ],
);

export const subscriptions = sqliteTable(
  "subscriptions",
  {
    id: text("id").primaryKey(),
    userId: text("user_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),
    provider: text("provider", { enum: ["google_play"] }).notNull().default("google_play"),
    plan: text("plan", { enum: ["free", "pro"] }).notNull(),
    productId: text("product_id").notNull(), // e.g. hydra_pro_monthly / hydra_pro_annual
    purchaseToken: text("purchase_token").notNull(), // Play purchase token, unique per purchase
    orderId: text("order_id"),
    status: text("status", {
      enum: ["active", "grace", "on_hold", "paused", "canceled", "expired", "pending"],
    }).notNull(),
    autoRenewing: integer("auto_renewing", { mode: "boolean" }).notNull().default(false),
    startsAt: integer("starts_at", { mode: "number" }),
    expiresAt: integer("expires_at", { mode: "number" }),
    acknowledged: integer("acknowledged", { mode: "boolean" }).notNull().default(false),
    lastVerifiedAt: integer("last_verified_at", { mode: "number" }),
    createdAt: integer("created_at", { mode: "number" }).notNull().default(now),
    updatedAt: integer("updated_at", { mode: "number" }).notNull().default(now),
  },
  (t) => [
    uniqueIndex("subscriptions_purchase_token_uq").on(t.purchaseToken),
    index("subscriptions_user_idx").on(t.userId),
  ],
);

export const modelMetadata = sqliteTable(
  "model_metadata",
  {
    id: text("id").primaryKey(), // stable slug, e.g. "hydra-fast-1b-q4km"
    name: text("name").notNull(),
    publisher: text("publisher").notNull(),
    tier: text("tier", { enum: ["fast", "balanced", "advanced"] }).notNull(),
    version: text("version").notNull(), // bumped on any file change
    description: text("description").notNull(),
    intendedUse: text("intended_use").notNull(),
    strengths: text("strengths").notNull().default(""), // newline separated
    limitations: text("limitations").notNull().default(""), // newline separated
    format: text("format").notNull().default("GGUF"),
    quantization: text("quantization").notNull(), // e.g. Q4_K_M
    downloadSizeBytes: integer("download_size_bytes", { mode: "number" }).notNull(),
    storageSizeBytes: integer("storage_size_bytes", { mode: "number" }).notNull(),
    minRamMb: integer("min_ram_mb", { mode: "number" }).notNull(),
    minAndroidSdk: integer("min_android_sdk", { mode: "number" }).notNull().default(29),
    licenseName: text("license_name").notNull(),
    licenseUrl: text("license_url").notNull(),
    commercialUseReviewed: integer("commercial_use_reviewed", { mode: "boolean" })
      .notNull()
      .default(false), // must be true before status can be "published"
    sourceUrl: text("source_url").notNull(),
    r2Key: text("r2_key").notNull(), // object key in the Cloudflare R2 bucket
    sha256: text("sha256").notNull(), // lowercase hex of the GGUF file
    requiresPro: integer("requires_pro", { mode: "boolean" }).notNull().default(false),
    status: text("status", { enum: ["draft", "published", "revoked"] }).notNull().default("draft"),
    sortOrder: integer("sort_order", { mode: "number" }).notNull().default(0),
    createdAt: integer("created_at", { mode: "number" }).notNull().default(now),
    updatedAt: integer("updated_at", { mode: "number" }).notNull().default(now),
  },
  (t) => [index("model_metadata_status_idx").on(t.status, t.sortOrder)],
);

export const shareBlobs = sqliteTable("share_blobs", {
  id: text("id").primaryKey(),
  ciphertext: text("ciphertext").notNull(), // base64 of AES-GCM ciphertext produced on device
  iv: text("iv").notNull(), // base64 nonce, not secret
  byteSize: integer("byte_size", { mode: "number" }).notNull(),
  sha256: text("sha256").notNull(), // of the ciphertext bytes, integrity check only
  createdAt: integer("created_at", { mode: "number" }).notNull().default(now),
});

export const shareRecords = sqliteTable(
  "share_records",
  {
    id: text("id").primaryKey(), // unguessable share id, 128-bit random, base64url
    ownerUserId: text("owner_user_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),
    blobId: text("blob_id")
      .notNull()
      .references(() => shareBlobs.id, { onDelete: "cascade" }),
    mode: text("mode", { enum: ["full", "selected", "read_only", "continuable"] }).notNull(),
    messageCount: integer("message_count", { mode: "number" }).notNull().default(0),
    createdAt: integer("created_at", { mode: "number" }).notNull().default(now),
    expiresAt: integer("expires_at", { mode: "number" }),
    revokedAt: integer("revoked_at", { mode: "number" }),
    viewCount: integer("view_count", { mode: "number" }).notNull().default(0),
    takedownAt: integer("takedown_at", { mode: "number" }), // set by admin, hides blob from recipients
  },
  (t) => [index("share_records_owner_idx").on(t.ownerUserId, t.createdAt)],
);

export const abuseReports = sqliteTable(
  "abuse_reports",
  {
    id: text("id").primaryKey(),
    shareId: text("share_id")
      .notNull()
      .references(() => shareRecords.id, { onDelete: "cascade" }),
    reporterUserId: text("reporter_user_id").references(() => users.id, { onDelete: "set null" }),
    reason: text("reason", {
      enum: ["illegal", "harassment", "sexual_content", "malware_or_scam", "privacy", "other"],
    }).notNull(),
    details: text("details").notNull().default(""),
    status: text("status", { enum: ["open", "reviewing", "actioned", "dismissed"] })
      .notNull()
      .default("open"),
    adminNote: text("admin_note"),
    createdAt: integer("created_at", { mode: "number" }).notNull().default(now),
    resolvedAt: integer("resolved_at", { mode: "number" }),
  },
  (t) => [index("abuse_reports_status_idx").on(t.status, t.createdAt)],
);

export type User = typeof users.$inferSelect;
export type Subscription = typeof subscriptions.$inferSelect;
export type ModelMetadata = typeof modelMetadata.$inferSelect;
export type ShareRecord = typeof shareRecords.$inferSelect;
export type AbuseReport = typeof abuseReports.$inferSelect;
