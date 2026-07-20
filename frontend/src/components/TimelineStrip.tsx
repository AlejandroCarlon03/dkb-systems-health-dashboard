import type { TimelineBucket } from '../types'
import { statusVar } from '../statusColor'

interface TimelineStripProps {
  buckets: TimelineBucket[]
  leftLabel: string
}

/** A horizontal strip of colored cells showing worst status per time slice over the window. */
export function TimelineStrip({ buckets, leftLabel }: TimelineStripProps) {
  return (
    <div className="timeline">
      <div className="timeline-bar">
        {buckets.map((b, i) => (
          <span
            key={i}
            className="tl-cell"
            style={{ background: statusVar(b.status) }}
            title={`${new Date(b.at).toLocaleString()} — ${b.status ?? 'no data'}`}
          />
        ))}
      </div>
      <div className="timeline-axis muted">
        <span>{leftLabel}</span>
        <span>now</span>
      </div>
    </div>
  )
}
