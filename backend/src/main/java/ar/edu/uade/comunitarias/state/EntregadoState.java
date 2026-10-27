package ar.edu.uade.comunitarias.state;
public final class EntregadoState extends EstadoSecuencial {
    @Override public EstadoPedido codigo() { return EstadoPedido.ENTREGADO; }
    @Override public PedidoState siguiente() { return null; }
}
