import type { Metadata } from "next";
import { SiteHeader } from "@/components/site-header";

export const metadata: Metadata = {
  title: "Shared chat",
  robots: { index: false, follow: false },
};

/**
 * Recipient landing page for an encrypted share. The browser reads the key from the URL fragment
 * (#k=...). The fragment is never sent to this server. Decryption happens client-side in the
 * Hydra.AI app or a future web viewer, so this page only explains what the link is.
 *
 * TODO(viewer): add a client component that fetches /api/v1/shares/:id and decrypts with the fragment key.
 */
export default async function SharedChatPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-2xl px-6 py-20">
        <h1 className="text-2xl font-semibold">Shared conversation</h1>
        <p className="mt-4 text-zinc-600">
          Someone shared a private Hydra.AI conversation with you. It is end-to-end encrypted, so it can only be opened on a
          device that has the key in this link. Open the link in the Hydra.AI app to read it.
        </p>
        <p className="mt-4 break-all font-mono text-xs text-zinc-400">ref {id}</p>
      </main>
    </>
  );
}
