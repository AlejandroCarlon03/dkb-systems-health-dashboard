import type { Status } from './types'

/** Maps a status (or null = no data) to the corresponding CSS color variable. */
export function statusVar(status: Status | null): string {
  switch (status) {
    case 'UP':
      return 'var(--up)'
    case 'DEGRADED':
      return 'var(--degraded)'
    case 'DOWN':
      return 'var(--down)'
    default:
      return 'var(--border)'
  }
}
