package ar.edu.uade.comunitarias.controller;

import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @Profile("supabase") @RequestMapping("/api/ofertas")
public class OfertaController {
    private final OfertaService service;
    public OfertaController(OfertaService service) { this.service = service; }
    @GetMapping
    public List<OfertaResponse> listar(@AuthenticationPrincipal UsuarioPrincipal actor) {
        return service.listar("OPERACIONES".equals(actor.identity().rol()));
    }
    @PostMapping
    public ResponseEntity<OfertaResponse> crear(@Valid @RequestBody CrearOfertaRequest request) {
        return ResponseEntity.status(201).body(service.crear(request));
    }
    @PostMapping("/{id}/publicacion")
    public OfertaResponse publicar(@PathVariable Long id) { return service.publicar(id); }
}
