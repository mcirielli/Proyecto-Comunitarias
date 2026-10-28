import { api, write } from './http'

export type Compra = {
  id: number; proveedor: string; tipo: string; estado: string; fecha: string; total: number
  solicitadaPor: string; autorizadaPor: string | null; fechaAutorizacion: string | null
  emitidaPor: string | null; fechaEmision: string | null
  detalles: { productoId: number; producto: string; cantidad: number; precioUnitario: number; subtotal: number }[]
}
export type ContextoCompras = {
  proveedores: { id: number; nombre: string; productos: { id: number; nombre: string; precio: number }[] }[]
  pedidos: { id: number; cliente: string }[]
}
export type SolicitudCompra = {
  claveSolicitud: string; proveedorId: number; pedidos: number[]
  productos: { productoId: number; cantidad: number }[]
}
export const compras = {
  disponibilidad: (): Promise<{ habilitado: boolean; mensaje: string }> => api('/compras/disponibilidad'),
  listar: (): Promise<Compra[]> => api('/compras'),
  contexto: (): Promise<ContextoCompras> => api('/compras/contexto'),
  crear: (tipo: 'demanda' | 'quinteros', solicitud: SolicitudCompra): Promise<{ coberturaId: number | null; orden: Compra | null; mensaje: string }> =>
    write('/compras/' + tipo, JSON.stringify(solicitud), true),
  actuar: (id: number, accion: 'autorizar' | 'emitir'): Promise<Compra> => write(`/compras/${id}/${accion}`),
}
