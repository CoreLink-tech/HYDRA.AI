import type { Metadata, Viewport } from "next";
import type { ReactNode } from "react";
import "./globals.css";

const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: {
    default: "HYDRA.AI: Your AI. Your Device. Your Privacy.",
    template: "%s | HYDRA.AI",
  },
  description:
    "HYDRA.AI runs language models directly on your Android phone. Chat privately, work offline, and keep your conversations where they belong. By ZenLink Technologies.",
  openGraph: {
    title: "HYDRA.AI: Your AI. Your Device. Your Privacy.",
    description: "Private, offline AI on your Android phone. By ZenLink Technologies.",
    url: siteUrl,
    siteName: "HYDRA.AI",
    type: "website",
  },
  robots: { index: true, follow: true },
};

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#fbfaf6" },
    { media: "(prefers-color-scheme: dark)", color: "#0b0e0d" },
  ],
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
