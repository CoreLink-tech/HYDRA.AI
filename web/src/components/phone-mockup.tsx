import { LogoMark } from "@/components/ui/logo";
import { CheckIcon, CloudIcon, LockIcon, PhoneIcon } from "@/components/ui/icons";

/**
 * Illustrations of the real Android screens, drawn from the app's own layout, colours and catalogue
 * values (model names, sizes and memory needs come from ModelCatalog). Sample chat text is only an
 * example of what a reply looks like.
 */

function Frame({ children, label }: { children: React.ReactNode; label: string }) {
  return (
    <figure
      role="img"
      aria-label={label}
      className="theme-dark relative h-[580px] w-[280px] shrink-0 overflow-hidden rounded-[38px] border-[7px] border-[#1d2521] bg-canvas text-fg shadow-pop"
    >
      <div className="flex h-7 items-center justify-between px-6 pt-2 text-[10px] text-fg-2">
        <span>9:41</span>
        <span className="h-3 w-3 rounded-full bg-[#1d2521]" />
        <span>5G</span>
      </div>
      {children}
    </figure>
  );
}

function BottomNav({ active }: { active: "chat" | "models" | "settings" }) {
  const item = (key: "chat" | "models" | "settings", label: string, glyph: React.ReactNode) => (
    <div className="flex flex-1 flex-col items-center gap-1">
      <div className={`flex h-[26px] w-12 items-center justify-center rounded-[9px] ${active === key ? "bg-accent-soft text-accent" : "text-fg-3"}`}>
        {glyph}
      </div>
      <span className={`text-[10px] ${active === key ? "text-accent" : "text-fg-3"}`}>{label}</span>
    </div>
  );
  return (
    <div className="absolute inset-x-0 bottom-0 border-t border-line bg-surface pb-3 pt-2">
      <div className="flex">
        {item("chat", "Chat", <svg viewBox="0 0 24 24" className="h-[18px] w-[18px]" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinejoin="round"><path d="M5 5h14a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-7l-4 3v-3H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z" /></svg>)}
        {item("models", "Models", <svg viewBox="0 0 24 24" className="h-[18px] w-[18px]" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round"><rect x="6" y="6" width="12" height="12" rx="2.5" /><path d="M9 3v3M15 3v3M9 18v3M15 18v3M3 9h3M3 15h3M18 9h3M18 15h3" /></svg>)}
        {item("settings", "Settings", <svg viewBox="0 0 24 24" className="h-[18px] w-[18px]" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round"><path d="M4 7h10M18 7h2M4 17h2M10 17h10" /><circle cx="16" cy="7" r="2" /><circle cx="8" cy="17" r="2" /></svg>)}
      </div>
    </div>
  );
}

export function ChatPhone() {
  return (
    <Frame label="HYDRA.AI chat screen showing a question and an answer written on the phone">
      <div className="flex h-12 items-center gap-2 px-4">
        <LogoMark className="h-6 w-6 text-accent" />
        <div className="leading-tight">
          <div className="text-[13px] font-semibold">HYDRA.AI</div>
          <div className="flex items-center gap-1.5 text-[10px] text-fg-2">
            <span className="h-1.5 w-1.5 rounded-full bg-accent" /> Hydra Fast
          </div>
        </div>
      </div>
      <div className="border-t border-line" />
      <div className="space-y-4 px-4 pt-4">
        <div className="flex justify-end">
          <div className="max-w-[78%] rounded-[16px] rounded-br-[5px] bg-accent-soft px-3 py-2 text-[12px] leading-[1.45]">
            Explain how photosynthesis works in simple terms
          </div>
        </div>
        <div>
          <div className="mb-1.5 flex items-center gap-1.5 text-[10px] text-fg-3">
            <LogoMark className="h-3.5 w-3.5 text-accent" /> HYDRA
          </div>
          <p className="text-[12px] leading-[1.55]">
            Plants make their own food using three things:
          </p>
          <ul className="mt-1.5 space-y-1 text-[12px] leading-[1.5]">
            <li className="flex gap-2"><span className="text-fg-3">&bull;</span><span><b className="font-semibold">Sunlight</b>, caught by chlorophyll in the leaves</span></li>
            <li className="flex gap-2"><span className="text-fg-3">&bull;</span><span><b className="font-semibold">Water</b>, drawn up from the roots</span></li>
            <li className="flex gap-2"><span className="text-fg-3">&bull;</span><span><b className="font-semibold">Carbon dioxide</b>, taken in from the air</span></li>
          </ul>
          <p className="mt-2 text-[12px] leading-[1.55]">They combine these into sugar for energy and release oxygen.</p>
        </div>
      </div>
      <div className="absolute inset-x-0 bottom-[58px] px-3">
        <div className="mb-2 flex items-center gap-1.5 px-1 text-[10px] text-fg-2">
          <span className="dot-pulse h-1.5 w-1.5 rounded-full bg-accent" /> Writing on this phone&hellip;
        </div>
        <div className="flex items-end gap-2 rounded-[16px] border border-line-strong bg-elevated py-1.5 pl-3.5 pr-1.5">
          <span className="flex-1 pb-1.5 pt-1 text-[12px] text-fg-3">Message HYDRA</span>
          <span className="flex h-9 w-9 items-center justify-center rounded-[11px] border border-line-strong bg-card text-fg">
            <svg viewBox="0 0 24 24" className="h-4 w-4" fill="currentColor"><rect x="7" y="7" width="10" height="10" rx="2" /></svg>
          </span>
        </div>
      </div>
      <BottomNav active="chat" />
    </Frame>
  );
}

export function ModelsPhone() {
  return (
    <Frame label="HYDRA.AI models screen listing an installed model and one available to download">
      <div className="px-4 pb-2 pt-1">
        <div className="text-[17px] font-semibold tracking-tight">Models</div>
        <div className="text-[10px] text-fg-2">1 installed &middot; 469 MB on this phone &middot; Wi-Fi only downloads</div>
      </div>
      <div className="border-t border-line" />
      <div className="space-y-3 px-3 pt-3">
        <div className="px-1 text-[9px] font-medium uppercase tracking-wider text-fg-3">Installed</div>
        <div className="rounded-[14px] border border-accent/50 bg-card p-3">
          <div className="text-[13px] font-semibold">Hydra Fast</div>
          <div className="mt-1.5 flex flex-wrap gap-1">
            <span className="rounded-[5px] bg-accent-soft px-1.5 py-0.5 text-[9px] font-medium text-accent">In use</span>
            <span className="rounded-[5px] bg-accent-soft px-1.5 py-0.5 text-[9px] font-medium text-accent">Recommended for this phone</span>
          </div>
          <p className="mt-2 text-[10.5px] leading-[1.45] text-fg-2">Small and quick. Good for short questions, summaries and rewriting.</p>
          <div className="mt-2 flex flex-wrap gap-1">
            {["469 MB download", "Q4_K_M", "3 GB RAM or more"].map((t) => (
              <span key={t} className="rounded-[5px] bg-elevated px-1.5 py-0.5 text-[9px] text-fg-2">{t}</span>
            ))}
          </div>
          <div className="mt-2.5 flex items-center gap-1.5 text-[10.5px] text-accent">
            <CheckIcon className="h-3.5 w-3.5" /> Ready &middot; in use
          </div>
        </div>
        <div className="px-1 pt-1 text-[9px] font-medium uppercase tracking-wider text-fg-3">Available</div>
        <div className="rounded-[14px] border border-line bg-card p-3">
          <div className="text-[13px] font-semibold">Hydra Balanced</div>
          <p className="mt-1.5 text-[10.5px] leading-[1.45] text-fg-2">The default assistant for writing, study explanations and planning.</p>
          <div className="mt-2 flex flex-wrap gap-1">
            {["1.0 GB download", "Q4_K_M", "6 GB RAM or more"].map((t) => (
              <span key={t} className="rounded-[5px] bg-elevated px-1.5 py-0.5 text-[9px] text-fg-2">{t}</span>
            ))}
          </div>
          <div className="mt-2.5 flex h-9 items-center justify-center rounded-[10px] bg-accent text-[11px] font-semibold text-on-accent">
            Download &middot; 1.0 GB
          </div>
        </div>
      </div>
      <BottomNav active="models" />
    </Frame>
  );
}

/** A small reassurance strip used under the hero visual. */
export function LocalBadges() {
  const item = (icon: React.ReactNode, text: string) => (
    <li className="flex items-center gap-2 text-sm text-fg-2">
      <span className="text-accent">{icon}</span>
      {text}
    </li>
  );
  return (
    <ul className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2">
      {item(<PhoneIcon className="h-4 w-4" />, "Runs on your phone")}
      {item(<LockIcon className="h-4 w-4" />, "Chats stored on your phone")}
      {item(<CloudIcon className="h-4 w-4" />, "Internet only for downloads")}
    </ul>
  );
}
