import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";
import { LinkButton } from "@/components/ui/button";

export default function NotFound() {
  return (
    <>
      <SiteHeader />
      <main className="mx-auto max-w-xl px-4 py-28 text-center sm:px-6">
        <p className="text-sm font-semibold text-accent">404</p>
        <h1 className="mt-3 text-3xl font-semibold tracking-tight">Page not found</h1>
        <p className="mt-3 text-fg-2">The page you are looking for does not exist or has moved.</p>
        <div className="mt-8">
          <LinkButton href="/">Back to HYDRA.AI</LinkButton>
        </div>
      </main>
      <SiteFooter />
    </>
  );
}
