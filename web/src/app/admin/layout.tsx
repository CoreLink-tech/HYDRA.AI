import Link from "next/link";
import type { ReactNode } from "react";
import { Logo } from "@/components/ui/logo";

export const dynamic = "force-dynamic";

const nav = [
  { href: "/admin", label: "Overview" },
  { href: "/admin/models", label: "Models" },
  { href: "/admin/users", label: "Users & subscriptions" },
  { href: "/admin/reports", label: "Abuse reports" },
];

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-canvas text-fg">
      <header className="border-b border-line bg-elevated">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 gap-y-2 px-4 py-3 sm:px-6">
          <div className="flex items-center gap-3">
            <Logo />
            <span className="rounded-md bg-surface px-2 py-0.5 text-xs font-medium text-fg-2">Admin</span>
          </div>
          <Link href="/" className="rounded-btn px-2 py-1 text-sm text-fg-3 hover:text-fg">
            View site
          </Link>
          <nav aria-label="Admin" className="-mx-2 flex w-full gap-1 overflow-x-auto text-sm">
            {nav.map((n) => (
              <Link key={n.href} href={n.href} className="whitespace-nowrap rounded-btn px-3 py-2 text-fg-2 hover:bg-surface hover:text-fg">
                {n.label}
              </Link>
            ))}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  );
}
