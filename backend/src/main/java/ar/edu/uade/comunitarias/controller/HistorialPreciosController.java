package ar.edu.uade.comunitarias.controller;

import ar.edu.uade.comunitarias.dto.HistorialPreciosResponse;
import ar.edu.uade.comunitarias.service.HistorialPreciosService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/** Acceso restringido a Operaciones por la regla existente de /api/productos/** en SecurityConfig. */
@RestController @Profile("supabase")
public class HistorialPreciosController {
    private final HistorialPreciosService service;

    public HistorialPreciosController(HistorialPreciosService service) { this.service = service; }

    @GetMapping("/api/productos/{id}/precios")
    public HistorialPreciosResponse historial(@PathVariable Long id) { return service.historial(id); }
}
