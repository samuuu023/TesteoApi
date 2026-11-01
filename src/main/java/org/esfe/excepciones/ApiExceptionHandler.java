package org.esfe.excepciones;

import java.time.Instant;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
  private ResponseEntity<?> error(int s, String msg) {
    return ResponseEntity.status(s)
        .body(Map.of("estado", s, "mensaje", msg, "fecha", Instant.now().toString()));
  }

  @ExceptionHandler(NegocioException.class)
  ResponseEntity<?> negocio(NegocioException e) {
    return error(e.getEstado(), e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validacion(MethodArgumentNotValidException e) {
    return error(
        400,
        e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Datos inválidos"));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<?> formato(Exception e) {
    return error(400, "Revise el formato de los datos enviados");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> integridad(Exception e) {
    return error(409, "Registro duplicado o relacionado con otros datos");
  }

  @ExceptionHandler({
    org.springframework.orm.ObjectOptimisticLockingFailureException.class,
    org.springframework.dao.CannotAcquireLockException.class
  })
  ResponseEntity<?> concurrencia(Exception e) {
    return error(409, "El registro cambió; actualice y vuelva a intentar");
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<?> credenciales(Exception e) {
    return error(401, "Credenciales incorrectas");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<?> denegado(Exception e) {
    return error(403, "Permisos insuficientes");
  }
}
