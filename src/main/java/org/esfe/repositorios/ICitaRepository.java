package org.esfe.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import org.esfe.modelos.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ICitaRepository
    extends JpaRepository<Cita, Integer>, JpaSpecificationExecutor<Cita> {
  boolean existsByCliente_Id(Integer id);

  boolean existsByBarbero_Id(Integer id);

  boolean existsByServicio_Id(Integer id);

  @Query(
      "select count(c) from Cita c where c.id<>:id and c.estado not in"
          + " (org.esfe.modelos.EstadoCita.CANCELADA) and (c.barbero.id=:barbero or"
          + " c.cliente.id=:cliente) and c.inicio<:fin and c.fin>:inicio")
  long contarCruces(
      @Param("id") Integer id,
      @Param("barbero") Integer barbero,
      @Param("cliente") Integer cliente,
      @Param("inicio") LocalDateTime inicio,
      @Param("fin") LocalDateTime fin);

  @Query(
      "select c from Cita c join fetch c.cliente join fetch c.barbero join fetch c.servicio where"
          + " c.inicio>=:desde and c.inicio<:hasta order by c.inicio")
  List<Cita> agenda(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
