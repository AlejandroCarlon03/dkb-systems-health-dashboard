export interface DonutSegment {
  label: string
  value: number
  color: string
}

interface DonutChartProps {
  segments: DonutSegment[]
  centerValue: number | string
  centerLabel: string
}

/** SVG donut chart with a legend listing each segment's value and share. */
export function DonutChart({ segments, centerValue, centerLabel }: DonutChartProps) {
  const total = segments.reduce((sum, s) => sum + s.value, 0) || 1
  const size = 180
  const stroke = 22
  const r = (size - stroke) / 2
  const c = 2 * Math.PI * r
  const cx = size / 2
  const cy = size / 2

  let offset = 0

  return (
    <div className="donut-wrap">
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="donut" role="img" aria-label={`${centerLabel}: ${centerValue}`}>
        <circle cx={cx} cy={cy} r={r} fill="none" stroke="var(--border)" strokeWidth={stroke} />
        {segments.map((seg, i) => {
          const len = (seg.value / total) * c
          const el = (
            <circle
              key={i}
              cx={cx}
              cy={cy}
              r={r}
              fill="none"
              stroke={seg.color}
              strokeWidth={stroke}
              strokeDasharray={`${len} ${c - len}`}
              strokeDashoffset={-offset}
              transform={`rotate(-90 ${cx} ${cy})`}
            />
          )
          offset += len
          return el
        })}
        <text x={cx} y={cy - 2} textAnchor="middle" className="donut-value">
          {centerValue}
        </text>
        <text x={cx} y={cy + 16} textAnchor="middle" className="donut-label">
          {centerLabel}
        </text>
      </svg>
      <ul className="legend">
        {segments.map((seg, i) => (
          <li key={i}>
            <span className="swatch" style={{ background: seg.color }} />
            <span className="legend-label">{seg.label}</span>
            <span className="legend-value">
              {seg.value.toLocaleString()} · {Math.round((seg.value / total) * 100)}%
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}
