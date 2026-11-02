import type { Metadata } from "next";
import type { ReactNode } from "react";
import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";

export const metadata: Metadata = { title: "Privacy" };

function H({ children }: { children: ReactNode }) {
  return <h2 className="mt-10 text-xl font-semibold tracking-tight">{children}</h2>;
}

export default function PrivacyPage() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-3xl px-4 py-14 sm:px-6 sm:py-20">
        <p className="text-xs font-semibold uppercase tracking-wider text-accent">Privacy</p>
        <h1 className="mt-3 text-balance text-3xl font-semibold tracking-tight sm:text-4xl">Your conversations stay on your phone</h1>
        <p className="mt-5 leading-7 text-fg-2">
          HYDRA.AI is built so the thinking happens on your device. This page summarises what the cloud service stores. It is a
          draft for legal review before launch.
        </p>
        <div className="leading-7 text-fg-2">
          <H>On your device</H>
          <p className="mt-2">Prompts, responses, chat history and downloaded models stay on your device. You can delete them from the app.</p>
          <H>On our servers</H>
          <ul className="mt-2 list-disc space-y-2 pl-6">
            <li>Account data: an anonymous install identifier, or an email address and password hash if you sign up.</li>
            <li>Subscription records: Google Play purchase identifiers and plan status, used to unlock Pro features.</li>
            <li>Shared chats: AES-GCM ciphertext and its nonce. We do not hold decryption keys and cannot read shared chats.</li>
            <li>Abuse reports: the reason and details you submit about a shared chat.</li>
          </ul>
          <H>What we do not do</H>
          <p className="mt-2">We do not upload prompts or responses for inference, analytics, advertising or model training.</p>
          <p className="mt-10 rounded-btn bg-warn-soft px-4 py-3 text-sm text-fg">
            Still to come before launch: the final policy with contact details, data retention periods and a deletion request route.
          </p>
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
