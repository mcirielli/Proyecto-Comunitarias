package ar.edu.uade.comunitarias.controller;
import ar.edu.uade.comunitarias.service.PedidoDemoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @Profile("supabase")
@ConditionalOnProperty(name="app.auth.demo-enabled",havingValue="true")
public class PedidoDemoController {
    private final PedidoDemoService service;
    public PedidoDemoController(PedidoDemoService service) {this.service=service;}
    @PostMapping("/api/demo/pedidos") public ResponseEntity<Void> preparar() {service.preparar();return ResponseEntity.noContent().build();}
}
