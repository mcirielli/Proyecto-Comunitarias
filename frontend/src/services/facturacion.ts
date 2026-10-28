import { api, write } from './http'
export type Pago = { id: number; facturaId: number; claveSolicitud: string; fechaPago: string; importe: number; medioPago: string; referencia: string | null }
export type Factura = { id: number; pedidoId: number; numero: string; cliente: string; fechaEmision: string; importeTotal: number; estado: string; pagado: number; saldo: number; detalles: { productoId: number; producto: string; cantidad: number; precioUnitario: number; subtotal: number }[]; pagos: Pago[] }
export type Elegible = { id: number; cliente: string; total: number }
export const facturacion = {
  disponibilidad: () => api('/facturas/disponibilidad') as Promise<{habilitado: boolean; mensaje: string}>,
  listar: () => api('/facturas') as Promise<Factura[]>,
  elegibles: () => api('/facturas/contexto') as Promise<Elegible[]>,
  emitir: (pedidoId: number) => write('/facturas', JSON.stringify({ pedidoId }), true) as Promise<Factura>,
  pagar: (body: { facturaId: number; claveSolicitud: string; importe: string; medioPago: string; referencia: string }) => write('/pagos', JSON.stringify(body), true) as Promise<Pago>,
}
