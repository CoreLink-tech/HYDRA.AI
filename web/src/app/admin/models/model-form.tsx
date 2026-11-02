"use client";

import { useState, useTransition } from "react";
import type { ModelMetadata } from "@/db/schema";
import { saveModel } from "./actions";

const field = "mt-1 w-full rounded-btn border border-line-strong bg-card px-3 py-2 text-sm text-fg";

export function ModelForm({ model }: { model: ModelMetadata | null }) {
  const [message, setMessage] = useState<string | null>(null);
  const [pending, start] = useTransition();

  return (
    <form
      className="mt-4 grid grid-cols-1 gap-4 rounded-card border border-line bg-card p-6 md:grid-cols-2"
      action={(fd) =>
        start(async () => {
          const res = await saveModel(fd);
          setMessage(res.message);
        })
      }
    >
      <label className="text-sm">ID (slug)<input name="id" required defaultValue={model?.id ?? ""} readOnly={!!model} className={field} /></label>
      <label className="text-sm">Name<input name="name" required defaultValue={model?.name ?? ""} className={field} /></label>
      <label className="text-sm">Publisher<input name="publisher" required defaultValue={model?.publisher ?? ""} className={field} /></label>
      <label className="text-sm">Tier
        <select name="tier" defaultValue={model?.tier ?? "fast"} className={field}>
          <option value="fast">Fast</option>
          <option value="balanced">Balanced</option>
          <option value="advanced">Advanced</option>
        </select>
      </label>
      <label className="text-sm">Version<input name="version" required placeholder="1.0.0" defaultValue={model?.version ?? "1.0.0"} className={field} /></label>
      <label className="text-sm">Quantization<input name="quantization" required placeholder="Q4_K_M" defaultValue={model?.quantization ?? ""} className={field} /></label>
      <label className="text-sm md:col-span-2">Description<textarea name="description" required rows={2} defaultValue={model?.description ?? ""} className={field} /></label>
      <label className="text-sm md:col-span-2">Intended use<input name="intendedUse" required defaultValue={model?.intendedUse ?? ""} className={field} /></label>
      <label className="text-sm">Strengths (one per line)<textarea name="strengths" rows={3} defaultValue={model?.strengths ?? ""} className={field} /></label>
      <label className="text-sm">Limitations (one per line)<textarea name="limitations" rows={3} defaultValue={model?.limitations ?? ""} className={field} /></label>
      <label className="text-sm">Download size (bytes)<input name="downloadSizeBytes" type="number" min={0} defaultValue={model?.downloadSizeBytes ?? 0} className={field} /></label>
      <label className="text-sm">Storage after install (bytes)<input name="storageSizeBytes" type="number" min={0} defaultValue={model?.storageSizeBytes ?? 0} className={field} /></label>
      <label className="text-sm">Minimum RAM (MB)<input name="minRamMb" type="number" min={0} defaultValue={model?.minRamMb ?? 0} className={field} /></label>
      <label className="text-sm">Minimum Android SDK<input name="minAndroidSdk" type="number" min={21} defaultValue={model?.minAndroidSdk ?? 29} className={field} /></label>
      <label className="text-sm">Licence name<input name="licenseName" required defaultValue={model?.licenseName ?? ""} className={field} /></label>
      <label className="text-sm">Licence URL (https)<input name="licenseUrl" required defaultValue={model?.licenseUrl ?? ""} className={field} /></label>
      <label className="text-sm">Source URL (https)<input name="sourceUrl" required defaultValue={model?.sourceUrl ?? ""} className={field} /></label>
      <label className="text-sm">R2 object key<input name="r2Key" required placeholder="models/fast/1.0.0.gguf" defaultValue={model?.r2Key ?? ""} className={field} /></label>
      <label className="text-sm md:col-span-2">SHA-256 of the GGUF file<input name="sha256" required pattern="[a-fA-F0-9]{64}" defaultValue={model?.sha256 ?? ""} className={field} /></label>
      <label className="text-sm">Sort order<input name="sortOrder" type="number" min={0} defaultValue={model?.sortOrder ?? 0} className={field} /></label>
      <label className="text-sm">Status
        <select name="status" defaultValue={model?.status ?? "draft"} className={field}>
          <option value="draft">Draft</option>
          <option value="published">Published</option>
          <option value="revoked">Revoked</option>
        </select>
      </label>
      <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="commercialUseReviewed" defaultChecked={model?.commercialUseReviewed ?? false} /> Commercial use and redistribution reviewed</label>
      <label className="flex items-center gap-2 text-sm"><input type="checkbox" name="requiresPro" defaultChecked={model?.requiresPro ?? false} /> Pro only</label>
      <div className="flex items-center gap-4 md:col-span-2">
        <button disabled={pending} className="min-h-11 rounded-btn bg-accent px-5 py-2 text-sm font-medium text-on-accent hover:bg-accent-strong disabled:opacity-50">
          {pending ? "Saving..." : "Save model"}
        </button>
        {message ? <span className="text-sm text-fg-2">{message}</span> : null}
      </div>
    </form>
  );
}
