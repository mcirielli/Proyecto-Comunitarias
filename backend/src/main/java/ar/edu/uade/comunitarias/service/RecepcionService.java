package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Profile("supabase") @Transactional
@ConditionalOnProperty(name="app.inventario.enabled",havingValue="true")
public class RecepcionService {
    private final OrdenCompraRepository ordenes;
    private final AsignacionInventarioRepository asignaciones;
    private final StockService stock;
    private final Clock clock;
    private final RecepcionCompraRepository recepciones;
    public record Recepcion(Long ordenId,String actor,java.time.Instant fecha){}
    @Transactional(readOnly=true)
    public Recepcion consultar(Long id) {
        var r=recepciones.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Recepción inexistente"));
        return new Recepcion(r.getOrdenId(),r.getActor(),r.getFecha());
    }
    public RecepcionService(OrdenCompraRepository ordenes,AsignacionInventarioRepository asignaciones,StockService stock,Clock clock,RecepcionCompraRepository recepciones) {
        this.ordenes=ordenes;this.asignaciones=asignaciones;this.stock=stock;this.clock=clock;
        this.recepciones=recepciones;
    }
    public CompraResponse recibir(Long id,SessionResponse actor) {
        if(actor==null || !"OPERACIONES".equals(actor.rol()))throw new org.springframework.security.access.AccessDeniedException("Acceso denegado");
        var lectura=ordenes.findById(id).orElseThrow(()->new RecursoNoEncontradoException("Compra inexistente"));
        stock.bloquearProductos(lectura.getDetalles().stream().map(d->d.getProducto().getId()).toList());
        var orden=ordenes.bloquear(id).orElseThrow(()->new RecursoNoEncontradoException("Compra inexistente"));
        // refresh necesario si otro receptor terminó mientras esperábamos el producto.
        refrescar(orden);
        orden.recibir(actor.email(),clock.instant());
        recepciones.saveAndFlush(new RecepcionCompra(id,actor.email(),clock.instant()));
        if(orden.getDetalles().isEmpty())throw new ReglaNegocioException("Orden sin detalles");
        for(var detalle:orden.getDetalles())stock.ingresar(id,detalle.getProducto().getId(),detalle.getCantidad(),actor.email());
        asignaciones.findByOrdenId(id).forEach(a->a.recibir(clock.instant()));
        ordenes.flush();
        return CompraResponse.de(orden);
    }
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    private void refrescar(OrdenCompra orden){entityManager.refresh(orden,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);}
}
