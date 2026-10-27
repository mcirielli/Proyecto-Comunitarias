package ar.edu.uade.comunitarias.state;
/** Regla común; cada clase concreta decide el sucesor permitido. */
public abstract class EstadoSecuencial implements PedidoState {
    @Override public PedidoState cambiarA(EstadoPedido destino) {
        PedidoState proximo = siguiente();
        if (proximo == null || proximo.codigo() != destino) {
            throw new TransicionInvalidaException("No se permite pasar de " + codigo() + " a " + destino);
        }
        return proximo;
    }
}
