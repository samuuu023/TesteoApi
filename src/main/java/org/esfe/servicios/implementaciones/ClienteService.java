package org.esfe.servicios.implementaciones;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.cliente.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.Cliente;
import org.esfe.repositorios.IClienteRepository;
import org.esfe.servicios.interfaces.IClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService implements IClienteService {
  private final IClienteRepository repo;
  private final ModelMapper mapper;
  private final org.esfe.repositorios.ICitaRepository citas;

  public List<ClienteSalida> obtenerTodos() {
    return repo.findAll().stream().map(e -> mapper.map(e, ClienteSalida.class)).toList();
  }

  public Page<ClienteSalida> obtenerTodosPaginados(String buscar, Pageable p) {
    return repo.findByNombreContainingIgnoreCase(buscar, p)
        .map(e -> mapper.map(e, ClienteSalida.class));
  }

  private Cliente encontrar(Integer id) {
    return repo.findById(id).orElseThrow(() -> new NegocioException(404, "Cliente no encontrado"));
  }

  public ClienteSalida obtenerPorId(Integer id) {
    return mapper.map(encontrar(id), ClienteSalida.class);
  }

  private void validar(Cliente e) {
    e.setNombre(e.getNombre().trim());
    e.setTelefono(e.getTelefono().trim());
    e.setEmail(e.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
    if (repo.existsByEmailIgnoreCaseAndIdNot(e.getEmail(), e.getId() == null ? 0 : e.getId()))
      throw new NegocioException(409, "Ya existe un cliente con ese dato");
  }

  @Transactional
  public ClienteSalida crear(ClienteGuardar d) {
    Cliente e = mapper.map(d, Cliente.class);
    validar(e);
    return mapper.map(repo.saveAndFlush(e), ClienteSalida.class);
  }

  @Transactional
  public ClienteSalida editar(Integer id, ClienteModificar d) {
    Cliente e = encontrar(id);
    mapper.map(d, e);
    validar(e);
    return mapper.map(repo.saveAndFlush(e), ClienteSalida.class);
  }

  @Transactional
  public void eliminarPorId(Integer id) {
    if (citas.existsByCliente_Id(id))
      throw new NegocioException(409, "El registro tiene citas asociadas");
    repo.delete(encontrar(id));
    repo.flush();
  }
}
