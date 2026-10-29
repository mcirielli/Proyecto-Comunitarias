package ar.edu.uade.comunitarias.controller;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @Profile("supabase") @ConditionalOnProperty(name="app.facturacion.enabled",havingValue="true") @RequestMapping("/api/facturas")
public class FacturaController {
    private final FacturacionService service;
    public FacturaController(FacturacionService service){this.service=service;}
    @GetMapping public List<FacturaResponse> listar(@AuthenticationPrincipal UsuarioPrincipal actor){return service.listar(actor.identity());}
    @GetMapping("/contexto") public List<FacturacionService.Elegible> contexto(@AuthenticationPrincipal UsuarioPrincipal actor){return service.contexto(actor.identity());}
    @GetMapping("/{id}") public FacturaResponse detalle(@PathVariable Long id,@AuthenticationPrincipal UsuarioPrincipal actor){return service.detalle(id,actor.identity());}
    @PostMapping public FacturaResponse emitir(@Valid @RequestBody EmitirFacturaRequest request,@AuthenticationPrincipal UsuarioPrincipal actor){return service.emitir(request,actor.identity());}
}
