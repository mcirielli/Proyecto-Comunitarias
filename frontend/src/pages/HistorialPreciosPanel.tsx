import { useEffect, useState } from 'react'
import { consultarHistorialPrecios } from '../services/precios'
import type { HistorialPrecios } from '../services/precios'

const dinero = (n: number | null) => n == null ? '—' : new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n)
const dia = (iso: string) => new Date(iso.length === 10 ? iso + 'T12:00:00' : iso).toLocaleDateString('es-AR', { timeZone: 'America/Argentina/Buenos_Aires' })
const estados: Record<string, string> = { PENDIENTE_AUTORIZACION: 'Pendiente de autorización', AUTORIZADA: 'Autorizada', EMITIDA: 'Emitida', PENDIENTE: 'Pendiente', RECIBIDA: 'Recibida', CANCELADA: 'Cancelada' }

function Variacion({ valor }: { valor: number | null }) {
  if (valor == null) return <span className="muted">—</span>
  const signo = valor > 0 ? '▲ +' : valor < 0 ? '▼ ' : ''
  return <span>{signo}{valor.toLocaleString('es-AR')}%</span>
}

export function HistorialPreciosPanel({ productoId, onClose }: { productoId: number; onClose: () => void }) {
  const [data, setData] = useState<HistorialPrecios | null>(null)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    let active = true; setError(''); setData(null)
    consultarHistorialPrecios(productoId).then(r => { if (active) setData(r) }).catch((e: Error) => { if (active) setError(e.message) })
    return () => { active = false }
  }, [productoId, attempt])
  return <section className="surface table-panel" aria-label="Historial de precios" style={{ marginBottom: '1.8rem' }}>
    <div className="panel-heading"><div><h2>Historial de precios{data ? ' · ' + data.producto : ''}</h2><p className="muted">Precios de venta por oferta semanal y de compra por orden. Solo lectura.</p></div>
      <div style={{ display: 'flex', gap: '.6rem' }}><button className="theme-switch" onClick={() => setAttempt(n => n + 1)}>Actualizar</button><button className="theme-switch" onClick={onClose}>Cerrar</button></div></div>
    {error ? <p className="notice error-text" role="alert">{error}</p> : !data ? <p className="empty-state" role="status">Consultando precios…</p> : <>
      <div className="stat-grid" style={{ padding: '1.5rem 1.5rem 0', marginBottom: '.5rem' }}>
        <article className="surface stat-card"><div className="stat-label"><p>Precio de venta vigente</p></div><p className="stat-value">{dinero(data.precioVentaVigente)}</p><p className="muted text-sm">{data.precioVentaVigente == null ? 'Sin oferta vigente hoy' : 'Por ' + data.unidadMedida}</p></article>
        <article className="surface stat-card"><div className="stat-label"><p>Último precio de compra</p></div><p className="stat-value">{dinero(data.ultimoPrecioCompra)}</p><p className="muted text-sm">{data.compras.length} órdenes con este producto</p></article>
        <article className="surface stat-card"><div className="stat-label"><p>Proveedores vinculados</p></div><p className="stat-value">{data.proveedores.length}</p><p className="muted text-sm">{data.proveedores.map(p => p.proveedor + ': ' + dinero(p.precioCompra)).join(' · ') || 'Sin vínculos registrados'}</p></article>
      </div>
      <h3 className="font-semibold" style={{ padding: '1rem 1.5rem .5rem' }}>Precios de venta</h3>
      {data.ventas.length === 0 ? <p className="empty-state muted">El producto todavía no figura en ofertas publicadas.</p> :
        <div className="table-scroll"><table><thead><tr><th>Oferta</th><th>Vigencia</th><th>Precio</th><th>Variación</th></tr></thead>
          <tbody>{data.ventas.map(v => <tr key={v.ofertaId}><td>{v.oferta}</td><td className="muted">{dia(v.desde)} – {dia(v.hasta)}</td><td>{dinero(v.precio)}</td><td><Variacion valor={v.variacion} /></td></tr>)}</tbody></table></div>}
      <h3 className="font-semibold" style={{ padding: '1.5rem 1.5rem .5rem' }}>Precios de compra</h3>
      {data.compras.length === 0 ? <p className="empty-state muted">El producto todavía no figura en órdenes de compra.</p> :
        <div className="table-scroll"><table><thead><tr><th>Orden</th><th>Fecha</th><th>Proveedor</th><th>Estado</th><th>Cantidad</th><th>Precio unitario</th><th>Variación</th></tr></thead>
          <tbody>{data.compras.map(c => <tr key={c.ordenId}><td>#{c.ordenId}</td><td className="muted">{dia(c.fecha)}</td><td>{c.proveedor}</td><td><span className="state-badge">{estados[c.estado] ?? c.estado}</span></td><td>{c.cantidad}</td><td>{dinero(c.precio)}</td><td><Variacion valor={c.variacion} /></td></tr>)}</tbody></table></div>}
    </>}
  </section>
}
