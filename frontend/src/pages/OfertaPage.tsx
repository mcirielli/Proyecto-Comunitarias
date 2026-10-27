import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { api, write } from '../services/http'

type Producto = { id: number; nombre: string; activo: boolean }
type Oferta = { id: number; titulo: string; fechaInicio: string; fechaFin: string; publicada: boolean; estado: string; productos: { id: number; nombre: string; unidadMedida: string; precioVenta: number; activo: boolean }[] }
type Fila = { productoId: string; precioVenta: string }
const money = (value: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(value)

export function OfertaPage({ operaciones, onPedido }: { operaciones: boolean; onPedido: (id: number) => void }) {
  const [ofertas, setOfertas] = useState<Oferta[]>([])
  const [productos, setProductos] = useState<Producto[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [attempt, setAttempt] = useState(0)
  const [creating, setCreating] = useState(false)
  const [filas, setFilas] = useState<Fila[]>([{ productoId: '', precioVenta: '' }])

  useEffect(() => {
    let active = true
    setLoading(true); setError('')
    Promise.all([api('/ofertas'), operaciones ? api('/productos') : Promise.resolve([])])
      .then(([rows, masters]) => { if (active) { setOfertas(rows); setProductos(masters.filter((p: Producto) => p.activo)) } })
      .catch((reason: Error) => { if (active) setError(reason.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [attempt, operaciones])

  async function crear(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); setSuccess('')
    const data = new FormData(event.currentTarget)
    if (String(data.get('fechaFin')) < String(data.get('fechaInicio'))) { setError('La fecha fin debe ser igual o posterior al inicio.'); return }
    if (new Set(filas.map(f => f.productoId)).size !== filas.length) { setError('No repitas productos en la oferta.'); return }
    setBusy(true)
    try {
      await write('/ofertas', JSON.stringify({ titulo: data.get('titulo'), fechaInicio: data.get('fechaInicio'), fechaFin: data.get('fechaFin'), detalles: filas.map(f => ({ productoId: Number(f.productoId), precioVenta: f.precioVenta })) }), true)
      setCreating(false); setFilas([{ productoId: '', precioVenta: '' }]); setSuccess('Borrador guardado. Revisalo antes de publicar.'); setAttempt(n => n + 1)
    } catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  async function publicar(id: number) {
    setBusy(true); setError(''); setSuccess('')
    try { await write('/ofertas/' + id + '/publicacion'); setSuccess('Oferta publicada. Será visible al Cliente durante su vigencia.'); setAttempt(n => n + 1) }
    catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  function cambiar(index: number, campo: keyof Fila, value: string) {
    setFilas(previous => previous.map((fila, i) => i === index ? { ...fila, [campo]: value } : fila))
  }
  return <>
    <div className="view-toolbar"><p className="muted">{operaciones ? 'Armá borradores y publicá la oferta para tus clientes.' : 'Elegí una oferta vigente para realizar tu pedido.'}</p><div className="form-actions"><button className="theme-switch" disabled={busy || loading} onClick={() => setAttempt(n => n + 1)}>Actualizar / Reintentar</button>{operaciones && <button className="action" disabled={busy || loading || !!error} onClick={() => setCreating(!creating)}>{creating ? 'Cerrar formulario' : '+ Nueva oferta'}</button>}</div></div>
    {error && <p role="alert" className="notice error-text">{error}</p>}{success && <p role="status" className="notice accent">{success}</p>}
    {creating && operaciones && <section className="surface form-panel"><h2>Nuevo borrador</h2><form onSubmit={crear}>
      <label>Título<input className="field" name="titulo" required maxLength={255} disabled={busy} /></label>
      <label>Fecha inicio<input className="field" type="date" name="fechaInicio" required disabled={busy} /></label>
      <label>Fecha fin (inclusive)<input className="field" type="date" name="fechaFin" required disabled={busy} /></label>
      <p className="muted mb-4">Vigencia según hora de Buenos Aires. Solo productos activos y precios de venta.</p>
      {productos.length === 0 ? <p className="notice muted">No hay productos activos. Cargalos en Productos antes de crear una oferta.</p> : filas.map((fila, index) => <div className="order-product" key={index}>
        <label>Producto {index + 1}<select className="field" required disabled={busy} value={fila.productoId} onChange={e => cambiar(index, 'productoId', e.target.value)}><option value="">Seleccionar producto</option>{productos.map(p => <option key={p.id} value={p.id} disabled={filas.some((f, i) => i !== index && f.productoId === String(p.id))}>{p.nombre}</option>)}</select></label>
        <label>Precio de venta ARS<input className="field" type="number" min="0" max="9999999999.99" step="0.01" required disabled={busy} value={fila.precioVenta} onChange={e => cambiar(index, 'precioVenta', e.target.value)} /></label>
        <button type="button" className="theme-switch" disabled={busy || filas.length === 1} onClick={() => setFilas(previous => previous.filter((_, i) => i !== index))}>Quitar producto {index + 1}</button>
      </div>)}
      <div className="form-actions mt-5"><button type="button" className="theme-switch" disabled={busy || filas.length >= Math.min(100, productos.length)} onClick={() => setFilas(previous => [...previous, { productoId: '', precioVenta: '' }])}>Agregar producto</button><button className="action" disabled={busy || !productos.length}>{busy ? 'Guardando…' : 'Guardar borrador'}</button></div>
    </form></section>}
    <section className="surface table-panel"><div className="panel-heading"><h2>{operaciones ? 'Ofertas registradas' : 'Ofertas disponibles'}</h2></div>
      {loading ? <p className="empty-state" role="status">Consultando ofertas…</p> : error ? <p className="empty-state muted">No se pudo actualizar el catálogo. Reintentá la consulta.</p> : ofertas.length === 0 ? <p className="empty-state muted">{operaciones ? 'Todavía no hay ofertas. Creá un borrador para comenzar.' : 'No hay ofertas publicadas vigentes.'}</p> : ofertas.map(oferta => <article className="order-card" key={oferta.id}>
        <div className="view-toolbar"><div><h3 className="font-semibold">{oferta.titulo}</h3><p className="muted">{oferta.fechaInicio} al {oferta.fechaFin} (inclusive)</p></div><span className="state-badge">{oferta.publicada ? 'Publicada · ' : ''}{oferta.estado.toLocaleLowerCase('es-AR')}</span></div>
        <div className="table-scroll"><table><thead><tr><th>Producto</th><th>Unidad</th><th>Precio de venta</th></tr></thead><tbody>{oferta.productos.map(p => <tr key={p.id}><td>{p.nombre}{!p.activo && ' · Inactivo'}</td><td>{p.unidadMedida}</td><td>{money(p.precioVenta)}</td></tr>)}</tbody></table></div>
        {oferta.productos.length === 0 && <p className="notice muted">Esta oferta no tiene productos activos disponibles.</p>}
        <div className="form-actions mt-5">{operaciones && !oferta.publicada && <button className="action" disabled={busy} onClick={() => publicar(oferta.id)}>Publicar oferta</button>}{oferta.estado === 'VIGENTE' && <button className="action" disabled={busy || !oferta.productos.some(p => p.activo)} onClick={() => onPedido(oferta.id)}>Crear pedido con esta oferta</button>}</div>
        {operaciones && oferta.publicada && <p className="muted mt-3">La oferta publicada no se edita. Para cambiarla, creá otro borrador.</p>}
      </article>)}
    </section>
  </>
}
