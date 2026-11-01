package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.ReporteService;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ar.edu.uade.comunitarias.FacturacionFixture.*;

class ReporteServiceTest {
    final PedidoRepository pedidos = mock(PedidoRepository.class);
    final FacturaRepository facturas = mock(FacturaRepository.class);
    final PagoRepository pagos = mock(PagoRepository.class);
    final ReporteService service = new ReporteService(pedidos, facturas, pagos, true);

    final Cliente kiosco = cliente(1L, "Kiosco Norte");
    final Cliente almacen = cliente(2L, "Almacén Sur");
    final Producto arroz = producto(10L, "Arroz");
    final Producto fideos = producto(11L, "Fideos");

    static Cliente cliente(Long id, String nombre) {
        var c = new Cliente(nombre); ReflectionTestUtils.setField(c, "id", id); return c;
    }
    static Producto producto(Long id, String nombre) {
        var p = new Producto(nombre, null, null, "kg"); ReflectionTestUtils.setField(p, "id", id); return p;
    }
    Pedido pedido(Long id, Cliente cliente, String fecha, boolean entregado, Object... lineas) {
        var oferta = new Oferta("Semana", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        var p = new Pedido(cliente, oferta, Instant.parse(fecha));
        ReflectionTestUtils.setField(p, "id", id);
        for (int i = 0; i < lineas.length; i += 3) p.agregar((Producto) lineas[i], (Integer) lineas[i + 1], new BigDecimal((String) lineas[i + 2]));
        if (entregado) for (var e : new EstadoPedido[]{EstadoPedido.CONFIRMADO, EstadoPedido.EN_PREPARACION,
                EstadoPedido.PREPARADO, EstadoPedido.DESPACHADO, EstadoPedido.ENTREGADO}) p.cambiarEstado(e, CLOCK.instant());
        return p;
    }

    @Test void soloGerenciaConsultaReportes() {
        for (var actor : List.of(OPS, CLI)) assertThrows(AccessDeniedException.class, () -> service.clientes(actor, null, null, null));
        assertThrows(AccessDeniedException.class, () -> service.clientes(null, null, null, null));
        verifyNoInteractions(pedidos, facturas, pagos);
    }

    @Test void agrupaMovimientosPorClienteConFacturadoYCobrado() {
        var p1 = pedido(1L, kiosco, "2026-10-20T15:00:00Z", true, arroz, 2, "100.00", fideos, 1, "50.00"); // 250
        var p2 = pedido(2L, kiosco, "2026-10-22T15:00:00Z", false, arroz, 1, "100.00");                    // 100
        var p3 = pedido(3L, almacen, "2026-10-21T15:00:00Z", true, fideos, 4, "50.00");                    // 200
        when(pedidos.findAllByOrderByIdDesc()).thenReturn(List.of(p3, p2, p1));
        var f1 = new Factura(p1, CLOCK.instant()); ReflectionTestUtils.setField(f1, "id", 7L);
        when(facturas.findByPedidoId(1L)).thenReturn(Optional.of(f1));
        when(pagos.totalFactura(7L)).thenReturn(new BigDecimal("150.00"));

        var r = service.clientes(GER, null, null, null);

        assertTrue(r.incluyeFacturacion());
        assertEquals(2, r.totales().clientes());
        assertEquals(3, r.totales().pedidos());
        assertEquals(2, r.totales().entregados());
        assertEquals(8, r.totales().unidades());
        assertEquals(0, new BigDecimal("550").compareTo(r.totales().totalPedidos()));
        assertEquals(0, new BigDecimal("250").compareTo(r.totales().facturado()));
        assertEquals(0, new BigDecimal("150").compareTo(r.totales().cobrado()));
        assertEquals(0, new BigDecimal("100").compareTo(r.totales().saldo()));

        var primero = r.clientes().get(0);
        assertEquals("Kiosco Norte", primero.cliente());
        assertEquals(2, primero.pedidos());
        assertEquals(1, primero.entregados());
        assertEquals(new BigDecimal("175.00"), primero.ticketPromedio());
        assertEquals(Instant.parse("2026-10-22T15:00:00Z"), primero.ultimoPedido());
        assertEquals("INT-PED-1", r.movimientos().stream().filter(m -> m.pedidoId() == 1L).findFirst().orElseThrow().factura());
        assertEquals("Almacén Sur", r.clientes().get(1).cliente());
    }

    @Test void filtraPeriodoEnHoraArgentinaYCliente() {
        // 2026-10-28T02:00Z equivale al 27/10 23:00 en Argentina.
        var tarde = pedido(1L, kiosco, "2026-10-28T02:00:00Z", false, arroz, 1, "10");
        var siguiente = pedido(2L, kiosco, "2026-10-28T04:00:00Z", false, arroz, 1, "10");
        var otro = pedido(3L, almacen, "2026-10-27T15:00:00Z", false, arroz, 1, "10");
        when(pedidos.findAllByOrderByIdDesc()).thenReturn(List.of(otro, siguiente, tarde));
        var dia27 = LocalDate.of(2026, 10, 27);

        var r = service.clientes(GER, dia27, dia27, null);
        assertEquals(Set.of(1L, 3L), new HashSet<>(r.movimientos().stream().map(m -> m.pedidoId()).toList()));

        var soloKiosco = service.clientes(GER, dia27, dia27, 1L);
        assertEquals(List.of(1L), soloKiosco.movimientos().stream().map(m -> m.pedidoId()).toList());
    }

    @Test void periodoInvertidoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> service.clientes(GER, LocalDate.of(2026, 10, 28), LocalDate.of(2026, 10, 27), null));
        verifyNoInteractions(pedidos);
    }

    @Test void sinFacturacionNoConsultaFacturasNiPagos() {
        var sinFacturacion = new ReporteService(pedidos, facturas, pagos, false);
        when(pedidos.findAllByOrderByIdDesc()).thenReturn(List.of(pedido(1L, kiosco, "2026-10-20T15:00:00Z", true, arroz, 1, "10")));
        var r = sinFacturacion.clientes(GER, null, null, null);
        assertFalse(r.incluyeFacturacion());
        assertEquals(0, BigDecimal.ZERO.compareTo(r.totales().facturado()));
        verifyNoInteractions(facturas, pagos);
    }

    @Test void facturaAnuladaNoSumaImportes() {
        var p1 = pedido(1L, kiosco, "2026-10-20T15:00:00Z", true, arroz, 1, "100");
        when(pedidos.findAllByOrderByIdDesc()).thenReturn(List.of(p1));
        var f = new Factura(p1, CLOCK.instant()); ReflectionTestUtils.setField(f, "id", 7L);
        ReflectionTestUtils.setField(f, "estado", EstadoFactura.ANULADA);
        when(facturas.findByPedidoId(1L)).thenReturn(Optional.of(f));
        var r = service.clientes(GER, null, null, null);
        assertEquals("ANULADA", r.movimientos().get(0).estadoFactura());
        assertEquals(0, BigDecimal.ZERO.compareTo(r.totales().facturado()));
        verify(pagos, never()).totalFactura(any());
    }
}
