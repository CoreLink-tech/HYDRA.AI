import { asc } from "drizzle-orm";
import { getDb, schema } from "@/db/client";
import { ModelForm } from "./model-form";
import { setModelStatus } from "./actions";

export const dynamic = "force-dynamic";

function mb(bytes: number) {
  return bytes ? `${(bytes / 1024 / 1024).toFixed(0)} MB` : "-";
}

export default async function ModelsPage({ searchParams }: { searchParams: Promise<{ edit?: string }> }) {
  const { edit } = await searchParams;
  const rows = await getDb()
    .select()
    .from(schema.modelMetadata)
    .orderBy(asc(schema.modelMetadata.sortOrder), asc(schema.modelMetadata.id));
  const editing = rows.find((r) => r.id === edit);

  return (
    <div className="space-y-8">
      <section>
        <h1 className="text-2xl font-semibold">Model catalogue</h1>
        <p className="mt-1 text-sm text-zinc-500">
          Only published rows appear in GET /api/v1/models. Drafts stay private until licence review is ticked.
        </p>
        <div className="mt-4 overflow-x-auto rounded-xl border border-zinc-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-500">
              <tr>
                <th className="px-4 py-2">Model</th>
                <th className="px-4 py-2">Tier</th>
                <th className="px-4 py-2">Version</th>
                <th className="px-4 py-2">Size</th>
                <th className="px-4 py-2">Status</th>
                <th className="px-4 py-2">Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((m) => (
                <tr key={m.id} className="border-t border-zinc-100">
                  <td className="px-4 py-2">
                    <div className="font-medium">{m.name}</div>
                    <div className="text-xs text-zinc-500">{m.id}</div>
                  </td>
                  <td className="px-4 py-2 capitalize">{m.tier}{m.requiresPro ? " (Pro)" : ""}</td>
                  <td className="px-4 py-2">{m.version}</td>
                  <td className="px-4 py-2">{mb(m.downloadSizeBytes)}</td>
                  <td className="px-4 py-2">
                    <span className="rounded-full bg-zinc-100 px-2 py-0.5 text-xs">{m.status}</span>
                  </td>
                  <td className="space-x-3 px-4 py-2">
                    <a className="text-indigo-600 hover:underline" href={`/admin/models?edit=${encodeURIComponent(m.id)}`}>Edit</a>
                    {m.status !== "published" ? (
                      <form action={async () => { "use server"; await setModelStatus(m.id, "published"); }} className="inline">
                        <button className="text-emerald-700 hover:underline">Publish</button>
                      </form>
                    ) : null}
                    {m.status === "published" ? (
                      <form action={async () => { "use server"; await setModelStatus(m.id, "revoked"); }} className="inline">
                        <button className="text-red-600 hover:underline">Revoke</button>
                      </form>
                    ) : null}
                  </td>
                </tr>
              ))}
              {rows.length === 0 ? (
                <tr><td colSpan={6} className="px-4 py-6 text-center text-zinc-500">No models yet. Run db:seed or add one below.</td></tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <h2 className="text-lg font-semibold">{editing ? `Edit ${editing.id}` : "Add a model"}</h2>
        <ModelForm key={editing?.id ?? "new"} model={editing ?? null} />
      </section>
    </div>
  );
}
