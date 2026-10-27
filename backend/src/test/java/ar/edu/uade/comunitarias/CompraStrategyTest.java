package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.strategy.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompraStrategyTest {
    final CompraStrategy demanda = new CompraPorDemandaStrategy();
    final CompraStrategy quinteros = new CompraQuinterosStrategy();
    ContextoCompra contexto(List<Map<Long,Integer>> pedidos, Map<Long,Integer> manual,
            Map<Long,Integer> stock, Map<Long,Integer> abiertas) {
        return new ContextoCompra(pedidos, manual, stock, abiertas);
    }
    @Test void consolidaPedidosYDescuentaCoberturaUnaSolaVez() {
        assertEquals(Map.of(1L, 5, 2L, 3), demanda.calcular(contexto(
            List.of(Map.of(1L, 7, 2L, 3), Map.of(1L, 6)), Map.of(), Map.of(1L, 5), Map.of(1L, 3))));
    }
    @Test void stockSuficienteNoCompra() {
        assertTrue(demanda.calcular(contexto(List.of(Map.of(1L, 4)), Map.of(), Map.of(1L, 4), Map.of())).isEmpty());
    }
    @Test void ordenAbiertaPuedeCubrirTodo() {
        assertTrue(demanda.calcular(contexto(List.of(Map.of(1L, 4)), Map.of(), Map.of(), Map.of(1L, 7))).isEmpty());
    }
    @Test void quinterosNoDependeDePedidosNiDescuentaStock() {
        assertEquals(Map.of(1L, 4), quinteros.calcular(contexto(List.of(), Map.of(1L, 4), Map.of(1L, 99), Map.of(1L, 99))));
    }
    @Test void cantidadesInvalidasSeRechazanEnAmbosAlgoritmos() {
        for (int cantidad : List.of(0, -1)) {
            assertThrows(IllegalArgumentException.class, () -> demanda.calcular(contexto(List.of(Map.of(1L, cantidad)), Map.of(), Map.of(), Map.of())));
            assertThrows(IllegalArgumentException.class, () -> quinteros.calcular(contexto(List.of(), Map.of(1L, cantidad), Map.of(), Map.of())));
        }
    }
    @Test void rechazaMezclarModos() {
        var mixto = contexto(List.of(Map.of(1L, 1)), Map.of(1L, 2), Map.of(), Map.of());
        assertThrows(IllegalArgumentException.class, () -> demanda.calcular(mixto));
        assertThrows(IllegalArgumentException.class, () -> quinteros.calcular(mixto));
    }
    @Test void evitaOverflowDeConsolidacion() {
        assertThrows(ArithmeticException.class, () -> demanda.calcular(contexto(List.of(Map.of(1L, Integer.MAX_VALUE), Map.of(1L, 1)), Map.of(), Map.of(), Map.of())));
    }
    @Test void sumaCoberturasSinOverflow() {
        assertTrue(demanda.calcular(contexto(List.of(Map.of(1L, Integer.MAX_VALUE)), Map.of(), Map.of(1L, Integer.MAX_VALUE), Map.of(1L, 1))).isEmpty());
    }
}
