package org.esfe.seguridad.servicios;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.esfe.excepciones.NegocioException;
import org.esfe.seguridad.dtos.*;
import org.esfe.seguridad.modelos.*;
import org.esfe.seguridad.repositorios.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
  private final UsuarioRepository usuarios;
  private final RolRepository roles;
  private final PasswordEncoder encoder;
  private final AuthenticationManager auth;
  private final JwtService jwt;

  public Map<String, Object> login(UsuarioLogin dto) {
    String login = dto.login().trim().toLowerCase(Locale.ROOT);
    auth.authenticate(new UsernamePasswordAuthenticationToken(login, dto.clave()));
    Usuario u = usuarios.findByLogin(login).orElseThrow();
    return Map.of(
        "token",
        jwt.getToken(u),
        "tipo",
        "Bearer",
        "expiraEn",
        3600,
        "nombre",
        u.getNombre(),
        "rol",
        u.getRol().getNombre());
  }

  @Transactional
  public Map<String, Object> registrar(UsuarioRegistrar d) {
    String login = d.login().trim().toLowerCase(Locale.ROOT);
    if (usuarios.findByLogin(login).isPresent())
      throw new NegocioException(409, "El correo ya está registrado");
    Rol rol =
        roles.findAll().stream()
            .filter(r -> r.getNombre().equals(d.rol()))
            .findFirst()
            .orElseThrow(() -> new NegocioException(400, "Rol inválido"));
    Usuario u =
        Usuario.builder()
            .nombre(d.nombre().trim())
            .apellido(d.apellido().trim())
            .login(login)
            .clave(encoder.encode(d.clave()))
            .rol(rol)
            .build();
    usuarios.save(u);
    return Map.of("id", u.getId(), "login", u.getLogin(), "rol", rol.getNombre());
  }
}
