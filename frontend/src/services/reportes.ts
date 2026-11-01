import { api } from './http'

export type ClienteFila = { clienteId: number; cliente: string; pedidos: number; entregados: number; unidades: number; totalPedidos: number; ticketPromedio: number; facturado: number; cobrado: number; saldo: number; ultimoPedido: string | null }
export type MovimientoCliente = { pedidoId: number; fecha: string; clienteId: number; cliente: string; estado: string; unidades: number; total: number; factura: string | null; estadoFactura: string | null; facturado: number; cobrado: number }
export type ReporteClientes = {
  desde: string | null; hasta: string | null; incluyeFacturacion: boolean
  totales: { clientes: number; pedidos: number; entregados: number; unidades: number; totalPedidos: number; facturado: number; cobrado: number; saldo: number }
  clientes: ClienteFila[]; movimientos: MovimientoCliente[]
}
export type FiltroReporte = { desde?: string; hasta?: string; clienteId?: number }

export function consultarReporteClientes(filtro: FiltroReporte) {
  const params = new URLSearchParams()
  if (filtro.desde) params.set('desde', filtro.desde)
  if (filtro.hasta) params.set('hasta', filtro.hasta)
  if (filtro.clienteId) params.set('clienteId', String(filtro.clienteId))
  const query = params.toString()
  return api('/reportes/clientes' + (query ? '?' + query : '')) as Promise<ReporteClientes>
}
