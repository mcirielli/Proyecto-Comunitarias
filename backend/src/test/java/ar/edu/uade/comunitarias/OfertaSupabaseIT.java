package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.*;
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

@SpringBootTest @ActiveProfiles("supabase") @ContextConfiguration(initializers=SupabaseTestEnvironment.class) @Transactional
class OfertaSupabaseIT {
    @Autowired OfertaService ofertasService;
    @Autowired PedidoService pedidosService;
    @Autowired OfertaRepository ofertas;
    @Autowired ProductoRepository productos;
    @Autowired ClienteRepository clientes;
    @Autowired EntityManager em;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;

    @Test @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void rollbackReviertePublicacionCabeceraYDetalles() {
        String titulo="IT_ROLLBACK_"+UUID.randomUUID();
        var tx=new org.springframework.transaction.support.TransactionTemplate(transactions);
        Long[] id=new Long[1];
        assertThrows(IllegalStateException.class,() -> tx.execute(status -> {
            var p=productos.saveAndFlush(new Producto(titulo,null,null,"unidad"));
            var hoy=LocalDate.now();
            id[0]=ofertasService.crear(new CrearOfertaRequest(titulo,hoy,hoy,
                    List.of(new CrearOfertaRequest.Item(p.getId(),BigDecimal.ONE)))).id();
            ofertasService.publicar(id[0]); em.flush(); em.clear();
            assertTrue(ofertas.findById(id[0]).orElseThrow().isPublicada());
            throw new IllegalStateException("Fallo simulado después de persistir publicación y detalles");
        }));
        tx.executeWithoutResult(status -> {
            assertFalse(ofertas.existsById(id[0]));
            Number detalles=(Number)em.createNativeQuery("select count(*) from oferta_detalle where oferta_id=:id")
                    .setParameter("id",id[0]).getSingleResult();
            assertEquals(0,detalles.longValue());
        });
    }

    @Test void inspeccionaEsquemaRealSinDdl() {
        var columnas=em.createNativeQuery("""
                select table_name,column_name,data_type,numeric_precision,numeric_scale
                from information_schema.columns where table_schema=current_schema()
                and table_name in ('ofertas','oferta_detalle') order by table_name,ordinal_position
                """).getResultList();
        assertEquals(9,columnas.size());
        for(Object row:columnas) System.out.println("OFERTA_SCHEMA "+Arrays.toString((Object[])row));
        var constraints=em.createNativeQuery("""
                select conrelid::regclass::text,pg_get_constraintdef(oid) from pg_constraint
                where conrelid in ('ofertas'::regclass,'oferta_detalle'::regclass) order by conrelid,contype
                """).getResultList();
        assertFalse(constraints.isEmpty());
        for(Object row:constraints) System.out.println("OFERTA_CONSTRAINT "+Arrays.toString((Object[])row));
    }

    @Test void publicarConsultarYPedirConPreciosHistoricos() {
        var hoy=LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        var producto=productos.saveAndFlush(new Producto("IT_OFERTA_"+UUID.randomUUID(),null,null,"unidad"));
        var cliente=clientes.saveAndFlush(new Cliente("IT_OFERTA_"+UUID.randomUUID()));
        var response=ofertasService.crear(new CrearOfertaRequest("IT_OFERTA_"+UUID.randomUUID(),hoy,hoy,
                List.of(new CrearOfertaRequest.Item(producto.getId(),new BigDecimal("12.35")))));
        em.clear();
        assertFalse(ofertas.findById(response.id()).orElseThrow().isPublicada());
        assertTrue(ofertasService.listar(false).stream().noneMatch(o -> o.id().equals(response.id())));
        ofertasService.publicar(response.id()); em.clear();
        var visible=ofertasService.listar(false).stream().filter(o -> o.id().equals(response.id())).findFirst().orElseThrow();
        assertEquals(1,visible.productos().size());
        var actor=new SessionResponse(100L,"IT","it@example.invalid","OPERACIONES");
        var pedido=pedidosService.crear(new CrearPedidoRequest(cliente.getId(),response.id(),
                List.of(new CrearPedidoRequest.Item(producto.getId(),2))),actor);
        assertEquals(0,new BigDecimal("24.70").compareTo(pedido.total()));
        // Simula cambio externo sin habilitar edición de publicaciones en la API.
        em.createNativeQuery("update oferta_detalle set precio_venta=99 where oferta_id=:id").setParameter("id",response.id()).executeUpdate();
        em.clear();
        var historico=pedidosService.listar(actor).stream().filter(p -> p.id().equals(pedido.id())).findFirst().orElseThrow();
        assertEquals(0,new BigDecimal("24.70").compareTo(historico.total()));
    }

    @Test void falloValidacionNoInsertaCabeceraNiDetalles() {
        long antes=ofertas.count();
        var producto=productos.saveAndFlush(new Producto("IT_OFERTA",null,null,"unidad"));
        var hoy=LocalDate.now(); var item=new CrearOfertaRequest.Item(producto.getId(),BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class,() -> ofertasService.crear(new CrearOfertaRequest("IT_OFERTA",hoy,hoy,List.of(item,item))));
        // La transacción está marcada rollback-only; no se deja dato comercial de prueba.
        assertEquals(antes,ofertas.count());
    }
}
