package org.esfe.servicios.implementaciones;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.servicio.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.Servicio;
import org.esfe.repositorios.IServicioRepository;
import org.esfe.servicios.interfaces.IServicioService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioService implements IServicioService {
  private final IServicioRepository repo;
  private final ModelMapper mapper;
  private final org.esfe.repositorios.ICitaRepository citas;

  public List<ServicioSalida> obtenerTodos() {
    return repo.findAll().stream().map(e -> mapper.map(e, ServicioSalida.class)).toList();
  }

  public Page<ServicioSalida> obtenerTodosPaginados(String buscar, Pageable p) {
    return repo.findByNombreContainingIgnoreCase(buscar, p)
        .map(e -> mapper.map(e, ServicioSalida.class));
  }

  private Servicio encontrar(Integer id) {
    return repo.findById(id).orElseThrow(() -> new NegocioException(404, "Servicio no encontrado"));
  }

  public ServicioSalida obtenerPorId(Integer id) {
    return mapper.map(encontrar(id), ServicioSalida.class);
  }

  private void validar(Servicio e) {
    e.setNombre(e.getNombre().trim());
    if (e.getDuracionMinutos() % 15 != 0)
      throw new NegocioException(400, "La duración debe ser múltiplo de 15");
    if (repo.existsByNombreIgnoreCaseAndIdNot(e.getNombre(), e.getId() == null ? 0 : e.getId()))
      throw new NegocioException(409, "Ya existe un servicio con ese dato");
  }

  @Transactional
  public ServicioSalida crear(ServicioGuardar d) {
    Servicio e = mapper.map(d, Servicio.class);
    validar(e);
    return mapper.map(repo.saveAndFlush(e), ServicioSalida.class);
  }

  @Transactional
  public ServicioSalida editar(Integer id, ServicioModificar d) {
    Servicio e = encontrar(id);
    mapper.map(d, e);
    validar(e);
    return mapper.map(repo.saveAndFlush(e), ServicioSalida.class);
  }

  @Transactional
  public void eliminarPorId(Integer id) {
    if (citas.existsByServicio_Id(id))
      throw new NegocioException(409, "El registro tiene citas asociadas");
    repo.delete(encontrar(id));
    repo.flush();
  }
}
