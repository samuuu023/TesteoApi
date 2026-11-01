package org.esfe.servicios.implementaciones;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.esfe.dtos.cita.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.*;
import org.esfe.repositorios.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {
  @Mock ICitaRepository citas;
  @Mock IClienteRepository clientes;
  @Mock IBarberoRepository barberos;
  @Mock IServicioRepository servicios;
  CitaService service;
  Cliente cliente;
  Barbero barbero;
  Servicio servicio;

  @BeforeEach
  void inicio() {
    service =
        new CitaService(
            citas,
            clientes,
            barberos,
            servicios,
            Clock.fixed(Instant.parse("2030-01-07T13:00:00Z"), ZoneId.of("America/El_Salvador")));
    cliente = new Cliente();
    cliente.setId(1);
    cliente.setNombre("Cliente");
    barbero = new Barbero();
    barbero.setId(1);
    barbero.setNombre("Barbero");
    barbero.setActivo(true);
    servicio = new Servicio();
    servicio.setId(1);
    servicio.setNombre("Corte");
    servicio.setActivo(true);
    servicio.setDuracionMinutos(30);
    servicio.setPrecio(new BigDecimal("7.50"));
  }

  void catalogos() {
    when(clientes.bloquear(1)).thenReturn(Optional.of(cliente));
    when(barberos.bloquear(1)).thenReturn(Optional.of(barbero));
    when(servicios.findById(1)).thenReturn(Optional.of(servicio));
  }

  Cita cita(EstadoCita e) {
    Cita c = new Cita();
    c.setId(1);
    c.setCliente(cliente);
    c.setBarbero(barbero);
    c.setServicio(servicio);
    c.setInicio(LocalDateTime.parse("2030-01-07T08:00:00"));
    c.setFin(c.getInicio().plusMinutes(30));
    c.setPrecio(servicio.getPrecio());
    c.setEstado(e);
    return c;
  }

  CitaGuardar datos(String fecha) {
    return new CitaGuardar(1, 1, 1, LocalDateTime.parse(fecha), "Prueba");
  }

  @Test
  void crearCalculaPrecioYFin() {
    catalogos();
    when(citas.saveAndFlush(any()))
        .thenAnswer(
            i -> {
              Cita c = i.getArgument(0);
              c.setId(1);
              return c;
            });
    var c = service.crear(datos("2030-01-07T08:00:00"));
    assertEquals(new BigDecimal("7.50"), c.precio());
    assertEquals(LocalDateTime.parse("2030-01-07T08:30:00"), c.fin());
    assertEquals(EstadoCita.PENDIENTE, c.estado());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "2029-01-01T08:00:00",
        "2030-01-06T09:00:00",
        "2030-01-13T09:00:00",
        "2030-01-07T07:45:00",
        "2030-01-07T17:45:00",
        "2030-01-07T08:10:00",
        "2030-01-07T08:00:01",
        "2030-01-07T23:45:00"
      })
  void rechazarHorario(String f) {
    catalogos();
    assertEquals(
        400, assertThrows(NegocioException.class, () -> service.crear(datos(f))).getEstado());
    verify(citas, never()).saveAndFlush(any());
  }

  @Test
  void barberoInactivo() {
    catalogos();
    barbero.setActivo(false);
    assertEquals(
        409,
        assertThrows(NegocioException.class, () -> service.crear(datos("2030-01-07T08:00:00")))
            .getEstado());
  }

  @Test
  void servicioInactivo() {
    catalogos();
    servicio.setActivo(false);
    assertThrows(NegocioException.class, () -> service.crear(datos("2030-01-07T08:00:00")));
  }

  @Test
  void clienteInexistente() {
    assertEquals(
        404,
        assertThrows(NegocioException.class, () -> service.crear(datos("2030-01-07T08:00:00")))
            .getEstado());
  }

  @Test
  void rechazarSolapamiento() {
    catalogos();
    when(citas.contarCruces(anyInt(), anyInt(), anyInt(), any(), any())).thenReturn(1L);
    assertEquals(
        409,
        assertThrows(NegocioException.class, () -> service.crear(datos("2030-01-07T08:00:00")))
            .getEstado());
  }

  @Test
  void confirmar() {
    when(citas.findById(1)).thenReturn(Optional.of(cita(EstadoCita.PENDIENTE)));
    when(citas.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    assertEquals(EstadoCita.CONFIRMADA, service.cambiarEstado(1, EstadoCita.CONFIRMADA).estado());
  }

  @Test
  void cancelar() {
    when(citas.findById(1)).thenReturn(Optional.of(cita(EstadoCita.CONFIRMADA)));
    when(citas.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    assertEquals(EstadoCita.CANCELADA, service.cambiarEstado(1, EstadoCita.CANCELADA).estado());
  }

  @Test
  void noCompletarPendiente() {
    when(citas.findById(1)).thenReturn(Optional.of(cita(EstadoCita.PENDIENTE)));
    assertThrows(NegocioException.class, () -> service.cambiarEstado(1, EstadoCita.COMPLETADA));
  }

  @Test
  void noCompletarAntesDeTerminar() {
    when(citas.findById(1)).thenReturn(Optional.of(cita(EstadoCita.CONFIRMADA)));
    assertThrows(NegocioException.class, () -> service.cambiarEstado(1, EstadoCita.COMPLETADA));
  }

  @Test
  void completarTerminada() {
    Cita c = cita(EstadoCita.CONFIRMADA);
    c.setFin(LocalDateTime.parse("2030-01-07T06:00:00"));
    when(citas.findById(1)).thenReturn(Optional.of(c));
    when(citas.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    assertEquals(EstadoCita.COMPLETADA, service.cambiarEstado(1, EstadoCita.COMPLETADA).estado());
  }

  @Test
  void noReabrirCancelada() {
    when(citas.findById(1)).thenReturn(Optional.of(cita(EstadoCita.CANCELADA)));
    assertThrows(NegocioException.class, () -> service.cambiarEstado(1, EstadoCita.CONFIRMADA));
  }

  @Test
  void noEditarCompletada() {
    when(citas.findById(1)).thenReturn(Optional.of(cita(EstadoCita.COMPLETADA)));
    assertThrows(
        NegocioException.class,
        () ->
            service.editar(
                1, new CitaModificar(1, 1, 1, LocalDateTime.parse("2030-01-08T08:00:00"), null)));
  }

  @Test
  void eliminarCancelada() {
    Cita c = cita(EstadoCita.CANCELADA);
    when(citas.findById(1)).thenReturn(Optional.of(c));
    service.eliminarPorId(1);
    verify(citas).delete(c);
  }

  @Test
  void noEliminarPendiente() {
    when(citas.findById(1)).thenReturn(Optional.of(cita(EstadoCita.PENDIENTE)));
    assertThrows(NegocioException.class, () -> service.eliminarPorId(1));
    verify(citas, never()).delete(any(Cita.class));
  }

  @Test
  void noEncontrada() {
    assertEquals(
        404, assertThrows(NegocioException.class, () -> service.obtenerPorId(99)).getEstado());
  }
}
