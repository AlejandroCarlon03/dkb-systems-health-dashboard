import { Icon, type IconName } from './Icon'
import { Sparkline } from './Sparkline'

interface StatCardProps {
  icon: IconName
  label: string
  value: number
  /** Recent values for this metric, oldest→newest, including the current value as the last entry. */
  series?: number[]
  accent?: string
  warn?: boolean
}

/** A metric tile: icon, big number, change-since-last-poll delta, and a sparkline of recent values. */
export function StatCard({ icon, label, value, series = [], accent = 'var(--accent)', warn }: StatCardProps) {
  const delta = series.length >= 2 ? value - series[series.length - 2] : 0

  return (
    <div className={`stat-card${warn ? ' warn' : ''}`}>
      <div className="stat-top">
        <span className="stat-icon" style={{ color: accent }}>
          <Icon name={icon} />
        </span>
        {delta !== 0 && (
          <span className={`delta ${delta > 0 ? 'up' : 'down'}`}>
            {delta > 0 ? '▲' : '▼'} {Math.abs(delta).toLocaleString()}
          </span>
        )}
      </div>
      <div className="stat-value">{value.toLocaleString()}</div>
      <div className="stat-label">{label}</div>
      <Sparkline values={series} color={accent} />
    </div>
  )
}
