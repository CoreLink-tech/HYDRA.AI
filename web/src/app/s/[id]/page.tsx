import type { Metadata } from "next";
import { SiteHeader } from "@/components/site-header";
import { SiteFooter } from "@/components/site-footer";
import { SharedChatViewer } from "./viewer";

export const metadata: Metadata = {
  title: "Shared conversation",
  robots: { index: false, follow: false },
  referrer: "no-referrer",
};

/**
 * Recipient page for an encrypted share. The server renders only this shell. The browser reads the
 * key from the URL fragment (#k=...), which is never sent to a server, fetches the ciphertext from
 * /api/v1/shares/:id and decrypts it locally.
 */
export default async function SharedChatPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return (
    <>
      <SiteHeader />
      <main className="mx-auto min-h-[60vh] max-w-2xl px-4 py-12 sm:px-6 sm:py-16">
        <SharedChatViewer id={id} />
      </main>
      <SiteFooter />
    </>
  );
}
