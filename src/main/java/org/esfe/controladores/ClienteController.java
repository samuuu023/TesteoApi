package org.esfe.controladores;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.esfe.dtos.cliente.*;
import org.esfe.servicios.interfaces.IClienteService;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
  private final IClienteService service;

  @GetMapping
  public org.esfe.dtos.Pagina<ClienteSalida> obtenerTodosPaginados(
      @RequestParam(defaultValue = "") String buscar,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return org.esfe.dtos.Pagina.desde(
        service.obtenerTodosPaginados(
            buscar,
            PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by("id"))));
  }

  @GetMapping("/lista")
  public List<ClienteSalida> obtenerTodos() {
    return service.obtenerTodos();
  }

  @GetMapping("/{id}")
  public ClienteSalida obtenerPorId(@PathVariable Integer id) {
    return service.obtenerPorId(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ClienteSalida> crear(@Valid @RequestBody ClienteGuardar d) {
    var e = service.crear(d);
    return ResponseEntity.created(URI.create("/api/clientes/" + e.getId())).body(e);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ClienteSalida editar(@PathVariable Integer id, @Valid @RequestBody ClienteModificar d) {
    return service.editar(id, d);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
    service.eliminarPorId(id);
    return ResponseEntity.noContent().build();
  }
}
