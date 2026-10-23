export function SiteFooter() {
  return (
    <footer className="border-t border-zinc-200 bg-zinc-50">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-6 py-10 text-sm text-zinc-500 md:flex-row md:items-center md:justify-between">
        <div>
          <div className="font-medium text-zinc-900">Hydra.AI</div>
          <div>A product of ZenLink Technologies. Hydra.AI is a working product name.</div>
        </div>
        <div className="flex gap-6">
          <a href="/privacy" className="hover:text-zinc-900">Privacy</a>
          <a href="/#models" className="hover:text-zinc-900">Model licences</a>
          <a href="/#pricing" className="hover:text-zinc-900">Pricing</a>
        </div>
      </div>
    </footer>
  );
}
