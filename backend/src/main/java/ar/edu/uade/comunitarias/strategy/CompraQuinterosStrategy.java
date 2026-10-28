package ar.edu.uade.comunitarias.strategy;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CompraQuinterosStrategy implements CompraStrategy {
    @Override
    public Map<Long, Integer> calcular(ContextoCompra contexto) {
        if (!contexto.pedidos().isEmpty() || contexto.productos().isEmpty())
            throw new IllegalArgumentException("Quinteros requiere productos y no utiliza pedidos");
        contexto.productos().forEach(CompraPorDemandaStrategy::validar);
        return Map.copyOf(contexto.productos());
    }
}
