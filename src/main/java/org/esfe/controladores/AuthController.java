package org.esfe.controladores;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.esfe.seguridad.dtos.*;
import org.esfe.seguridad.servicios.UsuarioService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final UsuarioService usuarios;

  @PostMapping("/login")
  public Map<String, Object> login(@Valid @RequestBody UsuarioLogin d) {
    return usuarios.login(d);
  }

  @PostMapping("/usuarios")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> registrar(@Valid @RequestBody UsuarioRegistrar d) {
    return ResponseEntity.status(201).body(usuarios.registrar(d));
  }

  @GetMapping("/me")
  public Map<String, String> me(Principal p) {
    return Map.of("login", p.getName());
  }
}
