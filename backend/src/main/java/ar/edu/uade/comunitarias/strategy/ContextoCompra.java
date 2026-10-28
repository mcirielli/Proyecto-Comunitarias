package ar.edu.uade.comunitarias.strategy;

import java.util.List;
import java.util.Map;

public record ContextoCompra(List<Map<Long, Integer>> pedidos, Map<Long, Integer> productos,
        Map<Long, Integer> disponible, Map<Long, Integer> comprasAbiertas) {
    public ContextoCompra {
        pedidos = pedidos.stream().map(Map::copyOf).toList();
        productos = Map.copyOf(productos);
        disponible = Map.copyOf(disponible);
        comprasAbiertas = Map.copyOf(comprasAbiertas);
    }
}
