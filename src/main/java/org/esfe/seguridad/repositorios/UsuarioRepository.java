package org.esfe.seguridad.repositorios;

import java.util.Optional;
import org.esfe.seguridad.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
  Optional<Usuario> findByLogin(String username);
}
