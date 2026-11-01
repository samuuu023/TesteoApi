package org.esfe.controladores;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.barbero.*;
import org.esfe.servicios.interfaces.IBarberoService;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barberos")
@RequiredArgsConstructor
public class BarberoController {
  private final IBarberoService service;

  @GetMapping
  public org.esfe.dtos.Pagina<BarberoSalida> obtenerTodosPaginados(
      @RequestParam(defaultValue = "") String buscar,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return org.esfe.dtos.Pagina.desde(
        service.obtenerTodosPaginados(
            buscar,
            PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by("id"))));
  }

  @GetMapping("/lista")
  public List<BarberoSalida> obtenerTodos() {
    return service.obtenerTodos();
  }

  @GetMapping("/{id}")
  public BarberoSalida obtenerPorId(@PathVariable Integer id) {
    return service.obtenerPorId(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<BarberoSalida> crear(@Valid @RequestBody BarberoGuardar d) {
    var e = service.crear(d);
    return ResponseEntity.created(URI.create("/api/barberos/" + e.getId())).body(e);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public BarberoSalida editar(@PathVariable Integer id, @Valid @RequestBody BarberoModificar d) {
    return service.editar(id, d);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
    service.eliminarPorId(id);
    return ResponseEntity.noContent().build();
  }
}
