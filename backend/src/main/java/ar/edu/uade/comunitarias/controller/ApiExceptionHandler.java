package ar.edu.uade.comunitarias.controller;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({ar.edu.uade.comunitarias.state.TransicionInvalidaException.class, ar.edu.uade.comunitarias.domain.ReglaNegocioException.class})
    public ResponseEntity<Map<String,String>> business(RuntimeException exception) {
        return ResponseEntity.status(409).body(Map.of("error", exception.getMessage()));
    }
    @ExceptionHandler(ar.edu.uade.comunitarias.domain.RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String,String>> missing(RuntimeException exception) {
        return ResponseEntity.status(404).body(Map.of("error", exception.getMessage()));
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String,String>> invalid(Exception exception) {
        return ResponseEntity.badRequest().body(Map.of("error", "Datos de entrada inválidos"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(409).body(Map.of("error", "La operación viola una restricción de datos"));
    }
}
