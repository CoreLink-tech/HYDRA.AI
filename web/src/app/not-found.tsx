import Link from "next/link";

export default function NotFound() {
  return (
    <main className="mx-auto max-w-xl px-6 py-32 text-center">
      <h1 className="text-3xl font-semibold">Page not found</h1>
      <Link href="/" className="mt-6 inline-block text-brand hover:underline">Back to Hydra.AI</Link>
    </main>
  );
}
