import type { Metadata } from "next";
import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";

export const metadata: Metadata = { title: "Privacy" };

export default function PrivacyPage() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-3xl px-6 py-16 leading-7 text-zinc-700">
        <h1 className="text-3xl font-semibold text-zinc-900">Privacy</h1>
        <p className="mt-4">Hydra.AI is built so your conversations stay on your phone. This page summarises what the cloud service stores. It is a draft for legal review before launch.</p>
        <h2 className="mt-8 text-xl font-semibold text-zinc-900">On your device</h2>
        <p className="mt-2">Prompts, responses, chat history and downloaded models stay on your device. You can delete them from the app.</p>
        <h2 className="mt-8 text-xl font-semibold text-zinc-900">On our servers</h2>
        <ul className="mt-2 list-disc space-y-2 pl-6">
          <li>Account data: an anonymous install identifier, or an email address and password hash if you sign up.</li>
          <li>Subscription records: Google Play purchase identifiers and plan status, used to unlock Pro features.</li>
          <li>Shared chats: AES-GCM ciphertext and its nonce. We do not hold decryption keys and cannot read shared chats.</li>
          <li>Abuse reports: the reason and details you submit about a shared chat.</li>
        </ul>
        <h2 className="mt-8 text-xl font-semibold text-zinc-900">What we do not do</h2>
        <p className="mt-2">We do not upload prompts or responses for inference, analytics, advertising or model training.</p>
        <p className="mt-8 text-sm text-zinc-500">TODO(legal): publish the final policy with contact details, data retention periods and a deletion request route.</p>
      </main>
      <SiteFooter />
    </>
  );
}
