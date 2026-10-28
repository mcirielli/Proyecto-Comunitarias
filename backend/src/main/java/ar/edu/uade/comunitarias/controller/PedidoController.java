package ar.edu.uade.comunitarias.controller;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @Profile("supabase") @RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;
    public PedidoController(PedidoService service) {this.service=service;}
    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<java.util.Map<String,String>> exceso(){return ResponseEntity.badRequest().body(java.util.Map.of("error","Cantidad fuera de rango"));}
    @GetMapping public List<PedidoResponse> listar(@AuthenticationPrincipal UsuarioPrincipal actor) {return service.listar(actor.identity());}
    @GetMapping("/contexto") public PedidoContextoResponse contexto(@AuthenticationPrincipal UsuarioPrincipal actor) {return service.contexto(actor.identity());}
    @PostMapping public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request,@AuthenticationPrincipal UsuarioPrincipal actor) {return ResponseEntity.status(201).body(service.crear(request,actor.identity()));}
    @PatchMapping("/{id}/estado") public PedidoResponse estado(@PathVariable Long id,@Valid @RequestBody CambiarEstadoPedidoRequest request) {return service.cambiarEstado(id,request.estado());}
}
