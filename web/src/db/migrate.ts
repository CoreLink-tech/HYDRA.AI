import { migrate } from "drizzle-orm/libsql/migrator";
import { getDb } from "./client";

async function main() {
  const db = getDb();
  await migrate(db, { migrationsFolder: "./src/db/migrations" });
  console.log("migrations applied");
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
