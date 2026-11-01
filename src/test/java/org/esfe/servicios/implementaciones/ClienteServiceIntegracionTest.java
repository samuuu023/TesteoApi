package org.esfe.servicios.implementaciones;

import static org.junit.jupiter.api.Assertions.*;

import org.esfe.dtos.cliente.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.servicios.interfaces.IClienteService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClienteServiceIntegracionTest {
  @Autowired IClienteService service;

  ClienteSalida crearDato() {
    return service.crear(new ClienteGuardar("Samuel Orellana", "71234567", "samuel@example.com"));
  }

  @Test
  void crear() {
    var r = crearDato();
    assertNotNull(r.getId());
    assertNotNull(r.getNombre());
  }

  @Test
  void obtenerTodos() {
    crearDato();
    assertEquals(1, service.obtenerTodos().size());
  }

  @Test
  void obtenerTodosPaginados() {
    crearDato();
    assertEquals(1, service.obtenerTodosPaginados("", PageRequest.of(0, 5)).getTotalElements());
  }

  @Test
  void obtenerPorId() {
    var r = crearDato();
    assertEquals(r.getNombre(), service.obtenerPorId(r.getId()).getNombre());
  }

  @Test
  void editar() {
    var r = crearDato();
    var editado =
        service.editar(
            r.getId(), new ClienteModificar("Samuel Actualizado", "76543210", "otro@example.com"));
    assertEquals(r.getId(), editado.getId());
    assertNotEquals(r.getNombre(), editado.getNombre());
  }

  @Test
  void eliminarPorId() {
    var r = crearDato();
    service.eliminarPorId(r.getId());
    assertTrue(service.obtenerTodos().isEmpty());
  }

  @Test
  void duplicado() {
    crearDato();
    assertEquals(409, assertThrows(NegocioException.class, () -> crearDato()).getEstado());
  }

  @Test
  void noEncontrado() {
    assertEquals(
        404, assertThrows(NegocioException.class, () -> service.obtenerPorId(999999)).getEstado());
  }
}
