import { useEffect, useRef, useState } from 'react'
import type { SaldoStock, MovimientoStock, OrdenRecepcion } from '../types/inventario'
import { consultarDisponibilidadInventario, consultarStock, consultarMovimientos, consultarOrdenesRecepcion, recibirOrden } from '../services/inventario'
import './inventario.css'

export function StockPage({ operaciones }: { operaciones: boolean }) {
  const [stock, setStock] = useState<SaldoStock[]>([])
  const [ordenes, setOrdenes] = useState<OrdenRecepcion[]>([])
  const [movimientos, setMovimientos] = useState<MovimientoStock[]>([])
  const [producto, setProducto] = useState<number>()
  const [pagina, setPagina] = useState(0)
  const [cargando, setCargando] = useState(true)
  const [ocupado, setOcupado] = useState(false)
  const [cargandoMovimientos, setCargandoMovimientos] = useState(false)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [revision, setRevision] = useState(0)
  const [habilitado, setHabilitado] = useState(false)
  const [pendiente, setPendiente] = useState<OrdenRecepcion | null>(null)
  const confirmacion = useRef<HTMLDialogElement>(null)
  useEffect(() => {
    const dialogo = confirmacion.current
    if (!dialogo) return
    if (pendiente && !dialogo.open) dialogo.showModal()
    if (!pendiente && dialogo.open) dialogo.close()
  }, [pendiente])
  useEffect(() => {
    let vigente = true
    setCargando(true); setError('')
    consultarDisponibilidadInventario().then(async estado => {
      if (!vigente) return
      setHabilitado(estado.habilitado)
      if (!estado.habilitado) return
      const [saldos, compras] = await Promise.all([consultarStock(), consultarOrdenesRecepcion()])
      if (vigente) { setStock(saldos); setOrdenes(compras) }
    })
      .catch((e: Error) => { if (vigente) setError(e.message) })
      .finally(() => { if (vigente) setCargando(false) })
    return () => { vigente = false }
  }, [revision])
  useEffect(() => {
    if (producto === undefined) return
    let vigente = true
    setCargandoMovimientos(true); setMovimientos([])
    consultarMovimientos(producto, pagina).then(datos => { if (vigente) setMovimientos(datos) })
      .catch((e: Error) => { if (vigente) setError(e.message) })
      .finally(() => { if (vigente) setCargandoMovimientos(false) })
    return () => { vigente = false }
  }, [producto, pagina, revision])
  async function recibir(orden: OrdenRecepcion) {
    setPendiente(null)
    setOcupado(true); setError(''); setExito('')
    try { await recibirOrden(orden.id); setExito(`Recepción de orden #${orden.id} registrada.`); setRevision(r => r + 1) }
    catch (e) { setError((e as Error).message) }
    finally { setOcupado(false) }
  }
  return <div className="inventario-module" aria-busy={cargando || ocupado}><section className="surface inventario-panel">
    <h2>Inventario y recepción</h2>
    <p>Disponible es mercadería libre. Reservado pertenece a pedidos en preparación. La reserva se realiza al iniciar la preparación desde Pedidos.</p>
    {error && <p role="alert" className="notice error-text">{error} <button className="action" disabled={ocupado} onClick={() => setRevision(r => r + 1)}>Reintentar</button></p>}
    {exito && <p role="status" className="notice">{exito}</p>}
    {cargando ? <p role="status">Cargando inventario…</p> : !error && !habilitado ? <p role="status">Inventario pendiente de habilitación. Las operaciones estarán disponibles cuando se complete su preparación.</p> : !error && <>
      {stock.length === 0 ? <p>No hay productos para consultar.</p> : <div className="inventario-table"><table><caption>Saldos actuales por producto</caption><thead><tr><th>Producto</th><th>Disponible</th><th>Reservado</th><th>Existencia física</th><th>Historial</th></tr></thead><tbody>{stock.map(s => <tr key={s.productoId}><td>{s.producto}</td><td>{s.disponible}</td><td>{s.reservado}</td><td>{s.existencia}</td><td><button className="action" onClick={() => { setProducto(s.productoId); setPagina(0) }}>Ver movimientos de {s.producto}</button></td></tr>)}</tbody></table></div>}
    </>}</section>
    {!cargando && !error && habilitado && <>
      {operaciones && <section className="surface inventario-panel"><h3>Recibir mercadería</h3><p>Solo órdenes emitidas. Recepción total, sin parciales ni excedentes. <a href="#compras">Consultar compras</a></p>{ordenes.filter(o => o.estado === 'EMITIDA').length === 0 ? <p>No hay órdenes elegibles.</p> : <ul className="inventario-orders">{ordenes.filter(o => o.estado === 'EMITIDA').map(o => <li className="inventario-order" key={o.id}><div><strong>Orden #{o.id} · {o.tipo.replaceAll('_', ' ')}</strong><p>{o.detalles.map(d => `${d.producto}: ${d.cantidad} unidades`).join(' · ')}</p></div><button className="action" disabled={ocupado} onClick={() => setPendiente(o)}>Recibir totalidad</button></li>)}</ul>}</section>}
      {producto !== undefined && <section className="surface inventario-panel"><h3>Movimientos del producto #{producto}</h3>{cargandoMovimientos ? <p role="status">Cargando movimientos…</p> : movimientos.length === 0 ? <p>No hay movimientos en esta página.</p> : <div className="inventario-table"><table><caption>Historial de ingresos, reservas y egresos</caption><thead><tr><th>Fecha</th><th>Tipo</th><th>Cantidad</th><th>Referencia</th></tr></thead><tbody>{movimientos.map(m => <tr key={m.id}><td>{new Date(m.fecha).toLocaleString('es-AR', { timeZone: 'America/Argentina/Buenos_Aires' })}</td><td>{m.tipo}</td><td>{m.cantidad}</td><td>{m.ordenId !== null ? `Orden #${m.ordenId}` : `Pedido #${m.pedidoId}`}</td></tr>)}</tbody></table></div>}<div className="inventario-pagination"><button className="action" disabled={pagina === 0 || cargandoMovimientos} onClick={() => setPagina(p => p - 1)}>Anterior</button><span> Página {pagina + 1} </span><button className="action" disabled={movimientos.length < 100 || cargandoMovimientos} onClick={() => setPagina(p => p + 1)}>Siguiente</button></div></section>}
    </>}
    <dialog ref={confirmacion} className="surface inventario-confirmacion" aria-labelledby="confirmar-recepcion" onCancel={e => { e.preventDefault(); setPendiente(null) }}>
      <h2 id="confirmar-recepcion">Confirmar recepción total</h2>
      {pendiente && <><p>Orden #{pendiente.id}. Se registrará el ingreso completo de:</p><ul>{pendiente.detalles.map(d => <li key={d.productoId}>{d.producto}: {d.cantidad} unidades</li>)}</ul><div className="inventario-pagination"><button className="theme-switch" onClick={() => setPendiente(null)}>Cancelar</button><button className="action" disabled={ocupado} onClick={() => recibir(pendiente)}>Confirmar recepción</button></div></>}
    </dialog>
  </div>
}
