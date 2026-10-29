package ar.edu.uade.comunitarias;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.PedidoService;
import ar.edu.uade.comunitarias.state.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"app.compras.enabled=true","app.inventario.enabled=true"}) @ActiveProfiles("supabase") @ContextConfiguration(initializers=SupabaseTestEnvironment.class) @Transactional
class PedidoSupabaseIT {
    @Autowired ClienteRepository clientes;
    @Autowired ProductoRepository productos;
    @Autowired OfertaRepository ofertas;
    @Autowired PedidoRepository pedidos;
    @Autowired PedidoService service;
    @Autowired EntityManager em;
    @Autowired StockRepository stock;
    @AfterEach void limpiarIdentidad(){org.springframework.security.core.context.SecurityContextHolder.clearContext();}
    @Test void cicloPersistidoReconstruyeStateYPreciosHistoricos() {
        var cliente=clientes.saveAndFlush(new Cliente("IT_STATE_"+UUID.randomUUID()));
        var producto=productos.saveAndFlush(new Producto("IT_STATE",null,null,"unidad"));
        var hoy=LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        var oferta=new Oferta("IT_STATE",hoy,hoy.plusDays(1));oferta.agregar(producto,new BigDecimal("10.25"));ofertas.saveAndFlush(oferta);
        var actor=new SessionResponse(-2L,"Demo","operaciones@demo.local","OPERACIONES");
        // Fixture propio con rollback: ahora el circuito integra reserva y consumo reales.
        var saldo=new Stock(producto.getId());saldo.ingresar(2);stock.saveAndFlush(saldo);
        var principal=new ar.edu.uade.comunitarias.service.UsuarioPrincipal(actor,"unused");
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities()));
        var response=service.crear(new CrearPedidoRequest(cliente.getId(),oferta.getId(),List.of(new CrearPedidoRequest.Item(producto.getId(),2))),actor);
        em.clear();
        assertEquals(EstadoPedido.RECIBIDO,pedidos.findById(response.id()).orElseThrow().state().codigo());
        em.createNativeQuery("update oferta_detalle set precio_venta=99 where oferta_id=:oferta").setParameter("oferta",oferta.getId()).executeUpdate();
        for(var destino:new EstadoPedido[]{EstadoPedido.CONFIRMADO,EstadoPedido.EN_PREPARACION,EstadoPedido.PREPARADO,EstadoPedido.DESPACHADO,EstadoPedido.ENTREGADO}) {
            em.clear(); var cambiado=service.cambiarEstado(response.id(),destino);assertEquals(destino,cambiado.estado());
            assertEquals(0,new BigDecimal("20.50").compareTo(cambiado.total()));
        }
        em.clear();var finalizado=pedidos.findById(response.id()).orElseThrow();
        assertInstanceOf(EntregadoState.class,finalizado.state()); assertNotNull(finalizado.getFechaDespacho());assertNotNull(finalizado.getFechaEntrega());
        assertEquals(0,stock.findByProductoId(producto.getId()).orElseThrow().existencia());
        assertThrows(TransicionInvalidaException.class,() -> service.cambiarEstado(response.id(),EstadoPedido.RECIBIDO));
    }
}
