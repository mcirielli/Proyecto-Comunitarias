package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.CompraService;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import ar.edu.uade.comunitarias.strategy.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class CompraServiceTest {
    final CompraAbastecimientoRepository abastecimiento=mock(CompraAbastecimientoRepository.class);
    final ProveedorRepository proveedores=mock(ProveedorRepository.class);
    final ProductoProveedorRepository precios=mock(ProductoProveedorRepository.class);
    final StockAbastecimientoRepository stock=mock(StockAbastecimientoRepository.class);
    final OrdenCompraRepository ordenes=mock(OrdenCompraRepository.class);
    final CoberturaCompraRepository coberturas=mock(CoberturaCompraRepository.class);
    final Clock clock=Clock.fixed(Instant.parse("2026-10-27T15:00:00Z"),ZoneOffset.UTC);
    final CompraService service=new CompraService(abastecimiento,proveedores,precios,stock,ordenes,coberturas,
        new CompraPorDemandaStrategy(),new CompraQuinterosStrategy(),clock);
    final SessionResponse operaciones=new SessionResponse(-2L,"Operaciones","operaciones@demo.local","OPERACIONES");
    final SessionResponse gerencia=new SessionResponse(-3L,"Gerencia","gerencia@demo.local","GERENCIA");
    Producto producto;
    Proveedor proveedor;
    ProductoProveedor vinculo;
    @BeforeEach void preparar() {
        producto=mock(Producto.class); when(producto.getId()).thenReturn(1L); when(producto.getNombre()).thenReturn("Producto"); when(producto.getActivo()).thenReturn(true);
        proveedor=mock(Proveedor.class); when(proveedor.getId()).thenReturn(2L); when(proveedor.getRazonSocial()).thenReturn("Proveedor"); when(proveedor.getActivo()).thenReturn(true);
        vinculo=mock(ProductoProveedor.class); when(vinculo.getProducto()).thenReturn(producto); when(vinculo.getPrecioCompra()).thenReturn(new BigDecimal("12.35"));
        when(proveedores.findById(2L)).thenReturn(Optional.of(proveedor));
        when(abastecimiento.bloquearProducto(1L)).thenReturn(Optional.of(producto));
        when(precios.findByProveedorId(2L)).thenReturn(List.of(vinculo));
        when(ordenes.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(coberturas.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }
    CrearCompraRequest quinteros() { return new CrearCompraRequest(UUID.randomUUID(),2L,List.of(),List.of(new CrearCompraRequest.Linea(1L,4))); }
    CrearCompraRequest demanda(Long... ids) { return new CrearCompraRequest(UUID.randomUUID(),2L,List.of(ids),List.of()); }
    void pedido(long id,int cantidad,EstadoPedido estado) {
        var pedido=mock(Pedido.class); var detalle=mock(DetallePedido.class);
        when(pedido.getEstado()).thenReturn(estado); when(pedido.getDetalles()).thenReturn(List.of(detalle));
        when(detalle.getProducto()).thenReturn(producto); when(detalle.getCantidad()).thenReturn(cantidad);
        when(abastecimiento.bloquearPedido(id)).thenReturn(Optional.of(pedido));
    }
    @Test void quinterosCopiaPrecioServidorYSiguePendienteSinPedidos() {
        var resultado=service.crear(TipoCompra.STOCK_QUINTEROS,quinteros(),operaciones);
        assertEquals(EstadoCompra.PENDIENTE_AUTORIZACION,resultado.orden().estado());
        assertEquals(new BigDecimal("49.40"),resultado.orden().total());
        assertEquals(new BigDecimal("12.35"),resultado.orden().detalles().getFirst().precioUnitario());
        verify(abastecimiento,never()).bloquearPedido(any());
        verify(coberturas).saveAndFlush(argThat(c -> c.getAsignaciones().isEmpty()));
    }
    @Test void demandaConsolidaYConservaAsignacionDeLaOrdenNueva() {
        pedido(3,5,EstadoPedido.CONFIRMADO); pedido(4,7,EstadoPedido.CONFIRMADO);
        var resultado=service.crear(TipoCompra.DEMANDA_CLIENTES,demanda(3L,4L),operaciones);
        assertEquals(12,resultado.orden().detalles().getFirst().cantidad());
        assertEquals(EstadoCompra.EMITIDA,resultado.orden().estado());
        verify(coberturas).saveAndFlush(argThat(c -> c.getAsignaciones().getFirst().getAbierta()==12));
    }
    @Test void suficienteStockRegistraCoberturaSinOrdenVacia() {
        pedido(3,5,EstadoPedido.CONFIRMADO);
        var fila=mock(StockAbastecimiento.class); when(fila.getProductoId()).thenReturn(1L); when(fila.getCantidadDisponible()).thenReturn(5);
        when(stock.findByProductoIdIn(any())).thenReturn(List.of(fila));
        var resultado=service.crear(TipoCompra.DEMANDA_CLIENTES,demanda(3L),operaciones);
        assertNull(resultado.orden()); verify(ordenes,never()).saveAndFlush(any());
        verify(coberturas).saveAndFlush(argThat(c -> c.getAsignaciones().getFirst().getStock()==5));
    }
    @Test void pedidosNoConfirmadosNoEscriben() {
        pedido(3,5,EstadoPedido.RECIBIDO);
        assertThrows(ReglaNegocioException.class,() -> service.crear(TipoCompra.DEMANDA_CLIENTES,demanda(3L),operaciones)); sinEscrituras();
    }
    @Test void pedidoCubiertoNoSeVuelveAComprar() {
        pedido(3,5,EstadoPedido.CONFIRMADO); when(coberturas.contarPedidosCubiertos(any())).thenReturn(1L);
        assertThrows(ReglaNegocioException.class,() -> service.crear(TipoCompra.DEMANDA_CLIENTES,demanda(3L),operaciones)); sinEscrituras();
    }
    @Test void solicitudRepetidaSeRechaza() {
        when(ordenes.existsByClaveSolicitud(any())).thenReturn(true);
        assertThrows(ReglaNegocioException.class,() -> service.crear(TipoCompra.STOCK_QUINTEROS,quinteros(),operaciones)); sinEscrituras();
    }
    @Test void proveedorIncompatibleNoEscribe() {
        when(precios.findByProveedorId(2L)).thenReturn(List.of());
        assertThrows(ReglaNegocioException.class,() -> service.crear(TipoCompra.STOCK_QUINTEROS,quinteros(),operaciones)); sinEscrituras();
    }
    @Test void precioInvalidoNoEscribe() {
        when(vinculo.getPrecioCompra()).thenReturn(new BigDecimal("-1"));
        assertThrows(ReglaNegocioException.class,() -> service.crear(TipoCompra.STOCK_QUINTEROS,quinteros(),operaciones)); sinEscrituras();
    }
    @Test void productoInactivoNoEscribe() {
        when(producto.getActivo()).thenReturn(false);
        assertThrows(ReglaNegocioException.class,() -> service.crear(TipoCompra.STOCK_QUINTEROS,quinteros(),operaciones)); sinEscrituras();
    }
    @Test void cantidadInvalidaYDuplicadosNoEscriben() {
        for(var lineas:List.of(List.of(new CrearCompraRequest.Linea(1L,0)),List.of(new CrearCompraRequest.Linea(1L,1),new CrearCompraRequest.Linea(1L,2)))) {
            var r=new CrearCompraRequest(UUID.randomUUID(),2L,List.of(),lineas);
            assertThrows(IllegalArgumentException.class,() -> service.crear(TipoCompra.STOCK_QUINTEROS,r,operaciones));
        }
        sinEscrituras();
    }
    @Test void coberturaPreviaNoReutilizaStockNiOrdenAbierta() {
        pedido(3,10,EstadoPedido.CONFIRMADO);
        var fila=mock(StockAbastecimiento.class); when(fila.getProductoId()).thenReturn(1L); when(fila.getCantidadDisponible()).thenReturn(8);
        when(stock.findByProductoIdIn(any())).thenReturn(List.of(fila));
        var abierta=new OrdenCompra(proveedor,TipoCompra.DEMANDA_CLIENTES,UUID.randomUUID(),"actor",clock.instant()); abierta.agregar(producto,9,new BigDecimal("1"));
        when(ordenes.findByEstadoIn(any())).thenReturn(List.of(abierta));
        when(coberturas.findAll()).thenReturn(List.of(new CoberturaCompra(UUID.randomUUID(),"actor",clock.instant(),Set.of(99L),null,List.of(new CoberturaCompra.Asignacion(1L,6,7)))));
        var resultado=service.crear(TipoCompra.DEMANDA_CLIENTES,demanda(3L),operaciones);
        assertEquals(6,resultado.orden().detalles().getFirst().cantidad());
    }
    @Test void gerenciaAutorizaYOperacionesEmiteUnaSolaVez() {
        var orden=new OrdenCompra(proveedor,TipoCompra.STOCK_QUINTEROS,UUID.randomUUID(),operaciones.email(),clock.instant()); orden.agregar(producto,4,new BigDecimal("12.35"));
        when(ordenes.bloquear(1L)).thenReturn(Optional.of(orden));
        assertThrows(ReglaNegocioException.class,() -> service.emitir(1L,operaciones));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,() -> service.autorizar(1L,operaciones));
        assertEquals(EstadoCompra.AUTORIZADA,service.autorizar(1L,gerencia).estado());
        assertThrows(ReglaNegocioException.class,() -> service.autorizar(1L,gerencia));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,() -> service.emitir(1L,gerencia));
        assertEquals(EstadoCompra.EMITIDA,service.emitir(1L,operaciones).estado());
        assertEquals(gerencia.email(),orden.getAutorizadaPor()); assertEquals(clock.instant(),orden.getFechaAutorizacion());
        assertThrows(ReglaNegocioException.class,() -> service.emitir(1L,operaciones));
    }
    @Test void historialEsIndependienteDelPrecioActual() {
        var orden=new OrdenCompra(proveedor,TipoCompra.STOCK_QUINTEROS,UUID.randomUUID(),operaciones.email(),clock.instant());
        orden.agregar(producto,4,vinculo.getPrecioCompra()); when(vinculo.getPrecioCompra()).thenReturn(new BigDecimal("99"));
        assertEquals(new BigDecimal("49.40"),orden.total());
    }
    @Test void clienteNoConsultaCostosYGerenciaNoCrea() {
        assertThrows(org.springframework.security.access.AccessDeniedException.class,() -> service.listar(new SessionResponse(1L,"Cliente","cliente@demo.local","CLIENTE")));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,() -> service.crear(TipoCompra.STOCK_QUINTEROS,quinteros(),gerencia));
        verifyNoInteractions(ordenes,proveedores);
    }
    void sinEscrituras() { verify(ordenes,never()).saveAndFlush(any()); verify(coberturas,never()).saveAndFlush(any()); }
}
