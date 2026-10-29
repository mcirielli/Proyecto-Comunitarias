package ar.edu.uade.comunitarias.controller;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @Profile("supabase") @ConditionalOnProperty(name="app.facturacion.enabled",havingValue="true") @RequestMapping("/api/pagos")
public class PagoController {
    private final PagoService pagos;private final FacturacionService facturas;
    public PagoController(PagoService pagos,FacturacionService facturas){this.pagos=pagos;this.facturas=facturas;}
    @GetMapping public List<PagoResponse> listar(@RequestParam Long facturaId,@AuthenticationPrincipal UsuarioPrincipal actor){return facturas.pagos(facturaId,actor.identity());}
    @PostMapping public PagoResponse registrar(@Valid @RequestBody RegistrarPagoRequest request,@AuthenticationPrincipal UsuarioPrincipal actor){return pagos.registrar(request,actor.identity());}
}
