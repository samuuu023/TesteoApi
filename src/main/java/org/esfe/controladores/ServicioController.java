package org.esfe.controladores;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.servicio.*;
import org.esfe.servicios.interfaces.IServicioService;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {
  private final IServicioService service;

  @GetMapping
  public org.esfe.dtos.Pagina<ServicioSalida> obtenerTodosPaginados(
      @RequestParam(defaultValue = "") String buscar,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return org.esfe.dtos.Pagina.desde(
        service.obtenerTodosPaginados(
            buscar,
            PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by("id"))));
  }

  @GetMapping("/lista")
  public List<ServicioSalida> obtenerTodos() {
    return service.obtenerTodos();
  }

  @GetMapping("/{id}")
  public ServicioSalida obtenerPorId(@PathVariable Integer id) {
    return service.obtenerPorId(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ServicioSalida> crear(@Valid @RequestBody ServicioGuardar d) {
    var e = service.crear(d);
    return ResponseEntity.created(URI.create("/api/servicios/" + e.getId())).body(e);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ServicioSalida editar(@PathVariable Integer id, @Valid @RequestBody ServicioModificar d) {
    return service.editar(id, d);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
    service.eliminarPorId(id);
    return ResponseEntity.noContent().build();
  }
}
