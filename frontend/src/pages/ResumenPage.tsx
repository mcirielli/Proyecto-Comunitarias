import { useEffect, useState } from 'react'
import { api } from '../services/http'
import type { Maestro } from '../types/session'
import { NavIcon } from '../components/NavIcon'
export function ResumenPage({ operaciones, navigate }: { operaciones: boolean; navigate: (view: string) => void }) {
  const [data, setData] = useState<{ productos: Maestro[]; proveedores: Maestro[] } | null>(null)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    if (!operaciones) return
    let active = true; setError(''); setData(null)
    Promise.all([api('/productos'), api('/proveedores')]).then(([productos, proveedores]) => { if (active) setData({ productos, proveedores }) })
      .catch((reason: Error) => { if (active) setError(reason.message) })
    return () => { active = false }
  }, [operaciones, attempt])
  if (!operaciones) return <section className="surface empty-state"><h2>Tu espacio de Comunitarias</h2><p className="muted mt-3">Tu sesión está activa. Las vistas comerciales de tu perfil se incorporarán en el próximo incremento.</p></section>
  return <>
    {error && <div className="notice error-text" role="alert">{error} <button className="row-action" onClick={() => setAttempt(value => value + 1)}>Reintentar</button></div>}
    <div className="stat-grid">{[
      { label: 'Productos registrados', value: data?.productos.length, icon: 'productos' as const, detail: data ? `${data.productos.filter(p => p.activo).length} activos en catálogo` : 'Consultando catálogo' },
      { label: 'Proveedores registrados', value: data?.proveedores.length, icon: 'proveedores' as const, detail: data ? `${data.proveedores.filter(p => p.activo).length} proveedores activos` : 'Consultando directorio' },
      { label: 'Módulos disponibles', value: 2, icon: 'resumen' as const, detail: 'Productos y proveedores' },
    ].map(stat => <article className="surface stat-card" key={stat.label}><div className="stat-label"><p>{stat.label}</p><span className="stat-icon"><NavIcon name={stat.icon} /></span></div><p className="stat-value">{stat.value ?? '—'}</p><p className="muted text-sm">{error && stat.icon !== 'resumen' ? 'Sin datos disponibles' : stat.detail}</p></article>)}</div>
    <section className="surface table-panel"><div className="panel-heading"><div><h2>Tu operación, en un solo lugar</h2><p className="muted">Accesos a los módulos disponibles</p></div></div><div className="quick-grid"><button className="quick-card" onClick={() => navigate('productos')}><NavIcon name="productos" /><strong>Productos</strong><span className="muted">Consultá el catálogo y administrá sus registros.</span><span className="accent">Ir a productos →</span></button><button className="quick-card" onClick={() => navigate('proveedores')}><NavIcon name="proveedores" /><strong>Proveedores</strong><span className="muted">Organizá tu directorio de abastecimiento.</span><span className="accent">Ir a proveedores →</span></button></div></section>
    <section className="surface roadmap-panel"><span className="eyebrow">PRÓXIMOS MÓDULOS</span><h2>Venta semanal y seguimiento de pedidos</h2><p className="muted">Oferta, pedidos y compras se incorporarán en las siguientes etapas. Sus indicadores estarán disponibles cuando existan operaciones reales.</p></section>
  </>
}
