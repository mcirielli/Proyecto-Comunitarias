import { useEffect, useState } from 'react'
import { ApiStatus } from '../components/ApiStatus'
import type { ApiState } from '../components/ApiStatus'
import { getHealth } from '../services/health'

export function HomePage() {
  const [state, setState] = useState<ApiState>('checking')
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => controller.abort(), 8000)
    let active = true
    setState('checking')
    setError('')
    getHealth(controller.signal).then(() => {
      if (active) setState('available')
    }).catch((reason: unknown) => {
      if (!active) return
      setState('unavailable')
      setError(controller.signal.aborted ? 'La API no respondió a tiempo.' :
        reason instanceof Error ? reason.message : 'No se pudo consultar la API.')
    }).finally(() => window.clearTimeout(timeout))
    return () => { active = false; window.clearTimeout(timeout); controller.abort() }
  }, [attempt])

  return <main className="mx-auto flex max-w-3xl flex-col px-6 py-12">
    <p className="mb-3 text-sm font-semibold uppercase tracking-widest accent">Proyecto UADE</p>
    <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">Comunitarias</h1>
    <p className="mt-4 text-lg muted">Una gestión mayorista más organizada.</p>
    <details className="connection-details"><summary>Estado de conexión</summary><section aria-labelledby="connection-title" className="surface mt-4 rounded-xl p-5">
      <h2 id="connection-title" className="mb-4 text-xl font-semibold">Estado de conexión</h2>
      <ApiStatus state={state} />
      {error && <p className="mt-3 text-sm error-text">{error}</p>}
      <p className="mt-4 text-sm muted">Este indicador comprueba la disponibilidad HTTP de la API.</p>
      <button type="button" disabled={state === 'checking'} onClick={() => setAttempt(value => value + 1)}
        className="action mt-4">
        Volver a comprobar
      </button>
    </section></details>
  </main>
}
