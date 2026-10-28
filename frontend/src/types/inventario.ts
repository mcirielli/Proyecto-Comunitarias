export interface SaldoStock { productoId: number; producto: string; disponible: number; reservado: number; existencia: number }
export interface MovimientoStock { id: number; productoId: number; ordenId: number | null; pedidoId: number | null; tipo: string; cantidad: number; fecha: string }
export interface OrdenRecepcion { id: number; estado: string; tipo: string; detalles: { productoId: number; cantidad: number }[] }
