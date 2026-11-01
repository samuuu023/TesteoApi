package org.esfe.controladores;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.cita.*;
import org.esfe.modelos.EstadoCita;
import org.esfe.servicios.interfaces.ICitaService;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {
  private final ICitaService service;

  @GetMapping
  public org.esfe.dtos.Pagina<CitaSalida> listar(
      @RequestParam(required = false) LocalDate fecha,
      @RequestParam(required = false) EstadoCita estado,
      @RequestParam(required = false) Integer barberoId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return org.esfe.dtos.Pagina.desde(
        service.obtenerTodosPaginados(
            fecha,
            estado,
            barberoId,
            PageRequest.of(
                Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by("inicio"))));
  }

  @GetMapping("/{id}")
  public CitaSalida buscar(@PathVariable Integer id) {
    return service.obtenerPorId(id);
  }

  @GetMapping("/agenda")
  public List<CitaSalida> agenda(@RequestParam LocalDate fecha) {
    return service.agenda(fecha);
  }

  @GetMapping("/resumen")
  public Map<String, Object> resumen(@RequestParam LocalDate fecha) {
    return service.resumen(fecha);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CitaSalida> crear(@Valid @RequestBody CitaGuardar d) {
    var c = service.crear(d);
    return ResponseEntity.created(URI.create("/api/citas/" + c.id())).body(c);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CitaSalida editar(@PathVariable Integer id, @Valid @RequestBody CitaModificar d) {
    return service.editar(id, d);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN')")
  public CitaSalida estado(@PathVariable Integer id, @Valid @RequestBody CitaCambiarEstado d) {
    return service.cambiarEstado(id, d.estado());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
    service.eliminarPorId(id);
    return ResponseEntity.noContent().build();
  }
}
