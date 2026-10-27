package ar.edu.uade.comunitarias.state;
public interface PedidoState {
    EstadoPedido codigo();
    PedidoState siguiente();
    PedidoState cambiarA(EstadoPedido destino);
}
