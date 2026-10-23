import { createClient, type Client } from "@libsql/client";
import { drizzle, type LibSQLDatabase } from "drizzle-orm/libsql";
import * as schema from "./schema";

type Db = LibSQLDatabase<typeof schema>;

declare global {
  // eslint-disable-next-line no-var
  var __hydraDb: { client: Client; db: Db } | undefined;
}

function createDb(): { client: Client; db: Db } {
  const url = process.env.TURSO_DATABASE_URL;
  if (!url) {
    throw new Error("TURSO_DATABASE_URL is not set. See web/README.md (Turso setup).");
  }
  const client = createClient({
    url,
    authToken: process.env.TURSO_AUTH_TOKEN,
  });
  return { client, db: drizzle(client, { schema }) };
}

/**
 * Lazily created, cached per process. In dev, Next hot reloads reuse the global so we
 * do not open a new connection on every change.
 */
export function getDb(): Db {
  if (!globalThis.__hydraDb) {
    globalThis.__hydraDb = createDb();
  }
  return globalThis.__hydraDb.db;
}

export { schema };
