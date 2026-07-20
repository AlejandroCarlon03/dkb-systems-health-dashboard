import type { AssetSummary } from '../types'
import { colorFor } from '../palette'
import { StatCard } from './StatCard'
import { DonutChart, type DonutSegment } from './DonutChart'

export interface AssetSeries {
  total: number[]
  unassigned: number[]
  overdue: number[]
}

/** Snipe-IT asset metrics: KPI cards + a status-breakdown donut. */
export function AssetsPanel({ assets, series }: { assets: AssetSummary | null; series: AssetSeries }) {
  if (!assets) {
    return <div className="unavailable">Asset data unavailable (Snipe-IT source failed).</div>
  }

  const segments: DonutSegment[] = Object.entries(assets.assetsByStatus)
    .sort((a, b) => b[1] - a[1])
    .map(([label, value], i) => ({ label, value, color: colorFor(i) }))

  return (
    <>
      <div className="grid">
        <StatCard icon="assets" label="Total assets" value={assets.totalAssets} series={series.total} accent="#2f6fed" />
        <StatCard icon="unassigned" label="Unassigned" value={assets.unassignedAssets} series={series.unassigned} accent="#0ea5e9" />
        <StatCard
          icon="overdue"
          label="Overdue check-ins"
          value={assets.overdueCheckouts}
          series={series.overdue}
          accent="#d64545"
          warn={assets.overdueCheckouts > 0}
        />
      </div>
      {segments.length > 0 && (
        <div className="panel">
          <div className="panel-title">Assets by status</div>
          <DonutChart segments={segments} centerValue={assets.totalAssets} centerLabel="assets" />
        </div>
      )}
    </>
  )
}
