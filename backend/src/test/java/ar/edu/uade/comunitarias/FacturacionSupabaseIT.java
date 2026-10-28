package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.*;
import static org.junit.jupiter.api.Assertions.*;
import static ar.edu.uade.comunitarias.FacturacionFixture.*;

@SpringBootTest(properties="app.facturacion.enabled=true") @ActiveProfiles("supabase")
@ContextConfiguration(initializers=SupabaseTestEnvironment.class) @Transactional
class FacturacionSupabaseIT {
    @Autowired EntityManager em;
    @Autowired FacturacionService facturacion;
    @Autowired PagoService pagos;
    @Autowired FacturaRepository facturas;
    @Autowired PagoRepository pagosRepo;
    @Autowired PlatformTransactionManager manager;
    record Fixture(Long pedido,Long cliente,Long oferta,Long producto) {}
    Fixture crear(String precio) {
        String token="IT_FACT_"+UUID.randomUUID();
        var c=new Cliente(token);var p=new Producto(token,null,null,"unidad");
        var o=new Oferta(token,LocalDate.now().minusDays(1),LocalDate.now().plusDays(1));
        em.persist(c);em.persist(p);o.agregar(p,new BigDecimal(precio));em.persist(o);em.flush();
        var pedido=new Pedido(c,o,Instant.now());pedido.agregar(p,2,new BigDecimal(precio));
        for(var estado:new EstadoPedido[]{EstadoPedido.CONFIRMADO,EstadoPedido.EN_PREPARACION,EstadoPedido.PREPARADO,EstadoPedido.DESPACHADO,EstadoPedido.ENTREGADO})pedido.cambiarEstado(estado,Instant.now());
        em.persist(pedido);em.flush();return new Fixture(pedido.getId(),c.getId(),o.getId(),p.getId());
    }
    RegistrarPagoRequest pago(Long factura,String importe,UUID clave){return new RegistrarPagoRequest(factura,clave,new BigDecimal(importe),MedioPago.TRANSFERENCIA,"IT");}
    @Test void emisionPagoParcialTotalReintentoEHistoriaPersistidos(){
        var f=crear("10.25");var emitida=facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS);em.clear();
        assertEquals(emitida.id(),facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS).id());
        UUID clave=UUID.randomUUID();var parcial=pagos.registrar(pago(emitida.id(),"10.25",clave),OPS);em.clear();
        assertEquals(parcial.id(),pagos.registrar(pago(emitida.id(),"10.25",clave),OPS).id());
        var visto=facturacion.detalle(emitida.id(),OPS);assertEquals(0,new BigDecimal("10.25").compareTo(visto.saldo()));assertEquals(EstadoFactura.PENDIENTE,visto.estado());
        pagos.registrar(pago(emitida.id(),"10.25",UUID.randomUUID()),OPS);em.clear();
        em.createNativeQuery("update oferta_detalle set precio_venta=999 where oferta_id=:id").setParameter("id",f.oferta()).executeUpdate();em.clear();
        var finalizada=facturacion.detalle(emitida.id(),OPS);assertEquals(EstadoFactura.PAGADA,finalizada.estado());assertEquals(0,finalizada.saldo().signum());assertEquals(2,finalizada.pagos().size());assertEquals(0,new BigDecimal("20.50").compareTo(finalizada.importeTotal()));
    }
    @Test void facturaCeroPagadaSinPagos(){var f=crear("0");var factura=facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS);em.clear();var leida=facturacion.detalle(factura.id(),OPS);assertEquals(EstadoFactura.PAGADA,leida.estado());assertTrue(leida.pagos().isEmpty());assertEquals(0,leida.saldo().signum());}
    @Test void claveDistintoContenidoSeRechaza(){var f=crear("10");var factura=facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS);var key=UUID.randomUUID();pagos.registrar(pago(factura.id(),"2",key),OPS);assertThrows(ReglaNegocioException.class,()->pagos.registrar(pago(factura.id(),"3",key),OPS));}
    List<Object> paralelo(Supplier<Object> a,Supplier<Object> b) throws Exception {
        var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
        try{
            var x=pool.submit(()->{start.await();try{return a.get();}catch(RuntimeException e){return e;}});
            var y=pool.submit(()->{start.await();try{return b.get();}catch(RuntimeException e){return e;}});
            start.countDown();return List.of(x.get(40,TimeUnit.SECONDS),y.get(40,TimeUnit.SECONDS));
        }finally{pool.shutdownNow();assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));}
    }
    Fixture fixtureCommit(){return new TransactionTemplate(manager).execute(status->crear("10"));}
    void limpiar(Fixture f){new TransactionTemplate(manager).executeWithoutResult(status->{
        em.createNativeQuery("delete from pagos where factura_id in (select id from facturas where pedido_id=:id)").setParameter("id",f.pedido()).executeUpdate();
        em.createNativeQuery("delete from facturas where pedido_id=:id").setParameter("id",f.pedido()).executeUpdate();
        em.createNativeQuery("delete from pedido_detalle where pedido_id=:id").setParameter("id",f.pedido()).executeUpdate();
        em.createNativeQuery("delete from pedidos where id=:id").setParameter("id",f.pedido()).executeUpdate();
        em.createNativeQuery("delete from oferta_detalle where oferta_id=:id").setParameter("id",f.oferta()).executeUpdate();
        em.createNativeQuery("delete from ofertas where id=:id").setParameter("id",f.oferta()).executeUpdate();
        em.createNativeQuery("delete from clientes where id=:id").setParameter("id",f.cliente()).executeUpdate();
        em.createNativeQuery("delete from productos where id=:id").setParameter("id",f.producto()).executeUpdate();
    });}
    @Test @Transactional(propagation=Propagation.NOT_SUPPORTED)
    void emisionConcurrenteDevuelveUnaFactura()throws Exception{
        var f=fixtureCommit();try{var rows=paralelo(()->facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS),()->facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS));
            assertInstanceOf(FacturaResponse.class,rows.get(0));assertInstanceOf(FacturaResponse.class,rows.get(1));assertEquals(((FacturaResponse)rows.get(0)).id(),((FacturaResponse)rows.get(1)).id());
        }finally{limpiar(f);}
    }
    @Test @Transactional(propagation=Propagation.NOT_SUPPORTED)
    void pagosConcurrentesNoSuperanSaldo()throws Exception{
        var f=fixtureCommit();try{var factura=facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS);
            var rows=paralelo(()->pagos.registrar(pago(factura.id(),"15",UUID.randomUUID()),OPS),()->pagos.registrar(pago(factura.id(),"15",UUID.randomUUID()),OPS));
            assertEquals(1,rows.stream().filter(PagoResponse.class::isInstance).count());assertEquals(1,rows.stream().filter(ReglaNegocioException.class::isInstance).count());
            var vista=facturacion.detalle(factura.id(),OPS);assertEquals(1,vista.pagos().size());assertEquals(0,new BigDecimal("5").compareTo(vista.saldo()));
        }finally{limpiar(f);}
    }
    @Test @Transactional(propagation=Propagation.NOT_SUPPORTED)
    void mismaClaveConcurrenteNoDuplicaPago()throws Exception{
        var f=fixtureCommit();try{var factura=facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS);var key=UUID.randomUUID();
            var rows=paralelo(()->pagos.registrar(pago(factura.id(),"10",key),OPS),()->pagos.registrar(pago(factura.id(),"10",key),OPS));
            assertInstanceOf(PagoResponse.class,rows.get(0));assertInstanceOf(PagoResponse.class,rows.get(1));assertEquals(((PagoResponse)rows.get(0)).id(),((PagoResponse)rows.get(1)).id());assertEquals(1,facturacion.detalle(factura.id(),OPS).pagos().size());
        }finally{limpiar(f);}
    }
    @Test @Transactional(propagation=Propagation.NOT_SUPPORTED)
    void falloDespuesDePersistirRevierteFacturaPagoYEstado(){
        var f=fixtureCommit();try{
            assertThrows(IllegalStateException.class,()->new TransactionTemplate(manager).execute(status->{var factura=facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS);pagos.registrar(pago(factura.id(),"20",UUID.randomUUID()),OPS);em.flush();throw new IllegalStateException("Fallo después del flush");}));
            assertTrue(Boolean.TRUE.equals(new TransactionTemplate(manager).execute(status->facturas.findByPedidoId(f.pedido()).isEmpty())));
            var factura=facturacion.emitir(new EmitirFacturaRequest(f.pedido()),OPS);
            assertThrows(IllegalStateException.class,()->new TransactionTemplate(manager).execute(status->{pagos.registrar(pago(factura.id(),"20",UUID.randomUUID()),OPS);em.flush();throw new IllegalStateException("Fallo después de pago");}));
            var leida=facturacion.detalle(factura.id(),OPS);assertTrue(leida.pagos().isEmpty());assertEquals(EstadoFactura.PENDIENTE,leida.estado());assertEquals(0,new BigDecimal("20").compareTo(leida.saldo()));
        }finally{limpiar(f);}
    }
}
