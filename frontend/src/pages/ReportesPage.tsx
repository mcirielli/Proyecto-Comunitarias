import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { NavIcon } from '../components/NavIcon'
import { consultarReporteClientes } from '../services/reportes'
import type { FiltroReporte, ReporteClientes } from '../services/reportes'
import './reportes.css'

const dinero = (n: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(n)
const fecha = (iso: string | null) => iso ? new Date(iso).toLocaleDateString('es-AR', { timeZone: 'America/Argentina/Buenos_Aires' }) : '—'
const estados: Record<string, string> = { RECIBIDO: 'Recibido', CONFIRMADO: 'Confirmado', EN_PREPARACION: 'En preparación', PREPARADO: 'Preparado', DESPACHADO: 'Despachado', ENTREGADO: 'Entregado' }

function csv(reporte: ReporteClientes) {
  const celda = (v: string | number | null) => '"' + String(v ?? '').replace(/"/g, '""') + '"'
  const filas = [['Cliente', 'Pedidos', 'Entregados', 'Unidades', 'Total pedidos', 'Ticket promedio', 'Facturado', 'Cobrado', 'Saldo', 'Último pedido'],
    ...reporte.clientes.map(c => [c.cliente, c.pedidos, c.entregados, c.unidades, c.totalPedidos, c.ticketPromedio, c.facturado, c.cobrado, c.saldo, fecha(c.ultimoPedido)])]
  const blob = new Blob(['﻿' + filas.map(f => f.map(celda).join(';')).join('\n')], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'reporte-clientes.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

export function ReportesPage() {
  const [filtro, setFiltro] = useState<FiltroReporte>({})
  const [reporte, setReporte] = useState<ReporteClientes | null>(null)
  const [clientes, setClientes] = useState<{ id: number; nombre: string }[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    let active = true; setLoading(true); setError('')
    consultarReporteClientes(filtro).then(data => {
      if (!active) return
      setReporte(data)
      if (!filtro.clienteId) setClientes(prev => {
        const mapa = new Map(prev.map(c => [c.id, c.nombre]))
        data.clientes.forEach(c => mapa.set(c.clienteId, c.cliente))
        return [...mapa].map(([id, nombre]) => ({ id, nombre })).sort((a, b) => a.nombre.localeCompare(b.nombre))
      })
    }).catch((e: Error) => { if (active) setError(e.message) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [filtro, attempt])
  function aplicar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    const desde = String(data.get('desde') ?? ''), hasta = String(data.get('hasta') ?? ''), cliente = Number(data.get('clienteId') ?? 0)
    if (desde && hasta && desde > hasta) { setError('La fecha desde no puede ser posterior a la fecha hasta.'); return }
    setFiltro({ desde: desde || undefined, hasta: hasta || undefined, clienteId: cliente || undefined })
  }
  const t = reporte?.totales
  return <>
    <div className="view-toolbar"><p className="muted">Movimientos y ventas por cliente. Información de solo lectura para Gerencia.</p>
      <div style={{ display: 'flex', gap: '.6rem' }}>
        <button className="theme-switch" disabled={!reporte || reporte.clientes.length === 0} onClick={() => reporte && csv(reporte)}>Exportar CSV</button>
        <button className="theme-switch" disabled={loading} onClick={() => setAttempt(n => n + 1)}>Actualizar</button>
      </div></div>
    <section className="surface form-panel"><h2>Filtros</h2>
      <form onSubmit={aplicar} className="form-grid reportes-filtros" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))' }}>
        <label>Desde<input className="field" type="date" name="desde" defaultValue={filtro.desde} /></label>
        <label>Hasta<input className="field" type="date" name="hasta" defaultValue={filtro.hasta} /></label>
        <label>Cliente<select className="field" name="clienteId" defaultValue={filtro.clienteId ?? ''}><option value="">Todos los clientes</option>{clientes.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}</select></label>
        <div className="form-actions"><button className="action" disabled={loading}>Aplicar filtros</button><button type="reset" className="row-action" onClick={() => setFiltro({})}>Limpiar</button></div>
      </form></section>
    {error && <p role="alert" className="notice error-text">{error}</p>}
    {loading && !reporte ? <p className="empty-state" role="status">Generando reporte…</p> : reporte && t && <>
      <div className="stat-grid">{[
        { label: 'Ventas en pedidos', value: dinero(t.totalPedidos), detail: `${t.pedidos} pedidos · ${t.entregados} entregados`, icon: 'pedidos' as const },
        { label: 'Clientes con movimientos', value: String(t.clientes), detail: `${t.unidades} unidades pedidas`, icon: 'resumen' as const },
        reporte.incluyeFacturacion
          ? { label: 'Saldo pendiente de cobro', value: dinero(t.saldo), detail: `Facturado ${dinero(t.facturado)} · Cobrado ${dinero(t.cobrado)}`, icon: 'reportes' as const }
          : { label: 'Facturación', value: '—', detail: 'Módulo no habilitado en este entorno', icon: 'reportes' as const },
      ].map(s => <article className="surface stat-card" key={s.label}><div className="stat-label"><p>{s.label}</p><span className="stat-icon"><NavIcon name={s.icon} /></span></div><p className="stat-value">{s.value}</p><p className="muted text-sm">{s.detail}</p></article>)}</div>
      <section className="surface table-panel"><div className="panel-heading"><div><h2>Resumen por cliente</h2><p className="muted">Ordenado por monto total de pedidos</p></div></div>
        {reporte.clientes.length === 0 ? <p className="empty-state muted">No hay pedidos para el período seleccionado.</p> :
          <div className="table-scroll"><table><thead><tr><th>Cliente</th><th>Pedidos</th><th>Entregados</th><th>Unidades</th><th>Total pedidos</th><th>Ticket promedio</th>{reporte.incluyeFacturacion && <><th>Facturado</th><th>Cobrado</th><th>Saldo</th></>}<th>Último pedido</th></tr></thead>
            <tbody>{reporte.clientes.map(c => <tr key={c.clienteId}><td>{c.cliente}</td><td>{c.pedidos}</td><td>{c.entregados}</td><td>{c.unidades}</td><td>{dinero(c.totalPedidos)}</td><td>{dinero(c.ticketPromedio)}</td>{reporte.incluyeFacturacion && <><td>{dinero(c.facturado)}</td><td>{dinero(c.cobrado)}</td><td>{dinero(c.saldo)}</td></>}<td>{fecha(c.ultimoPedido)}</td></tr>)}</tbody></table></div>}
      </section>
      <section className="surface table-panel" style={{ marginTop: '1.8rem' }}><div className="panel-heading"><div><h2>Movimientos</h2><p className="muted">Pedidos del período, del más reciente al más antiguo</p></div></div>
        {reporte.movimientos.length === 0 ? <p className="empty-state muted">Sin movimientos.</p> :
          <div className="table-scroll"><table><thead><tr><th>Pedido</th><th>Fecha</th><th>Cliente</th><th>Estado</th><th>Unidades</th><th>Total</th>{reporte.incluyeFacturacion && <><th>Factura</th><th>Cobrado</th></>}</tr></thead>
            <tbody>{reporte.movimientos.map(m => <tr key={m.pedidoId}><td>#{m.pedidoId}</td><td>{fecha(m.fecha)}</td><td>{m.cliente}</td><td><span className="state-badge">{estados[m.estado] ?? m.estado}</span></td><td>{m.unidades}</td><td>{dinero(m.total)}</td>{reporte.incluyeFacturacion && <><td>{m.factura ? `${m.factura} · ${m.estadoFactura}` : 'Sin factura'}</td><td>{dinero(m.cobrado)}</td></>}</tr>)}</tbody></table></div>}
      </section>
    </>}
  </>
}
