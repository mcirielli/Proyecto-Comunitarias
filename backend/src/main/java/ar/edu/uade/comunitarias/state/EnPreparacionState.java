package ar.edu.uade.comunitarias.state;
public final class EnPreparacionState extends EstadoSecuencial {
    @Override public EstadoPedido codigo() { return EstadoPedido.EN_PREPARACION; }
    @Override public PedidoState siguiente() { return new PreparadoState(); }
}
