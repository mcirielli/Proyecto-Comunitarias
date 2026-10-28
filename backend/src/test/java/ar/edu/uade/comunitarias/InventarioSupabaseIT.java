package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.*;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Solo ejecución explícita tras 004 autorizada. Fixtures propios confirmados para
 * compartirlos entre sesiones; cada prueba los elimina por IDs en finally/AfterEach.
 * No usa datos comerciales ni DDL; no confundir limpieza con rollback de secuencias. */
@SpringBootTest(properties={"app.compras.enabled=true","app.inventario.enabled=true"})
@ActiveProfiles("supabase") @ContextConfiguration(initializers=SupabaseTestEnvironment.class)
class InventarioSupabaseIT {
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager manager;
    @Autowired RecepcionService recepcion;
    @Autowired PedidoService pedidos;
    @Autowired CompraService compras;
    @Autowired StockRepository stocks;
    @Autowired OrdenCompraRepository ordenes;
    @SpyBean MovimientoStockRepository movimientos;
    final SessionResponse ops=new SessionResponse(-2L,"Operaciones","operaciones@demo.local","OPERACIONES");
    record Fixture(Long producto,Long proveedor,Long cliente,Long oferta,Long pedidoA,Long pedidoB,Long orden){}
    Fixture fixture;
    TransactionTemplate tx(){return new TransactionTemplate(manager);}
    void autenticar(){var p=new UsuarioPrincipal(ops,"unused");SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p,null,p.getAuthorities()));}
    Fixture crear(int libre,int a,int b,int compra){
        fixture=tx().execute(s->{
            String token="Inventario IT "+UUID.randomUUID();
            var producto=new Producto(token,null,null,"unidad");var proveedor=new Proveedor(token,null,null,null);var cliente=new Cliente(token);
            var oferta=new Oferta(token,java.time.LocalDate.now().minusDays(1),java.time.LocalDate.now().plusDays(1));
            em.persist(producto);em.persist(proveedor);em.persist(cliente);em.persist(oferta);em.flush();
            em.createNativeQuery("insert into producto_proveedor(producto_id,proveedor_id,precio_compra) values (:p,:v,12.35)").setParameter("p",producto.getId()).setParameter("v",proveedor.getId()).executeUpdate();
            var pa=new Pedido(cliente,oferta,Instant.now());pa.agregar(producto,a,BigDecimal.TEN);pa.cambiarEstado(EstadoPedido.CONFIRMADO,Instant.now());em.persist(pa);
            var pb=new Pedido(cliente,oferta,Instant.now());pb.agregar(producto,b,BigDecimal.TEN);pb.cambiarEstado(EstadoPedido.CONFIRMADO,Instant.now());em.persist(pb);
            var orden=new OrdenCompra(proveedor,TipoCompra.DEMANDA_CLIENTES,UUID.randomUUID(),ops.email(),Instant.now());orden.agregar(producto,compra,BigDecimal.ONE);em.persist(orden);
            if(libre>0){var saldo=new Stock(producto.getId());saldo.ingresar(libre);em.persist(saldo);}em.flush();
            return new Fixture(producto.getId(),proveedor.getId(),cliente.getId(),oferta.getId(),pa.getId(),pb.getId(),orden.getId());
        });return fixture;
    }
    @AfterEach void limpiar(){
        SecurityContextHolder.clearContext();reset(movimientos);
        if(fixture==null)return;
        tx().executeWithoutResult(s->{
            Long p=fixture.producto();
            var lotes=em.createNativeQuery("select cobertura_id from compras_cobertura_pedidos where pedido_id in (:a,:b)").setParameter("a",fixture.pedidoA()).setParameter("b",fixture.pedidoB()).getResultList();
            for(String sql:List.of(
                "delete from inventario_asignaciones where producto_id=:p",
                "delete from reservas_pedido where producto_id=:p",
                "delete from movimientos_stock where producto_id=:p",
                "delete from inventario_recepciones where orden_compra_id in (select orden_compra_id from orden_compra_detalle where producto_id=:p)",
                "delete from compras_cobertura_pedidos where cobertura_id in (select cobertura_id from compras_cobertura_detalles where producto_id=:p)",
                "delete from compras_cobertura_detalles where producto_id=:p",
                "delete from compras_coberturas where orden_compra_id in (select orden_compra_id from orden_compra_detalle where producto_id=:p)",
                "delete from orden_compra_detalle where producto_id=:p",
                "delete from pedido_detalle where producto_id=:p",
                "delete from stock where producto_id=:p",
                "delete from producto_proveedor where producto_id=:p"))em.createNativeQuery(sql).setParameter("p",p).executeUpdate();
            // Lotes sin compra de estas pruebas quedan sin detalle; IDs se identifican por sus pedidos.
            em.createNativeQuery("delete from compras_cobertura_pedidos where pedido_id in (:a,:b)").setParameter("a",fixture.pedidoA()).setParameter("b",fixture.pedidoB()).executeUpdate();
            if(!lotes.isEmpty())em.createNativeQuery("delete from compras_coberturas where id in (:ids)").setParameter("ids",lotes).executeUpdate();
            em.createNativeQuery("delete from pedidos where id in (:a,:b)").setParameter("a",fixture.pedidoA()).setParameter("b",fixture.pedidoB()).executeUpdate();
            em.createNativeQuery("delete from ordenes_compra where proveedor_id=:v").setParameter("v",fixture.proveedor()).executeUpdate();
            em.createNativeQuery("delete from productos where id=:p").setParameter("p",p).executeUpdate();
            em.createNativeQuery("delete from proveedores where id=:v").setParameter("v",fixture.proveedor()).executeUpdate();
            em.createNativeQuery("delete from clientes where id=:c").setParameter("c",fixture.cliente()).executeUpdate();
            em.createNativeQuery("delete from ofertas where id=:o").setParameter("o",fixture.oferta()).executeUpdate();
        });
    }
    long contar(String tabla){return tx().execute(s->((Number)em.createNativeQuery("select count(*) from "+tabla+" where producto_id=:p").setParameter("p",fixture.producto()).getSingleResult()).longValue());}
    void estado(Long pedido,EstadoPedido estado){autenticar();try{pedidos.cambiarEstado(pedido,estado);}finally{SecurityContextHolder.clearContext();}}
    @Test void dosRecepcionesEnSesionesDistintasIngresanUnaVez()throws Exception {
        var f=crear(0,2,2,5);var resultados=competir(()->{recepcion.recibir(f.orden(),ops);return true;});
        assertEquals(1,resultados.stream().filter(Boolean::booleanValue).count());
        assertEquals(1,contar("movimientos_stock"));
        tx().executeWithoutResult(s->{assertEquals(5,stocks.findByProductoId(f.producto()).orElseThrow().getDisponible());assertEquals(EstadoCompra.RECIBIDA,ordenes.findById(f.orden()).orElseThrow().getEstado());});
    }
    @Test void dosPedidosNoReservanLasMismasUnidades()throws Exception {
        var f=crear(3,3,3,1);var inicio=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try{var a=pool.submit(()->{inicio.await();return intentarEstado(f.pedidoA());});var b=pool.submit(()->{inicio.await();return intentarEstado(f.pedidoB());});inicio.countDown();assertNotEquals(a.get(30,TimeUnit.SECONDS),b.get(30,TimeUnit.SECONDS));}
        finally{pool.shutdownNow();}
        tx().executeWithoutResult(s->{var saldo=stocks.findByProductoId(f.producto()).orElseThrow();assertEquals(0,saldo.getDisponible());assertEquals(3,saldo.getReservado());});
        assertEquals(1,contar("reservas_pedido"));
    }
    boolean intentarEstado(Long id){try{estado(id,EstadoPedido.EN_PREPARACION);return true;}catch(ReglaNegocioException e){return false;}}
    List<Boolean> competir(Callable<Boolean> trabajo)throws Exception {
        var inicio=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        Callable<Boolean> tarea=()->{inicio.await();try{return trabajo.call();}catch(ReglaNegocioException e){return false;}};
        try{var a=pool.submit(tarea);var b=pool.submit(tarea);inicio.countDown();return List.of(a.get(30,TimeUnit.SECONDS),b.get(30,TimeUnit.SECONDS));}finally{pool.shutdownNow();}
    }
    @Test void fallaMovimientoRevierteSaldoReservaYEstado(){
        var f=crear(3,3,3,1);
        doThrow(new IllegalStateException("Fallo controlado IT")).when(movimientos).save(any(MovimientoStock.class));
        assertThrows(IllegalStateException.class,()->estado(f.pedidoA(),EstadoPedido.EN_PREPARACION));reset(movimientos);
        assertEquals(0,contar("reservas_pedido"));assertEquals(0,contar("movimientos_stock"));
        tx().executeWithoutResult(s->{assertEquals(3,stocks.findByProductoId(f.producto()).orElseThrow().getDisponible());assertEquals(EstadoPedido.CONFIRMADO,em.find(Pedido.class,f.pedidoA()).getEstado());});
    }
    @Test void recepcionReservaDespachoEntregaYReintentoPersisten(){
        var f=crear(0,5,1,5);recepcion.recibir(f.orden(),ops);
        estado(f.pedidoA(),EstadoPedido.EN_PREPARACION);estado(f.pedidoA(),EstadoPedido.PREPARADO);estado(f.pedidoA(),EstadoPedido.DESPACHADO);estado(f.pedidoA(),EstadoPedido.ENTREGADO);
        assertThrows(ReglaNegocioException.class,()->recepcion.recibir(f.orden(),ops));
        assertThrows(ar.edu.uade.comunitarias.state.TransicionInvalidaException.class,()->estado(f.pedidoA(),EstadoPedido.ENTREGADO));
        assertEquals(3,contar("movimientos_stock"));tx().executeWithoutResult(s->assertEquals(0,stocks.findByProductoId(f.producto()).orElseThrow().existencia()));
    }
    @Test void conciliaDosGruposSinReutilizarNiDescontarDosVeces(){
        var f=crear(4,7,5,6);
        var a=compras.crear(TipoCompra.DEMANDA_CLIENTES,new CrearCompraRequest(UUID.randomUUID(),f.proveedor(),List.of(f.pedidoA()),List.of()),ops);
        assertNull(a.orden());
        var b=compras.crear(TipoCompra.DEMANDA_CLIENTES,new CrearCompraRequest(UUID.randomUUID(),f.proveedor(),List.of(f.pedidoB()),List.of()),ops);
        assertEquals(2,b.orden().detalles().getFirst().cantidad());
        recepcion.recibir(f.orden(),ops);estado(f.pedidoA(),EstadoPedido.EN_PREPARACION);
        tx().executeWithoutResult(s->{var saldo=stocks.findByProductoId(f.producto()).orElseThrow();assertEquals(3,saldo.getDisponible());assertEquals(7,saldo.getReservado());});
        assertThrows(ReglaNegocioException.class,()->estado(f.pedidoB(),EstadoPedido.EN_PREPARACION));
        recepcion.recibir(b.orden().id(),ops);estado(f.pedidoB(),EstadoPedido.EN_PREPARACION);
        estado(f.pedidoA(),EstadoPedido.PREPARADO);estado(f.pedidoA(),EstadoPedido.DESPACHADO);estado(f.pedidoA(),EstadoPedido.ENTREGADO);
        tx().executeWithoutResult(s->{var saldo=stocks.findByProductoId(f.producto()).orElseThrow();assertEquals(0,saldo.getDisponible());assertEquals(5,saldo.getReservado());});
        assertThrows(ReglaNegocioException.class,()->compras.crear(TipoCompra.DEMANDA_CLIENTES,new CrearCompraRequest(UUID.randomUUID(),f.proveedor(),List.of(f.pedidoB()),List.of()),ops));
    }
    @Test void falloDeIngresoRevierteOrdenRecepcionYStockAusente(){
        var f=crear(0,2,2,5);doThrow(new IllegalStateException("Fallo controlado IT")).when(movimientos).save(any(MovimientoStock.class));
        assertThrows(IllegalStateException.class,()->recepcion.recibir(f.orden(),ops));reset(movimientos);
        assertEquals(0,contar("movimientos_stock"));assertEquals(0,contar("stock"));
        tx().executeWithoutResult(s->{assertEquals(EstadoCompra.EMITIDA,ordenes.findById(f.orden()).orElseThrow().getEstado());assertNull(em.find(RecepcionCompra.class,f.orden()));});
    }
    @Test void faltanteEnSegundaLineaRevierteLaPrimera(){
        var f=crear(3,3,1,1);
        Long segundo=tx().execute(s->{var p=new Producto("Inventario IT "+UUID.randomUUID(),null,null,"unidad");em.persist(p);em.flush();em.createNativeQuery("insert into pedido_detalle(pedido_id,producto_id,cantidad,precio_unitario) values (:pedido,:producto,1,10)").setParameter("pedido",f.pedidoA()).setParameter("producto",p.getId()).executeUpdate();return p.getId();});
        try{assertThrows(ReglaNegocioException.class,()->estado(f.pedidoA(),EstadoPedido.EN_PREPARACION));assertEquals(0,contar("reservas_pedido"));tx().executeWithoutResult(s->assertEquals(3,stocks.findByProductoId(f.producto()).orElseThrow().getDisponible()));}
        finally{tx().executeWithoutResult(s->{em.createNativeQuery("delete from pedido_detalle where producto_id=:p").setParameter("p",segundo).executeUpdate();em.createNativeQuery("delete from productos where id=:p").setParameter("p",segundo).executeUpdate();});}
    }
}
