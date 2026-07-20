import type { AppConfig } from '../types'

export type View = 'dashboard' | 'history'

interface SidebarProps {
  view: View
  onNavigate: (view: View) => void
  config: AppConfig | null
}

/** Left navigation: view switch + external links to the source systems. */
export function Sidebar({ view, onNavigate, config }: SidebarProps) {
  return (
    <aside className="sidebar">
      <div className="brand">
        DKB <span>Health</span>
      </div>

      <nav>
        <button
          type="button"
          className={`nav-btn${view === 'dashboard' ? ' active' : ''}`}
          onClick={() => onNavigate('dashboard')}
        >
          Dashboard
        </button>
        <button
          type="button"
          className={`nav-btn${view === 'history' ? ' active' : ''}`}
          onClick={() => onNavigate('history')}
        >
          History
        </button>
      </nav>

      <div className="sidebar-label">Open in source</div>
      <a className="ext-link" href={config?.odooUrl ?? '#'} target="_blank" rel="noopener noreferrer">
        <span>Odoo (CRM)</span>
        <span aria-hidden="true">↗</span>
      </a>
      <a className="ext-link" href={config?.snipeItUrl ?? '#'} target="_blank" rel="noopener noreferrer">
        <span>Snipe-IT (assets)</span>
        <span aria-hidden="true">↗</span>
      </a>

      <div className="sidebar-foot muted">DKB IT Health Dashboard</div>
    </aside>
  )
}
