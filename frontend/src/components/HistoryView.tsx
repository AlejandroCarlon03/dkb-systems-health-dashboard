import { useEffect, useState } from 'react'
import { fetchHistory } from '../api'
import type { HistoryResponse, Status } from '../types'
import { statusVar } from '../statusColor'
import { TimelineStrip } from './TimelineStrip'

const DAY_OPTIONS = [7, 14, 30]

function StatusPill({ status }: { status: Status }) {
  return (
    <span className="pill" style={{ background: `color-mix(in srgb, ${statusVar(status)} 18%, transparent)`, color: statusVar(status) }}>
      {status}
    </span>
  )
}

/** Persisted uptime + status-change history over a selectable window. */
export function HistoryView() {
  const [days, setDays] = useState(7)
  const [data, setData] = useState<HistoryResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    fetchHistory(days, controller.signal)
      .then((d) => {
        setData(d)
        setError(null)
      })
      .catch((e) => {
        if ((e as Error).name !== 'AbortError') setError((e as Error).message)
      })
      .finally(() => setLoading(false))
    return () => controller.abort()
  }, [days])

  return (
    <>
      <header className="view-header">
        <div>
          <h1>History</h1>
          <p className="sub">Uptime and status changes recorded every minute.</p>
        </div>
        <div className="controls day-select">
          {DAY_OPTIONS.map((d) => (
            <button key={d} type="button" className={`day-btn${d === days ? ' active' : ''}`} onClick={() => setDays(d)}>
              {d}d
            </button>
          ))}
        </div>
      </header>

      {error && (
        <div className="errors">
          Failed to load history: <code>{error}</code>
        </div>
      )}

      {data && data.sampleCount === 0 && (
        <div className="unavailable">
          No history recorded yet. Snapshots are collected every minute while the service runs — check
          back shortly.
        </div>
      )}

      {data && data.sampleCount > 0 && (
        <>
          <div className="grid">
            <div className="stat-card">
              <div className="stat-value">{data.uptimePercent}%</div>
              <div className="stat-label">Uptime (all sources healthy)</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{data.sampleCount.toLocaleString()}</div>
              <div className="stat-label">Samples</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--degraded)' }}>{data.degradedCount.toLocaleString()}</div>
              <div className="stat-label">Degraded samples</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--down)' }}>{data.downCount.toLocaleString()}</div>
              <div className="stat-label">Down samples</div>
            </div>
          </div>

          <div className="panel">
            <div className="panel-title">Status timeline · last {days} days</div>
            <TimelineStrip buckets={data.timeline} leftLabel={`${days}d ago`} />
          </div>

          <div className="section-title">Status changes ({data.events.length})</div>
          {data.events.length === 0 ? (
            <div className="unavailable">No status changes in this period — steady the whole time.</div>
          ) : (
            <ul className="event-log">
              {[...data.events].reverse().map((e, i) => (
                <li key={i}>
                  <span className="event-time">{new Date(e.at).toLocaleString()}</span>
                  <StatusPill status={e.from} />
                  <span aria-hidden="true">→</span>
                  <StatusPill status={e.to} />
                </li>
              ))}
            </ul>
          )}
        </>
      )}

      {!data && !error && loading && <p className="muted">Loading…</p>}
    </>
  )
}
