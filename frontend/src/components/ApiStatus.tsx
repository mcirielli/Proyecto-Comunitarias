export type ApiState = 'checking' | 'available' | 'unavailable'

const labels: Record<ApiState, string> = {
  checking: 'Comprobando API…', available: 'API disponible', unavailable: 'API no disponible',
}
const colors: Record<ApiState, string> = {
  checking: 'bg-slate-100 text-slate-700',
  available: 'bg-emerald-100 text-emerald-800',
  unavailable: 'bg-rose-100 text-rose-800',
}

export function ApiStatus({ state }: { state: ApiState }) {
  return <p role="status" aria-live="polite" className={'inline-flex rounded-full px-4 py-2 text-sm font-semibold ' + colors[state]}>
    {labels[state]}
  </p>
}
