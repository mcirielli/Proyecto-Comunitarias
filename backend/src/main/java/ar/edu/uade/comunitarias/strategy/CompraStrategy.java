package ar.edu.uade.comunitarias.strategy;

import java.util.Map;

/** Los algoritmos reciben cantidades verificadas por el caso de uso, sin JPA ni HTTP. */
public interface CompraStrategy {
    Map<Long, Integer> calcular(ContextoCompra contexto);
}
