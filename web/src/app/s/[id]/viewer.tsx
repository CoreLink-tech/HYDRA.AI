"use client";

import { useEffect, useState } from "react";
import { AlertIcon, LockIcon } from "@/components/ui/icons";
import { LinkButton } from "@/components/ui/button";

type Msg = { role: string; content: string };
type State =
  | { kind: "loading" }
  | { kind: "ready"; title: string; messages: Msg[] }
  | { kind: "no-key" }
  | { kind: "gone" }
  | { kind: "missing" }
  | { kind: "failed"; reason: string };

function b64ToBytes(b64: string): Uint8Array<ArrayBuffer> {
  const std = b64.replace(/-/g, "+").replace(/_/g, "/");
  const padded = std + "=".repeat((4 - (std.length % 4)) % 4);
  const bin = atob(padded);
  const out = new Uint8Array(new ArrayBuffer(bin.length));
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}

async function load(id: string): Promise<State> {
  const match = /(?:^|[#&])k=([A-Za-z0-9_-]+)/.exec(window.location.hash);
  const keyB64 = match?.[1];
  if (!keyB64) return { kind: "no-key" };
  if (!globalThis.crypto?.subtle) return { kind: "failed", reason: "This browser cannot decrypt securely. Try a recent browser over HTTPS." };

  const res = await fetch(`/api/v1/shares/${encodeURIComponent(id)}`, { cache: "no-store", referrerPolicy: "no-referrer" });
  if (res.status === 410) return { kind: "gone" };
  if (res.status === 404) return { kind: "missing" };
  if (!res.ok) return { kind: "failed", reason: "The server could not be reached. Try again in a moment." };

  const body = (await res.json()) as { ciphertext: string; iv: string };
  try {
    const key = await crypto.subtle.importKey("raw", b64ToBytes(keyB64), "AES-GCM", false, ["decrypt"]);
    const plain = await crypto.subtle.decrypt({ name: "AES-GCM", iv: b64ToBytes(body.iv), tagLength: 128 }, key, b64ToBytes(body.ciphertext));
    const parsed = JSON.parse(new TextDecoder().decode(plain)) as { title?: unknown; messages?: unknown };
    if (!Array.isArray(parsed.messages)) throw new Error("shape");
    const messages = parsed.messages
      .filter((m): m is Msg => !!m && typeof (m as Msg).role === "string" && typeof (m as Msg).content === "string")
      .slice(0, 2000);
    return { kind: "ready", title: typeof parsed.title === "string" && parsed.title ? parsed.title : "Shared conversation", messages };
  } catch {
    return { kind: "failed", reason: "This link could not be opened. It may be incomplete or the key may be wrong." };
  }
}

function Notice({ title, body, tone = "neutral" }: { title: string; body: string; tone?: "neutral" | "warn" }) {
  return (
    <div className="rise rounded-card border border-line bg-card p-6 sm:p-8">
      <span className={`flex h-11 w-11 items-center justify-center rounded-btn ${tone === "warn" ? "bg-warn-soft text-warn" : "bg-accent-soft text-accent"}`}>
        {tone === "warn" ? <AlertIcon /> : <LockIcon />}
      </span>
      <h1 className="mt-5 text-2xl font-semibold tracking-tight">{title}</h1>
      <p className="mt-3 text-pretty leading-7 text-fg-2">{body}</p>
      <div className="mt-6">
        <LinkButton href="/" variant="secondary">About HYDRA.AI</LinkButton>
      </div>
    </div>
  );
}

export function SharedChatViewer({ id }: { id: string }) {
  const [state, setState] = useState<State>({ kind: "loading" });

  useEffect(() => {
    let cancelled = false;
    load(id)
      .then((s) => !cancelled && setState(s))
      .catch(() => !cancelled && setState({ kind: "failed", reason: "Something went wrong while opening this link." }));
    return () => {
      cancelled = true;
    };
  }, [id]);

  switch (state.kind) {
    case "loading":
      return (
        <div role="status" aria-live="polite" className="space-y-4">
          <p className="text-sm text-fg-3">Opening and decrypting on your device&hellip;</p>
          <div className="skeleton h-16 rounded-card" />
          <div className="skeleton h-28 rounded-card" />
          <div className="skeleton h-16 rounded-card" />
        </div>
      );
    case "no-key":
      return <Notice tone="warn" title="This link is missing its key" body="Shared chats are end-to-end encrypted and the key lives after the # in the link. Ask the sender to share the full link again, and make sure nothing was cut off when it was copied." />;
    case "gone":
      return <Notice tone="warn" title="This conversation is no longer available" body="The sender stopped sharing it, or the link expired. Once a share is revoked, the encrypted data is deleted." />;
    case "missing":
      return <Notice tone="warn" title="We could not find this conversation" body="Check that the whole link was copied correctly." />;
    case "failed":
      return <Notice tone="warn" title="Could not open this conversation" body={state.reason} />;
    case "ready":
      return (
        <article>
          <p className="inline-flex items-center gap-2 rounded-md bg-accent-soft px-2.5 py-1 text-xs font-medium text-accent">
            <LockIcon className="h-3.5 w-3.5" /> Decrypted on your device
          </p>
          <h1 className="mt-4 text-balance text-2xl font-semibold tracking-tight sm:text-3xl">{state.title}</h1>
          <p className="mt-2 text-sm text-fg-3">Shared from HYDRA.AI. This is a read-only copy.</p>
          <ol className="mt-8 space-y-5">
            {state.messages.map((m, i) => {
              const user = m.role === "user";
              return (
                <li key={i} className={user ? "flex justify-end" : ""}>
                  <div className={user ? "max-w-[85%] rounded-card bg-accent-soft px-4 py-3" : "max-w-none"}>
                    <p className="mb-1 text-xs font-medium text-fg-3">{user ? "You" : "HYDRA"}</p>
                    <p className="whitespace-pre-wrap break-words text-[15px] leading-7">{m.content}</p>
                  </div>
                </li>
              );
            })}
          </ol>
          <p className="mt-10 border-t border-line pt-4 text-xs leading-5 text-fg-3">
            If this conversation breaks the rules, use the report option in the HYDRA.AI app.
          </p>
        </article>
      );
  }
}
