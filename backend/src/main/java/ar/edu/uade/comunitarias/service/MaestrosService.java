package ar.edu.uade.comunitarias.service;

import java.util.List;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("supabase")
@Transactional(readOnly=true)

public class MaestrosService {

    @Transactional
    public ProductoResponse cambiarProductoActivo(Long id, boolean activo) {
        var producto = productos.findById(id).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Producto inexistente"));
        producto.cambiarActivo(activo);
        return ProductoResponse.from(productos.saveAndFlush(producto));
    }

    @Transactional
    public ProveedorResponse cambiarProveedorActivo(Long id, boolean activo) {
        var proveedor = proveedores.findById(id).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Proveedor inexistente"));
        proveedor.cambiarActivo(activo);
        return ProveedorResponse.from(proveedores.saveAndFlush(proveedor));
    }


    private final ProductoRepository productos;
    private final ProveedorRepository proveedores;
    public MaestrosService(ProductoRepository productos, ProveedorRepository proveedores) {
        this.productos = productos;
        this.proveedores = proveedores;
    }

    public List<ProductoResponse> productos() {
        return productos.findAll().stream().map(ProductoResponse::from).toList();
    }

    public List<ProveedorResponse> proveedores() {
        return proveedores.findAll().stream().map(ProveedorResponse::from).toList();
    }

    @Transactional
    public ProductoResponse crearProducto(CrearProductoRequest request) {
        var producto = new Producto(required(request.nombre(), "nombre"),
                optional(request.descripcion()), optional(request.categoria()),
                required(request.unidadMedida(), "unidadMedida"));
        return ProductoResponse.from(productos.saveAndFlush(producto));
    }


    @Transactional
    public ProveedorResponse crearProveedor(CrearProveedorRequest request) {
        var email = optional(request.email());
        if (email != null && !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Email inválido");
        }
        var proveedor = new Proveedor(required(request.razonSocial(), "razonSocial"),
                optional(request.cuit()), email, optional(request.telefono()));
        return ProveedorResponse.from(proveedores.saveAndFlush(proveedor));
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " es obligatorio");
        return value.trim();
    }
    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
