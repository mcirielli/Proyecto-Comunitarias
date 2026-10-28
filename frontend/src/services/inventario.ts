import { api, write } from './http'
import type { SaldoStock, MovimientoStock, OrdenRecepcion } from '../types/inventario'
export const consultarDisponibilidadInventario = (): Promise<{ habilitado: boolean }> => api('/stock/disponibilidad')
export const consultarStock = (): Promise<SaldoStock[]> => api('/stock')
export const consultarMovimientos = (id: number, pagina: number): Promise<MovimientoStock[]> => api(`/stock/${id}/movimientos?pagina=${pagina}`)
export const consultarOrdenesRecepcion = (): Promise<OrdenRecepcion[]> => api('/compras')
export const recibirOrden = (id: number): Promise<OrdenRecepcion> => write(`/compras/${id}/recepcion`)
