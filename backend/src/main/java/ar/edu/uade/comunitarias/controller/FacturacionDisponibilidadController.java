package ar.edu.uade.comunitarias.controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
public class FacturacionDisponibilidadController {
    private final boolean enabled;
    public FacturacionDisponibilidadController(@Value("${app.facturacion.enabled:false}") boolean enabled){this.enabled=enabled;}
    @GetMapping("/api/facturas/disponibilidad") public Map<String,Object> disponibilidad(){return Map.of("habilitado",enabled,"mensaje",enabled ? "Facturación interna habilitada" : "Facturación no está habilitada en este entorno");}
}
