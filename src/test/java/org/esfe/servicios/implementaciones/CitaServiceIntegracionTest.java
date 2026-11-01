package org.esfe.servicios.implementaciones;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.*;
import org.esfe.dtos.barbero.*;
import org.esfe.dtos.cita.*;
import org.esfe.dtos.cliente.*;
import org.esfe.dtos.servicio.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.*;
import org.esfe.repositorios.*;
import org.esfe.servicios.interfaces.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CitaServiceIntegracionTest {
  @Autowired ICitaService citas;
  @Autowired IClienteService clientes;
  @Autowired IBarberoService barberos;
  @Autowired IServicioService servicios;
  @Autowired ICitaRepository repo;
  int cliente, barbero, servicio;
  LocalDateTime inicio;

  @BeforeEach
  void preparar() {
    cliente =
        clientes.crear(new ClienteGuardar("Samuel", "71234567", "samuel@example.com")).getId();
    barbero = barberos.crear(new BarberoGuardar("Eduard", "76543210", "Corte", true)).getId();
    servicio =
        servicios.crear(new ServicioGuardar("Corte", new BigDecimal("7.50"), 30, true)).getId();
    LocalDate fecha = LocalDate.now(ZoneId.of("America/El_Salvador")).plusDays(3);
    while (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) fecha = fecha.plusDays(1);
    inicio = fecha.atTime(9, 0);
  }

  CitaSalida crear() {
    return citas.crear(new CitaGuardar(cliente, barbero, servicio, inicio, null));
  }

  @Test
  void crearYConsultar() {
    var c = crear();
    assertEquals("Samuel", citas.obtenerPorId(c.id()).cliente());
    assertEquals(inicio.plusMinutes(30), c.fin());
  }

  @Test
  void rechazarCruceBarbero() {
    crear();
    int otro = clientes.crear(new ClienteGuardar("Otro", "71234568", "otro@example.com")).getId();
    assertEquals(
        409,
        assertThrows(
                NegocioException.class,
                () ->
                    citas.crear(
                        new CitaGuardar(otro, barbero, servicio, inicio.plusMinutes(15), null)))
            .getEstado());
  }

  @Test
  void rechazarCruceCliente() {
    crear();
    int otro = barberos.crear(new BarberoGuardar("Otro", "76543211", "Barba", true)).getId();
    assertThrows(
        NegocioException.class,
        () -> citas.crear(new CitaGuardar(cliente, otro, servicio, inicio, null)));
  }

  @Test
  void permitirCitaConsecutiva() {
    crear();
    assertNotNull(
        citas
            .crear(new CitaGuardar(cliente, barbero, servicio, inicio.plusMinutes(30), null))
            .id());
  }

  @Test
  void cancelacionLiberaHorario() {
    var c = crear();
    citas.cambiarEstado(c.id(), EstadoCita.CANCELADA);
    assertNotNull(crear().id());
  }

  @Test
  void editarSinColisionConsigoMisma() {
    var c = crear();
    assertEquals(
        c.id(),
        citas.editar(c.id(), new CitaModificar(cliente, barbero, servicio, inicio, "Cambio")).id());
  }

  @Test
  void agendaYFiltros() {
    crear();
    assertEquals(1, citas.agenda(inicio.toLocalDate()).size());
    assertEquals(
        1,
        citas
            .obtenerTodosPaginados(
                inicio.toLocalDate(), EstadoCita.PENDIENTE, barbero, PageRequest.of(0, 10))
            .getTotalElements());
    assertEquals(
        0,
        citas
            .obtenerTodosPaginados(
                inicio.toLocalDate(), EstadoCita.CANCELADA, barbero, PageRequest.of(0, 10))
            .getTotalElements());
  }

  @Test
  void resumenCalculaIngresosReales() {
    var c = crear();
    Cita entidad = repo.findById(c.id()).orElseThrow();
    entidad.setEstado(EstadoCita.COMPLETADA);
    repo.saveAndFlush(entidad);
    assertEquals(new BigDecimal("7.50"), citas.resumen(inicio.toLocalDate()).get("ingresos"));
  }

  @Test
  void precioHistorico() {
    var c = crear();
    servicios.editar(servicio, new ServicioModificar("Corte", new BigDecimal("10.00"), 45, true));
    assertEquals(new BigDecimal("7.50"), citas.obtenerPorId(c.id()).precio());
  }

  @Test
  void cancelarYEliminar() {
    var c = crear();
    citas.cambiarEstado(c.id(), EstadoCita.CANCELADA);
    citas.eliminarPorId(c.id());
    assertTrue(citas.agenda(inicio.toLocalDate()).isEmpty());
  }

  @Test
  void protegerClienteConCitas() {
    crear();
    assertEquals(
        409,
        assertThrows(NegocioException.class, () -> clientes.eliminarPorId(cliente)).getEstado());
  }
}
