package ar.edu.uade.comunitarias.state;
public final class RecibidoState extends EstadoSecuencial {
    @Override public EstadoPedido codigo() { return EstadoPedido.RECIBIDO; }
    @Override public PedidoState siguiente() { return new ConfirmadoState(); }
}
