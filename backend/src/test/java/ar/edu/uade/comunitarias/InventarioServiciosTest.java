package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.*;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class InventarioServiciosTest {
    final StockRepository stocks=mock(StockRepository.class);
    final MovimientoStockRepository movimientos=mock(MovimientoStockRepository.class);
    final ReservaPedidoRepository reservas=mock(ReservaPedidoRepository.class);
    final AsignacionInventarioRepository asignaciones=mock(AsignacionInventarioRepository.class);
    final CompraAbastecimientoRepository locks=mock(CompraAbastecimientoRepository.class);
    final OrdenCompraRepository ordenes=mock(OrdenCompraRepository.class);
    final Clock clock=Clock.fixed(Instant.parse("2026-10-27T15:00:00Z"),ZoneOffset.UTC);
    final StockService service=new StockService(stocks,movimientos,reservas,asignaciones,locks,clock);
    final CoberturaInventarioService ledger=new CoberturaInventarioService(asignaciones,ordenes,clock,stocks);
    final List<AsignacionInventario> lista=new ArrayList<>();
    final Map<Long,List<ReservaPedido>> porPedido=new HashMap<>();
    Producto producto;Stock saldo;
    @BeforeEach void preparar(){
        producto=new Producto("Producto IT",null,null,"unidad");ReflectionTestUtils.setField(producto,"id",1L);
        saldo=new Stock(1L);
        when(stocks.findByProductoId(1L)).thenReturn(Optional.of(saldo));
        when(locks.bloquearProducto(1L)).thenReturn(Optional.of(producto));
        when(asignaciones.findByProductoIdIn(any())).thenAnswer(i->lista.stream().filter(a->((Collection<?>)i.getArgument(0)).contains(a.getProductoId())).toList());
        when(asignaciones.findByPedidoId(anyLong())).thenAnswer(i->lista.stream().filter(a->a.getPedidoId().equals(i.getArgument(0))).toList());
        when(asignaciones.save(any())).thenAnswer(i->{var a=(AsignacionInventario)i.getArgument(0);lista.add(a);return a;});
        when(reservas.findByPedidoId(anyLong())).thenAnswer(i->porPedido.getOrDefault(i.getArgument(0),List.of()));
        when(reservas.save(any())).thenAnswer(i->{var r=(ReservaPedido)i.getArgument(0);Long id=(Long)ReflectionTestUtils.getField(r,"pedidoId");porPedido.computeIfAbsent(id,k->new ArrayList<>()).add(r);return r;});
    }
    Pedido pedido(long id,int cantidad){var oferta=new Oferta("IT",LocalDate.of(2026,10,27),LocalDate.of(2026,10,28));var p=new Pedido(new Cliente("IT"),oferta,clock.instant());ReflectionTestUtils.setField(p,"id",id);p.agregar(producto,cantidad,BigDecimal.ONE);return p;}
    OrdenCompra orden(long id,int cantidad){var o=new OrdenCompra(new Proveedor("IT",null,null,null),TipoCompra.DEMANDA_CLIENTES,UUID.randomUUID(),"ops",clock.instant());ReflectionTestUtils.setField(o,"id",id);o.agregar(producto,cantidad,BigDecimal.ONE);return o;}
    @Test void gruposCompitenAntesDespuesDeRecibirReservarYDespachar(){
        saldo.ingresar(4);var o1=orden(1,6);var a=pedido(10,7);var b=pedido(20,5);
        when(ordenes.findByEstadoIn(any())).thenReturn(List.of(o1));
        ledger.registrar(1L,Map.of(a.getId(),a),new TreeMap<>(Map.of(1L,4)));
        var libre=new TreeMap<>(Map.of(1L,4));var abiertas=new TreeMap<>(Map.of(1L,6));ledger.descontar(List.of(1L),libre,abiertas);
        assertEquals(0,libre.get(1L));assertEquals(3,abiertas.get(1L));
        var o2=orden(2,2);when(ordenes.findByEstadoIn(any())).thenReturn(List.of(o1,o2));
        ledger.registrar(2L,Map.of(b.getId(),b),libre);
        assertEquals(12,lista.stream().mapToInt(AsignacionInventario::getCantidad).sum());
        saldo.ingresar(6);lista.stream().filter(x->Objects.equals(x.getOrdenId(),1L)).forEach(x->x.recibir(clock.instant()));
        service.transicion(a,EstadoPedido.EN_PREPARACION,"ops");
        assertEquals(3,saldo.getDisponible());assertEquals(7,saldo.getReservado());
        libre=new TreeMap<>(Map.of(1L,3));abiertas=new TreeMap<>(Map.of(1L,2));ledger.descontar(List.of(1L),libre,abiertas);
        assertEquals(0,libre.get(1L));assertEquals(0,abiertas.get(1L));
        service.transicion(a,EstadoPedido.PREPARADO,"ops");service.transicion(a,EstadoPedido.DESPACHADO,"ops");service.transicion(a,EstadoPedido.ENTREGADO,"ops");
        assertEquals(3,saldo.existencia());assertEquals(0,saldo.getReservado());
        assertTrue(lista.stream().filter(x->x.getPedidoId().equals(10L)).allMatch(x->x.getFase()==AsignacionInventario.Fase.CONSUMIDA));
        assertThrows(ReglaNegocioException.class,()->service.transicion(b,EstadoPedido.EN_PREPARACION,"ops"));
        saldo.ingresar(2);lista.stream().filter(x->Objects.equals(x.getOrdenId(),2L)).forEach(x->x.recibir(clock.instant()));
        service.transicion(b,EstadoPedido.EN_PREPARACION,"ops");assertEquals(0,saldo.getDisponible());assertEquals(5,saldo.getReservado());
    }
    @Test void noTomaStockComprometidoDeOtroPedido(){saldo.ingresar(5);lista.add(new AsignacionInventario(1L,99L,1L,null,4,clock.instant()));assertThrows(ReglaNegocioException.class,()->service.transicion(pedido(10,2),EstadoPedido.EN_PREPARACION,"ops"));assertEquals(5,saldo.getDisponible());verify(movimientos,never()).save(any());}
    @Test void preparadoSinReservaPropiaNoEsValido(){saldo.ingresar(5);assertThrows(ReglaNegocioException.class,()->service.transicion(pedido(10,2),EstadoPedido.PREPARADO,"ops"));}
    @Test void reservaYDespachoNoSeRepiten(){saldo.ingresar(3);var p=pedido(10,3);service.transicion(p,EstadoPedido.EN_PREPARACION,"ops");assertThrows(ReglaNegocioException.class,()->service.transicion(p,EstadoPedido.EN_PREPARACION,"ops"));service.transicion(p,EstadoPedido.DESPACHADO,"ops");assertThrows(ReglaNegocioException.class,()->service.transicion(p,EstadoPedido.DESPACHADO,"ops"));assertEquals(0,saldo.existencia());verify(movimientos,times(2)).save(any());}
    @Test void productoHistoricoInactivoNoSeReactivaNiImpideReserva(){ReflectionTestUtils.setField(producto,"activo",false);saldo.ingresar(2);service.transicion(pedido(10,2),EstadoPedido.EN_PREPARACION,"ops");assertFalse(producto.getActivo());assertEquals(2,saldo.getReservado());}
    @Test void ledgerInconsistenteNoSeRecortaSilenciosamente(){lista.add(new AsignacionInventario(1L,10L,1L,null,5,clock.instant()));assertThrows(ReglaNegocioException.class,()->ledger.descontar(List.of(1L),new TreeMap<>(Map.of(1L,4)),new TreeMap<>()));}
    @Test void consultaAusenteNoCreaStock(){when(stocks.findByProductoId(1L)).thenReturn(Optional.empty());assertTrue(ledger.leerLibre(List.of(1L)).isEmpty());verify(stocks,never()).save(any());}
}
