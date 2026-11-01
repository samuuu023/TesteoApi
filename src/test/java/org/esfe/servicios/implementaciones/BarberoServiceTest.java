package org.esfe.servicios.implementaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.esfe.dtos.barbero.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.Barbero;
import org.esfe.repositorios.IBarberoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class BarberoServiceTest {
  @Mock IBarberoRepository repo;
  BarberoService service;

  @BeforeEach
  void inicio() {
    service =
        new BarberoService(
            repo, new ModelMapper(), mock(org.esfe.repositorios.ICitaRepository.class));
  }

  Barbero entidad() {
    var e =
        new ModelMapper()
            .map(
                new BarberoGuardar("Eduard Ayala", "76543210", "Corte y barba", true),
                Barbero.class);
    e.setId(1);
    return e;
  }

  @Test
  void crear() {
    when(repo.saveAndFlush(any()))
        .thenAnswer(
            i -> {
              Barbero e = i.getArgument(0);
              e.setId(1);
              return e;
            });
    var r = service.crear(new BarberoGuardar("Eduard Ayala", "76543210", "Corte y barba", true));
    assertEquals(1, r.getId());
    verify(repo).saveAndFlush(any());
  }

  @Test
  void rechazarDuplicado() {
    when(repo.existsByTelefonoAndIdNot(anyString(), anyInt())).thenReturn(true);
    assertEquals(
        409,
        assertThrows(
                NegocioException.class,
                () ->
                    service.crear(
                        new BarberoGuardar("Eduard Ayala", "76543210", "Corte y barba", true)))
            .getEstado());
    verify(repo, never()).saveAndFlush(any());
  }

  @Test
  void obtenerTodos() {
    when(repo.findAll()).thenReturn(List.of(entidad()));
    assertEquals(1, service.obtenerTodos().size());
  }

  @Test
  void obtenerTodosPaginados() {
    Pageable p = PageRequest.of(0, 10);
    when(repo.findByNombreContainingIgnoreCase("", p))
        .thenReturn(new PageImpl<>(List.of(entidad()), p, 1));
    assertEquals(1, service.obtenerTodosPaginados("", p).getTotalElements());
  }

  @Test
  void obtenerPorId() {
    when(repo.findById(1)).thenReturn(Optional.of(entidad()));
    assertEquals(1, service.obtenerPorId(1).getId());
  }

  @Test
  void idAusente() {
    assertEquals(
        404, assertThrows(NegocioException.class, () -> service.obtenerPorId(99)).getEstado());
  }

  @Test
  void editar() {
    when(repo.findById(1)).thenReturn(Optional.of(entidad()));
    when(repo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    var r =
        service.editar(
            1, new BarberoModificar("Eduard Actualizado", "76543211", "Degradado", true));
    assertEquals(1, r.getId());
    assertTrue(r.getNombre().contains("premium") || r.getNombre().contains("Actualizado"));
  }

  @Test
  void eliminarPorId() {
    var e = entidad();
    when(repo.findById(1)).thenReturn(Optional.of(e));
    assertDoesNotThrow(() -> service.eliminarPorId(1));
    verify(repo).delete(e);
    verify(repo).flush();
  }

  @Test
  void noEliminarAusente() {
    assertThrows(NegocioException.class, () -> service.eliminarPorId(99));
    verify(repo, never()).delete(any());
  }
}
