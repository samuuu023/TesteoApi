package org.esfe.repositorios;

import org.esfe.modelos.Barbero;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IBarberoRepository extends JpaRepository<Barbero, Integer> {
  Page<Barbero> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

  boolean existsByTelefonoAndIdNot(String valor, Integer id);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Barbero e where e.id=:id")
  java.util.Optional<Barbero> bloquear(
      @org.springframework.data.repository.query.Param("id") Integer id);
}
