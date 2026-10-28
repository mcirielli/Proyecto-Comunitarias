import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { facturacion } from '../services/facturacion'
import type { Factura, Elegible } from '../services/facturacion'

const dinero = (n: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n)
function PagoForm({ factura, busy, onPago }: { factura: Factura; busy: boolean; onPago: (body: { facturaId: number; claveSolicitud: string; importe: string; medioPago: string; referencia: string }) => Promise<boolean> }) {
  const clave = useRef(crypto.randomUUID())
  const firma = useRef('')
  async function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    const body = { facturaId: factura.id, importe: String(data.get('importe')), medioPago: String(data.get('medioPago')), referencia: String(data.get('referencia') ?? '') }
    const contenido = JSON.stringify(body)
    if (contenido !== firma.current) { clave.current = crypto.randomUUID(); firma.current = contenido }
    if (await onPago({ ...body, claveSolicitud: clave.current })) { clave.current = crypto.randomUUID(); firma.current = ''; form.reset() }
  }
  return <form onSubmit={enviar} className="mt-5"><fieldset disabled={busy}><legend className="font-semibold">Registrar pago manual</legend>
    <label>Importe ARS<input name="importe" className="field" type="number" required min="0.01" step="0.01" max={factura.saldo} /></label>
    <label>Medio de pago<select name="medioPago" className="field"><option value="EFECTIVO">Efectivo</option><option value="TRANSFERENCIA">Transferencia</option><option value="TARJETA">Tarjeta (registro manual)</option></select></label>
    <label>Referencia opcional<input className="field" name="referencia" maxLength={100} /></label>
    <p className="muted mb-4">Registrá únicamente un pago recibido. Esta acción no procesa dinero.</p>
    <button className="action">{busy ? 'Registrando…' : 'Registrar pago'}</button>
  </fieldset></form>
}
export function FacturacionPage({ operaciones }: { operaciones: boolean }) {
  const [facturas, setFacturas] = useState<Factura[]>([])
  const [elegibles, setElegibles] = useState<Elegible[]>([])
  const [habilitado, setHabilitado] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    let active = true; setLoading(true); setError('')
    async function cargar() {
      const state = await facturacion.disponibilidad()
      if (!active) return
      setHabilitado(state.habilitado)
      if (!state.habilitado) return
      const [rows, ctx] = await Promise.all([facturacion.listar(), operaciones ? facturacion.elegibles() : Promise.resolve([])])
      if (active) { setFacturas(rows); setElegibles(ctx) }
    }
    cargar().catch((e: Error) => { if (active) setError(e.message) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [attempt, operaciones])
  async function emitir(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError(''); setSuccess('')
    const data = new FormData(event.currentTarget)
    try { const f = await facturacion.emitir(Number(data.get('pedidoId'))); setSuccess('Factura ' + f.numero + ' disponible.'); setAttempt(n => n + 1) }
    catch (e) { setError((e as Error).message) } finally { setBusy(false) }
  }
  async function pagar(body: Parameters<typeof facturacion.pagar>[0]) {
    setBusy(true); setError(''); setSuccess('')
    try { const p = await facturacion.pagar(body); setSuccess('Pago #' + p.id + ' registrado.'); setAttempt(n => n + 1); return true }
    catch (e) { setError((e as Error).message); return false } finally { setBusy(false) }
  }
  return <>
    <div className="view-toolbar"><p className="muted">Comprobantes internos y pagos registrados. Sin validez fiscal.</p><button className="theme-switch" disabled={busy || loading} onClick={() => setAttempt(n => n + 1)}>Actualizar / Reintentar</button></div>
    {error && <p role="alert" className="notice error-text">{error}</p>}{success && <p role="status" className="notice accent">{success}</p>}
    {loading ? <p className="empty-state" role="status">Consultando facturación…</p> : !habilitado ? <p className="notice muted">Facturación no está habilitada en este entorno.</p> : <>
      {operaciones && <section className="surface form-panel"><h2>Emitir factura interna</h2>{elegibles.length === 0 ? <p className="muted">No hay pedidos entregados pendientes de facturación.</p> : <form onSubmit={emitir}><label>Pedido entregado<select className="field" name="pedidoId" required disabled={busy} defaultValue=""><option value="" disabled>Seleccionar pedido</option>{elegibles.map(p => <option key={p.id} value={p.id}>Pedido #{p.id} · {p.cliente} · {dinero(p.total)}</option>)}</select></label><button className="action" disabled={busy}>Emitir factura</button></form>}</section>}
      <section className="surface table-panel"><div className="panel-heading"><h2>{operaciones ? 'Facturas y pagos' : 'Consulta de facturas'}</h2></div>
        {facturas.length === 0 ? <p className="empty-state muted">Todavía no hay facturas para consultar.</p> : facturas.map(f => <article className="order-card" key={f.id}>
          <div className="view-toolbar"><div><h3 className="font-semibold">{f.numero} · {f.cliente}</h3><p className="muted">Pedido #{f.pedidoId} · {new Date(f.fechaEmision).toLocaleString('es-AR', { timeZone: 'America/Argentina/Buenos_Aires' })}</p></div><span className="state-badge">{f.estado}</span></div>
          <p className="notice">Total: {dinero(f.importeTotal)} · Pagado: {dinero(f.pagado)} · Saldo: {dinero(f.saldo)}</p>
          <details><summary className="accent">Detalle e historial de pagos</summary><div className="table-scroll mt-4"><table><thead><tr><th>Producto</th><th>Cantidad</th><th>Precio histórico</th><th>Subtotal</th></tr></thead><tbody>{f.detalles.map(d => <tr key={d.productoId}><td>{d.producto}</td><td>{d.cantidad}</td><td>{dinero(d.precioUnitario)}</td><td>{dinero(d.subtotal)}</td></tr>)}</tbody></table></div>
            <h4 className="font-semibold mt-5">Pagos registrados</h4>{f.pagos.length === 0 ? <p className="muted">Sin pagos registrados.</p> : <ul>{f.pagos.map(p => <li key={p.id} className="notice mt-3">Pago #{p.id} · {dinero(p.importe)} · {p.medioPago} · {new Date(p.fechaPago).toLocaleString('es-AR', { timeZone: 'America/Argentina/Buenos_Aires' })}{p.referencia && ' · ' + p.referencia}</li>)}</ul>}
          </details>
          {operaciones && f.estado === 'PENDIENTE' && f.saldo > 0 && <PagoForm factura={f} busy={busy} onPago={pagar} />}
        </article>)}
      </section>
    </>}
  </>
}
