import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";

const features = [
  { title: "Works without internet", body: "Download a model once, then chat, write, study and brainstorm with no connection. Core chat never depends on our servers." },
  { title: "Private by default", body: "Prompts, answers and history are processed and stored on your phone. We do not use your conversations for analytics, ads or training." },
  { title: "Models picked for your phone", body: "Hydra checks RAM, storage, Android version and chip, then recommends a model that will run well instead of crashing." },
  { title: "Honest about what models can do", body: "Every model lists its strengths, limits, size and licence before you download it. No inflated promises." },
  { title: "Share, end-to-end encrypted", body: "Share selected messages or a whole chat. Encryption keys stay on the devices. We cannot read what you share, and you can revoke it any time." },
  { title: "Integrity-checked downloads", body: "Every model file is verified with SHA-256 before it loads, and downloads resume if your connection drops." },
];

const tiers = [
  { name: "Fast", for: "Low-end and mid-range phones", good: "Quick chat, summaries, rewriting, basic questions", ram: "from 3 GB RAM" },
  { name: "Balanced", for: "Mid-range and newer phones", good: "Default assistant for writing, study, explanations and planning", ram: "from 6 GB RAM", featured: true },
  { name: "Advanced", for: "Phones with more RAM and storage", good: "Higher-quality answers for longer, harder tasks", ram: "from 8 GB RAM" },
];

const freeIncludes = ["One fast local model", "Unlimited offline chat", "Local conversation history", "Basic writing and study tools", "Standard model management", "Up to 10 live shares"];
const proIncludes = ["Everything in Free", "Additional curated models", "Higher-quality model options", "Advanced productivity tools", "Expanded customisation", "Enhanced sharing (up to 500 live shares)", "Priority support"];

export default function HomePage() {
  return (
    <>
      <SiteHeader />
      <main>
        <section className="relative overflow-hidden">
          <div className="pointer-events-none absolute inset-0 -z-10 bg-[radial-gradient(60%_50%_at_50%_0%,#eef0ff_0%,transparent_70%)]" />
          <div className="mx-auto max-w-4xl px-6 pb-20 pt-24 text-center md:pt-32">
            <span className="inline-flex items-center gap-2 rounded-full border border-zinc-200 bg-white px-3 py-1 text-xs text-zinc-600">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" /> Android · Offline-first · By ZenLink Technologies
            </span>
            <h1 className="mt-6 text-5xl font-semibold tracking-tight md:text-6xl">
              Your AI assistant.
              <br />
              <span className="text-brand">On your phone. Works offline.</span>
            </h1>
            <p className="mx-auto mt-6 max-w-2xl text-lg text-zinc-600">
              Hydra.AI runs open-weight AI models directly on your Android phone. Ask, write, learn and plan without
              sending your conversations to a cloud. Connect only when you want to download models or share.
            </p>
            <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
              <a href="#pricing" className="rounded-full bg-zinc-900 px-6 py-3 font-medium text-white hover:bg-zinc-700">Get Hydra.AI free</a>
              <a href="#privacy" className="rounded-full border border-zinc-300 px-6 py-3 font-medium text-zinc-800 hover:bg-zinc-50">How privacy works</a>
            </div>
            <p className="mt-4 text-xs text-zinc-500">Free to start. No account needed for offline chat.</p>
          </div>
        </section>

        <section id="features" className="mx-auto max-w-6xl px-6 py-20">
          <h2 className="text-3xl font-semibold tracking-tight">Built for everyday use, not for developers</h2>
          <p className="mt-3 max-w-2xl text-zinc-600">Hydra handles model formats, memory, and hardware compatibility so you only see a clean chat.</p>
          <div className="mt-12 grid gap-6 md:grid-cols-3">
            {features.map((f) => (
              <div key={f.title} className="rounded-2xl border border-zinc-200 p-6">
                <h3 className="font-semibold">{f.title}</h3>
                <p className="mt-2 text-sm leading-6 text-zinc-600">{f.body}</p>
              </div>
            ))}
          </div>
        </section>

        <section id="privacy" className="bg-zinc-950 py-20 text-white">
          <div className="mx-auto max-w-6xl px-6">
            <h2 className="text-3xl font-semibold tracking-tight">Privacy you can check, not just trust</h2>
            <div className="mt-10 grid gap-10 md:grid-cols-2">
              <ul className="space-y-4 text-zinc-300">
                <li>Prompts and responses: processed on your device. Never uploaded for inference.</li>
                <li>Conversation history: stored on your device. Deleted when you delete it.</li>
                <li>Downloaded models: fetched from our controlled storage and checked with SHA-256.</li>
                <li>Account data: only what is needed for sign-in and subscriptions.</li>
                <li>Shared chats: encrypted on your device. Keys never reach our servers.</li>
                <li>Crash reports: technical data only, with no prompt content.</li>
              </ul>
              <div className="rounded-2xl border border-white/10 bg-white/5 p-6 text-sm leading-6 text-zinc-300">
                <div className="font-medium text-white">How a shared chat works</div>
                <p className="mt-2">Your phone encrypts the chat with a key it generates. The encrypted data is uploaded, and the key travels only in the link the recipient opens (the part after the #, which browsers never send to any server).</p>
                <p className="mt-2">Revoke a share and its encrypted copy is deleted from our servers immediately.</p>
              </div>
            </div>
          </div>
        </section>

        <section id="models" className="mx-auto max-w-6xl px-6 py-20">
          <h2 className="text-3xl font-semibold tracking-tight">Three model tiers. A curated set, tested on real phones.</h2>
          <p className="mt-3 max-w-2xl text-zinc-600">We launch with a small set of licence-reviewed models instead of thousands of untested downloads. Hydra suggests the right tier for your device.</p>
          <div className="mt-12 grid gap-6 md:grid-cols-3">
            {tiers.map((t) => (
              <div key={t.name} className={`flex flex-col rounded-2xl border p-7 ${t.featured ? "border-brand shadow-lg shadow-indigo-100" : "border-zinc-200"}`}>
                <div className="flex items-center justify-between">
                  <h3 className="text-xl font-semibold">{t.name}</h3>
                  {t.featured ? <span className="rounded-full bg-brand-soft px-2.5 py-0.5 text-xs font-medium text-brand">Default</span> : null}
                </div>
                <p className="mt-1 text-sm text-zinc-500">{t.for}</p>
                <p className="mt-4 flex-1 text-sm leading-6 text-zinc-700">{t.good}</p>
                <p className="mt-6 text-xs font-medium uppercase tracking-wide text-zinc-500">{t.ram}</p>
              </div>
            ))}
          </div>
          <p className="mt-6 text-xs text-zinc-500">Each model lists its licence, size, storage needs and known limits before you download it. Exact model names and versions appear in the app once licence review is complete.</p>
        </section>

        <section id="pricing" className="bg-zinc-50 py-20">
          <div className="mx-auto max-w-6xl px-6">
            <h2 className="text-center text-3xl font-semibold tracking-tight">Free to start. Pro when you want more.</h2>
            <p className="mx-auto mt-3 max-w-xl text-center text-zinc-600">Offline chat stays working even if your Pro subscription ends. Downloaded models are yours to keep using.</p>
            <div className="mx-auto mt-12 grid max-w-4xl gap-6 md:grid-cols-2">
              <div className="rounded-2xl border border-zinc-200 bg-white p-8">
                <h3 className="text-xl font-semibold">Free</h3>
                <p className="mt-2 text-4xl font-semibold">$0</p>
                <ul className="mt-6 space-y-3 text-sm text-zinc-700">{freeIncludes.map((i) => <li key={i}>✓ {i}</li>)}</ul>
              </div>
              <div className="rounded-2xl border-2 border-brand bg-white p-8">
                <div className="flex items-center justify-between">
                  <h3 className="text-xl font-semibold">Pro</h3>
                  <span className="rounded-full bg-brand px-2.5 py-0.5 text-xs font-medium text-white">Subscription</span>
                </div>
                <p className="mt-2 text-sm text-zinc-500">Billed through Google Play. Monthly or annual.</p>
                {/* TODO(pricing): fill in final prices once Play Console products exist and local pricing is tested. */}
                <p className="mt-4 text-lg font-medium text-zinc-500">Price set in Google Play</p>
                <ul className="mt-6 space-y-3 text-sm text-zinc-700">{proIncludes.map((i) => <li key={i}>✓ {i}</li>)}</ul>
              </div>
            </div>
            <div className="mt-10 text-center">
              {/* TODO(launch): replace with the live Google Play listing URL once the app is published. */}
              <span className="inline-block rounded-full bg-zinc-900 px-6 py-3 font-medium text-white">Google Play listing: coming soon</span>
            </div>
          </div>
        </section>
      </main>
      <SiteFooter />
    </>
  );
}
