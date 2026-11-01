package org.esfe.servicios.interfaces;

import java.time.LocalDate;
import java.util.*;
import org.esfe.dtos.cita.*;
import org.esfe.modelos.EstadoCita;
import org.springframework.data.domain.*;

public interface ICitaService {
  CitaSalida crear(CitaGuardar d);

  CitaSalida editar(Integer id, CitaModificar d);

  CitaSalida obtenerPorId(Integer id);

  Page<CitaSalida> obtenerTodosPaginados(
      LocalDate fecha, EstadoCita estado, Integer barberoId, Pageable p);

  CitaSalida cambiarEstado(Integer id, EstadoCita estado);

  void eliminarPorId(Integer id);

  List<CitaSalida> agenda(LocalDate fecha);

  Map<String, Object> resumen(LocalDate fecha);
}
