import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import "./globals.css";

const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: {
    default: "Hydra.AI: your AI assistant. On your phone. Works offline.",
    template: "%s | Hydra.AI",
  },
  description:
    "Hydra.AI runs open-weight AI models directly on your Android phone. Private by default, works offline, and shares only what you choose, end-to-end encrypted. By ZenLink Technologies.",
  openGraph: {
    title: "Hydra.AI: your AI assistant. On your phone. Works offline.",
    description: "Private, offline AI on your Android phone. By ZenLink Technologies.",
    url: siteUrl,
    siteName: "Hydra.AI",
    type: "website",
  },
  robots: { index: true, follow: true },
};

export const viewport: Viewport = {
  themeColor: "#5b5bf6",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
