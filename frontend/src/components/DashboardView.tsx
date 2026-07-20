import { useHealthSummary, type MetricHistoryPoint } from '../useHealthSummary'
import { StatusBanner } from './StatusBanner'
import { AssetsPanel } from './AssetsPanel'
import { CrmPanel } from './CrmPanel'
import { SourceErrors } from './SourceErrors'
import { RelativeTime } from './RelativeTime'

/** Extract a non-null number series for one metric from the rolling history. */
function seriesOf(history: MetricHistoryPoint[], key: keyof MetricHistoryPoint): number[] {
  return history.map((p) => p[key]).filter((v): v is number => v != null)
}

/** The live "current health" view: status hero, asset/CRM KPI cards, donut, and source errors. */
export function DashboardView() {
  const { data, history, lastUpdatedAt, error, loading, refresh } = useHealthSummary()

  const sourcesTotal = 2
  const sourcesUp = data ? Number(data.assets != null) + Number(data.crm != null) : 0

  return (
    <>
      <header className="view-header">
        <div>
          <h1>Overview</h1>
          <p className="sub">Aggregated operational health across DKB internal systems.</p>
        </div>
        <div className="controls">
          <span className="muted updated">
            <RelativeTime timestamp={lastUpdatedAt} prefix="Updated " />
          </span>
          <button type="button" onClick={refresh} disabled={loading}>
            {loading ? 'Refreshing…' : 'Refresh'}
          </button>
        </div>
      </header>

      {error && !data && (
        <div className="errors">
          Failed to load dashboard: <code>{error}</code>
        </div>
      )}

      {data && (
        <>
          <StatusBanner status={data.status} sourcesUp={sourcesUp} sourcesTotal={sourcesTotal} />

          <div className="section-title">Assets — Snipe-IT</div>
          <AssetsPanel
            assets={data.assets}
            series={{
              total: seriesOf(history, 'totalAssets'),
              unassigned: seriesOf(history, 'unassignedAssets'),
              overdue: seriesOf(history, 'overdueCheckouts'),
            }}
          />

          <div className="section-title">CRM — Odoo</div>
          <CrmPanel
            crm={data.crm}
            series={{
              openOpportunities: seriesOf(history, 'openOpportunities'),
              recentActivity: seriesOf(history, 'recentActivityCount'),
            }}
          />

          <SourceErrors errors={data.sourceErrors} />
        </>
      )}

      {!data && !error && <p className="muted">Loading…</p>}
    </>
  )
}
