import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import type { Session } from '../types/session'
import { api, write } from '../services/http'
import { ThemeToggle } from '../components/ThemeToggle'
import { NavIcon } from '../components/NavIcon'
import { LoginPage } from './LoginPage'
import { ProductosPage } from './ProductosPage'
import { ProveedoresPage } from './ProveedoresPage'
import { ResumenPage } from './ResumenPage'
import { PedidosPage } from './PedidosPage'
import { ComprasPage } from './ComprasPage'
import { PendientePage } from './PendientePage'

const titles: Record<string, string> = { resumen: 'Resumen general', productos: 'Productos', proveedores: 'Proveedores', oferta: 'Oferta semanal', pedidos: 'Pedidos', compras: 'Compras', reportes: 'Reportes' }
function currentView() { const value = location.hash.slice(1); return value in titles ? value : 'resumen' }
export function OperacionesPage() {
  const [session, setSession] = useState<Session | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [view, setView] = useState(currentView)
  useEffect(() => {
    let active = true
    api('/auth/session').then(user => { if (active) setSession(user) }).catch(() => {})
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])
  useEffect(() => { const changed = () => setView(currentView()); window.addEventListener('hashchange', changed); return () => window.removeEventListener('hashchange', changed) }, [])
  function navigate(value: string) { location.hash = value; setView(value) }
  async function login(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError('')
    const form = event.currentTarget; const data = new FormData(form)
    try { setSession(await write('/auth/login', new URLSearchParams({ email: String(data.get('email')), password: String(data.get('password')) }))); navigate('resumen') }
    catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  async function logout() {
    setBusy(true); setError('')
    try { await write('/auth/logout'); setSession(null); navigate('resumen') }
    catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  if (loading) return <main className="session-loading" role="status">Cargando Comunitarias…</main>
  if (!session) return <LoginPage busy={busy} error={error} onLogin={login} />
  const operaciones = session.rol === 'OPERACIONES'
  const allowed = operaciones || (!['productos', 'proveedores'].includes(view) && !(view === 'compras' && session.rol === 'CLIENTE'))
  const date = new Intl.DateTimeFormat('es-AR', { dateStyle: 'full', timeZone: 'America/Argentina/Buenos_Aires' }).format(new Date())
  const items = [ { id: 'resumen', label: 'Resumen', icon: 'resumen' as const },
    ...(operaciones ? [{ id: 'productos', label: 'Productos', icon: 'productos' as const }, { id: 'proveedores', label: 'Proveedores', icon: 'proveedores' as const }] : []),
    ...(session.rol !== 'GERENCIA' ? [{ id: 'oferta', label: 'Oferta semanal', icon: 'oferta' as const }, { id: 'pedidos', label: 'Pedidos', icon: 'pedidos' as const }] : []),
    ...(session.rol !== 'CLIENTE' ? [{ id: 'compras', label: 'Compras', icon: 'compras' as const }] : []),
    ...(session.rol === 'GERENCIA' ? [{ id: 'reportes', label: 'Reportes', icon: 'reportes' as const }] : []) ]
  return <div className="workspace"><aside className="sidebar"><div className="sidebar-brand"><span className="brand-mark">C</span><div><strong>Comunitarias</strong><p className="muted">Gestión mayorista</p></div></div><p className="menu-label">ESPACIO DE TRABAJO</p><nav aria-label="Menú principal">{items.map(item => <a href={'#' + item.id} key={item.id} aria-current={view === item.id ? 'page' : undefined} className="nav-item"><NavIcon name={item.icon} /><span>{item.label}</span>{['oferta', 'reportes'].includes(item.id) && <span className="soon-dot" title="Próximamente" aria-label="Próximamente" />}</a>)}</nav><div className="sidebar-user"><span className="avatar">{session.nombre.slice(0, 2).toUpperCase()}</span><div><strong>{session.nombre}</strong><p className="muted">{session.rol === 'OPERACIONES' ? 'Operaciones' : session.rol === 'GERENCIA' ? 'Gerencia' : 'Cliente'}</p></div><button className="logout-button" disabled={busy} onClick={logout} aria-label="Cerrar sesión" title="Cerrar sesión">↪</button></div></aside>
    <div className="workspace-main"><header className="workspace-header"><div><p className="muted date-label">{date}</p><h1>{titles[view]}</h1><p className="muted">{view === 'resumen' ? 'Vista general de tu operación mayorista' : 'Tu espacio de gestión en Comunitarias'}</p></div><ThemeToggle /></header><main className="workspace-content" key={view}>
      {error && <p role="alert" className="notice error-text">{error}</p>}
      {!allowed ? <section className="surface empty-state">Tu perfil no tiene acceso a este módulo.</section> : view === 'resumen' ? <ResumenPage operaciones={operaciones} navigate={navigate} /> : view === 'productos' ? <ProductosPage /> : view === 'proveedores' ? <ProveedoresPage /> : view === 'pedidos' && session.rol !== 'GERENCIA' ? <PedidosPage operaciones={operaciones} /> : view === 'compras' && session.rol !== 'CLIENTE' ? <ComprasPage gerencia={session.rol === 'GERENCIA'} /> : <PendientePage title={titles[view]} />}
    </main><footer className="workspace-footer">Comunitarias · Proyecto UADE <span>Gestión mayorista</span></footer></div></div>
}
