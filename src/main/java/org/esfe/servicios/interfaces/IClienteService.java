package org.esfe.servicios.interfaces;

import java.util.List;
import org.esfe.dtos.cliente.*;
import org.springframework.data.domain.*;

public interface IClienteService {
  List<ClienteSalida> obtenerTodos();

  Page<ClienteSalida> obtenerTodosPaginados(String buscar, Pageable p);

  ClienteSalida obtenerPorId(Integer id);

  ClienteSalida crear(ClienteGuardar d);

  ClienteSalida editar(Integer id, ClienteModificar d);

  void eliminarPorId(Integer id);
}
