package ar.edu.uade.comunitarias.service;

import ar.edu.uade.comunitarias.domain.DetallePedido;
import ar.edu.uade.comunitarias.domain.EstadoFactura;
import ar.edu.uade.comunitarias.domain.Pedido;
import ar.edu.uade.comunitarias.dto.ReporteClientesResponse;
import ar.edu.uade.comunitarias.dto.ReporteClientesResponse.ClienteFila;
import ar.edu.uade.comunitarias.dto.ReporteClientesResponse.Movimiento;
import ar.edu.uade.comunitarias.dto.ReporteClientesResponse.Totales;
import ar.edu.uade.comunitarias.dto.SessionResponse;
import ar.edu.uade.comunitarias.repository.FacturaRepository;
import ar.edu.uade.comunitarias.repository.PagoRepository;
import ar.edu.uade.comunitarias.repository.PedidoRepository;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer de reportería gerencial: solo lectura, sin efectos sobre pedidos, facturas ni pagos.
 * Los montos de facturación se incluyen únicamente cuando el módulo está habilitado.
 */
@Service @Profile("supabase") @Transactional(readOnly = true)
public class ReporteService {
    public static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final PedidoRepository pedidos;
    private final FacturaRepository facturas;
    private final PagoRepository pagos;
    private final boolean facturacion;

    /** Facturas/pagos son opcionales: en contextos sin esos repositorios el reporte omite facturación. */
    @Autowired
    public ReporteService(PedidoRepository pedidos, ObjectProvider<FacturaRepository> facturas,
                          ObjectProvider<PagoRepository> pagos,
                          @Value("${app.facturacion.enabled:false}") boolean facturacion) {
        this(pedidos, facturas.getIfAvailable(), pagos.getIfAvailable(), facturacion);
    }

    public ReporteService(PedidoRepository pedidos, FacturaRepository facturas, PagoRepository pagos, boolean facturacion) {
        this.pedidos = pedidos; this.facturas = facturas; this.pagos = pagos;
        this.facturacion = facturacion && facturas != null && pagos != null;
    }

    public ReporteClientesResponse clientes(SessionResponse actor, LocalDate desde, LocalDate hasta, Long clienteId) {
        if (actor == null || !"GERENCIA".equals(actor.rol())) throw new AccessDeniedException("Solo Gerencia consulta reportes");
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw new IllegalArgumentException("Período inválido: la fecha desde es posterior a la fecha hasta");
        Instant inicio = desde == null ? null : desde.atStartOfDay(ZONA).toInstant();
        Instant fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA).toInstant();

        List<Movimiento> movimientos = pedidos.findAllByOrderByIdDesc().stream()
                .filter(p -> inicio == null || !p.getFecha().isBefore(inicio))
                .filter(p -> fin == null || p.getFecha().isBefore(fin))
                .filter(p -> clienteId == null || clienteId.equals(p.getCliente().getId()))
                .map(this::movimiento)
                .toList();

        Map<Long, List<Movimiento>> porCliente = new LinkedHashMap<>();
        for (Movimiento m : movimientos) porCliente.computeIfAbsent(m.clienteId(), k -> new ArrayList<>()).add(m);
        List<ClienteFila> filas = porCliente.values().stream().map(this::fila)
                .sorted(Comparator.comparing(ClienteFila::totalPedidos).reversed()
                        .thenComparing(ClienteFila::cliente, Comparator.nullsLast(String::compareTo)))
                .toList();

        BigDecimal facturado = suma(movimientos, Movimiento::facturado);
        BigDecimal cobrado = suma(movimientos, Movimiento::cobrado);
        Totales totales = new Totales(filas.size(), movimientos.size(), entregados(movimientos),
                unidades(movimientos), suma(movimientos, Movimiento::total), facturado, cobrado, facturado.subtract(cobrado));
        return new ReporteClientesResponse(desde, hasta, facturacion, totales, filas, movimientos);
    }

    private Movimiento movimiento(Pedido p) {
        long unidades = p.getDetalles().stream().mapToLong(DetallePedido::getCantidad).sum();
        String numero = null, estadoFactura = null;
        BigDecimal facturado = BigDecimal.ZERO, cobrado = BigDecimal.ZERO;
        if (facturacion) {
            var factura = facturas.findByPedidoId(p.getId());
            if (factura.isPresent()) {
                var f = factura.get();
                numero = f.getNumero();
                estadoFactura = f.getEstado().name();
                if (f.getEstado() != EstadoFactura.ANULADA) {
                    facturado = f.getImporteTotal();
                    cobrado = Objects.requireNonNullElse(pagos.totalFactura(f.getId()), BigDecimal.ZERO);
                }
            }
        }
        return new Movimiento(p.getId(), p.getFecha(), p.getCliente().getId(), p.getCliente().getNombreComercio(),
                p.getEstado().name(), unidades, p.total(), numero, estadoFactura, facturado, cobrado);
    }

    private ClienteFila fila(List<Movimiento> movimientos) {
        Movimiento primero = movimientos.get(0);
        BigDecimal total = suma(movimientos, Movimiento::total);
        BigDecimal facturado = suma(movimientos, Movimiento::facturado);
        BigDecimal cobrado = suma(movimientos, Movimiento::cobrado);
        BigDecimal ticket = total.divide(BigDecimal.valueOf(movimientos.size()), 2, RoundingMode.HALF_UP);
        Instant ultimo = movimientos.stream().map(Movimiento::fecha).max(Comparator.naturalOrder()).orElse(null);
        return new ClienteFila(primero.clienteId(), primero.cliente(), movimientos.size(), entregados(movimientos),
                unidades(movimientos), total, ticket, facturado, cobrado, facturado.subtract(cobrado), ultimo);
    }

    private static int entregados(List<Movimiento> movimientos) {
        return (int) movimientos.stream().filter(m -> EstadoPedido.ENTREGADO.name().equals(m.estado())).count();
    }

    private static long unidades(List<Movimiento> movimientos) {
        return movimientos.stream().mapToLong(Movimiento::unidades).sum();
    }

    private static BigDecimal suma(List<Movimiento> movimientos, Function<Movimiento, BigDecimal> campo) {
        return movimientos.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
