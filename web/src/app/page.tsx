import type { ReactNode } from "react";
import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";
import { ChatPhone, LocalBadges, ModelsPhone } from "@/components/phone-mockup";
import { LinkButton } from "@/components/ui/button";
import {
  AlertIcon,
  ArrowRightIcon,
  ChipIcon,
  CheckIcon,
  CloudIcon,
  DownloadIcon,
  LayersIcon,
  LockIcon,
  OfflineIcon,
  PhoneIcon,
  ShareIcon,
} from "@/components/ui/icons";
import { Reveal } from "@/components/ui/reveal";

/* ------------------------------------------------------------------------------------------- *
 * Content. Everything below is taken from what the app and API do today. Model names, sizes and
 * memory needs come from the Android ModelCatalog. Prices are set in Google Play, so none are shown.
 * ------------------------------------------------------------------------------------------- */

const features: { icon: ReactNode; title: string; body: string; soon?: boolean }[] = [
  {
    icon: <LockIcon />,
    title: "Private by design",
    body: "Prompts, answers and history are processed and stored on your phone. We do not use your conversations for analytics, ads or training.",
  },
  {
    icon: <OfflineIcon />,
    title: "Works offline",
    body: "Download a model once, then chat with no connection. Core chat never depends on our servers.",
  },
  {
    icon: <PhoneIcon />,
    title: "AI directly on your device",
    body: "Replies are written by a language model running on your phone's own processor, not by a cloud service.",
  },
  {
    icon: <LayersIcon />,
    title: "Multiple language models",
    body: "A curated set in three tiers. Each lists its licence, size and known limits before you download it.",
  },
  {
    icon: <ShareIcon />,
    title: "Encrypted conversation sharing",
    body: "Share a chat through an end-to-end encrypted link. The key stays in the link, so our server only ever holds ciphertext, and you can revoke a share at any time.",
    soon: true,
  },
  {
    icon: <ChipIcon />,
    title: "Device-conscious model selection",
    body: "HYDRA checks memory, free storage and your Android version, then recommends a model that will run well instead of one that struggles.",
  },
];

const steps = [
  {
    title: "Download HYDRA.AI",
    body: "Install the app on your Android phone. It needs network access only to download models.",
  },
  {
    title: "Choose and download a model",
    body: "HYDRA checks your phone and recommends a model that fits. Every download is verified before it is used.",
  },
  {
    title: "Chat privately, even offline",
    body: "Once the model is on your phone, ask, write and plan with no internet connection at all.",
  },
];

const tiers = [
  {
    name: "Hydra Fast",
    summary: "Small and quick. Good for short questions, summaries and rewriting.",
    ram: "3 GB",
    size: "469 MB",
    pro: false,
  },
  {
    name: "Hydra Balanced",
    summary: "The everyday assistant for writing, study explanations and planning.",
    ram: "6 GB",
    size: "1.0 GB",
    pro: true,
    featured: true,
  },
  {
    name: "Hydra Advanced",
    summary: "Higher-quality reasoning and longer answers for phones with plenty of memory. Needs Android 12 or newer.",
    ram: "8 GB",
    size: "2.2 GB",
    pro: true,
  },
];

const onPhone = [
  "Prompts and replies are generated on your device. They are never uploaded to get an answer.",
  "Conversation history is kept in an encrypted database on your phone.",
  "You can delete chats, models and settings from the app at any time.",
];

const online = [
  "Downloading a model. Files come from our storage and are checked with SHA-256 before use.",
  "Signing in and subscriptions. We keep only what is needed for your account and plan.",
  "Encrypted sharing, when you choose to use it. Your phone encrypts the chat and the key travels only in the link, after the #, which browsers never send to a server.",
  "Crash reports are off by default. If you turn them on, they contain technical details only, never your messages.",
];

const freeIncludes = [
  "The Hydra Fast model",
  "Offline chat with any model you have downloaded",
  "Chat history stored on your phone",
  "Device checks and model recommendations",
  "Encrypted sharing with a limit on live links",
];

const proIncludes = [
  "Everything in Free",
  "Hydra Balanced and Hydra Advanced models",
  "A higher limit on live shared links",
];

const requirements = [
  "Android 8.0 or newer",
  "An ARM64, 32-bit ARM or x86_64 processor",
  "At least 3 GB of memory and 1.5 GB of free storage for the smallest model",
  "An internet connection for the first model download",
];

/* ------------------------------------------------------------------------------------------- */

function SectionHead({ eyebrow, title, lead, center = false }: { eyebrow: string; title: string; lead?: string; center?: boolean }) {
  return (
    <div className={center ? "mx-auto max-w-2xl text-center" : "max-w-2xl"}>
      <p className="text-xs font-semibold uppercase tracking-wider text-accent">{eyebrow}</p>
      <h2 className="mt-3 text-balance text-3xl font-semibold tracking-tight sm:text-4xl">{title}</h2>
      {lead ? <p className="mt-4 text-pretty text-base leading-7 text-fg-2 sm:text-lg">{lead}</p> : null}
    </div>
  );
}

function Tag({ children, tone = "accent" }: { children: ReactNode; tone?: "accent" | "neutral" }) {
  return (
    <span
      className={`inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium ${
        tone === "accent" ? "bg-accent-soft text-accent" : "bg-surface text-fg-2"
      }`}
    >
      {children}
    </span>
  );
}

export default function HomePage() {
  return (
    <>
      <SiteHeader />
      <main id="top">
        {/* Hero */}
        <section className="overflow-hidden">
          <div className="mx-auto grid max-w-6xl items-center gap-14 px-4 pb-20 pt-14 sm:px-6 sm:pt-20 lg:grid-cols-[1fr_1.15fr] lg:gap-10 lg:pb-28">
            <div>
              <p className="rise inline-flex items-center gap-2 rounded-md border border-line bg-elevated px-3 py-1 text-xs text-fg-2">
                <span className="h-1.5 w-1.5 rounded-full bg-accent" /> Android &middot; Offline-first
              </p>
              <h1 className="rise mt-6 text-balance text-4xl font-semibold leading-[1.08] tracking-tight sm:text-5xl lg:text-[3.4rem]" style={{ ["--rise-delay" as string]: "80ms" }}>
                Your AI. Your Device. <span className="text-accent">Your Privacy.</span>
              </h1>
              <p className="rise mt-6 max-w-xl text-pretty text-base leading-7 text-fg-2 sm:text-lg" style={{ ["--rise-delay" as string]: "160ms" }}>
                Run powerful language models directly on your Android phone. Chat privately, work offline, and keep your
                conversations where they belong.
              </p>
              <div className="rise mt-9 flex flex-col gap-3 sm:flex-row" style={{ ["--rise-delay" as string]: "240ms" }}>
                <LinkButton href="/#download" size="lg">
                  <DownloadIcon className="h-5 w-5" /> Download HYDRA.AI
                </LinkButton>
                <LinkButton href="/#features" variant="secondary" size="lg">
                  Explore Features
                </LinkButton>
              </div>
              <p className="rise mt-4 text-sm text-fg-3" style={{ ["--rise-delay" as string]: "300ms" }}>
                Free to start. No account is needed to chat offline.
              </p>
            </div>

            <div className="rise flex items-start justify-center" style={{ ["--rise-delay" as string]: "200ms" }}>
              <ChatPhone />
              <div className="-ml-6 mt-12 hidden sm:block">
                <ModelsPhone />
              </div>
            </div>
          </div>
          <div className="border-y border-line bg-surface py-5">
            <div className="mx-auto max-w-6xl px-4 sm:px-6">
              <LocalBadges />
            </div>
          </div>
        </section>

        {/* Features */}
        <section id="features" className="mx-auto max-w-6xl px-4 py-20 sm:px-6 sm:py-28">
          <Reveal>
            <SectionHead
              eyebrow="Features"
              title="Everything runs where you are"
              lead="HYDRA handles model formats, memory and hardware checks, so all you see is a clean chat."
            />
          </Reveal>
          <div className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {features.map((f, i) => (
              <Reveal key={f.title} delay={(i % 3) * 70}>
                <article className="h-full rounded-card border border-line bg-card p-6 transition-[transform,border-color] duration-300 hover:-translate-y-0.5 hover:border-line-strong">
                  <div className="flex items-start justify-between">
                    <span className="flex h-11 w-11 items-center justify-center rounded-btn bg-accent-soft text-accent">{f.icon}</span>
                    {f.soon ? <Tag tone="neutral">Coming soon</Tag> : null}
                  </div>
                  <h3 className="mt-5 text-lg font-semibold tracking-tight">{f.title}</h3>
                  <p className="mt-2 text-sm leading-6 text-fg-2">{f.body}</p>
                </article>
              </Reveal>
            ))}
          </div>
        </section>

        {/* How it works */}
        <section id="how-it-works" className="border-y border-line bg-surface py-20 sm:py-28">
          <div className="mx-auto max-w-6xl px-4 sm:px-6">
            <Reveal>
              <SectionHead eyebrow="How it works" title="Three steps to a private assistant" center />
            </Reveal>
            <ol className="mt-14 grid gap-10 md:grid-cols-3 md:gap-6">
              {steps.map((s, i) => (
                <Reveal key={s.title} delay={i * 90}>
                  <li className="relative">
                    <div className="flex items-center gap-4 md:flex-col md:items-start md:gap-0">
                      <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-btn border border-line-strong bg-elevated text-base font-semibold text-accent">
                        {i + 1}
                      </span>
                      <h3 className="text-lg font-semibold tracking-tight md:mt-5">{s.title}</h3>
                    </div>
                    <p className="mt-3 text-sm leading-6 text-fg-2 md:max-w-xs">{s.body}</p>
                    {i < steps.length - 1 ? (
                      <ArrowRightIcon className="absolute right-0 top-3 hidden h-5 w-5 text-line-strong md:block" />
                    ) : null}
                  </li>
                </Reveal>
              ))}
            </ol>
          </div>
        </section>

        {/* Privacy */}
        <section id="privacy" className="theme-dark bg-canvas py-20 text-fg sm:py-28">
          <div className="mx-auto max-w-6xl px-4 sm:px-6">
            <Reveal>
              <SectionHead
                eyebrow="Privacy"
                title="Local by default. Online only when you choose."
                lead="Thinking happens on your phone. A connection is used for a few things you control, and we are clear about which."
              />
            </Reveal>
            <div className="mt-12 grid gap-4 md:grid-cols-2">
              <Reveal>
                <div className="h-full rounded-card border border-line bg-card p-6 sm:p-8">
                  <div className="flex items-center gap-3">
                    <span className="flex h-10 w-10 items-center justify-center rounded-btn bg-accent-soft text-accent"><PhoneIcon /></span>
                    <h3 className="text-lg font-semibold tracking-tight">On your phone</h3>
                  </div>
                  <ul className="mt-6 space-y-4">
                    {onPhone.map((t) => (
                      <li key={t} className="flex gap-3 text-sm leading-6 text-fg-2">
                        <CheckIcon className="mt-0.5 h-5 w-5 shrink-0 text-accent" /> {t}
                      </li>
                    ))}
                  </ul>
                </div>
              </Reveal>
              <Reveal delay={90}>
                <div className="h-full rounded-card border border-line bg-surface p-6 sm:p-8">
                  <div className="flex items-center gap-3">
                    <span className="flex h-10 w-10 items-center justify-center rounded-btn bg-elevated text-fg-2"><CloudIcon /></span>
                    <h3 className="text-lg font-semibold tracking-tight">Uses the internet, only if you choose</h3>
                  </div>
                  <ul className="mt-6 space-y-4">
                    {online.map((t) => (
                      <li key={t} className="flex gap-3 text-sm leading-6 text-fg-2">
                        <span className="mt-2.5 h-1.5 w-1.5 shrink-0 rounded-full bg-fg-3" /> {t}
                      </li>
                    ))}
                  </ul>
                </div>
              </Reveal>
            </div>
            <p className="mt-6 max-w-3xl text-sm leading-6 text-fg-3">
              To be clear: downloading models, subscriptions and sharing need a connection. Chatting with a model you have
              already downloaded does not.
            </p>
          </div>
        </section>

        {/* Models */}
        <section id="models" className="mx-auto max-w-6xl px-4 py-20 sm:px-6 sm:py-28">
          <Reveal>
            <SectionHead
              eyebrow="Models"
              title="Three tiers, picked to fit your phone"
              lead="We ship a small, licence-reviewed set instead of thousands of untested files. HYDRA suggests the tier your phone can actually run."
            />
          </Reveal>
          <div className="mt-12 grid gap-4 md:grid-cols-3">
            {tiers.map((t, i) => (
              <Reveal key={t.name} delay={i * 80}>
                <article
                  className={`flex h-full flex-col rounded-card border bg-card p-6 ${t.featured ? "border-accent/60" : "border-line"}`}
                >
                  <div className="flex items-center justify-between gap-3">
                    <h3 className="text-lg font-semibold tracking-tight">{t.name}</h3>
                    <Tag tone={t.pro ? "neutral" : "accent"}>{t.pro ? "Pro" : "Free"}</Tag>
                  </div>
                  <p className="mt-3 flex-1 text-sm leading-6 text-fg-2">{t.summary}</p>
                  <dl className="mt-6 grid grid-cols-2 gap-4 border-t border-line pt-5 text-sm">
                    <div>
                      <dt className="text-xs text-fg-3">Download</dt>
                      <dd className="mt-0.5 font-medium">{t.size}</dd>
                    </div>
                    <div>
                      <dt className="text-xs text-fg-3">Memory needed</dt>
                      <dd className="mt-0.5 font-medium">{t.ram} or more</dd>
                    </div>
                  </dl>
                </article>
              </Reveal>
            ))}
          </div>
          <p className="mt-6 max-w-3xl text-xs leading-5 text-fg-3">
            Built on open models: Qwen 2.5 (Apache 2.0) for Fast and Balanced, and Phi-3.5 mini (MIT) for Advanced. Each model
            lists its licence, size and limits in the app before you download it.
          </p>
        </section>

        {/* Pricing */}
        <section id="pricing" className="border-y border-line bg-surface py-20 sm:py-28">
          <div className="mx-auto max-w-6xl px-4 sm:px-6">
            <Reveal>
              <SectionHead
                eyebrow="Pricing"
                title="Free to start. Pro when you want more."
                lead="Offline chat never depends on a subscription. If Pro ends, models you have already downloaded keep working."
                center
              />
            </Reveal>
            <div className="mx-auto mt-12 grid max-w-4xl gap-4 md:grid-cols-2">
              <Reveal>
                <article className="h-full rounded-card border border-line bg-card p-7">
                  <h3 className="text-lg font-semibold tracking-tight">Free</h3>
                  <p className="mt-2 text-4xl font-semibold tracking-tight">$0</p>
                  <ul className="mt-6 space-y-3">
                    {freeIncludes.map((i) => (
                      <li key={i} className="flex gap-3 text-sm leading-6 text-fg-2">
                        <CheckIcon className="mt-0.5 h-5 w-5 shrink-0 text-accent" /> {i}
                      </li>
                    ))}
                  </ul>
                </article>
              </Reveal>
              <Reveal delay={90}>
                <article className="h-full rounded-card border border-accent/60 bg-card p-7">
                  <div className="flex items-center justify-between">
                    <h3 className="text-lg font-semibold tracking-tight">Pro</h3>
                    <Tag tone="neutral">Coming soon</Tag>
                  </div>
                  <p className="mt-2 text-2xl font-semibold tracking-tight text-fg-2">Price set in Google Play</p>
                  <p className="mt-1 text-sm text-fg-3">A subscription billed through Google Play.</p>
                  <ul className="mt-6 space-y-3">
                    {proIncludes.map((i) => (
                      <li key={i} className="flex gap-3 text-sm leading-6 text-fg-2">
                        <CheckIcon className="mt-0.5 h-5 w-5 shrink-0 text-accent" /> {i}
                      </li>
                    ))}
                  </ul>
                </article>
              </Reveal>
            </div>
          </div>
        </section>

        {/* Download */}
        <section id="download" className="mx-auto max-w-6xl px-4 py-20 sm:px-6 sm:py-28">
          <Reveal>
            <div className="grid gap-10 rounded-panel border border-line bg-card p-6 shadow-card sm:p-10 lg:grid-cols-[1.1fr_1fr] lg:gap-14">
              <div>
                <SectionHead eyebrow="Download" title="Get HYDRA.AI for Android" />
                <div className="mt-8 flex items-start gap-3 rounded-btn bg-warn-soft px-4 py-3 text-sm leading-6 text-fg">
                  <AlertIcon className="mt-0.5 h-5 w-5 shrink-0 text-warn" />
                  <p>
                    HYDRA.AI is not on Google Play yet. We will put the download link here as soon as the listing is live.
                  </p>
                </div>
                <span
                  aria-disabled="true"
                  className="mt-6 inline-flex min-h-12 cursor-not-allowed items-center justify-center gap-2 rounded-btn border border-line-strong bg-surface px-6 text-base font-medium text-fg-3"
                >
                  <DownloadIcon className="h-5 w-5" /> Google Play: coming soon
                </span>
              </div>
              <div>
                <h3 className="text-sm font-semibold uppercase tracking-wider text-fg-3">What your phone needs</h3>
                <ul className="mt-5 space-y-4">
                  {requirements.map((r) => (
                    <li key={r} className="flex gap-3 text-sm leading-6 text-fg-2">
                      <CheckIcon className="mt-0.5 h-5 w-5 shrink-0 text-accent" /> {r}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </Reveal>
        </section>
      </main>
      <SiteFooter />
    </>
  );
}
