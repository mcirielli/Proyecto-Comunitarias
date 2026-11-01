import { api } from './http'

export type PrecioVenta = { ofertaId: number; oferta: string; desde: string; hasta: string; precio: number; variacion: number | null }
export type PrecioCompra = { ordenId: number; fecha: string; proveedor: string; estado: string; tipo: string; cantidad: number; precio: number; variacion: number | null }
export type PrecioProveedor = { proveedorId: number; proveedor: string; precioCompra: number | null }
export type HistorialPrecios = {
  productoId: number; producto: string; unidadMedida: string
  precioVentaVigente: number | null; ultimoPrecioCompra: number | null
  ventas: PrecioVenta[]; compras: PrecioCompra[]; proveedores: PrecioProveedor[]
}

export const consultarHistorialPrecios = (productoId: number) => api('/productos/' + productoId + '/precios') as Promise<HistorialPrecios>
