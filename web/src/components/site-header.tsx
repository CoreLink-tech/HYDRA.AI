import Link from "next/link";
import { MobileNav, type NavItem } from "@/components/mobile-nav";
import { LinkButton } from "@/components/ui/button";
import { Logo } from "@/components/ui/logo";

const items: NavItem[] = [
  { href: "/#features", label: "Features" },
  { href: "/#privacy", label: "Privacy" },
  { href: "/#pricing", label: "Pricing" },
  { href: "/#download", label: "Download" },
];

export function SiteHeader() {
  return (
    <header className="sticky top-0 z-40 border-b border-line bg-canvas/85 backdrop-blur-md">
      <div className="relative mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
        <Link href="/" aria-label="HYDRA.AI home" className="rounded-btn">
          <Logo />
        </Link>
        <nav aria-label="Main" className="hidden items-center gap-1 md:flex">
          {items.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="rounded-btn px-3 py-2 text-sm text-fg-2 transition-colors hover:bg-surface hover:text-fg"
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="flex items-center gap-2">
          <LinkButton href="/#download" className="hidden sm:inline-flex">
            Get HYDRA.AI
          </LinkButton>
          <MobileNav items={items} />
        </div>
      </div>
    </header>
  );
}
