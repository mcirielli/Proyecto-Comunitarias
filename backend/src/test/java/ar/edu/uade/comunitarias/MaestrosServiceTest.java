package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import ar.edu.uade.comunitarias.service.MaestrosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaestrosServiceTest {
    @Test
    void desactivaYReactivaProductoSinCambiarIdentidad() {
        var producto = new ar.edu.uade.comunitarias.domain.Producto("Arroz", null, null, "kg");
        when(productos.findById(1L)).thenReturn(java.util.Optional.of(producto));
        assertFalse(service.cambiarProductoActivo(1L, false).activo());
        assertTrue(service.cambiarProductoActivo(1L, true).activo());
        assertEquals("Arroz", producto.getNombre());
        verify(productos, times(2)).saveAndFlush(producto);
    }
    @Test
    void noPersisteCambioSiProveedorNoExiste() {
        when(proveedores.findById(99L)).thenReturn(java.util.Optional.empty());
        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.cambiarProveedorActivo(99L, false));
        assertEquals(404, error.getStatusCode().value());
        verify(proveedores, never()).saveAndFlush(any());
    }
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final ProveedorRepository proveedores = mock(ProveedorRepository.class);
    private final MaestrosService service = new MaestrosService(productos, proveedores);

    @BeforeEach
    void guardar() {
        when(productos.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        when(proveedores.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void altaProductoNormalizaDatosYComienzaActivo() {
        var response = service.crearProducto(new CrearProductoRequest(" Arroz ", " ", null, " kg "));
        assertEquals("Arroz", response.nombre());
        assertEquals("kg", response.unidadMedida());
        assertNull(response.descripcion());
        assertTrue(response.activo());
        verifyNoInteractions(proveedores);
    }

    @Test
    void nombreVacioNoPersisteProducto() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearProducto(new CrearProductoRequest(" ", null, null, "kg")));
        verify(productos, never()).saveAndFlush(any());
    }

    @Test
    void unidadObligatoriaNoPersisteProducto() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearProducto(new CrearProductoRequest("Arroz", null, null, null)));
        verify(productos, never()).saveAndFlush(any());
    }

    @Test
    void proveedorSinCuitGuardaNullParaNoColisionarConUnicidad() {
        var response = service.crearProveedor(new CrearProveedorRequest(" Mayorista ", " ", "", null));
        assertEquals("Mayorista", response.razonSocial());
        assertNull(response.cuit());
        assertNull(response.email());
        assertTrue(response.activo());
    }

    @Test
    void emailInvalidoNoPersisteProveedor() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearProveedor(new CrearProveedorRequest("Mayorista", null, "sin-arroba", null)));
        verify(proveedores, never()).saveAndFlush(any());
    }

    @Test
    void emailValidoSeConserva() {
        var response = service.crearProveedor(new CrearProveedorRequest("Mayorista", "20", " ventas@example.org ", null));
        assertEquals("ventas@example.org", response.email());
    }

    @Test
    void razonSocialObligatoria() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearProveedor(new CrearProveedorRequest(null, null, null, null)));
        verify(proveedores, never()).saveAndFlush(any());
    }
}
