package org.esfe.config;

import lombok.RequiredArgsConstructor;
import org.esfe.seguridad.modelos.*;
import org.esfe.seguridad.repositorios.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {
  private final RolRepository roles;
  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;

  @Value("${app.admin.login}")
  private String login;

  @Value("${app.admin.password}")
  private String clave;

  @Override
  @Transactional
  public void run(String... args) {
    for (String nombre : new String[] {"ROLE_ADMIN", "ROLE_USER"})
      if (roles.findAll().stream().noneMatch(r -> r.getNombre().equals(nombre)))
        roles.save(Rol.builder().nombre(nombre).build());
    if (usuarios.count() == 0) {
      Rol rol =
          roles.findAll().stream()
              .filter(r -> r.getNombre().equals("ROLE_ADMIN"))
              .findFirst()
              .orElseThrow();
      usuarios.save(
          Usuario.builder()
              .nombre("Administrador")
              .apellido("Barbería")
              .login(login.trim().toLowerCase())
              .clave(encoder.encode(clave))
              .rol(rol)
              .build());
    }
  }
}
