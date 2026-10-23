import Link from "next/link";

export function SiteHeader() {
  return (
    <header className="sticky top-0 z-40 border-b border-zinc-200/70 bg-white/80 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        <Link href="/" className="flex items-center gap-2 font-semibold tracking-tight">
          <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand text-sm text-white">H</span>
          Hydra.AI
        </Link>
        <nav className="hidden items-center gap-7 text-sm text-zinc-600 md:flex">
          <a href="/#features" className="hover:text-zinc-900">Features</a>
          <a href="/#models" className="hover:text-zinc-900">Models</a>
          <a href="/#privacy" className="hover:text-zinc-900">Privacy</a>
          <a href="/#pricing" className="hover:text-zinc-900">Pricing</a>
        </nav>
        <a href="/#pricing" className="rounded-full bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700">
          Get Hydra.AI
        </a>
      </div>
    </header>
  );
}
