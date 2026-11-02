import type { ReactNode } from "react";

/** One outlined icon family, 1.75 stroke, so weight stays uniform across the site. */
function Icon({ children, className = "h-5 w-5" }: { children: ReactNode; className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.75"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={className}
    >
      {children}
    </svg>
  );
}

type P = { className?: string };

export const LockIcon = (p: P) => (
  <Icon {...p}>
    <rect x="4.5" y="10.5" width="15" height="10" rx="2.5" />
    <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" />
  </Icon>
);
export const OfflineIcon = (p: P) => (
  <Icon {...p}>
    <path d="M3 3l18 18" />
    <path d="M8.5 8.8A10 10 0 0 0 3 12.2M5.8 15.5a6 6 0 0 1 4-2.1M12.3 5.1A10 10 0 0 1 21 12.2M15.9 10.3A6 6 0 0 1 18.2 15.5" />
    <circle cx="12" cy="19" r="1" fill="currentColor" stroke="none" />
  </Icon>
);
export const ChipIcon = (p: P) => (
  <Icon {...p}>
    <rect x="6" y="6" width="12" height="12" rx="2.5" />
    <rect x="9.5" y="9.5" width="5" height="5" rx="1" />
    <path d="M9 3v3M15 3v3M9 18v3M15 18v3M3 9h3M3 15h3M18 9h3M18 15h3" />
  </Icon>
);
export const LayersIcon = (p: P) => (
  <Icon {...p}>
    <path d="M12 3.5l8.5 4.5-8.5 4.5L3.5 8z" />
    <path d="M3.5 12.5L12 17l8.5-4.5M3.5 16.5L12 21l8.5-4.5" />
  </Icon>
);
export const ShareIcon = (p: P) => (
  <Icon {...p}>
    <path d="M12 15V4M8 8l4-4 4 4" />
    <path d="M5 13v5a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-5" />
  </Icon>
);
export const PhoneIcon = (p: P) => (
  <Icon {...p}>
    <rect x="6.5" y="2.5" width="11" height="19" rx="3" />
    <path d="M11 18.5h2" />
  </Icon>
);
export const DownloadIcon = (p: P) => (
  <Icon {...p}>
    <path d="M12 4v11M8 11l4 4 4-4" />
    <path d="M5 19.5h14" />
  </Icon>
);
export const ShieldIcon = (p: P) => (
  <Icon {...p}>
    <path d="M12 3l7.5 3v5.5c0 4.5-3 7.8-7.5 9.5-4.5-1.700-7.500-5-7.500-9.500V6z" />
    <path d="M9 12l2 2 4-4" />
  </Icon>
);
export const CheckIcon = (p: P) => (
  <Icon {...p}>
    <path d="M5 12.5l4.500 4.500L19 7.500" />
  </Icon>
);
export const ArrowRightIcon = (p: P) => (
  <Icon {...p}>
    <path d="M5 12h14M13 6l6 6-6 6" />
  </Icon>
);
export const MenuIcon = (p: P) => (
  <Icon {...p}>
    <path d="M4 7h16M4 12h16M4 17h16" />
  </Icon>
);
export const CloseIcon = (p: P) => (
  <Icon {...p}>
    <path d="M6 6l12 12M18 6L6 18" />
  </Icon>
);
export const CloudIcon = (p: P) => (
  <Icon {...p}>
    <path d="M7 18.500a4.500 4.500 0 0 1-.5-8.970A6 6 0 0 1 18 10.500a4 4 0 0 1-.500 8z" />
  </Icon>
);
export const AlertIcon = (p: P) => (
  <Icon {...p}>
    <path d="M12 3.500l9 16H3z" />
    <path d="M12 10v4.500" />
    <circle cx="12" cy="17" r="0.5" fill="currentColor" />
  </Icon>
);
