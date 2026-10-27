package ar.edu.uade.comunitarias;

import ar.edu.uade.comunitarias.controller.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void integrityErrorDoesNotExposeInternalMessage() {
        var response = handler.conflict(new DataIntegrityViolationException(
                "SQL insert into proveedores; password=TEST_SENTINEL"));
        assertEquals(409, response.getStatusCode().value());
        assertEquals(java.util.Map.of("error", "La operación viola una restricción de datos"), response.getBody());
    }

    @Test
    void validationErrorDoesNotExposeInternalMessage() {
        var response = handler.invalid(new IllegalArgumentException("TEST_SENTINEL"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals(java.util.Map.of("error", "Datos de entrada inválidos"), response.getBody());
    }
}
