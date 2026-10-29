package ar.edu.uade.comunitarias.controller;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.*;
import java.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController @Profile("supabase")
@ConditionalOnProperty(name="app.inventario.enabled",havingValue="true")
public class InventarioController {
    private final StockConsultaService consultas;
    private final RecepcionService recepcion;
    public InventarioController(StockConsultaService consultas,RecepcionService recepcion) {
        this.consultas=consultas;this.recepcion=recepcion;
    }
    @GetMapping("/api/stock") @Transactional(readOnly=true)
    public List<StockConsultaService.Saldo> stock() {return consultas.listar();}
    @GetMapping("/api/stock/{id}/movimientos") @Transactional(readOnly=true)
    public List<StockConsultaService.Movimiento> movimientos(@PathVariable Long id,@RequestParam(defaultValue="0") int pagina) {return consultas.movimientos(id,pagina);}
    @PostMapping("/api/compras/{id}/recepcion")
    public CompraResponse recibir(@PathVariable Long id,@AuthenticationPrincipal UsuarioPrincipal actor,@RequestBody(required=false) String contenido) {
        if(contenido!=null && !contenido.isBlank())throw new IllegalArgumentException("Recepción total sin cantidades manuales");
        return recepcion.recibir(id,actor.identity());
    }
    @GetMapping("/api/compras/{id}/recepcion")
    public RecepcionService.Recepcion recepcion(@PathVariable Long id){return recepcion.consultar(id);}
    @ExceptionHandler(ArithmeticException.class)
    public org.springframework.http.ResponseEntity<Map<String,String>> exceso(){return org.springframework.http.ResponseEntity.badRequest().body(Map.of("error","Cantidad fuera de rango"));}
}
