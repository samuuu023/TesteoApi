package org.esfe.servicios.implementaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.esfe.dtos.servicio.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.Servicio;
import org.esfe.repositorios.IServicioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {
  @Mock IServicioRepository repo;
  ServicioService service;

  @BeforeEach
  void inicio() {
    service =
        new ServicioService(
            repo, new ModelMapper(), mock(org.esfe.repositorios.ICitaRepository.class));
  }

  Servicio entidad() {
    var e =
        new ModelMapper()
            .map(
                new ServicioGuardar("Corte clásico", new java.math.BigDecimal("7.50"), 30, true),
                Servicio.class);
    e.setId(1);
    return e;
  }

  @Test
  void crear() {
    when(repo.saveAndFlush(any()))
        .thenAnswer(
            i -> {
              Servicio e = i.getArgument(0);
              e.setId(1);
              return e;
            });
    var r =
        service.crear(
            new ServicioGuardar("Corte clásico", new java.math.BigDecimal("7.50"), 30, true));
    assertEquals(1, r.getId());
    verify(repo).saveAndFlush(any());
  }

  @Test
  void rechazarDuplicado() {
    when(repo.existsByNombreIgnoreCaseAndIdNot(anyString(), anyInt())).thenReturn(true);
    assertEquals(
        409,
        assertThrows(
                NegocioException.class,
                () ->
                    service.crear(
                        new ServicioGuardar(
                            "Corte clásico", new java.math.BigDecimal("7.50"), 30, true)))
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
            1, new ServicioModificar("Corte premium", new java.math.BigDecimal("9.00"), 45, true));
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
