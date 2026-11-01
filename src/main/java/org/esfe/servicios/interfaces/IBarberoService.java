package org.esfe.servicios.interfaces;

import java.util.List;
import org.esfe.dtos.barbero.*;
import org.springframework.data.domain.*;

public interface IBarberoService {
  List<BarberoSalida> obtenerTodos();

  Page<BarberoSalida> obtenerTodosPaginados(String buscar, Pageable p);

  BarberoSalida obtenerPorId(Integer id);

  BarberoSalida crear(BarberoGuardar d);

  BarberoSalida editar(Integer id, BarberoModificar d);

  void eliminarPorId(Integer id);
}
