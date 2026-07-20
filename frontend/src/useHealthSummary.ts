import { useCallback, useEffect, useRef, useState } from 'react'
import { fetchHealthSummary } from './api'
import type { HealthSummary } from './types'

const REFRESH_MS = 30_000
const HISTORY_LIMIT = 30

/** One sampled point of the metrics we chart over time. */
export interface MetricHistoryPoint {
  totalAssets: number | null
  unassignedAssets: number | null
  overdueCheckouts: number | null
  openOpportunities: number | null
  recentActivityCount: number | null
}

interface HealthState {
  data: HealthSummary | null
  history: MetricHistoryPoint[]
  lastUpdatedAt: number | null
  error: string | null
  loading: boolean
  refresh: () => void
}

function toPoint(s: HealthSummary): MetricHistoryPoint {
  return {
    totalAssets: s.assets?.totalAssets ?? null,
    unassignedAssets: s.assets?.unassignedAssets ?? null,
    overdueCheckouts: s.assets?.overdueCheckouts ?? null,
    openOpportunities: s.crm?.openOpportunities ?? null,
    recentActivityCount: s.crm?.recentActivityCount ?? null,
  }
}

/** Loads the health summary, auto-refreshes every 30s, and keeps a rolling history for charts. */
export function useHealthSummary(): HealthState {
  const [data, setData] = useState<HealthSummary | null>(null)
  const [history, setHistory] = useState<MetricHistoryPoint[]>([])
  const [lastUpdatedAt, setLastUpdatedAt] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const inFlight = useRef<AbortController | null>(null)

  const load = useCallback(async () => {
    inFlight.current?.abort()
    const controller = new AbortController()
    inFlight.current = controller
    setLoading(true)
    try {
      const summary = await fetchHealthSummary(controller.signal)
      setData(summary)
      setHistory((h) => [...h, toPoint(summary)].slice(-HISTORY_LIMIT))
      setLastUpdatedAt(Date.now())
      setError(null)
    } catch (e) {
      if ((e as Error).name !== 'AbortError') {
        setError((e as Error).message)
      }
    } finally {
      if (inFlight.current === controller) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    load()
    const id = setInterval(load, REFRESH_MS)
    return () => {
      clearInterval(id)
      inFlight.current?.abort()
    }
  }, [load])

  return { data, history, lastUpdatedAt, error, loading, refresh: load }
}
