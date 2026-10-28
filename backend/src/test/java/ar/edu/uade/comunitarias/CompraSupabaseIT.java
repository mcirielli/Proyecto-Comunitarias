package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.CompraService;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

/** Explícita, con rollback. Requiere migración 002 YA aplicada con autorización. No ejecuta DDL. */
@SpringBootTest(properties="app.compras.enabled=true") @ActiveProfiles("supabase")
@ContextConfiguration(initializers=SupabaseTestEnvironment.class) @Transactional
class CompraSupabaseIT {
    @Autowired EntityManager em;
    @Autowired CompraService service;
    final SessionResponse ops=new SessionResponse(-2L,"Operaciones","operaciones@demo.local","OPERACIONES");
    final SessionResponse gerente=new SessionResponse(-3L,"Gerencia","gerencia@demo.local","GERENCIA");
    record Fixture(Producto producto, Proveedor proveedor, Oferta oferta, Cliente cliente) {}
    Fixture fixture() {
        String token=UUID.randomUUID().toString();
        var producto=new Producto("Compra IT "+token,null,null,"unidad");
        var proveedor=new Proveedor("Compra IT "+token,null,null,null);
        var cliente=new Cliente("Compra IT "+token);
        var oferta=new Oferta("Compra IT "+token,LocalDate.now().minusDays(1),LocalDate.now().plusDays(1));
        em.persist(producto); em.persist(proveedor); em.persist(cliente); em.persist(oferta); em.flush();
        em.createNativeQuery("insert into producto_proveedor(producto_id,proveedor_id,precio_compra) values (:p,:v,12.35)")
            .setParameter("p",producto.getId()).setParameter("v",proveedor.getId()).executeUpdate();
        return new Fixture(producto,proveedor,oferta,cliente);
    }
    Pedido pedido(Fixture f,int cantidad) {
        var p=new Pedido(f.cliente(),f.oferta(),Instant.now());
        p.agregar(f.producto(),cantidad,new BigDecimal("25")); p.cambiarEstado(EstadoPedido.CONFIRMADO,Instant.now());
        em.persist(p); em.flush(); return p;
    }
    @Test void persisteAutorizaEmiteYReconstruyePrecioHistorico() {
        var f=fixture();
        var r=new CrearCompraRequest(UUID.randomUUID(),f.proveedor().getId(),List.of(),List.of(new CrearCompraRequest.Linea(f.producto().getId(),4)));
        var creada=service.crear(TipoCompra.STOCK_QUINTEROS,r,ops).orden();
        assertEquals(EstadoCompra.PENDIENTE_AUTORIZACION,creada.estado());
        service.autorizar(creada.id(),gerente); service.emitir(creada.id(),ops); em.flush(); em.clear();
        em.createNativeQuery("update producto_proveedor set precio_compra=99 where proveedor_id=:v")
            .setParameter("v",f.proveedor().getId()).executeUpdate();
        var reconstruida=em.find(OrdenCompra.class,creada.id());
        assertEquals(EstadoCompra.EMITIDA,reconstruida.getEstado());
        assertEquals(gerente.email(),reconstruida.getAutorizadaPor());
        assertEquals(0,new BigDecimal("49.40").compareTo(reconstruida.total()));
        assertNotNull(reconstruida.getFechaEmision());
    }
    @Test void consolidaPersistiendoCoberturaYEvitaRepeticion() {
        var f=fixture(); var a=pedido(f,5); var b=pedido(f,7);
        em.createNativeQuery("insert into stock(producto_id,cantidad_disponible,cantidad_reservada) values (:p,3,2)")
            .setParameter("p",f.producto().getId()).executeUpdate();
        var request=new CrearCompraRequest(UUID.randomUUID(),f.proveedor().getId(),List.of(a.getId(),b.getId()),List.of());
        var resultado=service.crear(TipoCompra.DEMANDA_CLIENTES,request,ops); em.flush(); em.clear();
        assertNotNull(resultado.coberturaId()); assertEquals(9,resultado.orden().detalles().getFirst().cantidad());
        assertEquals(3,em.find(CoberturaCompra.class,resultado.coberturaId()).getAsignaciones().getFirst().getStock());
        assertThrows(ReglaNegocioException.class,() -> service.crear(TipoCompra.DEMANDA_CLIENTES,
            new CrearCompraRequest(UUID.randomUUID(),f.proveedor().getId(),List.of(a.getId()),List.of()),ops));
    }
    @Test void sinFaltantePersisteLoteSinCompra() {
        var f=fixture(); var a=pedido(f,2);
        em.createNativeQuery("insert into stock(producto_id,cantidad_disponible,cantidad_reservada) values (:p,4,0)")
            .setParameter("p",f.producto().getId()).executeUpdate();
        var resultado=service.crear(TipoCompra.DEMANDA_CLIENTES,new CrearCompraRequest(UUID.randomUUID(),f.proveedor().getId(),List.of(a.getId()),List.of()),ops);
        em.flush(); em.clear(); assertNull(resultado.orden());
        assertNotNull(em.find(CoberturaCompra.class,resultado.coberturaId()));
    }
}
