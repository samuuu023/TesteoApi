package org.esfe.repositorios;

import org.esfe.modelos.Cliente;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
  Page<Cliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

  boolean existsByEmailIgnoreCaseAndIdNot(String valor, Integer id);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Cliente e where e.id=:id")
  java.util.Optional<Cliente> bloquear(
      @org.springframework.data.repository.query.Param("id") Integer id);
}
