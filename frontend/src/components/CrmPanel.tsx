import type { CrmSummary } from '../types'
import { StatCard } from './StatCard'

export interface CrmSeries {
  openOpportunities: number[]
  recentActivity: number[]
}

/** Odoo CRM metrics as KPI cards. */
export function CrmPanel({ crm, series }: { crm: CrmSummary | null; series: CrmSeries }) {
  if (!crm) {
    return <div className="unavailable">CRM data unavailable (Odoo source failed).</div>
  }

  return (
    <div className="grid">
      <StatCard
        icon="opportunities"
        label="Open opportunities"
        value={crm.openOpportunities}
        series={series.openOpportunities}
        accent="#8b5cf6"
      />
      <StatCard
        icon="activity"
        label="Recent activity (7d)"
        value={crm.recentActivityCount}
        series={series.recentActivity}
        accent="#1f9d55"
      />
    </div>
  )
}
