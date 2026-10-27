package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Comparator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Profile("supabase")
@ConditionalOnProperty(name="app.auth.demo-enabled",havingValue="true")
public class PedidoDemoService {
    private final ClienteRepository clientes; private final OfertaRepository ofertas; private final ProductoRepository productos; private final Clock clock;
    public PedidoDemoService(ClienteRepository clientes,OfertaRepository ofertas,ProductoRepository productos,Clock clock) {this.clientes=clientes;this.ofertas=ofertas;this.productos=productos;this.clock=clock;}
    @Transactional
    public synchronized void preparar() {
        var disponibles=productos.findAll().stream().filter(Producto::getActivo).sorted(Comparator.comparing(Producto::getId)).limit(3).toList();
        if(disponibles.isEmpty()) throw new ReglaNegocioException("Primero registre un producto activo");
        var demo=clientes.findByNombreComercio(PedidoService.CLIENTE_DEMO);
        if(demo.size()>1) throw new ReglaNegocioException("Hay clientes demo duplicados; revisar datos");
        if(demo.isEmpty()) clientes.saveAndFlush(new Cliente(PedidoService.CLIENTE_DEMO));
        var hoy=LocalDate.now(clock.withZone(ZoneId.of("America/Argentina/Buenos_Aires")));
        if(ofertas.findByTitulo("Oferta Demo UADE State").stream().anyMatch(o -> o.vigente(hoy))) return;
        var oferta=new Oferta("Oferta Demo UADE State",hoy,hoy.plusDays(7));
        for(var producto:disponibles) oferta.agregar(producto,new BigDecimal("1000.00"));
        ofertas.saveAndFlush(oferta);
    }
}
