/** The HYDRA.AI mark: one stem splitting into three heads. Same geometry as the Android app icon. */
export function LogoMark({ className = "h-7 w-7" }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className={className}>
      <path
        d="M12 20.5V13.5M12 13.5C12 10 8.5 9.5 6 6.5M12 13.5V5M12 13.5C12 10 15.5 9.5 18 6.5"
        stroke="currentColor"
        strokeWidth="1.9"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="6" cy="5.8" r="1.7" fill="currentColor" />
      <circle cx="12" cy="4.2" r="1.7" fill="currentColor" />
      <circle cx="18" cy="5.8" r="1.7" fill="currentColor" />
    </svg>
  );
}

export function Logo({ className = "" }: { className?: string }) {
  return (
    <span className={`inline-flex items-center gap-2.5 font-semibold tracking-tight text-fg ${className}`}>
      <LogoMark className="h-7 w-7 text-accent" />
      <span>HYDRA.AI</span>
    </span>
  );
}
