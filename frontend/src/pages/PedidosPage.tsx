import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { api, write } from '../services/http'

type Producto = { id: number; nombre: string; precio: number }
type Contexto = { clientes: { id: number; nombre: string }[]; ofertas: { id: number; titulo: string; fechaInicio: string; fechaFin: string; productos: Producto[] }[] }
type Pedido = { id: number; cliente: string; fecha: string; estado: string; siguienteEstado: string | null; fechaDespacho: string | null; fechaEntrega: string | null; total: number; detalles: { productoId: number; producto: string; cantidad: number; precioUnitario: number; subtotal: number }[] }
const estados = ['RECIBIDO', 'CONFIRMADO', 'EN_PREPARACION', 'PREPARADO', 'DESPACHADO', 'ENTREGADO']
const label = (value: string) => value.replaceAll('_', ' ').toLocaleLowerCase('es-AR')
const money = (value: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(value)

function EstadoActions({ pedido, busy, onChange }: { pedido: Pedido; busy: boolean; onChange: (id: number, state: string) => void }) {
  const [destino, setDestino] = useState(pedido.siguienteEstado ?? '')
  useEffect(() => { setDestino(pedido.siguienteEstado ?? '') }, [pedido.siguienteEstado])
  if (!pedido.siguienteEstado) return <span className="muted">Estado final</span>
  return <div className="state-controls"><label>Destino<select className="field" value={destino} onChange={event => setDestino(event.target.value)}>{estados.map(estado => <option key={estado} value={estado}>{label(estado)}</option>)}</select></label><button className="action" disabled={busy} onClick={() => onChange(pedido.id, destino)}>Cambiar estado</button></div>
}

export function PedidosPage({ operaciones, ofertaInicial }: { operaciones: boolean; ofertaInicial?: number }) {
  const [contexto, setContexto] = useState<Contexto | null>(null)
  const [pedidos, setPedidos] = useState<Pedido[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [creating, setCreating] = useState(!!ofertaInicial)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [inventarioHabilitado, setInventarioHabilitado] = useState(false)
  useEffect(() => {
    if (!operaciones) return
    let active = true
    api('/stock/disponibilidad').then(estado => { if (active) setInventarioHabilitado(estado.habilitado) }).catch(() => {})
    return () => { active = false }
  }, [operaciones])
  const [attempt, setAttempt] = useState(0)
  const [ofertaId, setOfertaId] = useState(String(ofertaInicial ?? ''))
  const [cantidades, setCantidades] = useState<Record<string, number>>({})
  useEffect(() => {
    let active = true; setLoading(true)
    Promise.all([api('/pedidos/contexto'), api('/pedidos')]).then(([ctx, rows]) => {
      if (active) { setContexto(ctx); setPedidos(rows); setOfertaId(previous => ctx.ofertas.some((o: { id: number }) => String(o.id) === previous) ? previous : '') }
    }).catch((reason: Error) => { if (active) setError(reason.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [attempt])
  const oferta = contexto?.ofertas.find(o => String(o.id) === ofertaId)
  async function preparar() {
    setBusy(true); setError(''); setSuccess('')
    try { await write('/demo/pedidos'); setSuccess('Datos de ejemplo listos. El cliente demo ya puede realizar pedidos.'); setAttempt(n => n + 1) }
    catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  async function crear(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); setSuccess('')
    const detalles = Object.entries(cantidades).filter(([, cantidad]) => cantidad > 0).map(([productoId, cantidad]) => ({ productoId: Number(productoId), cantidad }))
    if (!detalles.length) { setError('Seleccioná al menos un producto con cantidad mayor a cero.'); return }
    const data = new FormData(event.currentTarget)
    setBusy(true)
    try {
      const pedido: Pedido = await write('/pedidos', JSON.stringify({ ofertaId: Number(ofertaId), ...(operaciones ? { clienteId: Number(data.get('clienteId')) } : {}), detalles }), true)
      setCreating(false); setCantidades({}); setSuccess('Pedido #' + pedido.id + ' registrado en estado recibido.'); setAttempt(n => n + 1)
    } catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  async function cambiar(id: number, estado: string) {
    setBusy(true); setError(''); setSuccess('')
    try { const pedido: Pedido = await write('/pedidos/' + id + '/estado', JSON.stringify({ estado }), true, 'PATCH'); setSuccess('Pedido #' + id + ': ' + label(pedido.estado) + '.'); setAttempt(n => n + 1) }
    catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  return <>
    <div className="view-toolbar"><p className="muted">{operaciones ? 'Seguimiento y ciclo de vida de los pedidos.' : 'Realizá un pedido y consultá su estado.'}</p><div className="form-actions">{operaciones && <button className="theme-switch" disabled={busy} onClick={preparar}>Preparar datos de ejemplo</button>}<button className="action" disabled={busy || !contexto?.ofertas.length} onClick={() => setCreating(!creating)}>{creating ? 'Cerrar formulario' : '+ Nuevo pedido'}</button></div></div>
    {error && <p role="alert" className="notice error-text">{error}</p>}{success && <p role="status" className="notice accent">{success}</p>}
    <div className="surface roadmap-panel mb-6"><p className="eyebrow">CICLO DEL PEDIDO</p><div className="state-timeline">{estados.map((estado, i) => <span key={estado}><b>{i + 1}</b> {label(estado)}</span>)}</div><p className="muted mt-4">{operaciones ? 'Cada estado controla cuál es el próximo paso permitido. Los saltos y retrocesos se rechazan.' : 'Operaciones actualizará el estado de tu pedido.'}</p><p className="muted mt-2">{operaciones ? inventarioHabilitado ? 'Iniciar preparación reserva todo el pedido; despachar consume su reserva una sola vez.' : 'El control de inventario está pendiente de habilitación en este entorno.' : 'Operaciones controla la preparación y entrega de tu pedido.'}</p></div>
    {!loading && contexto?.ofertas.length === 0 && <p className="notice muted">No hay ofertas publicadas vigentes. {operaciones ? 'Podés preparar datos de ejemplo para la demostración.' : 'Consultá a Operaciones.'}</p>}
    {creating && contexto && <section className="surface form-panel"><h2>Nuevo pedido</h2><form onSubmit={crear}>
      {operaciones && <label>Cliente<select className="field" name="clienteId" required defaultValue=""><option value="" disabled>Seleccionar cliente</option>{contexto.clientes.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}</select></label>}
      <label>Oferta<select className="field" value={ofertaId} onChange={event => {setOfertaId(event.target.value); setCantidades({})}} required><option value="">Seleccionar oferta</option>{contexto.ofertas.map(o => <option key={o.id} value={o.id}>{o.titulo} · {o.fechaInicio} al {o.fechaFin}</option>)}</select></label>
      <p className="muted mb-4">Los precios corresponden a la oferta y quedan guardados al registrar el pedido.</p>
      <div className="order-products">{oferta?.productos.map(producto => <label key={producto.id} className="order-product"><span>{producto.nombre}<small className="muted">{money(producto.precio)}</small></span><input className="field" aria-label={'Cantidad de ' + producto.nombre} type="number" min="0" max="2147483647" step="1" value={cantidades[producto.id] ?? 0} onChange={event => setCantidades(previous => ({...previous, [producto.id]: Number(event.target.value)}))} /></label>)}</div>
      <button className="action mt-5" disabled={busy || !oferta?.productos.length}>{busy ? 'Registrando…' : 'Registrar pedido'}</button>
    </form></section>}
    <section className="surface table-panel"><div className="panel-heading"><div><h2>{operaciones ? 'Pedidos registrados' : 'Mis pedidos'}</h2><p className="muted">Estado y detalle de cada operación</p></div><button className="theme-switch" disabled={busy || loading} onClick={() => {setError(''); setAttempt(n => n + 1)}}>Actualizar</button></div>
      {loading ? <p className="empty-state" role="status">Consultando pedidos…</p> : pedidos.length === 0 ? <p className="empty-state muted">Todavía no hay pedidos registrados.</p> : pedidos.map(pedido => <article className="order-card" key={pedido.id}><div className="view-toolbar"><div><h3 className="font-semibold">Pedido #{pedido.id} · {pedido.cliente}</h3><p className="muted text-sm mt-2">{new Date(pedido.fecha).toLocaleString('es-AR')} · {money(pedido.total)}</p></div><span className="state-badge status-available capitalize">{label(pedido.estado)}</span></div>
        <details><summary className="accent">Ver productos y precios</summary><div className="table-scroll mt-4"><table><thead><tr><th>Producto</th><th>Cantidad</th><th>Precio histórico</th><th>Subtotal</th></tr></thead><tbody>{pedido.detalles.map(d => <tr key={d.productoId}><td>{d.producto}</td><td>{d.cantidad}</td><td>{money(d.precioUnitario)}</td><td>{money(d.subtotal)}</td></tr>)}</tbody></table></div></details>
        {pedido.fechaDespacho && <p className="muted text-sm mt-3">Despachado: {new Date(pedido.fechaDespacho).toLocaleString('es-AR')}</p>}{pedido.fechaEntrega && <p className="muted text-sm mt-2">Entregado: {new Date(pedido.fechaEntrega).toLocaleString('es-AR')}</p>}
        {operaciones && <div className="mt-5"><p className="muted text-sm">Siguiente permitido: {pedido.siguienteEstado ? label(pedido.siguienteEstado) : 'ninguno, pedido finalizado'}</p><EstadoActions key={pedido.estado} pedido={pedido} busy={busy} onChange={cambiar} /></div>}
      </article>)}
    </section>
  </>
}
