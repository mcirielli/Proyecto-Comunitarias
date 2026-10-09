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

  return <main className="mx-auto flex min-h-screen max-w-3xl flex-col justify-center px-6 py-12">
    <p className="mb-3 text-sm font-semibold uppercase tracking-widest text-emerald-700">Proyecto UADE</p>
    <h1 className="text-4xl font-bold tracking-tight sm:text-5xl">Comunitarias</h1>
    <p className="mt-4 text-lg text-slate-600">Una gestión mayorista más organizada.</p>
    <section aria-labelledby="connection-title" className="mt-10 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <h2 id="connection-title" className="mb-4 text-xl font-semibold">Estado de conexión</h2>
      <ApiStatus state={state} />
      {error && <p className="mt-3 text-sm text-rose-700">{error}</p>}
      <p className="mt-4 text-sm text-slate-600">Estamos preparando la plataforma. Las funciones comerciales se incorporarán en próximas etapas.</p>
      <button type="button" disabled={state === 'checking'} onClick={() => setAttempt(value => value + 1)}
        className="mt-6 rounded-lg bg-emerald-700 px-4 py-2 font-medium text-white hover:bg-emerald-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-700 disabled:opacity-50">
        Volver a comprobar
      </button>
    </section>
  </main>
}
