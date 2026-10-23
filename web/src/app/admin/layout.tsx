import Link from "next/link";
import type { ReactNode } from "react";

export const dynamic = "force-dynamic";

const nav = [
  { href: "/admin", label: "Overview" },
  { href: "/admin/models", label: "Models" },
  { href: "/admin/users", label: "Users & subscriptions" },
  { href: "/admin/reports", label: "Abuse reports" },
];

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen bg-zinc-50 text-zinc-900">
      <header className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-6">
            <span className="font-semibold">Hydra.AI admin</span>
            <nav className="flex gap-4 text-sm">
              {nav.map((n) => (
                <Link key={n.href} href={n.href} className="text-zinc-600 hover:text-zinc-900">
                  {n.label}
                </Link>
              ))}
            </nav>
          </div>
          <Link href="/" className="text-sm text-zinc-500 hover:text-zinc-900">
            View site
          </Link>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">{children}</main>
    </div>
  );
}
