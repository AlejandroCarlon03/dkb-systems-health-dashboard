import { useEffect, useState } from 'react'
import './App.css'
import { fetchConfig } from './api'
import type { AppConfig } from './types'
import { Sidebar, type View } from './components/Sidebar'
import { DashboardView } from './components/DashboardView'
import { HistoryView } from './components/HistoryView'

function App() {
  const [view, setView] = useState<View>('dashboard')
  const [config, setConfig] = useState<AppConfig | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    fetchConfig(controller.signal)
      .then(setConfig)
      .catch(() => {
        /* external links just fall back to '#' if config is unavailable */
      })
    return () => controller.abort()
  }, [])

  return (
    <div className="layout">
      <Sidebar view={view} onNavigate={setView} config={config} />
      <main className="main">
        <div className="view">
          {view === 'dashboard' ? <DashboardView /> : <HistoryView />}
        </div>
      </main>
    </div>
  )
}

export default App
