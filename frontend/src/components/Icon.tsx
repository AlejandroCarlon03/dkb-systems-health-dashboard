import type { ReactNode } from 'react'

export type IconName = 'assets' | 'unassigned' | 'overdue' | 'opportunities' | 'activity'

const ICONS: Record<IconName, ReactNode> = {
  assets: (
    <>
      <rect x="3" y="4" width="18" height="6" rx="1" />
      <rect x="3" y="14" width="18" height="6" rx="1" />
      <path d="M7 7h.01M7 17h.01" />
    </>
  ),
  unassigned: (
    <>
      <circle cx="9" cy="8" r="3" />
      <path d="M4 20c0-3 2.4-5 5-5" />
      <path d="M16 9l4 4M20 9l-4 4" />
    </>
  ),
  overdue: (
    <>
      <circle cx="12" cy="13" r="7" />
      <path d="M12 10v3l2 2M9 2h6" />
    </>
  ),
  opportunities: (
    <>
      <circle cx="12" cy="12" r="8" />
      <circle cx="12" cy="12" r="4" />
      <circle cx="12" cy="12" r="1" />
    </>
  ),
  activity: <path d="M3 12h4l3 8 4-16 3 8h4" />,
}

/** Small inline stroke icon (uses currentColor). */
export function Icon({ name }: { name: IconName }) {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {ICONS[name]}
    </svg>
  )
}
