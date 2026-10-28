package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.state.EstadoPedido;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Profile("supabase") @Transactional(readOnly=true)
public class PedidoService {
    public static final String CLIENTE_DEMO="Kiosco Demo UADE State";
    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final OfertaRepository ofertas;
    private final Clock clock;
    private final boolean demo;
    private StockService inventario;
    @org.springframework.beans.factory.annotation.Autowired(required=false)
    public void configurarInventario(StockService inventario){this.inventario=inventario;}
    public PedidoService(PedidoRepository pedidos,ClienteRepository clientes,OfertaRepository ofertas,Clock clock,
            @Value("${app.auth.demo-enabled:false}") boolean demo) {
        this.pedidos=pedidos;this.clientes=clientes;this.ofertas=ofertas;this.clock=clock;this.demo=demo;
    }
    private LocalDate hoy() { return LocalDate.now(clock.withZone(ZoneId.of("America/Argentina/Buenos_Aires"))); }
    private boolean operaciones(SessionResponse actor) { return "OPERACIONES".equals(actor.rol()); }
    private void permitir(SessionResponse actor) {
        if(!operaciones(actor) && !"CLIENTE".equals(actor.rol())) throw new ReglaNegocioException("Perfil sin acceso a pedidos");
    }
    private Cliente clientePropio(SessionResponse actor) {
        if(demo && Long.valueOf(-1).equals(actor.id()) && "cliente@demo.local".equals(actor.email())) {
            var coincidencias=clientes.findByNombreComercio(CLIENTE_DEMO);
            if(coincidencias.size()!=1) throw new ReglaNegocioException("Operaciones debe preparar los datos demo antes de usar esta cuenta");
            return coincidencias.get(0);
        }
        return clientes.findByUsuarioId(actor.id()).orElseThrow(() -> new ReglaNegocioException("La cuenta no tiene cliente asociado"));
    }
    public List<PedidoResponse> listar(SessionResponse actor) {
        permitir(actor);
        return (operaciones(actor)?pedidos.findAllByOrderByIdDesc():pedidos.findByClienteIdOrderByIdDesc(clientePropio(actor).getId()))
            .stream().map(PedidoResponse::from).toList();
    }
    public PedidoContextoResponse contexto(SessionResponse actor) {
        permitir(actor);
        var listaClientes=operaciones(actor)?clientes.findAll().stream().filter(Cliente::isActivo).toList():List.of(clientePropio(actor));
        var listaOfertas=ofertas.findAll().stream().filter(o -> o.vigente(hoy())).sorted(Comparator.comparing(Oferta::getId).reversed())
            .map(o -> new PedidoContextoResponse.OfertaItem(o.getId(),o.getTitulo(),o.getFechaInicio(),o.getFechaFin(),o.getDetalles().stream()
                .filter(d -> d.getProducto().getActivo()).map(d -> new PedidoContextoResponse.ProductoItem(d.getProducto().getId(),d.getProducto().getNombre(),d.getPrecioVenta())).toList())).toList();
        return new PedidoContextoResponse(listaClientes.stream().map(c -> new PedidoContextoResponse.ClienteItem(c.getId(),c.getNombreComercio())).toList(),listaOfertas);
    }
    @Transactional
    public PedidoResponse crear(CrearPedidoRequest request,SessionResponse actor) {
        permitir(actor);
        Cliente cliente;
        if(operaciones(actor)) {
            if(request.clienteId()==null) throw new IllegalArgumentException("Seleccione un cliente");
            cliente=clientes.findById(request.clienteId()).orElseThrow(() -> new RecursoNoEncontradoException("Cliente inexistente"));
        } else {
            cliente=clientePropio(actor);
            if(request.clienteId()!=null && !request.clienteId().equals(cliente.getId())) throw new ReglaNegocioException("No puede crear pedidos de otro cliente");
        }
        if(!cliente.isActivo()) throw new ReglaNegocioException("Cliente inactivo");
        var oferta=ofertas.findById(request.ofertaId()).orElseThrow(() -> new RecursoNoEncontradoException("Oferta inexistente"));
        if(!oferta.vigente(hoy())) throw new ReglaNegocioException("La oferta no está publicada o está fuera de vigencia");
        if(request.detalles()==null || request.detalles().isEmpty() || request.detalles().size()>100) throw new IllegalArgumentException("Pedido vacío o demasiado grande");
        var pedido=new Pedido(cliente,oferta,clock.instant());
        for(var item:request.detalles()) {
            if(item==null) throw new IllegalArgumentException("Detalle vacío");
            var precio=oferta.getDetalles().stream().filter(d -> d.getProducto().getId().equals(item.productoId())).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Producto fuera de la oferta"));
            if(!precio.getProducto().getActivo()) throw new ReglaNegocioException("Producto inactivo");
            pedido.agregar(precio.getProducto(),item.cantidad(),precio.getPrecioVenta());
        }
        return PedidoResponse.from(pedidos.saveAndFlush(pedido));
    }
    @Transactional
    public PedidoResponse cambiarEstado(Long id,EstadoPedido destino) {
        var pedido=pedidos.buscarParaCambiar(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido inexistente"));
        pedido.cambiarEstado(destino,clock.instant());
        if(inventario!=null) {
            var authentication=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if(authentication==null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal) || !"OPERACIONES".equals(principal.identity().rol()))
                throw new org.springframework.security.access.AccessDeniedException("Operaciones requerido");
            inventario.transicion(pedido,destino,principal.identity().email());
        }
        return PedidoResponse.from(pedidos.saveAndFlush(pedido));
    }
}
