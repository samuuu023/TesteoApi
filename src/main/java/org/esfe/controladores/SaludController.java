package org.esfe.controladores;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class SaludController {
  @GetMapping("/api/salud")
  public Map<String, String> salud() {
    return Map.of("estado", "OK", "aplicacion", "Cool Barber Shop");
  }
}
