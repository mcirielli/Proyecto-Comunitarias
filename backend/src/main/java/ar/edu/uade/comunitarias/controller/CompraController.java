package ar.edu.uade.comunitarias.controller;
import ar.edu.uade.comunitarias.domain.TipoCompra;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @Profile("supabase") @RequestMapping("/api/compras")
@ConditionalOnProperty(name="app.compras.enabled", havingValue="true")
public class CompraController {
    private final CompraService service;
    public CompraController(CompraService service) { this.service=service; }
    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<Map<String,String>> exceso() {
        return ResponseEntity.badRequest().body(Map.of("error","Las cantidades consolidadas exceden el máximo permitido"));
    }
    @GetMapping public List<CompraResponse> listar(@AuthenticationPrincipal UsuarioPrincipal actor) { return service.listar(actor.identity()); }
    @GetMapping("/contexto") public Map<String,Object> contexto(@AuthenticationPrincipal UsuarioPrincipal actor) { return service.contexto(actor.identity()); }
    @PostMapping("/demanda") public ResponseEntity<ResultadoCompraResponse> demanda(@Valid @RequestBody CrearCompraRequest request,@AuthenticationPrincipal UsuarioPrincipal actor) {
        return ResponseEntity.status(201).body(service.crear(TipoCompra.DEMANDA_CLIENTES,request,actor.identity()));
    }
    @PostMapping("/quinteros") public ResponseEntity<ResultadoCompraResponse> quinteros(@Valid @RequestBody CrearCompraRequest request,@AuthenticationPrincipal UsuarioPrincipal actor) {
        return ResponseEntity.status(201).body(service.crear(TipoCompra.STOCK_QUINTEROS,request,actor.identity()));
    }
    @PostMapping("/{id}/autorizar") public CompraResponse autorizar(@PathVariable Long id,@AuthenticationPrincipal UsuarioPrincipal actor) { return service.autorizar(id,actor.identity()); }
    @PostMapping("/{id}/emitir") public CompraResponse emitir(@PathVariable Long id,@AuthenticationPrincipal UsuarioPrincipal actor) { return service.emitir(id,actor.identity()); }
}
