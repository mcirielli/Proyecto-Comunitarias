package ar.edu.uade.comunitarias.controller;

import java.util.List;
import jakarta.validation.Valid;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.MaestrosService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("supabase")
@RequestMapping("/api")
public class MaestrosController {
    @PatchMapping("/productos/{id}/activo")
    public ProductoResponse cambiarProducto(@PathVariable Long id, @Valid @RequestBody CambiarActivoRequest request) {
        return service.cambiarProductoActivo(id, request.activo());
    }
    @PatchMapping("/proveedores/{id}/activo")
    public ProveedorResponse cambiarProveedor(@PathVariable Long id, @Valid @RequestBody CambiarActivoRequest request) {
        return service.cambiarProveedorActivo(id, request.activo());
    }
    private final MaestrosService service;
    public MaestrosController(MaestrosService service) { this.service = service; }
    @GetMapping("/productos")
    public List<ProductoResponse> productos() { return service.productos(); }
    @PostMapping("/productos")
    public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody CrearProductoRequest request) {
        return ResponseEntity.status(201).body(service.crearProducto(request));
    }
    @GetMapping("/proveedores")
    public List<ProveedorResponse> proveedores() { return service.proveedores(); }
    @PostMapping("/proveedores")
    public ResponseEntity<ProveedorResponse> crearProveedor(@Valid @RequestBody CrearProveedorRequest request) {
        return ResponseEntity.status(201).body(service.crearProveedor(request));
    }
}
