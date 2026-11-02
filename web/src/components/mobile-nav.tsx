"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { CloseIcon, MenuIcon } from "@/components/ui/icons";

export type NavItem = { href: string; label: string };

/** The small-screen menu. A real button with aria-expanded; closes on Escape or after choosing a link. */
export function MobileNav({ items }: { items: NavItem[] }) {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setOpen(false);
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open]);

  return (
    <div className="md:hidden">
      <button
        type="button"
        aria-expanded={open}
        aria-controls="mobile-menu"
        aria-label={open ? "Close menu" : "Open menu"}
        onClick={() => setOpen((v) => !v)}
        className="inline-flex h-11 w-11 items-center justify-center rounded-btn text-fg-2 transition-colors hover:bg-surface hover:text-fg"
      >
        {open ? <CloseIcon /> : <MenuIcon />}
      </button>
      <div
        id="mobile-menu"
        inert={!open}
        className={`absolute inset-x-0 top-full border-b border-line bg-canvas px-4 pb-4 pt-2 shadow-card transition-[opacity,transform] duration-200 ${
          open ? "translate-y-0 opacity-100" : "pointer-events-none -translate-y-2 opacity-0"
        }`}
      >
        <nav aria-label="Mobile" className="flex flex-col">
          {items.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              onClick={() => setOpen(false)}
              className="flex min-h-12 items-center rounded-btn px-3 text-base text-fg-2 transition-colors hover:bg-surface hover:text-fg"
            >
              {item.label}
            </Link>
          ))}
        </nav>
      </div>
    </div>
  );
}
