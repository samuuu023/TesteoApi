package org.esfe.servicios.implementaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.esfe.dtos.cliente.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.Cliente;
import org.esfe.repositorios.IClienteRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
  @Mock IClienteRepository repo;
  ClienteService service;

  @BeforeEach
  void inicio() {
    service =
        new ClienteService(
            repo, new ModelMapper(), mock(org.esfe.repositorios.ICitaRepository.class));
  }

  Cliente entidad() {
    var e =
        new ModelMapper()
            .map(
                new ClienteGuardar("Samuel Orellana", "71234567", "samuel@example.com"),
                Cliente.class);
    e.setId(1);
    return e;
  }

  @Test
  void crear() {
    when(repo.saveAndFlush(any()))
        .thenAnswer(
            i -> {
              Cliente e = i.getArgument(0);
              e.setId(1);
              return e;
            });
    var r = service.crear(new ClienteGuardar("Samuel Orellana", "71234567", "samuel@example.com"));
    assertEquals(1, r.getId());
    verify(repo).saveAndFlush(any());
  }

  @Test
  void rechazarDuplicado() {
    when(repo.existsByEmailIgnoreCaseAndIdNot(anyString(), anyInt())).thenReturn(true);
    assertEquals(
        409,
        assertThrows(
                NegocioException.class,
                () ->
                    service.crear(
                        new ClienteGuardar("Samuel Orellana", "71234567", "samuel@example.com")))
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
            1, new ClienteModificar("Samuel Actualizado", "76543210", "otro@example.com"));
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
