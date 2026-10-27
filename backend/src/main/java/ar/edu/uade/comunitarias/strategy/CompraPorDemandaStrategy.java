package ar.edu.uade.comunitarias.strategy;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

@Component
public class CompraPorDemandaStrategy implements CompraStrategy {
    @Override
    public Map<Long, Integer> calcular(ContextoCompra contexto) {
        if (contexto.pedidos().isEmpty() || !contexto.productos().isEmpty())
            throw new IllegalArgumentException("Demanda requiere pedidos, sin cantidades manuales");
        Map<Long, Integer> demanda = new TreeMap<>();
        for (var pedido : contexto.pedidos()) {
            if (pedido.isEmpty()) throw new IllegalArgumentException("Pedido vacío");
            pedido.forEach((id, cantidad) -> {
                validar(id, cantidad);
                demanda.merge(id, cantidad, Math::addExact);
            });
        }
        Map<Long, Integer> resultado = new TreeMap<>();
        demanda.forEach((id, cantidad) -> {
            int stock = contexto.disponible().getOrDefault(id, 0);
            int abierta = contexto.comprasAbiertas().getOrDefault(id, 0);
            if (stock < 0 || abierta < 0) throw new IllegalArgumentException("Cobertura negativa");
            long faltante = Math.max(0L, (long) cantidad - stock - abierta);
            if (faltante > 0) resultado.put(id, Math.toIntExact(faltante));
        });
        return Map.copyOf(resultado);
    }
    static void validar(Long id, Integer cantidad) {
        if (id == null || id <= 0 || cantidad == null || cantidad <= 0)
            throw new IllegalArgumentException("Producto o cantidad inválidos");
    }
}
