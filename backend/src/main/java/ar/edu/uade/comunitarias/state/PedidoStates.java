package ar.edu.uade.comunitarias.state;
import java.util.Map;
import java.util.function.Supplier;
/** Registro solo para reconstrucción; las transiciones pertenecen a los objetos State. */
public final class PedidoStates {
    private PedidoStates() {}
    private static final Map<EstadoPedido, Supplier<PedidoState>> FACTORY = Map.of(
        EstadoPedido.RECIBIDO, RecibidoState::new,
        EstadoPedido.CONFIRMADO, ConfirmadoState::new,
        EstadoPedido.EN_PREPARACION, EnPreparacionState::new,
        EstadoPedido.PREPARADO, PreparadoState::new,
        EstadoPedido.DESPACHADO, DespachadoState::new,
        EstadoPedido.ENTREGADO, EntregadoState::new);
    public static PedidoState reconstruir(EstadoPedido estado) {
        var supplier = FACTORY.get(estado);
        if (supplier == null) throw new IllegalArgumentException("Estado de pedido desconocido");
        return supplier.get();
    }
}
