import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { compras } from '../services/compras'
import type { Compra, ContextoCompras } from '../services/compras'
import './compras.css'

const dinero = (n: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n)
const fecha = (s: string) => new Date(s).toLocaleString('es-AR')
export function ComprasPage({ gerencia }: { gerencia: boolean }) {
  const [ordenes, setOrdenes] = useState<Compra[]>([])
  const [contexto, setContexto] = useState<ContextoCompras>({ proveedores: [], pedidos: [] })
  const [habilitado, setHabilitado] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [mensaje, setMensaje] = useState('')
  const [tipo, setTipo] = useState<'demanda' | 'quinteros'>('demanda')
  const [proveedor, setProveedor] = useState('')
  const [pedidos, setPedidos] = useState<number[]>([])
  const [cantidades, setCantidades] = useState<Record<number, string>>({})
  // Se conserva al fallar una respuesta para que reintentar no duplique una escritura.
  const clave = useRef(crypto.randomUUID())
  const firma = useRef('')
  async function cargar() {
    const estado = await compras.disponibilidad()
    setHabilitado(estado.habilitado)
    if (!estado.habilitado) { setMensaje(estado.mensaje); return }
    const [lista, ctx] = await Promise.all([compras.listar(), gerencia ? Promise.resolve(null) : compras.contexto()])
    setOrdenes(lista)
    if (ctx) setContexto(ctx)
  }
  useEffect(() => {
    let active = true
    cargar().catch(e => { if (active) setError((e as Error).message) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [gerencia])
  const seleccionado = contexto.proveedores.find(p => p.id === Number(proveedor))
  async function crear(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError(''); setMensaje('')
    const contenido = { proveedorId: Number(proveedor), pedidos: tipo === 'demanda' ? pedidos : [],
      productos: tipo === 'quinteros' ? Object.entries(cantidades).filter(([, c]) => c !== '').map(([id, c]) => ({ productoId: Number(id), cantidad: Number(c) })) : [] }
    const nuevaFirma = JSON.stringify({ tipo, ...contenido })
    if (firma.current !== nuevaFirma) { clave.current = crypto.randomUUID(); firma.current = nuevaFirma }
    try {
      const resultado = await compras.crear(tipo, { claveSolicitud: clave.current, ...contenido })
      setMensaje(resultado.mensaje + (resultado.orden ? ` · Orden #${resultado.orden.id}` : ` · Cobertura #${resultado.coberturaId}`))
      clave.current = crypto.randomUUID(); firma.current = ''; setPedidos([]); setCantidades({})
      await cargar()
    } catch (e) { setError((e as Error).message) }
    finally { setBusy(false) }
  }
  async function actuar(id: number, accion: 'autorizar' | 'emitir') {
    setBusy(true); setError(''); setMensaje('')
    try { const orden = await compras.actuar(id, accion); setMensaje(`Orden #${orden.id}: ${orden.estado}`); await cargar() }
    catch (e) { setError((e as Error).message) }
    finally { setBusy(false) }
  }
  if (loading) return <p role="status">Cargando compras…</p>
  return <div className="compras-module">
    <section className="surface compras-intro"><div><p className="menu-label">ABASTECIMIENTO</p><h2>{gerencia ? 'Autorización de compras' : 'Compras por demanda y Quinteros'}</h2>
      <p className="muted">{gerencia ? 'Revisá proveedor, cantidades y costos antes de autorizar. Operaciones emite la orden.' : 'Consolidá pedidos confirmados o solicitá una compra para stock.'}</p></div>
      <button className="theme-switch" disabled={busy} onClick={() => { setError(''); setBusy(true); cargar().catch(e => setError((e as Error).message)).finally(() => setBusy(false)) }}>Actualizar</button></section>
    {error && <p role="alert" className="notice error-text">{error}</p>}
    {mensaje && <p role="status" className="notice">{mensaje}</p>}
    {habilitado && <p className="notice">Circuito de demo: las coberturas se conservan. Recepción y liberación requieren integrar inventario.</p>}
    {habilitado && !gerencia && <section className="surface compras-form"><h2>Nueva operación</h2><form onSubmit={crear}>
      <fieldset disabled={busy}><legend>Origen de compra</legend><div className="compras-fields">
        <label>Tipo<select className="field" value={tipo} onChange={e => setTipo(e.target.value as typeof tipo)}><option value="demanda">Demanda de clientes</option><option value="quinteros">Stock Quinteros</option></select></label>
        <label>Proveedor<select className="field" required value={proveedor} onChange={e => { setProveedor(e.target.value); setCantidades({}) }}><option value="">Seleccionar proveedor</option>{contexto.proveedores.map(p => <option key={p.id} value={p.id}>{p.nombre}</option>)}</select></label></div>
      {tipo === 'demanda' ? <div className="compras-selection"><p>Pedidos confirmados sin cobertura previa. Se cubren completos con un único proveedor.</p>
        {contexto.pedidos.length === 0 && <p className="muted">No hay pedidos elegibles. Confirmá un pedido desde Pedidos.</p>}
        {contexto.pedidos.map(p => <label key={p.id}><input type="checkbox" checked={pedidos.includes(p.id)} onChange={e => setPedidos(e.target.checked ? [...pedidos, p.id] : pedidos.filter(id => id !== p.id))} /> Pedido #{p.id} · {p.cliente}</label>)}</div>
        : <div className="compras-selection"><p>Ingresá cantidades para los productos elegidos. Los precios se validan en el servidor.</p>
          {!seleccionado?.productos.length && <p className="muted">Seleccioná un proveedor con productos vinculados.</p>}
          {seleccionado?.productos.map(p => <label key={p.id} className="compras-quantity"><span>{p.nombre}<small className="muted">{dinero(p.precio)} por unidad</small></span><input className="field" aria-label={`Cantidad de ${p.nombre}`} type="number" min="1" max="2147483647" step="1" value={cantidades[p.id] ?? ''} onChange={e => setCantidades({ ...cantidades, [p.id]: e.target.value })} /></label>)}</div>}
      <button className="action" type="submit" disabled={!proveedor || (tipo === 'demanda' ? pedidos.length === 0 : !Object.values(cantidades).some(c => c !== ''))}>{busy ? 'Procesando…' : tipo === 'demanda' ? 'Cubrir demanda' : 'Solicitar autorización'}</button>
      </fieldset></form></section>}
    {habilitado && <section className="surface compras-list"><h2>Órdenes de compra</h2>{ordenes.length === 0 ? <p className="muted">Todavía no hay órdenes.</p> : ordenes.map(o => <article className="compra-card" key={o.id}><div className="compra-heading"><div><h3>Orden #{o.id} · {o.proveedor}</h3><p className="muted">{o.tipo === 'STOCK_QUINTEROS' ? 'Stock Quinteros' : 'Demanda de clientes'} · {fecha(o.fecha)}</p></div><strong>{o.estado.replaceAll('_', ' ')}</strong></div>
      <div className="compras-table"><table><thead><tr><th>Producto</th><th>Cantidad</th><th>Precio histórico</th><th>Subtotal</th></tr></thead><tbody>{o.detalles.map(d => <tr key={d.productoId}><td>{d.producto}</td><td>{d.cantidad}</td><td>{dinero(d.precioUnitario)}</td><td>{dinero(d.subtotal)}</td></tr>)}</tbody></table></div>
      <p className="compra-total">Total: {dinero(o.total)}</p><p className="muted">Solicitó: {o.solicitadaPor ?? 'Registro previo'}</p>
      {o.autorizadaPor && <p className="muted">Autorizó: {o.autorizadaPor} · {o.fechaAutorizacion && fecha(o.fechaAutorizacion)}</p>}
      {o.emitidaPor && <p className="muted">Emitió: {o.emitidaPor} · {o.fechaEmision && fecha(o.fechaEmision)}</p>}
      {gerencia && o.estado === 'PENDIENTE_AUTORIZACION' && <button className="theme-switch" disabled={busy} onClick={() => actuar(o.id, 'autorizar')}>Autorizar compra #{o.id}</button>}
      {!gerencia && o.estado === 'AUTORIZADA' && <button className="theme-switch" disabled={busy} onClick={() => actuar(o.id, 'emitir')}>Emitir compra #{o.id}</button>}
    </article>)}</section>}
  </div>
}
