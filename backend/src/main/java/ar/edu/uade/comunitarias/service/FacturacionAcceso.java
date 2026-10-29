package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.SessionResponse;
import ar.edu.uade.comunitarias.repository.ClienteRepository;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
@Component @Profile("supabase") @ConditionalOnProperty(name="app.facturacion.enabled",havingValue="true")
public class FacturacionAcceso {
    private final ClienteRepository clientes;private final boolean demo;
    public FacturacionAcceso(ClienteRepository clientes,@Value("${app.auth.demo-enabled:false}") boolean demo){this.clientes=clientes;this.demo=demo;}
    public void consulta(SessionResponse actor){if(actor==null || !java.util.Set.of("OPERACIONES","GERENCIA","CLIENTE").contains(actor.rol()))throw new AccessDeniedException("Sin acceso a facturación");}
    public void operacion(SessionResponse actor){if(actor==null || !"OPERACIONES".equals(actor.rol()))throw new AccessDeniedException("Solo Operaciones puede registrar");}
    public Long clientePropio(SessionResponse actor){
        consulta(actor);
        if(!"CLIENTE".equals(actor.rol()))return null;
        Cliente cliente;
        if(demo && Long.valueOf(-1).equals(actor.id()) && "cliente@demo.local".equals(actor.email())){
            var lista=clientes.findByNombreComercio(PedidoService.CLIENTE_DEMO);
            if(lista.size()!=1)throw new ReglaNegocioException("Operaciones debe preparar la asociación del cliente demo");
            cliente=lista.get(0);
        }else cliente=clientes.findByUsuarioId(actor.id()).orElseThrow(()->new ReglaNegocioException("La cuenta no tiene cliente asociado"));
        return cliente.getId();
    }
    public void comprobar(Factura factura,SessionResponse actor){
        Long id=clientePropio(actor);
        if(id!=null && !id.equals(factura.getPedido().getCliente().getId()))throw new RecursoNoEncontradoException("Factura inexistente");
    }
}
