import { useEffect, useState } from 'react'

/** Renders a live "Ns ago" label that ticks every second. */
export function RelativeTime({ timestamp, prefix = '' }: { timestamp: number | null; prefix?: string }) {
  const [, tick] = useState(0)

  useEffect(() => {
    const id = setInterval(() => tick((n) => n + 1), 1000)
    return () => clearInterval(id)
  }, [])

  if (!timestamp) return <>—</>

  const secs = Math.max(0, Math.round((Date.now() - timestamp) / 1000))
  const label =
    secs < 5 ? 'just now' : secs < 60 ? `${secs}s ago` : `${Math.floor(secs / 60)}m ${secs % 60}s ago`

  return (
    <>
      {prefix}
      {label}
    </>
  )
}
