import type { AppConfig, HealthSummary, HistoryResponse } from './types'

/**
 * Fetch the aggregated health summary from the backend.
 * In dev, Vite proxies /api to the backend on :8090 (see vite.config.ts).
 */
export async function fetchHealthSummary(signal?: AbortSignal): Promise<HealthSummary> {
  const res = await fetch('/api/health/summary', {
    headers: { Accept: 'application/json' },
    signal,
  })
  if (!res.ok) {
    throw new Error(`Request failed: HTTP ${res.status}`)
  }
  return res.json() as Promise<HealthSummary>
}

/** Fetch persisted uptime/event history over the last `days`. */
export async function fetchHistory(days: number, signal?: AbortSignal): Promise<HistoryResponse> {
  const res = await fetch(`/api/health/history?days=${days}`, {
    headers: { Accept: 'application/json' },
    signal,
  })
  if (!res.ok) {
    throw new Error(`Request failed: HTTP ${res.status}`)
  }
  return res.json() as Promise<HistoryResponse>
}

/** Fetch non-secret config (external system URLs). */
export async function fetchConfig(signal?: AbortSignal): Promise<AppConfig> {
  const res = await fetch('/api/config', { headers: { Accept: 'application/json' }, signal })
  if (!res.ok) {
    throw new Error(`Request failed: HTTP ${res.status}`)
  }
  return res.json() as Promise<AppConfig>
}
