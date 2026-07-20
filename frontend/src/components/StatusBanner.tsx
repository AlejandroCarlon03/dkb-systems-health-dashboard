import type { Status } from '../types'

const STATUS_TEXT: Record<Status, string> = {
  UP: 'All sources healthy',
  DEGRADED: 'Some sources unavailable',
  DOWN: 'All sources unavailable',
}

interface StatusBannerProps {
  status: Status
  sourcesUp: number
  sourcesTotal: number
}

/** Prominent color-coded hero summarizing overall health and how many sources are online. */
export function StatusBanner({ status, sourcesUp, sourcesTotal }: StatusBannerProps) {
  return (
    <div className={`status-banner ${status}`}>
      <span className="dot" />
      <div className="status-main">
        <div className="status-label">{status}</div>
        <div className="muted">{STATUS_TEXT[status]}</div>
      </div>
      <div className="status-meta">
        <div className="status-count">
          {sourcesUp}/{sourcesTotal}
        </div>
        <div className="muted">sources online</div>
      </div>
    </div>
  )
}
