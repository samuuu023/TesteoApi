package org.esfe.servicios.implementaciones;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.cita.*;
import org.esfe.excepciones.NegocioException;
import org.esfe.modelos.*;
import org.esfe.repositorios.*;
import org.esfe.servicios.interfaces.ICitaService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CitaService implements ICitaService {
  private final ICitaRepository citas;
  private final IClienteRepository clientes;
  private final IBarberoRepository barberos;
  private final IServicioRepository servicios;
  private final Clock clock;

  private Cita encontrar(Integer id) {
    return citas.findById(id).orElseThrow(() -> new NegocioException(404, "Cita no encontrada"));
  }

  private CitaSalida salida(Cita c) {
    return new CitaSalida(
        c.getId(),
        c.getCliente().getId(),
        c.getCliente().getNombre(),
        c.getBarbero().getId(),
        c.getBarbero().getNombre(),
        c.getServicio().getId(),
        c.getServicio().getNombre(),
        c.getInicio(),
        c.getFin(),
        c.getPrecio(),
        c.getEstado(),
        c.getObservaciones());
  }

  private void asignar(
      Cita c,
      Integer cliente,
      Integer barbero,
      Integer servicio,
      LocalDateTime inicio,
      String notas) {
    Cliente cl =
        clientes
            .bloquear(cliente)
            .orElseThrow(() -> new NegocioException(404, "Cliente no encontrado"));
    Barbero b =
        barberos
            .bloquear(barbero)
            .orElseThrow(() -> new NegocioException(404, "Barbero no encontrado"));
    Servicio s =
        servicios
            .findById(servicio)
            .orElseThrow(() -> new NegocioException(404, "Servicio no encontrado"));
    if (!Boolean.TRUE.equals(b.getActivo()) || !Boolean.TRUE.equals(s.getActivo()))
      throw new NegocioException(409, "El barbero o servicio está inactivo");
    if (!inicio.isAfter(LocalDateTime.now(clock)))
      throw new NegocioException(400, "La cita debe ser futura");
    LocalDateTime fin = inicio.plusMinutes(s.getDuracionMinutos());
    if (inicio.getDayOfWeek() == DayOfWeek.SUNDAY
        || inicio.getMinute() % 15 != 0
        || inicio.getSecond() != 0
        || inicio.getNano() != 0
        || inicio.toLocalTime().isBefore(LocalTime.of(8, 0))
        || !inicio.toLocalDate().equals(fin.toLocalDate())
        || fin.toLocalTime().isAfter(LocalTime.of(18, 0)))
      throw new NegocioException(
          400, "Horario: lunes a sábado, 08:00 a 18:00, intervalos de 15 minutos");
    if (citas.contarCruces(c.getId() == null ? 0 : c.getId(), barbero, cliente, inicio, fin) > 0)
      throw new NegocioException(409, "El barbero o cliente ya tiene una cita en ese horario");
    c.setCliente(cl);
    c.setBarbero(b);
    c.setServicio(s);
    c.setInicio(inicio);
    c.setFin(fin);
    c.setPrecio(s.getPrecio());
    c.setObservaciones(notas);
  }

  @Transactional
  public CitaSalida crear(CitaGuardar d) {
    Cita c = new Cita();
    c.setEstado(EstadoCita.PENDIENTE);
    asignar(c, d.clienteId(), d.barberoId(), d.servicioId(), d.inicio(), d.observaciones());
    return salida(citas.saveAndFlush(c));
  }

  @Transactional
  public CitaSalida editar(Integer id, CitaModificar d) {
    Cita c = encontrar(id);
    if (c.getEstado() == EstadoCita.CANCELADA || c.getEstado() == EstadoCita.COMPLETADA)
      throw new NegocioException(409, "No se puede reprogramar una cita finalizada");
    asignar(c, d.clienteId(), d.barberoId(), d.servicioId(), d.inicio(), d.observaciones());
    return salida(citas.saveAndFlush(c));
  }

  public CitaSalida obtenerPorId(Integer id) {
    return salida(encontrar(id));
  }

  public Page<CitaSalida> obtenerTodosPaginados(
      LocalDate fecha, EstadoCita estado, Integer barbero, Pageable p) {
    Specification<Cita> spec = (root, q, cb) -> cb.conjunction();
    if (fecha != null)
      spec =
          spec.and(
              (r, q, b) ->
                  b.and(
                      b.greaterThanOrEqualTo(r.get("inicio"), fecha.atStartOfDay()),
                      b.lessThan(r.get("inicio"), fecha.plusDays(1).atStartOfDay())));
    if (estado != null) spec = spec.and((r, q, b) -> b.equal(r.get("estado"), estado));
    if (barbero != null) spec = spec.and((r, q, b) -> b.equal(r.get("barbero").get("id"), barbero));
    return citas.findAll(spec, p).map(this::salida);
  }

  @Transactional
  public CitaSalida cambiarEstado(Integer id, EstadoCita nuevo) {
    Cita c = encontrar(id);
    EstadoCita actual = c.getEstado();
    boolean valido =
        (actual == EstadoCita.PENDIENTE
                && (nuevo == EstadoCita.CONFIRMADA || nuevo == EstadoCita.CANCELADA))
            || (actual == EstadoCita.CONFIRMADA
                && (nuevo == EstadoCita.COMPLETADA || nuevo == EstadoCita.CANCELADA));
    if (!valido) throw new NegocioException(409, "Transición de estado no permitida");
    if (nuevo == EstadoCita.COMPLETADA && c.getFin().isAfter(LocalDateTime.now(clock)))
      throw new NegocioException(409, "La cita todavía no ha terminado");
    c.setEstado(nuevo);
    return salida(citas.saveAndFlush(c));
  }

  @Transactional
  public void eliminarPorId(Integer id) {
    Cita c = encontrar(id);
    if (c.getEstado() != EstadoCita.CANCELADA)
      throw new NegocioException(409, "Solo se eliminan citas canceladas");
    citas.delete(c);
    citas.flush();
  }

  public List<CitaSalida> agenda(LocalDate fecha) {
    return citas.agenda(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay()).stream()
        .map(this::salida)
        .toList();
  }

  public Map<String, Object> resumen(LocalDate fecha) {
    List<Cita> lista = citas.agenda(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    Map<String, Object> r = new LinkedHashMap<>();
    r.put("fecha", fecha);
    r.put("total", lista.size());
    for (EstadoCita e : EstadoCita.values())
      r.put(e.name().toLowerCase(), lista.stream().filter(c -> c.getEstado() == e).count());
    r.put(
        "ingresos",
        lista.stream()
            .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
            .map(Cita::getPrecio)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    return r;
  }
}
