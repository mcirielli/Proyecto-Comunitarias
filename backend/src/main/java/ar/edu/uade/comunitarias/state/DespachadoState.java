package ar.edu.uade.comunitarias.state;
public final class DespachadoState extends EstadoSecuencial {
    @Override public EstadoPedido codigo() { return EstadoPedido.DESPACHADO; }
    @Override public PedidoState siguiente() { return new EntregadoState(); }
}
