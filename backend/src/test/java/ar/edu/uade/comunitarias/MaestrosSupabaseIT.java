package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.service.MaestrosService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

/** Selección explícita; cada transacción termina en rollback, también ante duplicados. */
@SpringBootTest
@ActiveProfiles("supabase")
@ContextConfiguration(initializers = SupabaseTestEnvironment.class)
@Transactional
class MaestrosSupabaseIT {
    @Autowired MaestrosService service;
    @Autowired EntityManager entityManager;

    @Test
    void altaYConsultaProductoDesdeBase() {
        String nombre = "IT_COMUNITARIAS_" + UUID.randomUUID();
        var creado = service.crearProducto(new CrearProductoRequest(nombre, null, null, "kg"));
        assertNotNull(creado.id());
        entityManager.clear();
        var leido = service.productos().stream().filter(p -> p.id().equals(creado.id())).findFirst().orElseThrow();
        assertEquals(creado, leido);
        service.cambiarProductoActivo(creado.id(), false);
        entityManager.clear();
        assertFalse(service.productos().stream().filter(p -> p.id().equals(creado.id())).findFirst().orElseThrow().activo());
        service.cambiarProductoActivo(creado.id(), true);
        entityManager.clear();
        assertTrue(service.productos().stream().filter(p -> p.id().equals(creado.id())).findFirst().orElseThrow().activo());
    }

    @Test
    void altaYConsultaProveedorDesdeBase() {
        var creado = service.crearProveedor(new CrearProveedorRequest(
                "IT_COMUNITARIAS_" + UUID.randomUUID(), null, "ficticio@example.invalid", null));
        assertNotNull(creado.id());
        entityManager.clear();
        var leido = service.proveedores().stream().filter(p -> p.id().equals(creado.id())).findFirst().orElseThrow();
        assertEquals(creado, leido);
    }

    @Test
    void rechazaCuitDuplicado() {
        // Prefijo no fiscal para evitar confusión con una persona real. No se valida CUIT fiscal en este incremento.
        String cuit = "IT_" + UUID.randomUUID().toString().substring(0, 8);
        service.crearProveedor(new CrearProveedorRequest("IT_COMUNITARIAS_A", cuit, null, null));
        assertThrows(DataIntegrityViolationException.class, () -> service.crearProveedor(
                new CrearProveedorRequest("IT_COMUNITARIAS_B", cuit, null, null)));
        // No continuar consultando: PostgreSQL deja la transacción abortada hasta el rollback.
    }
}
