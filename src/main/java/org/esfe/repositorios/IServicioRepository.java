package org.esfe.repositorios;

import org.esfe.modelos.Servicio;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IServicioRepository extends JpaRepository<Servicio, Integer> {
  Page<Servicio> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

  boolean existsByNombreIgnoreCaseAndIdNot(String valor, Integer id);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Servicio e where e.id=:id")
  java.util.Optional<Servicio> bloquear(
      @org.springframework.data.repository.query.Param("id") Integer id);
}
