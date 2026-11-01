package org.esfe.servicios.implementaciones;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.barbero.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.Barbero;
import org.esfe.repositorios.IBarberoRepository;
import org.esfe.servicios.interfaces.IBarberoService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarberoService implements IBarberoService {
  private final IBarberoRepository repo;
  private final ModelMapper mapper;
  private final org.esfe.repositorios.ICitaRepository citas;

  public List<BarberoSalida> obtenerTodos() {
    return repo.findAll().stream().map(e -> mapper.map(e, BarberoSalida.class)).toList();
  }

  public Page<BarberoSalida> obtenerTodosPaginados(String buscar, Pageable p) {
    return repo.findByNombreContainingIgnoreCase(buscar, p)
        .map(e -> mapper.map(e, BarberoSalida.class));
  }

  private Barbero encontrar(Integer id) {
    return repo.findById(id).orElseThrow(() -> new NegocioException(404, "Barbero no encontrado"));
  }

  public BarberoSalida obtenerPorId(Integer id) {
    return mapper.map(encontrar(id), BarberoSalida.class);
  }

  private void validar(Barbero e) {
    e.setNombre(e.getNombre().trim());
    e.setTelefono(e.getTelefono().trim());
    e.setEspecialidad(e.getEspecialidad().trim());
    if (repo.existsByTelefonoAndIdNot(e.getTelefono(), e.getId() == null ? 0 : e.getId()))
      throw new NegocioException(409, "Ya existe un barbero con ese dato");
  }

  @Transactional
  public BarberoSalida crear(BarberoGuardar d) {
    Barbero e = mapper.map(d, Barbero.class);
    validar(e);
    return mapper.map(repo.saveAndFlush(e), BarberoSalida.class);
  }

  @Transactional
  public BarberoSalida editar(Integer id, BarberoModificar d) {
    Barbero e = encontrar(id);
    mapper.map(d, e);
    validar(e);
    return mapper.map(repo.saveAndFlush(e), BarberoSalida.class);
  }

  @Transactional
  public void eliminarPorId(Integer id) {
    if (citas.existsByBarbero_Id(id))
      throw new NegocioException(409, "El registro tiene citas asociadas");
    repo.delete(encontrar(id));
    repo.flush();
  }
}
