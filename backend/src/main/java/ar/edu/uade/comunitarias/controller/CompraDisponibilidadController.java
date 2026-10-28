package ar.edu.uade.comunitarias.controller;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompraDisponibilidadController {
    private final boolean enabled;
    public CompraDisponibilidadController(@Value("${app.compras.enabled:false}") boolean enabled) { this.enabled=enabled; }
    @GetMapping("/api/compras/disponibilidad")
    public Map<String,Object> disponibilidad() {
        return Map.of("habilitado",enabled,"mensaje", enabled ? "Módulo habilitado" : "Compras requiere la migración aprobada y su habilitación. Consultá la documentación del módulo.");
    }
}
