package ar.edu.uade.comunitarias.state;
public final class ConfirmadoState extends EstadoSecuencial {
    @Override public EstadoPedido codigo() { return EstadoPedido.CONFIRMADO; }
    @Override public PedidoState siguiente() { return new EnPreparacionState(); }
}
