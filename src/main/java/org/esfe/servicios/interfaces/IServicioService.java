package org.esfe.servicios.interfaces;

import java.util.List;
import org.esfe.dtos.servicio.*;
import org.springframework.data.domain.*;

public interface IServicioService {
  List<ServicioSalida> obtenerTodos();

  Page<ServicioSalida> obtenerTodosPaginados(String buscar, Pageable p);

  ServicioSalida obtenerPorId(Integer id);

  ServicioSalida crear(ServicioGuardar d);

  ServicioSalida editar(Integer id, ServicioModificar d);

  void eliminarPorId(Integer id);
}
