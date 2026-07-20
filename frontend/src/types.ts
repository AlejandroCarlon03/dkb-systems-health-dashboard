// Mirrors the backend HealthSummary JSON returned by GET /api/health/summary.

export type Status = 'UP' | 'DEGRADED' | 'DOWN'

export interface AssetSummary {
  totalAssets: number
  unassignedAssets: number
  overdueCheckouts: number
  assetsByStatus: Record<string, number>
  retrievedAt: string
}

export interface CrmSummary {
  openOpportunities: number
  recentActivityCount: number
  retrievedAt: string
}

export interface HealthSummary {
  status: Status
  generatedAt: string
  assets: AssetSummary | null
  crm: CrmSummary | null
  sourceErrors: string[]
}

// GET /api/health/history?days=N
export interface HistoryEvent {
  at: string
  from: Status
  to: Status
}

export interface TimelineBucket {
  at: string
  status: Status | null
}

export interface HistoryResponse {
  days: number
  sampleCount: number
  upCount: number
  degradedCount: number
  downCount: number
  uptimePercent: number
  firstSampleAt: string | null
  lastSampleAt: string | null
  events: HistoryEvent[]
  timeline: TimelineBucket[]
}

// GET /api/config
export interface AppConfig {
  snipeItUrl: string
  odooUrl: string
}
