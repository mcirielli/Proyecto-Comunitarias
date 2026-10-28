package ar.edu.uade.comunitarias.controller;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
@RestController @Profile("supabase")
public class InventarioDisponibilidadController {
    private final boolean habilitado;
    public InventarioDisponibilidadController(@Value("${app.inventario.enabled:false}") boolean habilitado){this.habilitado=habilitado;}
    @GetMapping("/api/stock/disponibilidad") public Map<String,Boolean> disponibilidad(){return Map.of("habilitado",habilitado);}
}
