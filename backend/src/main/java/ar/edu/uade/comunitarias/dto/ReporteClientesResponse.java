package ar.edu.uade.comunitarias.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Reporte gerencial de movimientos por cliente para un período opcional. */
public record ReporteClientesResponse(
        LocalDate desde,
        LocalDate hasta,
        boolean incluyeFacturacion,
        Totales totales,
        List<ClienteFila> clientes,
        List<Movimiento> movimientos) {

    public record Totales(int clientes, int pedidos, int entregados, long unidades,
                          BigDecimal totalPedidos, BigDecimal facturado, BigDecimal cobrado, BigDecimal saldo) {}

    public record ClienteFila(Long clienteId, String cliente, int pedidos, int entregados, long unidades,
                              BigDecimal totalPedidos, BigDecimal ticketPromedio,
                              BigDecimal facturado, BigDecimal cobrado, BigDecimal saldo, Instant ultimoPedido) {}

    public record Movimiento(Long pedidoId, Instant fecha, Long clienteId, String cliente, String estado,
                             long unidades, BigDecimal total, String factura, String estadoFactura,
                             BigDecimal facturado, BigDecimal cobrado) {}
}
