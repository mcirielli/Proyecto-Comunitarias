package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.repository.*;
import java.time.Clock;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Evolución de CMP-07. El ledger 002 se conserva, pero no vuelve a descontarse. */
@Service @Profile("supabase") @Transactional
@ConditionalOnProperty(name="app.inventario.enabled",havingValue="true")
public class CoberturaInventarioService {
    private final AsignacionInventarioRepository asignaciones;
    private final OrdenCompraRepository ordenes;
    private final Clock clock;
    private final StockRepository stocks;
    public CoberturaInventarioService(AsignacionInventarioRepository asignaciones,OrdenCompraRepository ordenes,Clock clock,StockRepository stocks){this.asignaciones=asignaciones;this.ordenes=ordenes;this.clock=clock;this.stocks=stocks;}
    public Map<Long,Integer> leerLibre(Collection<Long> ids) {
        Map<Long,Integer> libre=new TreeMap<>();
        for(Long id:ids)stocks.findByProductoId(id).ifPresent(s->libre.put(id,s.getDisponible()));
        return libre;
    }
    public void descontar(Collection<Long> ids,Map<Long,Integer> libre,Map<Long,Integer> abiertas) {
        for(var a:asignaciones.findByProductoIdIn(ids)) {
            Map<Long,Integer> saldo=switch(a.getFase()) { case FISICA->libre;case ABIERTA->abiertas;default->null; };
            if(saldo!=null) {
                int disponible=saldo.getOrDefault(a.getProductoId(),0);
                if(disponible<a.getCantidad())throw new ReglaNegocioException("Cobertura inconsistente; requiere conciliación");
                saldo.put(a.getProductoId(),disponible-a.getCantidad());
            }
        }
    }
    public void registrar(Long cobertura,Map<Long,Pedido> pedidos,Map<Long,Integer> libre) {
        Set<Long> ids=new TreeSet<>();pedidos.values().forEach(p->p.getDetalles().forEach(d->ids.add(d.getProducto().getId())));
        record Fuente(Long orden,Long producto){}
        Map<Fuente,Integer> capacidad=new LinkedHashMap<>();
        ordenes.findByEstadoIn(List.of(EstadoCompra.EMITIDA,EstadoCompra.PENDIENTE)).stream().sorted(Comparator.comparing(OrdenCompra::getId))
            .forEach(o->o.getDetalles().stream().sorted(Comparator.comparing(d->d.getProducto().getId())).forEach(d->{
                if(ids.contains(d.getProducto().getId()))capacidad.put(new Fuente(o.getId(),d.getProducto().getId()),d.getCantidad());
            }));
        for(var a:asignaciones.findByProductoIdIn(ids))if(a.getFase()==AsignacionInventario.Fase.ABIERTA) {
            var fuente=new Fuente(a.getOrdenId(),a.getProductoId());int disponible=capacidad.getOrDefault(fuente,0);
            if(disponible<a.getCantidad())throw new ReglaNegocioException("Origen de cobertura inconsistente");
            capacidad.put(fuente,disponible-a.getCantidad());
        }
        Map<Long,Integer> saldo=new TreeMap<>(libre);
        for(var p:new TreeMap<>(pedidos).values())for(var d:p.getDetalles()) {
            Long producto=d.getProducto().getId();int falta=d.getCantidad();
            int fisico=Math.min(falta,saldo.getOrDefault(producto,0));
            if(fisico>0) {asignaciones.save(new AsignacionInventario(cobertura,p.getId(),producto,null,fisico,clock.instant()));saldo.put(producto,saldo.get(producto)-fisico);falta-=fisico;}
            for(var entry:capacidad.entrySet())if(falta>0 && entry.getKey().producto().equals(producto)) {
                int tomar=Math.min(falta,entry.getValue());
                if(tomar>0){asignaciones.save(new AsignacionInventario(cobertura,p.getId(),producto,entry.getKey().orden(),tomar,clock.instant()));entry.setValue(entry.getValue()-tomar);falta-=tomar;}
            }
            if(falta!=0)throw new ReglaNegocioException("No se pudo asignar un origen a toda la demanda");
        }
    }
}
