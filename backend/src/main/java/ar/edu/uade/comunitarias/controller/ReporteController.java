package ar.edu.uade.comunitarias.controller;

import ar.edu.uade.comunitarias.dto.ReporteClientesResponse;
import ar.edu.uade.comunitarias.service.ReporteService;
import ar.edu.uade.comunitarias.service.UsuarioPrincipal;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @Profile("supabase") @RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService service;

    public ReporteController(ReporteService service) { this.service = service; }

    @GetMapping("/clientes")
    public ReporteClientesResponse clientes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long clienteId,
            @AuthenticationPrincipal UsuarioPrincipal actor) {
        return service.clientes(actor.identity(), desde, hasta, clienteId);
    }
}
