package ar.edu.uade.comunitarias.state;
public final class PreparadoState extends EstadoSecuencial {
    @Override public EstadoPedido codigo() { return EstadoPedido.PREPARADO; }
    @Override public PedidoState siguiente() { return new DespachadoState(); }
}
