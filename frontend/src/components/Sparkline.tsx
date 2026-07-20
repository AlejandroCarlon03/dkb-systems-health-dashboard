interface SparklineProps {
  values: number[]
  color?: string
}

/** Tiny inline line chart of recent values (fills its container width). */
export function Sparkline({ values, color = 'var(--accent)' }: SparklineProps) {
  const w = 100
  const h = 28
  const pad = 2

  if (values.length < 2) {
    // Not enough history yet — render an empty box so layout stays stable.
    return <svg className="spark" viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="none" aria-hidden="true" />
  }

  const min = Math.min(...values)
  const max = Math.max(...values)
  const range = max - min || 1
  const step = (w - pad * 2) / (values.length - 1)

  const points = values
    .map((v, i) => {
      const x = pad + i * step
      const y = pad + (h - pad * 2) * (1 - (v - min) / range)
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')

  return (
    <svg className="spark" viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="none" aria-hidden="true">
      <polyline
        points={points}
        fill="none"
        stroke={color}
        strokeWidth="2"
        strokeLinejoin="round"
        strokeLinecap="round"
      />
    </svg>
  )
}
